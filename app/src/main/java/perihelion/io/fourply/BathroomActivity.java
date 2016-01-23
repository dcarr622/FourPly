package perihelion.io.fourply;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.List;

import perihelion.io.fourply.chat.ChatActivity;
import perihelion.io.fourply.data.Bathroom;
import perihelion.io.fourply.data.Review;

public class BathroomActivity extends AppCompatActivity {

    String bathroomName = "PennApps Bathroom";
    String bathroomID = "E8VjTcnzRu";
    float numRolls = 3.7f;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.bathroomfab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BathroomActivity.this, ARGraffitiActivity.class);
                startActivity(intent);
            }
        });

        ParseQuery<Bathroom> bathrooms = ParseQuery.getQuery(Bathroom.class);
        bathrooms.getInBackground(bathroomID, new GetCallback<Bathroom>() {
            @Override
            public void done(Bathroom object, ParseException e) {
                ImageView hero = (ImageView) findViewById(R.id.heroImage);
                Picasso.with(BathroomActivity.this).load(object.getHeroImage()).into(hero);
            }
        });

        ParseQuery<Review> reviews = ParseQuery.getQuery(Review.class);
        reviews.whereEqualTo("parent", bathroomID);
        reviews.findInBackground(new FindCallback<Review>() {
            @Override
            public void done(List<Review> objects, ParseException e) {
                if (e == null) {
                    for (Review review : objects) {
                        numRolls += review.getRolls();
                    }
                    numRolls /= objects.size();
                }
            }
        });

        RatingBar rolls = (RatingBar) findViewById(R.id.num_rolls_bar);
        rolls.setRating(numRolls);

        TextView rollamt = (TextView) findViewById(R.id.rollquantity);
        rollamt.setText(String.format(getString(R.string.ratingunit), numRolls));
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
