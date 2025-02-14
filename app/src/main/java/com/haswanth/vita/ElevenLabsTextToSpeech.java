package com.haswanth.vita;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ElevenLabsTextToSpeech {
    private static final String API_KEY = "sk_ea2a9bb1cbbeb6e6796029b02a3571bae61154343c174154";
    private static final String VOICE_ID = "cgSgspJ2msm6clMCkdW9";
    private static final String API_URL = "https://api.elevenlabs.io/v1/text-to-speech/" + VOICE_ID + "/stream?output_format=mp3_44100_128";

    private static final OkHttpClient client = new OkHttpClient();
    private static MediaPlayer mediaPlayer;

    public static void speak(String text, Context context) {
        new Thread(() -> {
            try {
                // Create JSON body
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("text", text);
                jsonBody.put("model_id", "eleven_multilingual_v2");

                // Request Body
                RequestBody body = RequestBody.create(
                        jsonBody.toString(),
                        MediaType.get("application/json")
                );

                // Build the request
                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .addHeader("xi-api-key", API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .build();

                // Execute the request
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    // Save audio stream to file
                    File audioFile = saveAudioToFile(response.body().byteStream(), context);
                    if (audioFile != null) {
                        playAudio(audioFile);
                    }
                } else {
                    Log.e("ElevenLabsTTS", "Error: " + response.code() + " " + response.message());
                }
            } catch (Exception e) {
                Log.e("ElevenLabsTTS", "Error in TTS process", e);
            }
        }).start();
    }

    private static File saveAudioToFile(InputStream inputStream, Context context) {
        try {
            File audioFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "tts_audio.mp3");
            try (FileOutputStream outputStream = new FileOutputStream(audioFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return audioFile;
        } catch (IOException e) {
            Log.e("ElevenLabsTTS", "Error saving audio file", e);
            return null;
        }
    }

    private static void playAudio(File audioFile) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e("ElevenLabsTTS", "Error playing audio", e);
        }
    }
}