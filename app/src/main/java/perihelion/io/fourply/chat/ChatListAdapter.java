package perihelion.io.fourply.chat;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Query;
import com.squareup.picasso.Picasso;

import perihelion.io.fourply.R;
import perihelion.io.fourply.util.CircularTransformation;

/**
 * @author greg
 * @since 6/21/13
 *
 * This class is an example of how to use FirebaseListAdapter. It uses the <code>Chat</code> class to encapsulate the
 * data for each individual chat message
 */
public class ChatListAdapter extends FirebaseListAdapter<Chat> {

    // The mUsername for this client. We use this to indicate which messages originated from this user
    private String mUsername;
    private Activity mActivity;
    private CircularTransformation circularTransformation;

    public ChatListAdapter(Query ref, Activity activity, int layout, String mUsername) {
        super(ref, Chat.class, layout, activity);
        this.mUsername = mUsername;
        this.mActivity = activity;
        circularTransformation = new CircularTransformation((int) (32 * (mActivity.getResources().getDisplayMetrics().density)), false);
    }

    /**
     * Bind an instance of the <code>Chat</code> class to our view. This method is called by <code>FirebaseListAdapter</code>
     * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
     * to the constructor, as well as a single <code>Chat</code> instance that represents the current data to bind.
     *
     * @param view A view instance corresponding to the layout we passed to the constructor.
     * @param chat An instance representing the current state of a chat message
     */
    @Override
    protected void populateView(View view, Chat chat) {
        // Map a Chat object to an entry in our listview
        String author = chat.getAuthor();
        TextView authorText = (TextView) view.findViewById(R.id.author);
        authorText.setText(author + ": ");
        // If the message was sent by this user, color it differently
        if (author != null && author.equals(mUsername)) {
            authorText.setTextColor(view.getContext().getResources().getColor(R.color.fourply_logo_pink));
        } else {
            authorText.setTextColor(view.getContext().getResources().getColor(R.color.blue_500));
        }
        ((TextView) view.findViewById(R.id.message)).setText(chat.getMessage());
        Picasso.with(mActivity).load(chat.getProfileImage()).transform(circularTransformation).into((ImageView) view.findViewById(R.id.prof_pic));
    }
}
