package perihelion.io.fourply.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by david on 1/22/16.
 */

@ParseClassName("Review")
public class Review extends ParseObject {

    public Review() {}

    public Review(String subject, String message, float rating, String parent) {
        put("subject", subject);
        put("message", message);
        put("rating", rating);
        put("parent", parent);
    }

    public int getRating() {
        return getInt("rating");
    }

    public String getSubject() {
        return getString("subject");
    }

    public String getMessage() {
        return getString("message");
    }

    public String getParent() {
        return getString("parent");
    }

}
