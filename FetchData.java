package com.example.cyawait;

import android.os.AsyncTask;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.net.ssl.SSLContext;


public class FetchData extends AsyncTask<Object, String, String> {
    String googleNearByPlacesData;
    GoogleMap googleMap;
    String url;
    MainActivity mainActivity;
    private PlacesLoadedListener callback;

    public FetchData(MainActivity mmainActivity){
        mainActivity = mmainActivity;
        this.callback = mmainActivity;
    }

    @Override
    protected String doInBackground(Object... objects) {
        try{
            googleMap = (GoogleMap)objects[0];
            url = (String)objects[1];
            String data = new DownloadURL().retrieveURL(url);

            googleNearByPlacesData = data;
        }catch(IOException e ){
            e.printStackTrace();
        }
        return googleNearByPlacesData;
    }

    protected void onPostExecute(String s){
        try{
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            for(int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                JSONObject getLocation = jsonObject1.getJSONObject("geometry").getJSONObject("location");
                String lat = getLocation.getString("lat");
                String lng = getLocation.getString("lng");
                JSONObject getName = jsonArray.getJSONObject(i);
                String name = getName.getString("name");

                //add marker to fetched location on map
                LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble((lng)));
                MarkerOptions markerOptions = new MarkerOptions();

                String distance = DistanceCalculator.calculateDistance(mainActivity.getUserLocation(), latLng);
                markerOptions.title(name + " | " + distance + "km away");
                markerOptions.position(latLng);

                mainActivity.addPlace(markerOptions);
                googleMap.addMarker(markerOptions);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (callback != null) {
            callback.onPlacesLoaded();
        }
    }
}


