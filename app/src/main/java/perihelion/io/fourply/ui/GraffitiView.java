package perihelion.io.fourply.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vincente on 1/22/16
 */
public class GraffitiView extends View {

    private Path mPath;
    private Paint mPaint;
    private Canvas mCanvas;
    private Bitmap mCanvasBitmap;
    private Bitmap customBitmap;
    private boolean isBrushActive = false;
    private ColorFilter mColorFilter;

    private Brush mBrush;
    private List<Brush> mBrushList;

    /** A list of positions in which the Bitmap Brush has traversed */
    private List<Vector2> mBrushPositions = new ArrayList<>(100);

    /** A list of our Layers. Items will be added once the user lifts up their finger */
    private LinkedList<Layer> mLayerList = new LinkedList<>();

    /** The current color */
    private int mColor;

    /** The current position of the user's finger */
    private float mX, mY;

    /** The tolerance to move */
    private static final float TOLERANCE = 5;

    public GraffitiView(Context context) {
        super(context);
        init();
    }

    public GraffitiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraffitiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GraffitiView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /**
     * Setup the HashMaps and set the Default Color To Black
     */
    private void init(){
        mPath = new Path();
        mPaint = createNewPaint(Color.BLACK);
        mColorFilter = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        setColor(Color.BLACK);
    }

    /**
     * Set the Bitmap to use for the canvas
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap){
        mCanvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mCanvas = new Canvas(mCanvasBitmap);
        invalidate();
    }

    /**
     * Clear the Canvas
     */
    public void clear(){
        mLayerList.clear();
        invalidate();
    }

    /**
     * Undo the last action
     */
    public void undo(){
        if(mLayerList.size() > 0) {
            mLayerList.removeLast();
            invalidate();
        }
    }

    /**
     * Will get teh Graffiti from the canvas and then save it to a bitmap so someone can store it in
     * a file
     * @return a bitmap with the graffiti stored on it
     */
    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);
        return bitmap;
    }

    /**
     * Set the color of the Brush.
     * @param color desired color of the brush
     */
    public void setColor(int color){
        mColor = color;
        mPaint.setColor(color);
        mColorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Return the Current Color
     * @return color that is currently being used
     */
    public int getCurrentColor(){
        return mColor;
    }

    /**
     * Sets the Width of the brush
     * Default is 5f
     * @param width width of the brush
     */
    public void setWidth(float width){
        mPaint.setStrokeWidth(width);
    }

    /**
     * Start Drawing the path
     * @param x x-coordinate
     * @param y y-coordinate
     */
    private void drawOnDown(float x, float y){
        if(isBrushActive){
            mBrushPositions = new ArrayList<>();
            mLayerList.add(new Layer(mColorFilter, mBrush, mBrushPositions));
            mBrushPositions.add(new Vector2(x, y));
        }
        else {
            mPath = new Path();
            mPath.moveTo(x, y);
            mLayerList.add(new Layer(mColor, mPath));
        }

        mX = x;
        mY = y;
        Log.d(getClass().getSimpleName(), "Drawing with Color: " + mColor);
    }

    /**
     * Draw while moving
     * @param x x-coordinate
     * @param y y-coordinate
     */
    private void drawOnMove(float x, float y){
        if(isBrushActive) {
            mBrushPositions.add(
                    new Vector2(
                            x - mBrush.bitmapDimensions.x / 2,
                            y - mBrush.bitmapDimensions.y / 2
                    )
            );
        }
        else{
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx > TOLERANCE || dy > TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }
    }

    /**
     * Finish drawing by adding the last small dot
     */
    private void drawOnUp(){
        if(!isBrushActive) {
            mPath.lineTo(mX, mY);
        }
    }

    /**
     * Creates a new Paint Object
     * @param color color that you want the new paint to be
     * @return a fully setup paint object
     */
    private Paint createNewPaint(int color){
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4f);
        return mPaint;
    }

    /**
     * Set a list of brushes that the user can use
     * @param list List of Brushes to use
     */
    public void setBrushList(List<Bitmap> list){
        mBrushList = new ArrayList<>();
        for(Bitmap bitmap: list){
            Vector2 dimens = new Vector2(bitmap.getWidth(), bitmap.getHeight());
            mBrushList.add(new Brush(bitmap, dimens));
        }
    }

    /**
     * Select a brush to use
     * @param pos index of brush (-1 for normal line)
     */
    public void setActiveBrush(int pos){
        if(0 <= pos && pos < mBrushList.size()){
            this.isBrushActive = true;
            mBrush = mBrushList.get(pos);
        }
        else{
            isBrushActive = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Draw all of our old Items
        for(Layer layer : mLayerList){
            if(layer.isPath()){
                mPaint.setColor(layer.color);
                canvas.drawPath(layer.path, mPaint);
            }
            else{
                mPaint.setColorFilter(layer.colorFilter);
                for(Vector2 pos: layer.brushPositions)
                    canvas.drawBitmap(layer.brush.bitmap, pos.x, pos.y, mPaint);
            }
        }
        mPaint.setColor(mColor);
        mPaint.setColorFilter(null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(mCanvasBitmap == null)
            mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mCanvasBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                drawOnDown(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                drawOnMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                drawOnUp();
                invalidate();
                break;
            default:
        }
        return true;
    }

    /**
     * Class containing a Bitmap and its dimensions
     */
    private static final class Brush{
        public Bitmap bitmap;
        public Vector2 bitmapDimensions;

        public Brush(Bitmap bitmap, Vector2 bitmapDimensions) {
            this.bitmap = bitmap;
            this.bitmapDimensions = bitmapDimensions;
        }
    }

    private static final class Vector2 {
        public Vector2(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public final float x;
        public final float y;
    }

    private static final class Layer{
        public Path path = null;
        public Brush brush;
        public List<Vector2> brushPositions = null;
        public int color;
        public ColorFilter colorFilter;

        public Layer(int color, Path path){
            this.color = color;
            this.path = path;
        }

        public Layer(ColorFilter colorFilter, Brush brush, List<Vector2> brushPositions){
            this.colorFilter = colorFilter;
            this.brush = brush;
            this.brushPositions = brushPositions;
        }

        public boolean isPath(){
            return path!=null;
        }
    }
}
