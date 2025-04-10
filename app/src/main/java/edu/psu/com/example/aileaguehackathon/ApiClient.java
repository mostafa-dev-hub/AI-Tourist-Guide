package edu.psu.com.example.aileaguehackathon;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://url:5000";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient client = new OkHttpClient();
    public interface ApiCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public static void registerUser(User user, ApiCallback callback) {
        try {
            // Create JSON object with user data
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", user.getUserId());
            jsonBody.put("name", user.getName());
            jsonBody.put("language", user.getLanguage());

            // Add favorite teams
            JSONArray teamsArray = new JSONArray();
            for (String team : user.getFavoriteTeams()) {
                teamsArray.put(team);
            }
            jsonBody.put("favorite_teams", teamsArray);

            // Add interests
            JSONArray interestsArray = new JSONArray();
            for (String interest : user.getInterests()) {
                interestsArray.put(interest);
            }
            jsonBody.put("interests", interestsArray);

            // Add dietary preferences
            JSONArray dietaryArray = new JSONArray();
            for (String diet : user.getDietaryPreferences()) {
                dietaryArray.put(diet);
            }
            jsonBody.put("dietary_preferences", dietaryArray);

            // Add accessibility needs
            jsonBody.put("needs_accessibility", user.isNeedsAccessibility() ? "Yes" : "No");

            // Add current location
            jsonBody.put("current_location", user.getCurrentLocation());

            // Send to server
            makeApiCall("/register", jsonBody, callback);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON: " + e.getMessage());
            callback.onFailure("Error creating user profile: " + e.getMessage());
        }
    }

    public static void sendChatMessage(String userId, String message, ApiCallback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", userId);
            jsonBody.put("query", message);

            makeApiCall("/chat", jsonBody, callback);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON: " + e.getMessage());
            callback.onFailure("Error sending message: " + e.getMessage());
        }
    }

    private static void makeApiCall(String endpoint, JSONObject jsonBody, ApiCallback callback) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API call failed: " + e.getMessage());
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    callback.onSuccess(responseBody);
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "API error: " + responseBody);
                    callback.onFailure("Server error: " + responseBody);
                }
            }
        });
    }
}