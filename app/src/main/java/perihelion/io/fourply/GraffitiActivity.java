package perihelion.io.fourply;

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
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;

import perihelion.io.fourply.ui.GraffitiView;

/**
 * Created by vincente on 1/23/16
 */
public class GraffitiActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String BRUSH_NAMES[] = {"Spray Paint, Paint", "Pen"};

    private static final int BRUSH_RES[] = {
            R.drawable.brush_spray,
            R.drawable.brush_paint
    };

    private static final int BRUSH_PREV[] = {
            R.drawable.brush_preview_spray,
            R.drawable.brush_preview_paint
    };

    private LinearLayout container;
    private GraffitiView graffitiView;
    private ArrayList<Bitmap> brushList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graffiti);

        container = (LinearLayout) findViewById(R.id.ll_brushes);
        graffitiView = (GraffitiView) findViewById(R.id.gv_grafitti);

        int brushPreviewSize = getResources().getDimensionPixelOffset(R.dimen.brush_preview_size);
        int brushSize = getResources().getDimensionPixelOffset(R.dimen.brush_size);
        int brushMargin = getResources().getDimensionPixelOffset(R.dimen.brush_preview_margin_left);
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

    @Override
    public void onClick(View v) {
        int pos = (int) v.getTag();
        graffitiView.setActiveBrush(pos);
    }
}
