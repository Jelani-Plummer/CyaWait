package com.example.cyawait;

import android.os.AsyncTask;
import android.os.Bundle;

import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.maps.GeoApiContext;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OrderActivity extends AppCompatActivity{
    private CompleteOrder completeOrder;
    private final OrderActivity orderActivity = OrderActivity.this;
    public String place_name;
    public String place_type;
    String[] available_food;
    String current_item = ""; //item currently selected by the food item dropdown list
    LatLng current_location;
    ArrayList<Order> Orders;

    TextView wait_time_view;
    LinearLayout container;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), "AIzaSyCopbL-Ks-lfqOa55svYiAFseKPZbTu_NE");
        container = findViewById(R.id.item_list);
        //Get user location from main screen
        current_location = new LatLng(getIntent().getDoubleExtra("User Latitude", 0), getIntent().getDoubleExtra("User Longitude", 0));

        String fullName = getIntent().getStringExtra("Place Name");
        String[] parts = fullName.split("\\|");
        place_name = parts[0].trim(); // remove distance from place name
        place_type = getIntent().getStringExtra("Place type");

        TextView activity_name = findViewById(R.id.place_name_text);
        activity_name.setText(place_name + " order creation");
        activity_name.setLayoutParams(new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT));

        findViewById(R.id.add_meal_button).setOnClickListener(v -> onClickCreate());

        generateFoodOptions();

        Orders = new ArrayList<>();

        wait_time_view = findViewById(R.id.wait_time_view);
        wait_time_view.setText("-");
    }

    private void onClickCreate(){
        //controls the main add meal button
        EditItemView editItemView = new EditItemView(this, orderActivity);
        container.addView(editItemView);
    }

    private void generateFoodOptions(){
        //Create and Execute the class required to get places based on food sold
        MapGetNearbyPlacesData PlaceDataGetter = new MapGetNearbyPlacesData();
        PlaceDataGetter.execute();
        if(!PlaceDataGetter.getAvailable_food_list().isEmpty()){
            available_food = PlaceDataGetter.getAvailable_food_list().toArray(new String[0]);
        }else{
            available_food = getResources().getStringArray(R.array.max_food_item_array);
        }
    }

    public void UpdateOrderList(Order order, EditItemView editItemView){
        Orders.add(order);

        ItemQuantityView itemQuantityView = new ItemQuantityView(this);
        itemQuantityView.setItemName(order.getItem());
        itemQuantityView.setQuantity(String.valueOf(order.getQuantity()));

        container.addView(itemQuantityView);
        container.removeView(editItemView);

        completeOrder = new CompleteOrder(Orders, place_name, place_type);
        updateWaitTime();
    }

    public void updateWaitTime() {
        new Thread(() -> {
            WaitTimePredictionClient waitTimePredictionClient = new WaitTimePredictionClient(
                    this,completeOrder.getRestaurantName(), completeOrder.getDayOfWeek(),
                    completeOrder.getTime(), completeOrder.getItems().get(0).getItem(),
                    completeOrder.getRestaurantType(), completeOrder.getOrderSize(),
                    true, "lightly populated",
                    wait_time -> runOnUiThread(() -> {
                        if (wait_time != null) {
                            wait_time_view.setText(wait_time);
                        } else {
                            wait_time_view.setText("Server error");
                        }
                    })
            );
        }).start();
    }



    public void UpdateOrderList(EditItemView editItemView){
        container.removeView(editItemView);
    }
    private class MapGetNearbyPlacesData extends AsyncTask<GoogleMap, Void, List<MarkerOptions>> {
        private List<String> available_food_list = new ArrayList<>();
        private final GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCopbL-Ks-lfqOa55svYiAFseKPZbTu_NE")
                .build();

        @Override
        protected List<MarkerOptions> doInBackground(GoogleMap... maps) {
                return new ArrayList<>();
        }

        public List<String> getAvailable_food_list(){
            return available_food_list;
        }

        @Override
        protected void onPreExecute() {
            String[] max_food_list = getResources().getStringArray(R.array.max_food_item_array);
            Set<String> available_food_set = ConcurrentHashMap.newKeySet(); // Thread-safe set for collecting results
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // ThreadPool to manage threads to process multiple queries at once

            try {
                for (String food : max_food_list) { //For each food that we recognize
                    executor.submit(() -> {
                        try {
                            //Query to get list of places near to user that sell current food
                            String keywords = food + " near me";
                            NearbySearchRequest req = PlacesApi.nearbySearchQuery(context, new com.google.maps.model.LatLng(MainActivity.currentLocation.getLatitude(), MainActivity.currentLocation.getLongitude()));
                            PlacesSearchResponse response = req.keyword(keywords).type(PlaceType.RESTAURANT).radius(8000).await();
                            //Search for the current restaurant in list of places and if it's there add the current food to the list of items that business sells
                            if (response.results != null) {
                                for (PlacesSearchResult result : response.results) {
                                    JsonArrayParser.PlaceInformation[] details_array = new JsonArrayParser(result.toString()).placesInformation;
                                    for (JsonArrayParser.PlaceInformation details : details_array) {
                                        if (Objects.equals(details.name, place_name)) {
                                            available_food_set.add(food);
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            //Cry
                        }
                    });
                }
            } finally {
                executor.shutdown();
                try {
                    if(executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)){ // Wait for all tasks to finish
                        available_food_list = new ArrayList<>(available_food_set); // Convert set to list
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        protected void onProgressUpdate (Void...values){
        }
    }
}