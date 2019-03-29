package com.example.myapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class reConnect extends AppCompatActivity {

    String address = "";
    static Boolean connected = false;
    Timer timer = new Timer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_connect);
        final TextView textView = findViewById(R.id.tryAgain);
        TextView ip = findViewById(R.id.ip);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            int z;
            FileInputStream fileInputStream = openFileInput("Address.txt");
            while ((z = fileInputStream.read()) != -1) {
                stringBuffer.append((char) z);
            }
            address = stringBuffer.toString();
        }
        catch (IOException e)
        {}

        MyClientTask myClientTask = new MyClientTask(address);
                myClientTask.execute("on");
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timer.schedule(newTask, 500);
                    }
                });
                ip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), changeIp.class);
                        startActivity(i);
                        finish();
                    }
                });
        }
    final TimerTask newTask = new TimerTask() {
        @Override
        public void run() {
            if(connected)
                {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                    startActivity(i);
                }
                else
                {
                    Intent i = new Intent(getApplicationContext(), reConnect.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                    startActivity(i);
                }
        }
    };

    public class MyClientTask extends AsyncTask<String,Void,String> {

        String server;
        MyClientTask(String server){
            this.server = server;
        }

        @Override
        protected String doInBackground(String... params) {

            StringBuffer response = new StringBuffer("");

            final String val = params[0];
            final String p = "http://"+ server+"/"+val+"?key=ok";
            String serverResponse = "";
            try {
                URL url = new URL(p);
                HttpURLConnection connection;
                connection= (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                }
                inputStream.close();
                if(response!=null)
                {
                    connected = true;
                }
                else
                {
                    connected = false;
                }
                System.out.println("Response: " + response.toString());
                connection.disconnect();
                return "success";
            } catch (IOException e) {
                e.printStackTrace();
                serverResponse = e.getMessage();
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String s) {

        }
    }

}
