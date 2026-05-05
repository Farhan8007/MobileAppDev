package com.example.activitydemo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btnClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnClick = findViewById(R.id.btnClick);

        Toast.makeText(this, "onCreate Called", Toast.LENGTH_SHORT).show();
        Log.d("ActivityLife", "onCreate");

        btnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "MY NAME IS AB", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "onStart Called", Toast.LENGTH_SHORT).show();
        Log.d("ActivityLife", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "onResume Called", Toast.LENGTH_SHORT).show();
        Log.d("ActivityLife", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ActivityLife", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ActivityLife", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ActivityLife", "onDestroy");
    }
}
