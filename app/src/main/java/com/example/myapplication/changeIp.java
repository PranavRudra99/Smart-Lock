package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class changeIp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_ip);
        Button connect = findViewById(R.id.connect);
        final EditText ip = findViewById(R.id.IP);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = ip.getText().toString();
                if(text!=null)
                {
                    File file = new File(getFilesDir(),"Address");
                    if(file.exists())
                    {
                        file.delete();
                    }
                    try {
                        FileOutputStream fileOutputStream = openFileOutput("Address.txt", Context.MODE_PRIVATE);
                        byte[] bytes = text.getBytes();
                        fileOutputStream.write(bytes);
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
