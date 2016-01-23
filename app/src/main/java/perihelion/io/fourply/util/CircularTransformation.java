package perihelion.io.fourply.util;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

/**
 * Created by david on 1/22/16.
 */

public class CircularTransformation implements Transformation {
    public int radius;
    private boolean border;

    public CircularTransformation(int radius, boolean border) {
        this.radius = radius;
        this.border = border;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        if (border) {
            Paint whitePaint = new Paint();
            whitePaint.setStyle(Paint.Style.FILL);
            whitePaint.setColor(Color.WHITE);
            whitePaint.setAntiAlias(true);
            canvas.drawRoundRect(new RectF(0, 0, source.getWidth(), source.getHeight()), radius, radius, whitePaint);
            canvas.drawRoundRect(new RectF(4, 4, source.getWidth() - 4, source.getHeight() - 4), radius - 8, radius - 8, paint);
        } else {
            canvas.drawRoundRect(new RectF(0, 0, source.getWidth(), source.getHeight()), radius, radius, paint);
        }

        source.recycle();
        return output;
    }

    @Override
    public String key() {
        return "circular transform radius: " + radius;
    }
}
