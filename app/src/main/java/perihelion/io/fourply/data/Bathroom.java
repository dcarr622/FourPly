package perihelion.io.fourply.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;

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

    public String getLat() {
        return getString("lat");
    }

    public String getLng() {
        return getString("lng");
    }
}
