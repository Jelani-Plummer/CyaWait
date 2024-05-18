package com.example.cyawait;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ItemQuantityView extends LinearLayout {
    //Displays user entered item information
    private TextView tvItemName;
    private TextView tvQuantity;

    public ItemQuantityView(Context context) {
        super(context);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_item_quantity, this);

        tvItemName = view.findViewById(R.id.tvItemName);
        tvQuantity = view.findViewById(R.id.tvQuantity);
    }

    public void setItemName(String itemName) {
        tvItemName.setText(itemName);
    }

    public void setQuantity(String quantity) {
        tvQuantity.setText(quantity);
    }
}
