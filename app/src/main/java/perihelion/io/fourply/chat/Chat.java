package perihelion.io.fourply.chat;

/**
 * @author greg
 * @since 6/21/13
 */
public class Chat {

    private String message;
    private String author;
    private String time;
    private String img;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    private Chat() {
    }

    public Chat(String message, String author, String profImg, String time) {
        this.message = message;
        this.author = author;
        this.time = time;
        this.img = profImg;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public String getTime() {
        return time;
    }

    public String getProfileImage() {
        return img;
    }
}
