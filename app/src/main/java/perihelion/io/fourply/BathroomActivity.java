package perihelion.io.fourply;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

public class BathroomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bathroom_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Parse.enableLocalDatastore(this);
        ParseUser.enableAutomaticUser();
        ParseInstallation.getCurrentInstallation().saveInBackground();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.bathroomfab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ImageView hero = (ImageView) findViewById(R.id.bathroom_hero);
        Picasso.with(this).load("http://www.adsbeat.com/wp-content/uploads/2014/06/bathroom-fantastic-nice-bathroom-ideas-with-amazing-black-subway-tile-stone-and-brown-wooden-cabinet-also-white-marble-sink-combine-with-white-pendant-lamps-for-bathroom-design-ideas-creative-and-inno.jpg").into(hero);
    }
}
