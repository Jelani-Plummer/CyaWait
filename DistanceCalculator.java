package com.example.cyawait;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

public class DistanceCalculator {

    // Radius of the Earth in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate the distance between two LatLng points in kilometers using the Haversine formula.
     *
     * @param point1 The first LatLng point
     * @param point2 The second LatLng point
     * @return The distance between the two points in kilometers
     */
    public static String calculateDistance(LatLng point1, LatLng point2) {
        double lat1 = Math.toRadians(point1.latitude);
        double lon1 = Math.toRadians(point1.longitude);
        double lat2 = Math.toRadians(point2.latitude);
        double lon2 = Math.toRadians(point2.longitude);

        // Haversine formula
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        // Format distance to one decimal place
        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(distance);
    }
}

