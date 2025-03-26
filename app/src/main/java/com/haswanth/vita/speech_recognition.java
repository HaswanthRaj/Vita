package com.haswanth.vita;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class speech_recognition extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private SpeechRecognizer speechRecognizer;
    private TextView recognizedTextView;
    private Button startListeningButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_recognition);

        recognizedTextView = findViewById(R.id.Text);
        startListeningButton = findViewById(R.id.startListeningButton);

        // Check and request microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            initializeSpeechRecognizer();
        }

        startListeningButton.setOnClickListener(v -> startListening());
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0);
                    recognizedTextView.setText("You: " + recognizedText);

                    // Send recognized text to AI
                    AIResponseHandler.fetchAIResponse(recognizedText, new AIResponseHandler.AIResponseCallback() {
                        @Override
                        public void onSuccess(String aiResponse) {
                            runOnUiThread(() -> {
                                recognizedTextView.append("\nAI: " + aiResponse);
                                ElevenLabsTextToSpeech.speak(aiResponse, speech_recognition.this);
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            runOnUiThread(() -> Toast.makeText(speech_recognition.this, errorMessage, Toast.LENGTH_SHORT).show());
                        }
                    });
                }
            }

            @Override
            public void onError(int error) {
                String message;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO: message = "Audio recording error"; break;
                    case SpeechRecognizer.ERROR_CLIENT: message = "Client-side error"; break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: message = "Insufficient permissions"; break;
                    case SpeechRecognizer.ERROR_NETWORK: message = "Network error"; break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: message = "Network timeout"; break;
                    case SpeechRecognizer.ERROR_NO_MATCH: message = "No match found"; break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: message = "Recognizer busy"; break;
                    case SpeechRecognizer.ERROR_SERVER: message = "Server error"; break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: message = "No speech input"; break;
                    default: message = "Unknown error"; break;
                }
                Log.e("SpeechRecognition", "Error: " + message);
                Toast.makeText(speech_recognition.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }

            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        if (speechRecognizer != null) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizer.startListening(intent);
        } else {
            Toast.makeText(this, "Speech recognizer not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }

    // Handle microphone permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeSpeechRecognizer();
            } else {
                Toast.makeText(this, "Permission denied. Speech recognition won't work.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
