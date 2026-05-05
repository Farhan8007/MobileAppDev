package com.example.notification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    String channel_id = "01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b = findViewById(R.id.button);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NotificationManager nm =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(MainActivity.this, channel_id)
                                .setSmallIcon(R.drawable.logo) // make sure logo exists
                                .setContentTitle("Notification of Application")
                                .setContentText("This is my first push notification")
                                .setPriority(NotificationCompat.PRIORITY_HIGH);

                // For Android Oreo and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel nc = new NotificationChannel(
                            channel_id,
                            "CHANNEL_01",
                            NotificationManager.IMPORTANCE_HIGH
                    );
                    nm.createNotificationChannel(nc);
                }

                nm.notify(1, mBuilder.build());
            }
        });
    }
}