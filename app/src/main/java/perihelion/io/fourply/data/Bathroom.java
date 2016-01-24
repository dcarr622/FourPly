package perihelion.io.fourply.data;

import android.content.Context;
import android.util.Log;

import com.parse.GetDataCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by david on 1/22/16.
 */

@ParseClassName("Bathroom")
public class Bathroom extends ParseObject {

    public Bathroom() {}

    public String getHeroImage() {
        return getString("heroImage");
    }

    public String getName() {
        return getString("name");
    }

    public String getDescription() {
        return getString("description");
    }

    public interface GraffitiListener {
        void onComplete();
    }

    public void setGraffiti(String path) {
        File inputFile = new File(path);
        ParseFile fileObject = new ParseFile(inputFile);
        fileObject.saveInBackground();
        put("graffiti", fileObject);
        saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("PARSE", "done saving image");
            }
        });
    }

    public void getGraffiti(final Context ctx, final GraffitiListener listener) {
        ParseFile fileObject = getParseFile("graffiti");
        if (fileObject != null) {
            fileObject.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        try {
                            BufferedOutputStream bos = new BufferedOutputStream(ctx.openFileOutput(getObjectId() + ".png", Context.MODE_PRIVATE));
                            bos.write(data);
                            bos.flush();
                            bos.close();
                            listener.onComplete();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            listener.onComplete();
                        }
                    } else {
                        listener.onComplete();
                    }
                }
            });
        } else {
            listener.onComplete();
        }
    }

    public double getLat() {
        return getDouble("lat");
    }

    public double getLng() {
        return getDouble("lng");
    }

    public float getAverageReview() {
        return (float) getDouble("averageReview");
    }
}
