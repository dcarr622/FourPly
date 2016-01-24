package perihelion.io.fourply.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;

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
