package com.haswanth.vita;

import android.util.Log;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class AIResponseHandler {
    private static final String API_URL = "https://androidchatbot.haswanthraj777.workers.dev/";

    // OkHttpClient with increased timeout
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public interface AIResponseCallback {
        void onSuccess(String response);
        void onFailure(String errorMessage);
    }

    // ✅ **Send AI request**
    public static void fetchAIResponse(String userMessage, AIResponseCallback callback) {
        // Get mobile's current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat time24Format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        SimpleDateFormat time12Format = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());

        String date = dateFormat.format(new Date());
        String time24h = time24Format.format(new Date());
        String time12h = time12Format.format(new Date());

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("prompt", userMessage);
            jsonBody.put("date", date);
            jsonBody.put("time_24h", time24h);
            jsonBody.put("time_12h", time12h);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure("JSON Error");
            return;
        }

        // Log JSON request before sending
        Log.d("AIResponseHandler", "📤 Sending API request: " + jsonBody.toString());

        // Build the request
        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8")))
                .addHeader("Content-Type", "application/json")
                .build();

        sendRequestWithRetry(request, callback, 1); // Initial call with retry enabled
    }

    // ✅ **Send request with retry mechanism**
    private static void sendRequestWithRetry(Request request, AIResponseCallback callback, int retryCount) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("AIResponseHandler", "❌ API call failed: " + e.getMessage());

                // Retry once if timeout occurs
                if (retryCount > 0 && e.getMessage() != null && e.getMessage().contains("timeout")) {
                    Log.w("AIResponseHandler", "🔄 Retrying API call...");
                    sendRequestWithRetry(request, callback, retryCount - 1);
                } else {
                    callback.onFailure("Failed to get AI response");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d("AIResponseHandler", "📥 AI Response: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject responseObject = jsonResponse.optJSONObject("response");
                        String aiResponse = responseObject != null ? responseObject.optString("response", "Oops! I couldn't understand that.") : "Oops! I couldn't understand that.";

                        callback.onSuccess(aiResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onFailure("Error parsing AI response");
                    }
                } else {
                    Log.e("AIResponseHandler", "❌ API call unsuccessful: " + response.code());
                    callback.onFailure("API call unsuccessful: " + response.code());
                }
            }
        });
    }
}
