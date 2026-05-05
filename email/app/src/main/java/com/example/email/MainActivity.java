package com.example.email;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText etTo, etSubject, etMessage;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔴 THIS LINE WAS MISSING
        setContentView(R.layout.activity_main);

        // Initialize views
        etTo = findViewById(R.id.etTo);
        etSubject = findViewById(R.id.etSubject);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String to = etTo.getText().toString();
                String subject = etSubject.getText().toString();
                String message = etMessage.getText().toString();

                Toast.makeText(
                        MainActivity.this,
                        "To: " + to + "\nSubject: " + subject,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
