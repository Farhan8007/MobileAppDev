package com.example.newdb;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.content.ContentValues;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase db;
    Button b1, b2;
    TextView t1;
    EditText eid, ename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = findViewById(R.id.button1);
        b2 = findViewById(R.id.button2);
        t1 = findViewById(R.id.textView3);
        eid = findViewById(R.id.editText1);
        ename = findViewById(R.id.editText2);

        try {
            db = openOrCreateDatabase("StudentDB", MODE_PRIVATE, null);

            // FIX: table name consistency (Temp everywhere)
            db.execSQL("CREATE TABLE IF NOT EXISTS Temp(id INTEGER, name TEXT)");

        } catch (SQLException e) {
            Toast.makeText(this, "Database Error", Toast.LENGTH_SHORT).show();
        }

        // INSERT BUTTON
        b1.setOnClickListener(v -> {

            String id = eid.getText().toString();
            String name = ename.getText().toString();

            if (id.isEmpty() || name.isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("name", name);

            long result = db.insert("Temp", null, values);

            if (result != -1) {
                Toast.makeText(MainActivity.this, "Record Inserted", Toast.LENGTH_SHORT).show();
                eid.setText("");
                ename.setText("");
            } else {
                Toast.makeText(MainActivity.this, "Insert Error", Toast.LENGTH_SHORT).show();
            }
        });

        // DISPLAY BUTTON
        b2.setOnClickListener(v -> {

            Cursor c = db.rawQuery("SELECT * FROM Temp", null);

            if (c.getCount() == 0) {
                t1.setText("No Data Found");
                return;
            }

            StringBuilder str = new StringBuilder();

            while (c.moveToNext()) {
                str.append("ID: ").append(c.getInt(0))
                        .append(" | Name: ").append(c.getString(1))
                        .append("\n");
            }

            t1.setText(str.toString());
            c.close();
        });
    }
}