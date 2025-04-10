package edu.psu.com.example.aileaguehackathon;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class VR_ViewActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_view);

        Intent intent = getIntent();
        String MODEL_UID = intent.getStringExtra("design_id");



        WebView webView;


        webView = findViewById(R.id.webView);

        // Enable JavaScript and necessary settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // Load Sketchfab Model
        String iframe = "<iframe title='Sketchfab' width='100%' height='100%' frameborder='0' allowfullscreen " +
                "mozallowfullscreen='true' webkitallowfullscreen='true' " +
                "src='https://sketchfab.com/models/" + MODEL_UID + "/embed?autospin=1&autostart=1'></iframe>";

        String htmlData = "<html><body style='margin:0;padding:0;'>" + iframe + "</body></html>";
        webView.loadData(htmlData, "text/html", "UTF-8");

    }

    public void navigateToVR_Menues(View view) {
        Intent intent = new Intent(VR_ViewActivity.this, VRsMenue.class);
        startActivity(intent);
    }

    public void navigateToHomePage(View view) {
        Intent intent = new Intent(VR_ViewActivity.this, MainActivity.class);
        startActivity(intent);
    }



}