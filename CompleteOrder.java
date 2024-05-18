package com.example.cyawait;

import android.os.Build;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CompleteOrder {
    private ArrayList<Order> items;
    private int order_size = 0;
    private final String restaurant_name;
    private final String restaurant_type;
    private String day_of_week;
    private String time;

    public CompleteOrder(ArrayList<Order> food, String rname, String rtype){
        items = food;
        restaurant_name = rname;
        restaurant_type = rtype;

        for (Order item:food) {
            order_size = order_size + item.getQuantity();
        }

        LocalDateTime now = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            now = LocalDateTime.now();
        }

        DayOfWeek dayOfWeek = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dayOfWeek = now.getDayOfWeek();
        }
        String dayFormatted = dayOfWeek.toString();
        day_of_week = dayFormatted.substring(0, 1) + dayFormatted.substring(1).toLowerCase();

        DateTimeFormatter timeFormatter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            time = now.format(timeFormatter);
        }
    }

    public ArrayList<Order> getItems() {
        return items;
    }

    public int getOrderSize() {
        return order_size;
    }

    public String getRestaurantName() {
        return restaurant_name;
    }

    public String getRestaurantType() {
        return restaurant_type;
    }

    public String getDayOfWeek() {
        return day_of_week;
    }

    public String getTime() {
        return time;
    }
}
