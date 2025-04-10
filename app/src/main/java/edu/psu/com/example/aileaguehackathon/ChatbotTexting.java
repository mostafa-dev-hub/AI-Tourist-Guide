package edu.psu.com.example.aileaguehackathon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;




public class ChatbotTexting extends AppCompatActivity {

    private static final String PREF_NAME = "WorldCupApp";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USER_NAME = "USER_NAME";

    RecyclerView recyclerView;
    EditText messageEditText;
    ImageButton sendButton;
    LinearLayout suggested_messages_layout;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    public static final MediaType JSON = MediaType.get("application/json");
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String url = "http://url:5000"; // URL for Flask server
    private String POST = "POST";
    private String GET = "GET";
    private static final String TAG = "Whisper";
    private String userId;

    private static final String API_KEY = "openai_key";

    private ImageButton recordButton;
    private MediaRecorder recorder;
    private String fileName = null;
    private boolean isRecording = false;
    private OkHttpClient client;
    private String userName;

    RelativeLayout relativeLayout;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot_texting);

        messageList = new ArrayList<>();
        relativeLayout = findViewById(R.id.persistent_header);
        // relativeLayout.setVisibility(View.GONE);

        // Get userId from intent or saved instance state
        if (savedInstanceState != null) {
            userId = savedInstanceState.getString(KEY_USER_ID);
            userName = savedInstanceState.getString(KEY_USER_NAME);
        } else {
            userId = getIntent().getStringExtra("USER_ID");
            userName = getIntent().getStringExtra("User_Name");
        }

        // If userId is still not available, try to get it from SharedPreferences
        if (userId == null || userId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            userId = prefs.getString(KEY_USER_ID, "");
            userName = prefs.getString(KEY_USER_NAME, "");

            if (userId.isEmpty()) {
                Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_LONG).show();
                finish(); // Close activity if no user ID is found
                return;
            }
        }

        // Save user data to SharedPreferences for persistence
        saveUserData();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

        // Log user ID for debugging
        Log.d("ChatbotTexting", "User ID: " + userId);
        suggested_messages_layout = findViewById(R.id.suggested_messages);
        recyclerView = findViewById(R.id.recycler_view2);
        recyclerView.setVisibility(View.GONE);
        messageEditText = findViewById(R.id.message_edit_text2);
        sendButton = findViewById(R.id.send_bottom2);
        recordButton = findViewById(R.id.recordBtn);

        // setup recyclerview
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        // Check for Recording permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        // Setup HTTP client
        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        // Test server connection after client is initialized
        testServerConnection();

        // Setup file path for recordings
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audio_recording.mp3";

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                } else {
                    stopRecording();
                    uploadAudio();
                }
            }
        });

        sendButton.setOnClickListener(
                view -> {
                    String userQuery = messageEditText.getText().toString().trim();

                    if (userQuery.isEmpty()) {
                        messageEditText.setError("This cannot be empty for post request");
                        return;
                    }

                    suggested_messages_layout.setVisibility(View.GONE);
                    relativeLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);

                    addToChat(userQuery, Message.SENT_BY_ME);
                    messageEditText.setText("");

                    sendChatRequest(userQuery);
                });
    }

    // Add this method to save user data to SharedPreferences
    private void saveUserData() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    // Override onSaveInstanceState to preserve user data during configuration changes
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_USER_ID, userId);
        outState.putString(KEY_USER_NAME, userName);
    }

    // Consider adding this method to restore chat history when returning
    @Override
    protected void onResume() {
        super.onResume();

        // Check for user data again just in case
        if (userId == null || userId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            userId = prefs.getString(KEY_USER_ID, "");
            userName = prefs.getString(KEY_USER_NAME, "");

            if (userId.isEmpty()) {
                Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_LONG).show();
                finish(); // Close activity if no user ID is found
                return;
            }
        }

        // If you've just navigated back to this activity and need to restore chat
        if (recyclerView.getVisibility() == View.VISIBLE && messageList.isEmpty()) {
            // You could either reload chat history from server or SharedPreferences here
            // For simplicity, just adding a reconnection message:
            if (!messageList.isEmpty()) {
                return; // Already has messages
            }
            suggested_messages_layout.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            addToChat("Welcome back! How can I help you today?", Message.SENT_BY_BOT);
        }
    }

    // New method for testing server connection
    private void testServerConnection() {
        Request request = new Request.Builder()
                .url(url + "/user/" + userId)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ServerTest", "Connection failed: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(
                        ChatbotTexting.this,
                        "Server connection failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "No response body";
                Log.d("ServerTest", "Response code: " + response.code());
                Log.d("ServerTest", "Response body: " + responseBody);

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(
                            ChatbotTexting.this,
                            "Server error: " + response.code() + " - " + responseBody,
                            Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    // New method specifically for chat requests
    private void sendChatRequest(String userQuery) {
        try {
            // Create a JSON object with both user query and user ID
            JSONObject json = new JSONObject();
            json.put("query", userQuery);
            json.put("user_id", userId);

            String fullURL = url + "/chat";

            Log.d("ChatRequest", "Sending to " + fullURL + ": " + json.toString());

            RequestBody requestBody = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            messageList.add(new Message("Thinking... 🤔", Message.SENT_BY_BOT));

            Request request = new Request.Builder()
                    .url(fullURL)
                    .post(requestBody)
                    .header("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("ChatRequest", "Request failed: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        addResponse("Oops! Connection failed: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    final String responseData = response.body() != null ? response.body().string() : "";
                    Log.d("ChatRequest", "Response code: " + response.code());
                    Log.d("ChatRequest", "Response body: " + responseData);

                    if(response.isSuccessful()) {
                        try {
                            // Parse the JSON response
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String botResponse = jsonResponse.getString("response");
                            runOnUiThread(() -> addResponse(botResponse.trim()));
                        } catch (JSONException e) {
                            // If the response is not in JSON format, use it directly
                            runOnUiThread(() -> addResponse(responseData.trim()));
                        }
                    } else {
                        runOnUiThread(() -> {
                            // Show actual error message for debugging
                            addResponse("Oops! Saud is busy. Try Again");
                            Log.e("ChatError", "Server error: " + responseData);
                        });
                    }
                }
            });

        } catch (JSONException e) {
            Log.e("ChatRequest", "JSON error: " + e.getMessage(), e);
            runOnUiThread(() -> {
                addResponse("Oops! Error preparing your message: " + e.getMessage());
            });
        }
    }

    void addToChat(String message, String sentBY){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message,sentBY)) ;
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response, Message.SENT_BY_BOT);
    }

    public void sendRecommendedMessages(View view){
        String text = ((TextView) view).getText().toString().trim();
        suggested_messages_layout.setVisibility(View.GONE);
        relativeLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        addToChat(text, Message.SENT_BY_ME);
        messageEditText.setText("");
        sendChatRequest(text);
    }

    // Keep the old method for backward compatibility
    public void sendRequest(String type, String method, String paramName, String paramValue) {
        if (method.equals("chat")) {
            sendChatRequest(paramValue);
            return;
        }

        String fullURL = url + "/" + method;
        Request request;

        if (type.equals(POST)) {
            try {
                // Create a JSON object with the parameter and user ID
                JSONObject json = new JSONObject();
                json.put(paramName, paramValue);
                json.put("user_id", userId);

                RequestBody requestBody = RequestBody.create(
                        json.toString(),
                        MediaType.parse("application/json; charset=utf-8")
                );

                messageList.add(new Message("Thinking... 🤔", Message.SENT_BY_BOT));

                request = new Request.Builder()
                        .url(fullURL)
                        .post(requestBody)
                        .header("Content-Type", "application/json")
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    addResponse("Error preparing your message: " + e.getMessage());
                });
                return;
            }
        } else {
            request = new Request.Builder()
                    .url(fullURL)
                    .get()
                    .build();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    addResponse("Oops! Connection failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";

                if(response.isSuccessful()) {
                    runOnUiThread(() -> addResponse(responseData.trim()));
                } else {
                    runOnUiThread(() -> {
                        addResponse("Oops! Saud is busy. Error: " + responseData);
                        Log.e("ChatError", "Server error: " + responseData);
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio recording permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Audio recording permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
            messageEditText.setText("Listening...");
            recordButton.setImageResource(R.drawable.stop_recording_icon);
        } catch (IOException e) {
            Log.e(TAG, "Recording failed: " + e.getMessage());
            Toast.makeText(this, "Recording failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
                isRecording = false;
                recordButton.setImageResource(R.drawable.voice_icon);
            } catch (Exception e) {
                Log.e(TAG, "Stop recording failed: " + e.getMessage());
            }
        }
    }

    private void uploadAudio() {
        File file = new File(fileName);
        if (!file.exists()) {
            return;
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("audio/mpeg"), file))
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("translate", "false")
                .build();

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .header("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Audio upload failed: " + e.getMessage());
                runOnUiThread(() -> {
                    messageEditText.setText("");
                    Toast.makeText(ChatbotTexting.this,
                            "Audio upload failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject jsonObject = new JSONObject(responseData);
                            String transcription = jsonObject.getString("text");
                            messageEditText.setText(transcription);
                        } else {
                            messageEditText.setText("");
                            Toast.makeText(ChatbotTexting.this,
                                    "Error: " + responseData,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        messageEditText.setText("");
                        Toast.makeText(ChatbotTexting.this,
                                "Error parsing response: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    // Modified navigateTo method to pass user information
    private void navigateTo(Class<?> destinationActivity) {
        if (this.getClass() != destinationActivity) {
            Intent intent = new Intent(this, destinationActivity);
            // Add user information to the intent
            intent.putExtra("USER_ID", userId);
            intent.putExtra("User_Name", userName);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_saud); // Highlight current tab

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_vr) {
                navigateTo(VRsMenue.class);
                return true;
            } else if (itemId == R.id.nav_saud) {
                return true; // Already on this page
            } else if (itemId == R.id.nav_tick) {
                navigateTo(TestTicketPrice.class);
                return true;
            }
            return false;
        });
    }
}