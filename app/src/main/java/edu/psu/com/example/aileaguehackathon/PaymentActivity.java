package edu.psu.com.example.aileaguehackathon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class PaymentActivity extends AppCompatActivity {

    private TextView matchNameText;
    private TextView seatCategoryText;
    private TextView matchDateText;
    private TextView ticketPriceText;
    private TextView totalPriceText;

    private MaterialCardView applePayCard;
    private MaterialCardView googlePayCard;
    private MaterialCardView paypalCard;

    private RadioButton applePayRadio;
    private RadioButton googlePayRadio;
    private RadioButton paypalRadio;

    private MaterialButton confirmPaymentButton;

    private String selectedPaymentMethod = "";
    private double ticketPrice = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize views
        matchNameText = findViewById(R.id.matchNameText);
        seatCategoryText = findViewById(R.id.seatCategoryText);
        matchDateText = findViewById(R.id.matchDateText);
        ticketPriceText = findViewById(R.id.ticketPriceText);
        totalPriceText = findViewById(R.id.totalPriceText);

        applePayCard = findViewById(R.id.applePayCard);
        googlePayCard = findViewById(R.id.googlePayCard);
        paypalCard = findViewById(R.id.paypalCard);

        applePayRadio = findViewById(R.id.applePayRadio);
        googlePayRadio = findViewById(R.id.googlePayRadio);
        paypalRadio = findViewById(R.id.paypalRadio);

        confirmPaymentButton = findViewById(R.id.confirmPaymentButton);

        // Get ticket data from intent
        getTicketDataFromIntent();

        // Setup payment method selection
        setupPaymentMethodSelection();

        // Setup confirm button
        confirmPaymentButton.setOnClickListener(v -> {
            if (selectedPaymentMethod.isEmpty()) {
                Toast.makeText(PaymentActivity.this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            } else {
                processPayment();
            }
        });
    }

    private void getTicketDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String matchName = intent.getStringExtra("match_name");
            String seatCategory = intent.getStringExtra("seat_category");
            String matchDate = intent.getStringExtra("match_date");
            ticketPrice = intent.getDoubleExtra("price", 0.0);

            // Set values to views
            if (matchName != null) matchNameText.setText(matchName);
            if (seatCategory != null) seatCategoryText.setText(seatCategory);
            if (matchDate != null) matchDateText.setText(matchDate);

            // Format currency values
            ticketPriceText.setText(String.format("$%.2f", ticketPrice));

            // Calculate total (could add fees, taxes, etc.)
            double serviceFee = 15.00; // Example service fee
            double total = ticketPrice + serviceFee;

            // Show itemized costs
            ticketPriceText.setText(String.format("$%.2f", ticketPrice));
            totalPriceText.setText(String.format("$%.2f", total));

            // Update the layout to show service fee
            // This requires adding a new TextView in the layout for service fee
            TextView serviceFeeText = findViewById(R.id.serviceFeeText);
            if (serviceFeeText != null) {
                serviceFeeText.setText(String.format("$%.2f", serviceFee));
            }
        }
    }

    private void setupPaymentMethodSelection() {
        // Apple Pay selection
        applePayCard.setOnClickListener(v -> {
            selectPaymentMethod("Apple Pay");
            applePayRadio.setChecked(true);
            googlePayRadio.setChecked(false);
            paypalRadio.setChecked(false);
        });

        applePayRadio.setOnClickListener(v -> {
            selectPaymentMethod("Apple Pay");
            googlePayRadio.setChecked(false);
            paypalRadio.setChecked(false);
        });

        // Google Pay selection
        googlePayCard.setOnClickListener(v -> {
            selectPaymentMethod("Google Pay");
            applePayRadio.setChecked(false);
            googlePayRadio.setChecked(true);
            paypalRadio.setChecked(false);
        });

        googlePayRadio.setOnClickListener(v -> {
            selectPaymentMethod("Google Pay");
            applePayRadio.setChecked(false);
            paypalRadio.setChecked(false);
        });

        // PayPal selection
        paypalCard.setOnClickListener(v -> {
            selectPaymentMethod("PayPal");
            applePayRadio.setChecked(false);
            googlePayRadio.setChecked(false);
            paypalRadio.setChecked(true);
        });

        paypalRadio.setOnClickListener(v -> {
            selectPaymentMethod("PayPal");
            applePayRadio.setChecked(false);
            googlePayRadio.setChecked(false);
        });
    }

    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;
        Toast.makeText(this, method + " selected", Toast.LENGTH_SHORT).show();
    }

    private void processPayment() {
        // Show loading indicator
        confirmPaymentButton.setEnabled(false);
        confirmPaymentButton.setText("Processing...");

        // Simulate payment processing
        confirmPaymentButton.postDelayed(() -> {
            // Show success message with actual payment amount
            double totalAmount = ticketPrice;
            TextView totalPriceText = findViewById(R.id.totalPriceText);
            if (totalPriceText != null) {
                try {
                    String totalText = totalPriceText.getText().toString();
                    if (totalText.startsWith("$")) {
                        totalAmount = Double.parseDouble(totalText.substring(1));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            Toast.makeText(PaymentActivity.this,
                    String.format("Payment of $%.2f successful with %s", totalAmount, selectedPaymentMethod),
                    Toast.LENGTH_LONG).show();

            // In a real app, you might navigate to a confirmation screen or ticket display
            // For now, just finish the activity
            finish();
        }, 2000); // 2 second delay to simulate processing
    }
}