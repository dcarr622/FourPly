package perihelion.io.fourply.data;

import android.content.Context;

import com.parse.GetDataCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

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

    public Bathroom(String name, String description, String url, float lat, float lng) {
        put("name", name);
        put("description", description);
        put("heroImage", url);
        put("lat", lat);
        put("lng", lng);
    }

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
        void onComplete(String path);
        void onError();
    }

    public void setGraffiti(String path) {
        File inputFile = new File(path);
        ParseFile fileObject = new ParseFile(inputFile);
        fileObject.saveInBackground();
        put("graffiti", fileObject);
        saveInBackground();
    }

    public void getGraffiti(Context ctx, final GraffitiListener listener) {
        ParseFile fileObject = getParseFile("graffiti");
        final File outputFile = new File(ctx.getFilesDir(), getName() + ".png");
        fileObject.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if (e == null) {
                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
                        bos.write(data);
                        bos.flush();
                        bos.close();
                        listener.onComplete(outputFile.getPath());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        listener.onError();
                    }
                } else {
                    listener.onError();
                }
            }
        });
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
