package com.example.cyawait;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.Manifest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PlacesLoadedListener{
    private final int FINE_PERMISSION_CODE = 1;
    private final MainActivity mainActivity = this;
    public static Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap gMap;
    ImageButton search_button;
    private final ArrayList<MarkerOptions> food_places = new ArrayList<>(); //Map doesn't keep record of markers, so a custom list is required
    private EditText search_bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        search_bar = findViewById(R.id.search_bar);
        search_button = findViewById(R.id.search_button);
        search_button.setOnClickListener(v -> {

            if(search_bar.getText().toString().equals("")){
                loadPlaceButtons();
            }else {
                loadPlaceButtons(search_bar.getText().toString().toLowerCase());
            }
        });
    }
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if(location != null) {
                currentLocation = location;
                //initialize map
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                try{
                    assert mapFragment != null;
                    mapFragment.getMapAsync(MainActivity.this);
                }catch(NullPointerException e){
                    e.printStackTrace();
                }
            }
        });
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        //if location permission granted set map location to device location otherwise request user to allow the permission
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == FINE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }else{
                Toast.makeText(this, "Location permission was denied, please grant permission.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //when map loads, zoom into and put a pin at devices location
        gMap = googleMap;
        LatLng location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        //create marker for user's device
        MarkerOptions user_marker = new MarkerOptions();
        user_marker.position(location);
        user_marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        user_marker.title("You");

        gMap.addMarker(user_marker);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));

        assembleFetchData();
    }
    public void addPlace(MarkerOptions place_marker){
        food_places.add(place_marker);
    }
    public void loadPlaceButtons(){
        if(food_places != null) {
            LinearLayout button_container = findViewById(R.id.restaurant_button_container);
            //for each food place(markers on map) create a button that represents it
            for (MarkerOptions place : food_places) {
                Button place_button = new Button(this);
                place_button.setText(place.getTitle());
                place_button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100));
                place_button.setOnClickListener(v -> {
                    Intent order_intent = new Intent(MainActivity.this, OrderActivity.class);
                    order_intent.putExtra("Place Name", place.getTitle());
                    order_intent.putExtra("Place type", "Fast food");
                    order_intent.putExtra("User Latitude", currentLocation.getLatitude());
                    order_intent.putExtra("User Longitude", currentLocation.getLongitude());
                    startActivity(order_intent);
                });
                button_container.addView(place_button);
                button_container.invalidate();
            }
        }
    }
    public void loadPlaceButtons(String filter){
        if(food_places != null) {
            LinearLayout button_container = findViewById(R.id.restaurant_button_container);
            button_container.removeAllViews();
            //for each food place(markers on map) create a button that represents it
            for (MarkerOptions place : food_places) {
                String lowercase_place_name = place.getTitle().toLowerCase();
                if(lowercase_place_name.contains(filter)) {
                    Button place_button = new Button(this);
                    place_button.setText(place.getTitle());
                    place_button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100));
                    place_button.setOnClickListener(v -> {
                        Intent order_intent = new Intent(MainActivity.this, OrderActivity.class);
                        order_intent.putExtra("Place Name", place.getTitle());
                        order_intent.putExtra("Place type", "fast food");
                        order_intent.putExtra("User Latitude", currentLocation.getLatitude());
                        order_intent.putExtra("User Longitude", currentLocation.getLongitude());
                        startActivity(order_intent);
                    });
                    button_container.addView(place_button);
                    button_container.invalidate();
                }
            }
        }
    }

    public void assembleFetchData(){
        //assemble the query for nearby places
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + "location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() +
                "&radius=5000" +
                "&type=restaurant" +
                "&sensor=true" +
                "&key=AIzaSyCopbL-Ks-lfqOa55svYiAFseKPZbTu_NE";
        Object[] fetch_data_params = new Object[2];
        fetch_data_params[0] = gMap;
        fetch_data_params[1] = url;

        FetchData fetchData = new FetchData(mainActivity);
        fetchData.execute(fetch_data_params);
    }
    public LatLng getUserLocation(){
        return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    @Override
    public void onPlacesLoaded() {
        loadPlaceButtons();
    }
}