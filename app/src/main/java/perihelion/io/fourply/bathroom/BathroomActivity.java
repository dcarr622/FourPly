package perihelion.io.fourply.bathroom;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.List;

import perihelion.io.fourply.ARGraffitiActivity;
import perihelion.io.fourply.R;
import perihelion.io.fourply.chat.ChatActivity;
import perihelion.io.fourply.data.Bathroom;
import perihelion.io.fourply.data.Review;

public class BathroomActivity extends AppCompatActivity {

    private String bathroomName;
    private String bathroomID;
    private RatingBar ratingBar;
    private Bathroom bathroom;
    private TextView ratingText;
    private LinearLayout reviewsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bathroom_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            bathroomName = extras.getString("name");
            bathroomID = extras.getString("id");
        }

        CollapsingToolbarLayout layout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        layout.setTitle(bathroomName);

        ParseQuery<Bathroom> bathrooms = ParseQuery.getQuery(Bathroom.class);
        bathrooms.getInBackground(bathroomID, new GetCallback<Bathroom>() {
            @Override
            public void done(Bathroom object, ParseException e) {
                bathroom = object;
                setupView();
            }
        });

        ratingBar = (RatingBar) findViewById(R.id.num_rolls_bar);
        ratingText = (TextView) findViewById(R.id.rollquantity);
        reviewsList = (LinearLayout) findViewById(R.id.reviews_list);

        TextView emergency = (TextView) findViewById(R.id.emergencyButton);
        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent postmatesIntent = new Intent(BathroomActivity.this, EmergencyActivity.class);
                startActivity(postmatesIntent);
            }
        });

        TextView leaveReview = (TextView) findViewById(R.id.leaveRatingButton);
        leaveReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                ReviewFragment reviewFragment = ReviewFragment.createInstance(bathroom);
                reviewFragment.show(fm, null);
            }
        });

        FloatingActionButton reviewfab = (FloatingActionButton) findViewById(R.id.reviewfab);
        reviewfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                ReviewFragment reviewFragment = ReviewFragment.createInstance(bathroom);
                reviewFragment.show(fm, null);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ParseQuery<Review> reviews = ParseQuery.getQuery(Review.class);
        reviews.whereEqualTo("parent", bathroomID);
        reviews.findInBackground(new FindCallback<Review>() {
            @Override
            public void done(List<Review> reviews, ParseException e) {
                if (e == null) {
                    reviewsList.removeAllViews();
                    Log.d("REVIEWs", "found : " + reviews.size());
                    float numRolls = 0.0f;
                    for (Review review : reviews) {
                        numRolls += review.getRating();
                        View view = View.inflate(BathroomActivity.this, R.layout.review_list_item, null);
                        TextView subject = (TextView) view.findViewById(R.id.subject);
                        subject.setText(review.getSubject());
                        TextView message = (TextView) view.findViewById(R.id.message);
                        message.setText(review.getMessage());
                        RatingBar rating = (RatingBar) view.findViewById(R.id.tiny_rolls_bar);
                        rating.setRating(review.getRating());
                        reviewsList.addView(view);
                    }
                    numRolls /= reviews.size();
                    if (numRolls > 0) {
                        ratingBar.setRating(numRolls);
                        ratingText.setText(String.format(getString(R.string.ratingunit), numRolls));
                    } else {
                        ratingText.setText(getString(R.string.no_reviews));
                    }
                }
            }
        });
    }

    private void setupView(){
        //Setup Hero Image
        ImageView hero = (ImageView) findViewById(R.id.heroImage);
        Picasso.with(BathroomActivity.this).load(bathroom.getHeroImage()).into(hero);

        TextView description = (TextView) findViewById(R.id.description);
        description.setText(bathroom.getDescription());

        //Setup the Fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.bathroomfab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bathroom.getGraffiti(BathroomActivity.this, new Bathroom.GraffitiListener() {
                    @Override
                    public void onComplete() {
                        Intent intent = new Intent(BathroomActivity.this, ARGraffitiActivity.class);
                        intent.putExtra("id", bathroomID);
                        intent.putExtra("name", bathroomName);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bathroom, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.chatbutton:
                Intent chatIntent = new Intent(this, ChatActivity.class);
                chatIntent.putExtra("id", bathroomID);
                chatIntent.putExtra("name", bathroomName);
                startActivity(chatIntent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}
