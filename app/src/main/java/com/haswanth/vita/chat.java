package com.haswanth.vita;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.haswanth.vita.Message;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class chat extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private EditText inputMessage;
    private FloatingActionButton sendButton;

    private static final String API_URL = "https://androidchatbot.haswanthraj777.workers.dev/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        chatRecyclerView = findViewById(R.id.recycler_gchat);
        inputMessage = findViewById(R.id.edit_gchat_message);
        sendButton = findViewById(R.id.button_gchat_send);

        // Set up the RecyclerView and adapter
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Load initial messages
        loadInitialMessages();

        // Handle send button click
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = inputMessage.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    String timestamp = getCurrentTime();

                    // Add user's message to the chat
                    messageList.add(new Message(userMessage, true, timestamp));
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);

                    // Clear the input field
                    inputMessage.setText("");

                    // Fetch AI response from API
                    fetchAIResponse(userMessage, timestamp);
                }
            }
        });


        findViewById(R.id.button_speech_recognition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(chat.this, speech_recognition.class);
                startActivity(intent);
            }
        });
    }


    private void loadInitialMessages() {
        String timestamp = getCurrentTime();
        messageList.add(new Message("Hi there! How can I assist you today?", false, timestamp)); // AI message
        chatAdapter.notifyDataSetChanged();
    }

    private void fetchAIResponse(String userMessage, String userTimestamp) {
        OkHttpClient client = new OkHttpClient();

        // Prepare the JSON body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("prompt", userMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RequestBody requestBody = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        // Build the request
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "API call failed: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(chat.this, "Failed to get AI response", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        // Parse the AI response and extract the nested 'response' field
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject responseObject = jsonResponse.optJSONObject("response");
                        String aiResponse = responseObject != null ? responseObject.optString("response", "Oops! I couldn't understand that.") : "Oops! I couldn't understand that.";

                        // Generate timestamp for AI response
                        String aiTimestamp = getCurrentTime();

                        // Add the AI response to the chat
                        runOnUiThread(() -> {
                            messageList.add(new Message(aiResponse, false, aiTimestamp));
                            chatAdapter.notifyItemInserted(messageList.size() - 1);
                            chatRecyclerView.scrollToPosition(messageList.size() - 1);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(chat.this, "Error parsing AI response", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("MainActivity", "API call unsuccessful: " + response.code());
                    runOnUiThread(() -> Toast.makeText(chat.this, "Failed to get AI response", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }
}