package perihelion.io.fourply.nearby;

import android.Manifest;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import perihelion.io.fourply.AddBathroomFragment;
import perihelion.io.fourply.BathroomActivity;
import perihelion.io.fourply.R;
import perihelion.io.fourply.data.Bathroom;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private CoordinatorLayout mLayout;
    private GoogleApiClient mGoogleApiClient;
    private RecyclerView mRecyclerView;
    private Map<Marker, Bathroom> mBathroomMarkers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = (CoordinatorLayout) findViewById(R.id.main_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.bathroom_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Provide an additional rationale to the user if the permission was not granted
                // and the user would benefit from additional context for the use of the permission.
                // For example if the user has previously denied the permission.
                Snackbar.make(mLayout, R.string.permission_location_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        3309);
                            }
                        })
                        .show();
            } else {
                // Camera permission has not been granted yet. Request it directly.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        ParseQuery<Bathroom> bathrooms = ParseQuery.getQuery(Bathroom.class);
        bathrooms.findInBackground(new FindCallback<Bathroom>() {
            public void done(List<Bathroom> bathrooms, ParseException exception) {
                BathroomListAdapter adapter = new BathroomListAdapter(MainActivity.this, mRecyclerView, bathrooms);
                mRecyclerView.setAdapter(adapter);
                for (Bathroom bathroom: bathrooms) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(bathroom.getLat(), bathroom.getLng()))
                            .title(bathroom.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
                    mBathroomMarkers.put(marker, bathroom);
                }
            }
        });


    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setOnInfoWindowClickListener(this);
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    mMap.setMyLocationEnabled(true);
                    updateLocation();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateLocation() {
        try {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 18));
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        updateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Bathroom bathroom = mBathroomMarkers.get(marker);
        Intent bathroomIntent = new Intent(this, BathroomActivity.class);
        bathroomIntent.putExtra("name", bathroom.getName());
        bathroomIntent.putExtra("id", bathroom.getObjectId());
        startActivity(bathroomIntent);
    }

    private void setupView(){
        //Setup the Fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = AddBathroomFragment.createInstance();
                FragmentTransaction manager = getFragmentManager().beginTransaction();
                manager.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                dialogFragment.show(manager, "Review");
            }
        });

    }

}
