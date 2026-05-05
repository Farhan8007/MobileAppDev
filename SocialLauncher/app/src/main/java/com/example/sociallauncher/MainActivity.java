package com.example.sociallauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnWhatsApp = findViewById(R.id.btnWhatsApp);
        Button btnInstagram = findViewById(R.id.btnInstagram);
        Button btnFacebook = findViewById(R.id.btnFacebook);
        Button btnTwitter = findViewById(R.id.btnTwitter);

        btnWhatsApp.setOnClickListener(v -> openApp("com.whatsapp"));
        btnInstagram.setOnClickListener(v -> openApp("com.instagram.android"));
        btnFacebook.setOnClickListener(v -> openApp("com.facebook.katana"));
        btnTwitter.setOnClickListener(v -> openApp("com.twitter.android"));
    }

    private void openApp(String packageName) {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        } else {
            // App not installed, open Play Store
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + packageName)));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
            Toast.makeText(this, "App not installed. Redirecting to Play Store", Toast.LENGTH_SHORT).show();
        }
    }
}