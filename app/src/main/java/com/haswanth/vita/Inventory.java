package com.haswanth.vita;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inventory extends AppCompatActivity {
    private EditText itemName, itemQuantity;
    private Button addItemButton;
    private RecyclerView itemRecyclerView;

    private DatabaseReference databaseRef;
    private InventoryAdapter adapter;
    private List<InventoryItem> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        itemName = findViewById(R.id.itemName);
        itemQuantity = findViewById(R.id.itemQuantity);
        addItemButton = findViewById(R.id.addItemButton);
        itemRecyclerView = findViewById(R.id.itemRecyclerView);

        databaseRef = FirebaseDatabase.getInstance().getReference("inventory").child("items");

        itemList = new ArrayList<>();
        adapter = new InventoryAdapter(this, itemList);
        itemRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemRecyclerView.setAdapter(adapter);

        addItemButton.setOnClickListener(v -> addItemToFirebase());

        fetchItemsFromFirebase();
    }

    private void addItemToFirebase() {
        String name = itemName.getText().toString().trim();
        String quantity = itemQuantity.getText().toString().trim();

        if (name.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(this, "Enter both item name and quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        String itemId = databaseRef.push().getKey();
        if (itemId != null) {
            InventoryItem item = new InventoryItem(itemId, name, quantity);
            databaseRef.child(itemId).setValue(item)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
                        itemName.setText("");
                        itemQuantity.setText("");
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error adding item", Toast.LENGTH_SHORT).show());
        }
    }

    private void fetchItemsFromFirebase() {
        databaseRef = FirebaseDatabase.getInstance().getReference("inventory"); // Ensure correct path
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear(); // Clear old data before adding new
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    InventoryItem item = itemSnapshot.getValue(InventoryItem.class); // Convert to object
                    if (item != null) {
                        itemList.add(item);
                    }
                }
                adapter.notifyDataSetChanged(); // Update UI
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Inventory.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static void fetchAndSendInventory(AIResponseHandler.AIResponseCallback callback) {
        Log.e("Inventory", "🔍 Fetching inventory from Firebase...");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("inventory");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e("Inventory", "❌ Firebase database is EMPTY!");
                    callback.onSuccess("Your inventory is empty.");
                    return;
                }

                Map<String, String> inventory = new HashMap<>();
                StringBuilder inventoryText = new StringBuilder("📦 Inventory Items:\n");

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Log.d("Inventory", "🔍 Raw Firebase Data: " + itemSnapshot.getValue()); // DEBUGGING

                    InventoryItem item = itemSnapshot.getValue(InventoryItem.class);
                    if (item != null && item.getName() != null && item.getQuantity() != null) {
                        inventory.put(item.getName(), item.getQuantity());
                        inventoryText.append("✅ ").append(item.getName()).append(": ").append(item.getQuantity()).append("\n");
                    } else {
                        Log.e("Inventory", "⚠️ Item missing name or quantity: " + itemSnapshot.getValue());
                    }
                }

                // **LOG INVENTORY BEFORE SENDING**
                Log.d("Inventory", "✅ Sending to AI:\n" + inventoryText.toString());

                // **Send to AI**
                AIResponseHandler.fetchAIResponse("Here is the latest inventory.", callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Inventory", "❌ Firebase fetch failed: " + error.getMessage());
                callback.onFailure("Failed to fetch inventory.");
            }
        });
    }

}



