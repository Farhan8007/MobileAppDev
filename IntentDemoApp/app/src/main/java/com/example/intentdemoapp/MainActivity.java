package com.example.intentdemoapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final int REQUEST_SMS_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCall = findViewById(R.id.btnCall);
        Button btnSms = findViewById(R.id.btnSms);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnNetwork = findViewById(R.id.btnNetwork);

        // Make a Call
        btnCall.setOnClickListener(v -> {
            String phone = "1234567890"; // Replace with number
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phone));

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
            } else {
                startActivity(callIntent);
            }
        });

        // Send SMS
        btnSms.setOnClickListener(v -> {
            String smsNumber = "1234567890";
            String message = "Hello from my app!";
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + smsNumber));
            smsIntent.putExtra("sms_body", message);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
            } else {
                startActivity(smsIntent);
            }
        });

        // Open Settings
        btnSettings.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(settingsIntent);
        });

        // Fetch Network Info
        btnNetwork.setOnClickListener(v -> {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
            } else {
                String networkOperator = tm.getNetworkOperatorName();
                Toast.makeText(this, "Network: " + networkOperator, Toast.LENGTH_LONG).show();
            }
        });
    }
}