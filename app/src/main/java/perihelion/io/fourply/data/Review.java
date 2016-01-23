package perihelion.io.fourply.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by david on 1/22/16.
 */

@ParseClassName("Review")
public class Review extends ParseObject {
    public Review() {}

    public int getRolls() {
        return getInt("rolls");
    }

    public String getName() {
        return getString("name");
    }

    public String getText() {
        return getString("text");
    }

}
