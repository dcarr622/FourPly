package perihelion.io.fourply;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import perihelion.io.fourply.ui.GraffitiView;
import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by vincente on 1/23/16
 */
public class GraffitiActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String KEY_OBJECT_ID = "id";
    private static final String BRUSH_NAMES[] = {"Spray Paint, Paint", "Pen"};

    private static final int BRUSH_RES[] = {
            R.drawable.brush_spray,
            R.drawable.brush_paint
    };

    private static final int BRUSH_PREV[] = {
            R.drawable.brush_preview_spray,
            R.drawable.brush_preview_paint,
            R.drawable.brush_preview_pen
    };

    private LinearLayout container;
    private GraffitiView graffitiView;
    private ArrayList<Bitmap> brushList = new ArrayList<>();
    private String bathroomId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graffiti);

        Bundle extras = getIntent().getExtras();
        if(extras != null && extras.containsKey(KEY_OBJECT_ID)){
            bathroomId = extras.getString(KEY_OBJECT_ID, "lolFail");
        }

        container = (LinearLayout) findViewById(R.id.ll_brushes);
        graffitiView = (GraffitiView) findViewById(R.id.gv_grafitti);

        //Load the bathroom Id if it is saved
        if(bathroomId != null)
            loadBitmap();

        int brushPreviewSize = getResources().getDimensionPixelOffset(R.dimen.brush_preview_size);
        int brushSize = getResources().getDimensionPixelOffset(R.dimen.brush_size);
        int brushMargin = getResources().getDimensionPixelOffset(R.dimen.brush_preview_margin);

        //Generate and add the Brushes to the View
        for(int i=0; i<BRUSH_NAMES.length; i++){
            ImageButton button = new ImageButton(this);
            LayerDrawable layerDrawable = (LayerDrawable) getDrawable(BRUSH_PREV[i]);
            Drawable drawable = layerDrawable.getDrawable(1);

            if(drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                bitmapDrawable.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
                bitmapDrawable.setFilterBitmap(true);
                brushList.add(
                        Bitmap.createScaledBitmap(
                                BitmapFactory.decodeResource(getResources(), BRUSH_RES[i]),
                                brushSize,
                                brushSize,
                                true)
                );
            }

            button.setImageDrawable(layerDrawable);
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setOnClickListener(this);
            button.setTag(i);

            LinearLayout.MarginLayoutParams lp = new LinearLayout.MarginLayoutParams(
                    brushPreviewSize,
                    brushPreviewSize);

            lp.leftMargin = brushMargin;

            container.addView(button, lp);
        }
        graffitiView.setBrushList(brushList);
    }

    private void loadBitmap() {
        FileInputStream in = null;
        try {
            in = openFileInput(bathroomId + ".png");
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            graffitiView.setBitmap(bitmap);
            Log.d(getClass().getSimpleName(), "Set the bitmap");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveImageToBitmap(){
        if(bathroomId == null){
            Log.e(GraffitiActivity.class.getSimpleName(), "Couldn't get bathroom id");
            return;
        }

        FileOutputStream out = null;
        try {
            out = openFileOutput(bathroomId + ".png", Context.MODE_PRIVATE);
            graffitiView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getTag() != null) {
            int pos = (int) v.getTag();
            graffitiView.setActiveBrush(pos);
        }
        else{
            switch(v.getId()){
                case R.id.btn_brush:
                    View view = findViewById(R.id.ll_brushes);
                    view.setVisibility(view.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
                    break;
                case R.id.btn_undo:
                    graffitiView.undo();
                    break;
                case R.id.btn_clear:
                    graffitiView.clear();
                    break;
                case R.id.btn_color:
                    new AmbilWarnaDialog(this, graffitiView.getCurrentColor(), true, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                        @Override
                        public void onCancel(AmbilWarnaDialog dialog) {
                            //Do Nothing
                        }

                        @Override
                        public void onOk(AmbilWarnaDialog dialog, int color) {
                            graffitiView.setColor(color);
                            findViewById(R.id.btn_color).setBackgroundTintList(
                                    new ColorStateList(
                                            new int[][] {
                                                    new int[] { android.R.attr.state_enabled}, // enabled
                                            },
                                            new int[] {
                                                    color
                                            }
                                    )
                            );
                        }
                    }).show();
                    break;
                case R.id.btn_save:
                    saveImageToBitmap();
                    break;
                default:
            }
        }
    }
}
