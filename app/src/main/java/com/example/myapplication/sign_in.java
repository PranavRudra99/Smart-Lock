package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.os.Handler;
import android.widget.Toast;

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

public class sign_in extends AppCompatActivity {
    String address = "";
    static Boolean connected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
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

        final TimerTask newTask = new TimerTask() {
                @Override
                public void run() {
                    if (haveNetworkConnection()) {
                        if (connected) {
                            Intent i = new Intent(getApplicationContext(), create_new_account.class);
                            startActivity(i);
                            finish();
                        } else {
                            Intent i = new Intent(getApplicationContext(), reConnect.class);
                            startActivity(i);
                            finish();
                        }
                    } else {
                        Intent i = new Intent(getApplicationContext(), noInternet.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    }
                }
            };
            final TimerTask newTask1 = new TimerTask() {
                @Override
                public void run() {
                    if (haveNetworkConnection()) {
                        if (connected) {
                            Intent i = new Intent(getApplicationContext(), sign_in_existing.class);
                            startActivity(i);
                            finish();
                        } else {
                            Intent i = new Intent(getApplicationContext(), reConnect.class);
                            startActivity(i);
                            finish();
                        }
                    } else {
                        Intent i = new Intent(getApplicationContext(), noInternet.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    }
                }
            };
            MyClientTask myClientTask = new MyClientTask(address);
            myClientTask.execute("");
            Button b1 = findViewById(R.id.create_new_account);
            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Timer newTimer = new Timer();
                    newTimer.schedule(newTask, 0);
                }
            });
            Button b2 = findViewById(R.id.login);
            b2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Timer newTimer = new Timer();
                    newTimer.schedule(newTask1, 0);
                }
            });
    }
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public class MyClientTask extends AsyncTask<String,Void,String> {

        String server;
        MyClientTask(String server){
            this.server = server;
        }

        @Override
        protected String doInBackground(String... params) {

            StringBuffer response = new StringBuffer("");

            final String val = params[0];
            final String p = "http://"+ server+"/"+val;
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
