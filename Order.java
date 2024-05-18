package com.example.cyawait;

import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Objects;

public class Order implements Comparable<Order>{
    private String item;
    private int quantity;

    public Order(String itm, int qty){
            this.item = itm;
            quantity = qty;
    }

    public String getItem(){

        return item;
    }

    public int getQuantity(){
        return quantity;
    }

    @Override
    public int compareTo(Order o) {
        if(o.item.compareTo(this.getItem()) == 0 && o.quantity == this.getQuantity()){
            return 0;
        }
        return 1;
    }
}
