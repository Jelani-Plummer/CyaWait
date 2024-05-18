package com.example.cyawait;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Objects;

public class EditItemView extends LinearLayout {
    //View group for editing an items name and quantity
    private Spinner add_item;

    public EditItemView(Context context, OrderActivity orderActivity) {
        super(context);
        initializeViews(context);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, orderActivity.available_food);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        add_item.setAdapter(adapter);
        add_item.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                orderActivity.current_item = add_item.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //vibe
            }
        });
        EditText current_qty = findViewById(R.id.quantityInput);
        current_qty.setText("0");

        Button save_button = findViewById(R.id.saveButton);
        save_button.setOnClickListener(v -> {
            if(!Objects.equals(orderActivity.current_item,"") && !Objects.equals(current_qty.getText(), "")){
                Order order = new Order(orderActivity.current_item, Integer.parseInt(current_qty.getText().toString()));
                orderActivity.UpdateOrderList(order, this);
            }
        });

        Button cancel_button = findViewById(R.id.cancelButton);
        cancel_button.setOnClickListener(v->{
            orderActivity.UpdateOrderList(this);
        });
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.edit_item, this);

        add_item = view.findViewById(R.id.add_big_item);
    }
}