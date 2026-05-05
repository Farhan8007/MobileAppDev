package com.example.SOS;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SafePulse_Main";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CHECK_SETTINGS = 101;
    private static final String PREFS_NAME = "SafePulsePrefs";
    private static final String KEY_CONTACTS = "trusted_contacts";

    private FusedLocationProviderClient fusedLocationClient;
    private List<String> trustedContacts;
    private View mainLayout;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.main);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadContacts();

        MaterialButton btnSafe = findViewById(R.id.btnSafe);
        MaterialButton btnSos = findViewById(R.id.btnSos);
        View fabHistory = findViewById(R.id.fabHistory);
        View fabContacts = findViewById(R.id.fabContacts);

        btnSafe.setOnClickListener(v -> {
            Log.d(TAG, "Safe button clicked");
            checkPermissionsAndSendStatus(false);
        });

        // Inform users they need to hold the button
        btnSos.setOnClickListener(v -> {
            Toast.makeText(this, "HOLD the button for 2 seconds to send SOS!", Toast.LENGTH_SHORT).show();
        });

        btnSos.setOnLongClickListener(v -> {
            Log.d(TAG, "SOS button long pressed");
            checkPermissionsAndSendStatus(true);
            return true;
        });

        fabHistory.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HistoryActivity.class)));
        fabContacts.setOnClickListener(v -> showContactsDialog());

        requestPermissions();
    }

    private void loadContacts() {
        String savedContacts = prefs.getString(KEY_CONTACTS, "");
        if (savedContacts.isEmpty()) {
            trustedContacts = new ArrayList<>();
        } else {
            trustedContacts = new ArrayList<>(Arrays.asList(savedContacts.split(",")));
        }
        Log.d(TAG, "Loaded contacts: " + trustedContacts.size());
    }

    private void saveContacts(String contactsCsv) {
        prefs.edit().putString(KEY_CONTACTS, contactsCsv).apply();
        loadContacts();
    }

    private void showContactsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manage Trusted Contacts");
        builder.setMessage("Enter phone numbers separated by commas (e.g., 1234567890, 0987654321)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setText(prefs.getString(KEY_CONTACTS, ""));
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            saveContacts(input.getText().toString().trim());
            Toast.makeText(this, "Contacts updated", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        if (!hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean hasPermissions(String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void checkPermissionsAndSendStatus(boolean isEmergency) {
        if (trustedContacts.isEmpty()) {
            Toast.makeText(this, "Please add contacts first!", Toast.LENGTH_LONG).show();
            showContactsDialog();
            return;
        }

        if (hasPermissions(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION)) {
            checkLocationSettingsAndFetch(isEmergency);
        } else {
            requestPermissions();
        }
    }

    private void checkLocationSettingsAndFetch(boolean isEmergency) {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            fetchLocationAndSend(isEmergency);
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    showLocationFailureSnackbar(isEmergency);
                }
            } else {
                showLocationFailureSnackbar(isEmergency);
            }
        });
    }

    private void fetchLocationAndSend(boolean isEmergency) {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null && (System.currentTimeMillis() - location.getTime() < 30000)) {
                    sendSms(isEmergency, location);
                } else {
                    requestCurrentLocation(isEmergency);
                }
            }).addOnFailureListener(e -> requestCurrentLocation(isEmergency));
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission error", e);
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestCurrentLocation(boolean isEmergency) {
        try {
            CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMaxUpdateAgeMillis(5000)
                    .build();

            CancellationTokenSource cts = new CancellationTokenSource();
            fusedLocationClient.getCurrentLocation(request, cts.getToken())
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            sendSms(isEmergency, location);
                        } else {
                            showLocationFailureSnackbar(isEmergency);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Could not fetch current location", e);
                        showLocationFailureSnackbar(isEmergency);
                    });
        } catch (SecurityException e) {
            showLocationFailureSnackbar(isEmergency);
        }
    }

    private void showLocationFailureSnackbar(boolean isEmergency) {
        Snackbar.make(mainLayout, "Location unavailable. Send anyway?", Snackbar.LENGTH_LONG)
                .setAction("SEND ANYWAY", v -> sendSms(isEmergency, null))
                .show();
    }

    private void sendSms(boolean isEmergency, Location location) {
        String message;
        String locationLink = location != null ? 
                "\nMy location: https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude() : "";

        if (isEmergency) {
            // Using [EMERGENCY] text instead of emoji to ensure standard encoding
            message = "[EMERGENCY] - I need help!" + locationLink;
        } else {
            message = "I am Safe and have reached the destination safely. Don't Worry :)" + locationLink;
        }

        SmsManager smsManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            smsManager = getSystemService(SmsManager.class);
        } else {
            smsManager = SmsManager.getDefault();
        }

        if (smsManager == null) {
            Toast.makeText(this, "SMS Manager not available", Toast.LENGTH_SHORT).show();
            return;
        }

        int contactsCount = 0;
        for (String contact : trustedContacts) {
            String cleanNumber = contact.trim();
            if (!cleanNumber.isEmpty()) {
                try {
                    // Use divideMessage and sendMultipartTextMessage to handle longer messages or encoding changes
                    ArrayList<String> parts = smsManager.divideMessage(message);
                    smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null);
                    contactsCount++;
                    Log.d(TAG, "SMS queued for: " + cleanNumber);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to send SMS to " + cleanNumber, e);
                }
            }
        }

        if (contactsCount > 0) {
            Toast.makeText(this, (isEmergency ? "🚨 SOS" : "✅ Status") + " sent to " + contactsCount + " contacts", Toast.LENGTH_SHORT).show();
            saveToHistory(message, contactsCount);
        } else {
            Toast.makeText(this, "Failed to send SMS. Check signals/contacts.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToHistory(String message, int contactsCount) {
        HistoryEntry entry = new HistoryEntry(System.currentTimeMillis(), contactsCount, message);
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(getApplicationContext()).historyDao().insert(entry);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) allGranted = false;
            }
            if (allGranted) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions required!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "GPS enabled! Try sending again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
