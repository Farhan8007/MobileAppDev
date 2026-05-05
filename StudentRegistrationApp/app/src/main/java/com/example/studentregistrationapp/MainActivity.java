package com.example.studentregistrationapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText etName, etRoll, etDept;
    Button btnSubmit;
    ListView listView;

    ArrayList<String> studentList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etRoll = findViewById(R.id.etRoll);
        etDept = findViewById(R.id.etDept);
        btnSubmit = findViewById(R.id.btnSubmit);
        listView = findViewById(R.id.listView);

        studentList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                studentList);

        listView.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> {

            String name = etName.getText().toString();
            String roll = etRoll.getText().toString();
            String dept = etDept.getText().toString();

            String studentData = "Name: " + name +
                    "\nRoll: " + roll +
                    "\nDept: " + dept;

            studentList.add(studentData);
            adapter.notifyDataSetChanged();

            etName.setText("");
            etRoll.setText("");
            etDept.setText("");
        });
    }
}