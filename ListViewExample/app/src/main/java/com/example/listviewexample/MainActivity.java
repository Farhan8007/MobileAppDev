package com.example.listviewexample;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    String[] mobileArray = {
            "Android","IPhone","WindowsMobile",
            "Blackberry","WebOS","Ubuntu",
            "Windows7","Mac OS X"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.mobile_list);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.activity_listview,
                R.id.label,
                mobileArray
        );

        listView.setAdapter(adapter);
    }
}
