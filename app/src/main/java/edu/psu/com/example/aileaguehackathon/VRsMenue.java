package edu.psu.com.example.aileaguehackathon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class VRsMenue extends AppCompatActivity {


    BottomNavigationView bottomNavigationView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test3_dmodels);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

    }


    public void navigateToVR_ShowActivity(View view){
        int id  = view.getId();
        String idName = getResources().getResourceEntryName(id);

        Intent intent = new Intent(VRsMenue.this, VR_ViewActivity.class);
        intent.putExtra("design_id", idName.substring(2)); // Passing the URL
        startActivity(intent);
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
        bottomNavigationView.setSelectedItemId(R.id.nav_vr); // Highlight current tab

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_saud) {
                navigateTo(ChatbotTexting.class);
                return true;
            } else if (itemId == R.id.nav_vr) {
                return true; // Already on this page
            } else if (itemId == R.id.nav_tick) {
                navigateTo(TestTicketPrice.class);
                return true;
            }
            return false;
        });
    }


}