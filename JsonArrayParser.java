package com.example.cyawait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//Parses the response. The name JsonArrayParser is misleading, its not Json the response is in a unique form that required creating a parser for.
public class JsonArrayParser {

    PlaceInformation[] placesInformation;

    public JsonArrayParser(String data) {
        ArrayList<PlaceInformation> places_information_list = new ArrayList<>();
        data = data.trim().replaceAll("\"", "");  // Remove all quotes for simplicity

        String[] parts = data.split(",(?![^\\[\\]]*\\])");  // Split by commas not inside brackets

        PlaceInformation placeInfo = new PlaceInformation();
        try {
            for (String part : parts) {
                if (part.contains("PlacesSearchResult:")) {
                    placeInfo.name = part.split(":")[1].trim();
                } else if (part.contains("geometry:")) {
                    String geometry = part.substring(part.indexOf("[") + 1, part.indexOf("]"));
                    String[] geoParts = geometry.split(", ");
                    placeInfo.latitude = Double.parseDouble(geoParts[0].split(":")[1].trim());
                    placeInfo.longitude = Double.parseDouble(geoParts[1].trim());
                } else if (part.contains("placeId:")) {
                    placeInfo.placeId = part.split(":")[1].trim();
                } else if (part.contains("vicinity:")) {
                    placeInfo.vicinity = part.split(":")[1].trim();
                } else if (part.contains("types:")) {
                    String types = part.substring(part.indexOf("[") + 1, part.indexOf("]"));
                    placeInfo.types = new ArrayList<>(Arrays.asList(types.split(", ")));
                } else if (part.contains("rating:")) {
                    placeInfo.rating = Double.parseDouble(part.split(":")[1].trim());
                } else if (part.contains("userRatingsTotal:")) {
                    placeInfo.userRatingsTotal = Integer.parseInt(part.split(":")[1].trim());
                } else if (part.contains("businessStatus:")) {
                    placeInfo.businessStatus = part.split(":")[1].trim();
                }
            }
            places_information_list.add(placeInfo);
            placesInformation = places_information_list.toArray(new PlaceInformation[0]);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric values: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error accessing parsed data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("General error during parsing: " + e.getMessage());
        }
    }

    class PlaceInformation {
        String name;
        double latitude;
        double longitude;
        String placeId;
        String vicinity;
        List<String> types;
        double rating;
        int userRatingsTotal;
        String businessStatus;
    }
}

