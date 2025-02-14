package com.haswanth.vita;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class chat extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private EditText inputMessage;
    private FloatingActionButton sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        chatRecyclerView = findViewById(R.id.recycler_gchat);
        inputMessage = findViewById(R.id.edit_gchat_message);
        sendButton = findViewById(R.id.button_gchat_send);

        // Set up RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Load initial AI message
        loadInitialMessages();

        // Handle send button click
        sendButton.setOnClickListener(v -> {
            String userMessage = inputMessage.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                sendMessage(userMessage);
            }
        });

        findViewById(R.id.button_speech_recognition).setOnClickListener(v -> {
            Intent intent = new Intent(chat.this, speech_recognition.class);
            startActivity(intent);
        });
    }

    private void loadInitialMessages() {
        String timestamp = getCurrentTime();
        messageList.add(new Message("Hi there! How can I assist you today?", false, timestamp)); // AI message
        chatAdapter.notifyDataSetChanged();
    }

    private void sendMessage(String userMessage) {
        String timestamp = getCurrentTime();

        // Add user's message to the chat
        messageList.add(new Message(userMessage, true, timestamp));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.scrollToPosition(messageList.size() - 1);

        // Clear input field
        inputMessage.setText("");

        AIResponseHandler.fetchAIResponse(userMessage, new AIResponseHandler.AIResponseCallback() {
            @Override
            public void onSuccess(String aiResponse) {
                runOnUiThread(() -> {
                    messageList.add(new Message(aiResponse, false, getCurrentTime()));
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(chat.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });

    }


    private String getCurrentTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }
}
