package com.example.filehandling;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    Button b1,b2;
    TextView tv;
    EditText et1;
    String file="mydata.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1=findViewById(R.id.b1);
        b2=findViewById(R.id.b2);
        tv=findViewById(R.id.tv);
        et1=findViewById(R.id.et1);

        b1.setOnClickListener(v->{
            String data=et1.getText().toString();
            try{
                FileOutputStream fos=openFileOutput(file,MODE_APPEND);
                fos.write(data.getBytes());
                fos.close();

                Toast.makeText(this, "Data Saved", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
                e.printStackTrace();
            }
            });

        b2.setOnClickListener(v->{
                try{
                    FileInputStream fin=openFileInput(file);
                    int c;
                    String data="";
                    while ((c=fin.read())!=-1){
                        data=data+(char)c;
                    }
                    tv.setText(data);
                    fin.close();

                    Toast.makeText(this, "Data Retrieved", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }

        });
    }
}