package com.example.cyawait;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WaitTimePredictionClient {
    String wait_time;
    public WaitTimePredictionClient(Context context, String name_of_restaurant, String day_of_week, String time, String food_type, String restaurant_type, int order_size, boolean popular_item, String location_score, WaitTimeCallback callback){
        try {
            String ip = "192.168.0.134";
            URL url = new URL("http://" + ip + ":5000/predict");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            @SuppressLint("DefaultLocale")
            String jsonData = String.format("{"
                    + "\"Name of restaurant\": \"%s\","
                    + "\"Day of week\": \"%s\","
                    + "\"Arrival time\": \"%s\","
                    + "\"Food type\": \"%s\","
                    + "\"Restaurant type\": \"%s\","
                    + "\"Size\": %d,"
                    + "\"popular\": %b,"
                    + "\"Location score\": \"%s\""
                    + "}", name_of_restaurant, day_of_week, time, food_type, restaurant_type, order_size, popular_item, location_score);

            // Sending data
            Log.d("Server Connection Attempt", "Trying to connect to " + url);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            StringBuilder response;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray waitTimesArray = jsonObject.getJSONArray("Predicted wait times (in minutes)");

                // Extract the first element of the array(wait time) and round it
                double waitTime = waitTimesArray.getDouble(0);
                int roundedWaitTime = (int) Math.round(waitTime);

                // Convert to string
                wait_time = String.valueOf(roundedWaitTime + order_size);
            }
            callback.onResultReceived(wait_time);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onResultReceived(null);
        }
    }
}