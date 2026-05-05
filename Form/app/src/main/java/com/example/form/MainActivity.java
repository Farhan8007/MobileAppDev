package com.example.form;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText etName, etAge, etDob;
    Button btnSubmit;
    TextView tvResult;

    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etDob = findViewById(R.id.etDob);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvResult = findViewById(R.id.tvResult);

        calendar = Calendar.getInstance();

        // 📅 Date Picker
        etDob.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String dob = dayOfMonth + "/" + (month + 1) + "/" + year;
                        etDob.setText(dob);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // 🚀 Submit Button
        btnSubmit.setOnClickListener(v -> {

            String name = etName.getText().toString();
            String age = etAge.getText().toString();
            String dob = etDob.getText().toString();

            if (name.isEmpty() || age.isEmpty() || dob.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentTime = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm:ss",
                    Locale.getDefault()
            ).format(Calendar.getInstance().getTime());

            String result = "Name: " + name +
                    "\nAge: " + age +
                    "\nDOB: " + dob +
                    "\nSubmitted at: " + currentTime;

            tvResult.setText(result);
        });
    }
}