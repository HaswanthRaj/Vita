package com.haswanth.vita;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
    private Context context;
    private List<InventoryItem> itemList;

    public InventoryAdapter(Context context, List<InventoryItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = itemList.get(position);
        holder.itemName.setText(item.getName());
        holder.itemQuantity.setText("Qty: " + item.getQuantity());

        holder.deleteButton.setOnClickListener(v -> {
            if (item.getId() != null) {
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("inventory").child(item.getId());
                dbRef.removeValue().addOnSuccessListener(aVoid -> {
                    itemList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, itemList.size());
                }).addOnFailureListener(e ->
                        Log.e("Firebase", "Failed to delete item", e)
                );
            }
        });
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemQuantity;
        Button deleteButton;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemNameText);
            itemQuantity = itemView.findViewById(R.id.itemQuantityText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
