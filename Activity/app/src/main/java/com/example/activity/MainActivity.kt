package com.example.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        print("lifecycle onCreate invoked")
    }

    protected override fun onStart() {
        super.onStart()
        Log.d("lifecycle", "onStart invoked")
        Toast.makeText(getApplicationContext(), "onStart Called", Toast.LENGTH_LONG).show()
    }

    protected override fun onResume() {
        super.onResume()
        Log.d("lifecycle", "onResume invoked")
        Toast.makeText(getApplicationContext(), "onResume Called", Toast.LENGTH_LONG).show()
    }

    protected override fun onPause() {
        super.onPause()
        Log.d("lifecycle", "onPause invoked")
        Toast.makeText(getApplicationContext(), "onPause Called", Toast.LENGTH_LONG).show()
    }

    protected override fun onStop() {
        super.onStop()
        Log.d("lifecycle", "onStop invoked")
        Toast.makeText(getApplicationContext(), "onStop Called", Toast.LENGTH_LONG).show()
    }

    protected override fun onRestart() {
        super.onRestart()
        Log.d("lifecycle", "onRestart invoked")
        Toast.makeText(getApplicationContext(), "onRestart Called", Toast.LENGTH_LONG).show()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Log.d("lifecycle", "onDestroy invoked")
        Toast.makeText(getApplicationContext(), "onDestroy Called", Toast.LENGTH_LONG).show()
    }
}