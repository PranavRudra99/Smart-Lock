package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

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

public class MainActivity extends AppCompatActivity {
    public static Handler UIHandler;
    String address = "";
    static boolean connected = false;
    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File file = new File(getFilesDir(),"Address.txt");
        if(file.exists())
        {
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
                final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                final TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if(haveNetworkConnection())
                        {
                            Intent i = new Intent(getApplicationContext(), sign_in.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        }
                        else
                        {
                            Intent i = new Intent(getApplicationContext(),noInternet.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        }
                    }
                };
                final TimerTask Task = new TimerTask() {
                    @Override
                    public void run() {
                        if(haveNetworkConnection())
                        {
                            MainActivity.runOnUI(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),"Welcome back "+account.getDisplayName(),Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent i = new Intent(getApplicationContext(), lock_unlock.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        }
                        else
                        {
                            Intent i = new Intent(getApplicationContext(),noInternet.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        }
                    }
                };
                final TimerTask newTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (account == null)
                        {
                            if(connected)
                            {
                                Timer timer = new Timer();
                                timer.schedule(timerTask,0);
                            }
                            else
                            {
                                Intent i = new Intent(getApplicationContext(), reConnect.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                finish();
                                startActivity(i);
                            }
                        }
                        else
                        {
                            if(connected)
                            {
                                Timer timer = new Timer();
                                timer.schedule(Task, 0);
                            }
                            else
                            {
                                Intent i = new Intent(getApplicationContext(), reConnect.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                finish();
                                startActivity(i);
                            }
                        }
                    }
                };
                Timer newTimer = new Timer();
                UIHandler = new Handler(Looper.getMainLooper());
                newTimer.schedule(newTask,2000);
                MyClientTask myClientTask = new MyClientTask(address);
                myClientTask.execute("");
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(),changeIp.class);
            startActivity(intent);
        }
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
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
    public class MyClientTask extends AsyncTask<String,Void,String>{

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