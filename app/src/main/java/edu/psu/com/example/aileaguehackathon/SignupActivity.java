package edu.psu.com.example.aileaguehackathon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

    public class SignupActivity extends AppCompatActivity {

        private static final String TAG = "SignupActivity";
        private static final String API_URL = "http://url:5000/register";
        public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        private TextInputEditText etName, etEmail, etPassword;
        private Spinner spinnerCity;
        private AutoCompleteTextView actvLanguage;
    private CheckBox cbAccessibility;
    private ChipGroup chipGroupTeams, chipGroupInterests, chipGroupDietary;
    private Button btnRegister;

    private OkHttpClient client = new OkHttpClient();
    private List<String> selectedTeams = new ArrayList<>();
    private List<String> selectedInterests = new ArrayList<>();
    private List<String> selectedDietary = new ArrayList<>();
    private String selectedCity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        initViews();
        setupToolbar();
        setupCitySpinner();
        setupLanguageDropdown();
        setupChips();
        setupClickListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerCity = findViewById(R.id.spinnerCity);
        actvLanguage = findViewById(R.id.actvLanguage);
        cbAccessibility = findViewById(R.id.cbAccessibility);
        chipGroupTeams = findViewById(R.id.chipGroupTeams);
        chipGroupInterests = findViewById(R.id.chipGroupInterests);
        chipGroupDietary = findViewById(R.id.chipGroupDietary);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupCitySpinner() {
        // List of Saudi Arabian cities
        String[] cities = {
                "Select a city", // Default prompt item
                "Riyadh", "Jeddah", "Mecca", "Medina", "Dammam",
                "Al Khobar", "Tabuk", "Abha", "Taif", "Khamis Mushait",
                "Buraidah", "Al-Ahsa", "Najran", "Yanbu", "Sakaka",
                "Al Qatif", "Jubail", "Al-Kharj", "Qurayyat"
        };

        // Use custom layouts for the spinner items
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, cities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip the "Select a city" prompt
                    selectedCity = parent.getItemAtPosition(position).toString();
                } else {
                    selectedCity = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCity = "";
            }
        });
    }

    private void setupLanguageDropdown() {
        String[] languages = {"English", "Arabic", "Spanish", "French", "German", "Chinese", "Japanese"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, languages);
        actvLanguage.setAdapter(adapter);
        actvLanguage.setText(languages[0], false);
    }

    private void setupChips() {
        // Teams
        String[] teams = {"Saudi Arabia", "Brazil", "Argentina", "Germany", "France", "Spain", "England", "Italy", "Netherlands", "Portugal"};
        for (String team : teams) {
            Chip chip = createChip(team);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (selectedTeams.size() < 3) {
                        selectedTeams.add(team);
                    } else {
                        chip.setChecked(false);
                        Toast.makeText(SignupActivity.this, "You can select up to 3 teams", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    selectedTeams.remove(team);
                }
            });
            chipGroupTeams.addView(chip);
        }

        // Interests
        String[] interests = {"Stadiums", "Local Cuisine", "Cultural Sites", "Shopping", "Museums", "Outdoor Activities", "Nightlife", "Fan Events"};
        for (String interest : interests) {
            Chip chip = createChip(interest);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedInterests.add(interest);
                } else {
                    selectedInterests.remove(interest);
                }
            });
            chipGroupInterests.addView(chip);
        }

        // Dietary Preferences
        String[] dietary = {"Vegetarian", "Vegan", "Halal", "Kosher", "Gluten-Free", "Dairy-Free", "None"};
        for (String diet : dietary) {
            Chip chip = createChip(diet);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedDietary.add(diet);
                } else {
                    selectedDietary.remove(diet);
                }
            });
            chipGroupDietary.addView(chip);
        }
    }

    private Chip createChip(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCheckable(true);
        chip.setClickable(true);
        return chip;
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> {
            if (validateForm()) {
                registerUser();
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty()) {
            ((TextInputLayout) findViewById(R.id.tilName)).setError("Name is required");
            valid = false;
        } else {
            ((TextInputLayout) findViewById(R.id.tilName)).setError(null);
        }

        if (email.isEmpty()) {
            ((TextInputLayout) findViewById(R.id.tilEmail)).setError("Email is required");
            valid = false;
        } else {
            ((TextInputLayout) findViewById(R.id.tilEmail)).setError(null);
        }

        if (password.isEmpty()) {
            ((TextInputLayout) findViewById(R.id.tilPassword)).setError("Password is required");
            valid = false;
        } else {
            ((TextInputLayout) findViewById(R.id.tilPassword)).setError(null);
        }

        if (selectedCity.isEmpty()) {
            Toast.makeText(this, "Please select a city", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private void registerUser() {
        try {
            // Generate a unique user ID
            String userId = UUID.randomUUID().toString();

            // Create JSON object with user data
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", userId);
            jsonBody.put("name", etName.getText().toString().trim());
            jsonBody.put("language", actvLanguage.getText().toString());

            // Add favorite teams
            JSONArray teamsArray = new JSONArray();
            for (String team : selectedTeams) {
                teamsArray.put(team);
            }
            jsonBody.put("favorite_teams", teamsArray);

            // Add interests
            JSONArray interestsArray = new JSONArray();
            for (String interest : selectedInterests) {
                interestsArray.put(interest);
            }
            jsonBody.put("interests", interestsArray);

            // Add dietary preferences
            JSONArray dietaryArray = new JSONArray();
            for (String diet : selectedDietary) {
                dietaryArray.put(diet);
            }
            jsonBody.put("dietary_preferences", dietaryArray);

            // Add accessibility needs
            jsonBody.put("needs_accessibility", cbAccessibility.isChecked() ? "Yes" : "No");

            // Add current location (selected city)
            jsonBody.put("current_location", selectedCity);

            // Send to server
            sendRegistrationToServer(jsonBody, userId);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON: " + e.getMessage());
            Toast.makeText(this, "Error creating user profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRegistrationToServer(JSONObject jsonBody, String userId) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API call failed: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(SignupActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // For demo purposes, proceed anyway
                    saveUserLocally(userId);
                    proceedToChatScreen(userId);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(SignupActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        saveUserLocally(userId);
                        proceedToChatScreen(userId);
                    });
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "API error: " + responseBody);
                    runOnUiThread(() -> {
                        Toast.makeText(SignupActivity.this, "Server error: " + responseBody, Toast.LENGTH_SHORT).show();
                        // For demo purposes, proceed anyway
                        saveUserLocally(userId);
                        proceedToChatScreen(userId);
                    });
                }
            }
        });
    }

    private void saveUserLocally(String userId) {
        SharedPreferences prefs = getSharedPreferences("WorldCupApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("USER_ID", userId);
        editor.putString("USER_NAME", etName.getText().toString().trim());
        editor.putString("USER_LOCATION", selectedCity);
        editor.apply();
    }

    private void proceedToChatScreen(String userId) {
        Intent intent = new Intent(SignupActivity.this, ChatbotTexting.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("User_Name",etName.getText().toString());
        startActivity(intent);
        finish();
    }
}