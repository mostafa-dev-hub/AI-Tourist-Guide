package edu.psu.com.example.aileaguehackathon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// MainActivity.java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TestTicketPrice extends AppCompatActivity {

    private static final String API_BASE_URL = "http://Url:5000/"; // Change to your actual API endpoint
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;
    private TicketAdapter ticketAdapter;
    private List<Ticket> ticketList;

    // UI components - using Material Components
    private MaterialAutoCompleteTextView matchDropdown;
    private MaterialAutoCompleteTextView seatCategoryDropdown;
    private TextInputEditText selectedDateText;
    private Slider demandSlider;
    private MaterialButton calculateButton;
    private MaterialButton payNowButton;
    private View loadingProgressBar;
    private RecyclerView ticketsRecyclerView;
    private android.widget.TextView resultPriceText;
    private android.widget.TextView currentDateText;
    BottomNavigationView bottomNavigationView;

    // Match data
    private List<Match> matches;

    // Mapping to maintain backend compatibility
    private Map<String, String> seatCategoryMap = new HashMap<>();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_ticket_price);

        // Initialize OkHttpClient
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Initialize UI components
        matchDropdown = findViewById(R.id.matchSpinner);
        seatCategoryDropdown = findViewById(R.id.seatCategorySpinner);
        selectedDateText = findViewById(R.id.selectedDateText);
        demandSlider = findViewById(R.id.demandSlider);
        calculateButton = findViewById(R.id.calculateButton);
        payNowButton = findViewById(R.id.payNowButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        ticketsRecyclerView = findViewById(R.id.ticketsRecyclerView);
        resultPriceText = findViewById(R.id.resultPriceText);
        currentDateText = findViewById(R.id.currentDateText);

        // Initialize category mapping
        setupCategoryMapping();

        // Setup RecyclerView
        ticketList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(ticketList);
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ticketsRecyclerView.setAdapter(ticketAdapter);

        // Initialize match data
        initializeMatchData();

        // Setup date picker
        setupDatePicker();

        // Update current date display
        updateCurrentDateDisplay();

        // Setup calculate button
        calculateButton.setOnClickListener(v -> calculatePrice());

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();


    }

    private void navigateTo(Class<?> destinationActivity) {
        if (this.getClass() != destinationActivity) {
            Intent intent = new Intent(this, destinationActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_tick); // Highlight current tab

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_vr) {
                navigateTo(VRsMenue.class);
                return true;
            } else if (itemId == R.id.nav_tick) {
                return true; // Already on this page
            } else if (itemId == R.id.nav_saud) {
                navigateTo(ChatbotTexting.class);
                return true;
            }
            return false;
        });
    }

    private void setupCategoryMapping() {
        seatCategoryMap.put("VIP - Executive Box", "category_1");
        seatCategoryMap.put("Premium - Midfield View", "category_2");
        seatCategoryMap.put("Standard - Good Visibility", "category_3");
        seatCategoryMap.put("Economy - Basic Seating", "category_4");
    }

    private void initializeMatchData() {
        // In a real app, this would come from an API
        matches = new ArrayList<>();

        // Sample matches for KSA World Cup 2034
        matches.add(new Match("Opening Match: KSA vs Brazil", "Riyadh", 80000, 10));
        matches.add(new Match("Group A: France vs Germany", "Jeddah", 60000, 8));
        matches.add(new Match("Group B: Spain vs Argentina", "Dammam", 50000, 9));
        matches.add(new Match("Round of 16: TBD vs TBD", "Riyadh", 80000, 7));
        matches.add(new Match("Quarter Final: TBD vs TBD", "Jeddah", 60000, 9));
        matches.add(new Match("Semi Final: TBD vs TBD", "Riyadh", 80000, 10));
        matches.add(new Match("Final: TBD vs TBD", "Riyadh", 80000, 10));

        // Setup match dropdown with ArrayAdapter for AutoCompleteTextView
        List<String> matchNames = new ArrayList<>();
        for (Match match : matches) {
            matchNames.add(match.getName());
        }

        ArrayAdapter<String> matchAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, matchNames);
        matchDropdown.setAdapter(matchAdapter);
        matchDropdown.setText(matchNames.get(0), false); // Set default selection

        // Setup seating category dropdown
        String[] seatCategories = {
                "VIP - Executive Box",
                "Premium - Midfield View",
                "Standard - Good Visibility",
                "Economy - Basic Seating"
        };

        ArrayAdapter<String> seatAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, seatCategories);
        seatCategoryDropdown.setAdapter(seatAdapter);
        seatCategoryDropdown.setText(seatCategories[0], false); // Set default selection
    }

    private void setupDatePicker() {
        selectedDateText.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Match Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Date date = new Date(selection);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                selectedDateText.setText(sdf.format(date));
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
    }

    private void updateCurrentDateDisplay() {
        // Get current date
        Date currentDate = new Date();

        // Format the date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(currentDate);

        // Update the TextView
        if (currentDateText != null) {
            currentDateText.setText("Current Date: " + formattedDate);
        }
    }

    private Match findMatchByName(String matchName) {
        for (Match match : matches) {
            if (match.getName().equals(matchName)) {
                return match;
            }
        }
        return null;
    }

    private void calculatePrice() {
        String selectedDateValue = selectedDateText.getText().toString();
        if (selectedDateValue.isEmpty() || "Select Date".equals(selectedDateValue)) {
            Toast.makeText(this, "Please select a match date", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingProgressBar.setVisibility(View.VISIBLE);

        try {
            // Get selected match
            String selectedMatchName = matchDropdown.getText().toString();
            Match selectedMatch = null;
            for (Match match : matches) {
                if (match.getName().equals(selectedMatchName)) {
                    selectedMatch = match;
                    break;
                }
            }

            if (selectedMatch == null) {
                Toast.makeText(this, "Please select a valid match", Toast.LENGTH_SHORT).show();
                loadingProgressBar.setVisibility(View.GONE);
                return;
            }

            // Get selected seat category (map to backend category)
            String selectedSeatName = seatCategoryDropdown.getText().toString();
            String seatCategory = seatCategoryMap.get(selectedSeatName);

            // Parse date and calculate days until match
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date matchDate = sdf.parse(selectedDateText.getText().toString());
            Date currentDate = new Date();

            long diffInMillies = Math.abs(matchDate.getTime() - currentDate.getTime());
            long daysUntilMatch = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            // Check if date is in the future
            if (matchDate.before(currentDate)) {
                Toast.makeText(this, "Please select a future date", Toast.LENGTH_SHORT).show();
                loadingProgressBar.setVisibility(View.GONE);
                return;
            }

            // Get current demand from slider
            float currentDemand = demandSlider.getValue() / 100f;

            // Check if the selected date is a weekend
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(matchDate);
            boolean isWeekend = (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);

            // Get seat quality
            int seatingQuality = getSeatQuality(selectedSeatName);

            // Prepare request data
            JSONObject requestData = new JSONObject();
            requestData.put("match_importance", selectedMatch.getImportance());
            requestData.put("days_until_match", daysUntilMatch);
            requestData.put("current_demand", currentDemand);
            requestData.put("venue_capacity", selectedMatch.getCapacity());
            requestData.put("seating_category", seatCategory);
            requestData.put("seating_quality", seatingQuality);
            requestData.put("is_weekend", isWeekend);
            requestData.put("participating_teams_ranking", 20); // Example value - would be dynamic in real app
            requestData.put("historical_interest", 8); // Example value - would be dynamic in real app

            // Create request
            RequestBody body = RequestBody.create(requestData.toString(), JSON);
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "calculate_price")
                    .post(body)
                    .build();

            // Execute request
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(TestTicketPrice.this, "Network error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        loadingProgressBar.setVisibility(View.GONE);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();


                        String selectedMatchName = matchDropdown.getText().toString();
                        final Match selectedMatch = findMatchByName(selectedMatchName);

                        if (selectedMatch == null) {
                            Toast.makeText(TestTicketPrice.this, "Please select a valid match", Toast.LENGTH_SHORT).show();
                            loadingProgressBar.setVisibility(View.GONE);
                            return;
                        }

                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            double price = jsonResponse.getDouble("price");

                            runOnUiThread(() -> {
                                showCalculatedPrice(price);

                                // Add to ticket list
                                ticketList.add(0, new Ticket(
                                        selectedMatch.getName(),
                                        selectedSeatName,
                                        sdf.format(matchDate),
                                        price
                                ));
                                ticketAdapter.notifyDataSetChanged();

                                loadingProgressBar.setVisibility(View.GONE);
                            });

                        } catch (JSONException e) {
                            runOnUiThread(() -> {
                                Toast.makeText(TestTicketPrice.this, "Error parsing response",
                                        Toast.LENGTH_SHORT).show();
                                loadingProgressBar.setVisibility(View.GONE);
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(TestTicketPrice.this,
                                    "Server error: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            loadingProgressBar.setVisibility(View.GONE);
                        });
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            loadingProgressBar.setVisibility(View.GONE);
        }
    }



    private void showCalculatedPrice(double price) {
        resultPriceText.setText(String.format(Locale.getDefault(), "Calculated Price: $%.2f", price));

        // Show the pay button
        payNowButton.setVisibility(View.VISIBLE);
    }

    public void processPayment(View view) {
        // Get the last/most recently added ticket from the ticket list
        if (ticketList.isEmpty()) {
            Toast.makeText(this, "No ticket available for payment", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the most recent ticket (first in the list since we add at index 0)
        Ticket selectedTicket = ticketList.get(0);

        // Create intent to launch PaymentActivity
        Intent intent = new Intent(TestTicketPrice.this, PaymentActivity.class);

        // Add ticket details as extras
        intent.putExtra("match_name", selectedTicket.getMatchName());
        intent.putExtra("seat_category", selectedTicket.getSeatCategory());
        intent.putExtra("match_date", selectedTicket.getMatchDate());
        intent.putExtra("price", selectedTicket.getPrice());

        // Start PaymentActivity
        startActivity(intent);
    }

    private int getSeatQuality(String seatCategory) {
        switch (seatCategory) {
            case "VIP - Executive Box": return 10;
            case "Premium - Midfield View": return 8;
            case "Standard - Good Visibility": return 6;
            case "Economy - Basic Seating": return 4;
            default: return 5;
        }
    }
}


 class Match {
    private String name;
    private String venue;
    private int capacity;
    private int importance;

    public Match(String name, String venue, int capacity, int importance) {
        this.name = name;
        this.venue = venue;
        this.capacity = capacity;
        this.importance = importance;
    }

    public String getName() {
        return name;
    }

    public String getVenue() {
        return venue;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getImportance() {
        return importance;
    }
}

 class Ticket {
    private String matchName;
    private String seatCategory;
    private String matchDate;
    private double price;

    public Ticket(String matchName, String seatCategory, String matchDate, double price) {
        this.matchName = matchName;
        this.seatCategory = seatCategory;
        this.matchDate = matchDate;
        this.price = price;
    }

    public String getMatchName() {
        return matchName;
    }

    public String getSeatCategory() {
        return seatCategory;
    }

    public String getMatchDate() {
        return matchDate;
    }

    public double getPrice() {
        return price;
    }
}





 class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Ticket> tickets;

    public TicketAdapter(List<Ticket> tickets) {
        this.tickets = tickets;
    }



     @NonNull
     @Override
     public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view = LayoutInflater.from(parent.getContext())
                 .inflate(R.layout.item_ticket, parent, false);
         return new TicketViewHolder(view);
     }
     @Override
     public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
         Ticket ticket = tickets.get(position);

         holder.matchNameText.setTextColor(Color.parseColor("#9D2235"));
         holder.seatCategoryText.setTextColor(Color.parseColor("#555555"));
         holder.matchDateText.setTextColor(Color.parseColor("#555555"));
         holder.priceText.setTextColor(Color.parseColor("#9D2235"));

         holder.matchNameText.setText(ticket.getMatchName());
         holder.seatCategoryText.setText("Category: " + ticket.getSeatCategory());
         holder.matchDateText.setText("Date: " + ticket.getMatchDate());
         holder.priceText.setText(String.format(Locale.getDefault(),
                 "Price: $%.2f", ticket.getPrice()));
     }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView matchNameText;
        TextView seatCategoryText;
        TextView matchDateText;
        TextView priceText;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            matchNameText = itemView.findViewById(R.id.matchNameText);
            seatCategoryText = itemView.findViewById(R.id.seatCategoryText);
            matchDateText = itemView.findViewById(R.id.matchDateText);
            priceText = itemView.findViewById(R.id.priceText);
        }
    }
}