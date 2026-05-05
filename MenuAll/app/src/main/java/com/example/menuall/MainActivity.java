package com.example.menuall;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    RelativeLayout layout;
    TextView text;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.layout);
        text = findViewById(R.id.text);
        button = findViewById(R.id.button);

        // Context menu register
        registerForContextMenu(text);

        // Popup menu
        button.setOnClickListener(view -> {

            PopupMenu popup = new PopupMenu(MainActivity.this, button);

            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                Toast.makeText(MainActivity.this,
                        "Clicked " + item.getTitle(),
                        Toast.LENGTH_SHORT).show();
                return true;
            });

            popup.show();
        });
    }

    // OPTION MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.android) {
            layout.setBackgroundColor(Color.RED);
        }
        else if (item.getItemId() == R.id.java) {
            layout.setBackgroundColor(Color.GREEN);
        }
        else if (item.getItemId() == R.id.kotlin) {
            layout.setBackgroundColor(Color.BLUE);
        }

        return true;
    }

    // CONTEXT MENU
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle("Choose Color");

        menu.add(0, v.getId(), 0, "Yellow");
        menu.add(0, v.getId(), 0, "Gray");
        menu.add(0, v.getId(), 0, "Cyan");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals("Yellow")) {
            layout.setBackgroundColor(Color.YELLOW);
        }
        else if (item.getTitle().equals("Gray")) {
            layout.setBackgroundColor(Color.GRAY);
        }
        else if (item.getTitle().equals("Cyan")) {
            layout.setBackgroundColor(Color.CYAN);
        }

        return true;
    }
}