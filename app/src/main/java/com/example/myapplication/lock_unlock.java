package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

public class lock_unlock extends AppCompatActivity {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    String mail;
    String address = "";
    int val = 0;
    static Boolean connected = false;
    static Boolean valid = false;
    public static Handler UIHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Key obj = new Key();
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_unlock);
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

        if(haveNetworkConnection())
        {
            ImageView settings = findViewById(R.id.settings);
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIHandler = new Handler(Looper.getMainLooper());
                    if(haveNetworkConnection())
                    {
                        Intent i = new Intent(getApplicationContext(), settings.class);
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
            });
            if(account!=null) {
                mail = account.getEmail();
                String temp = new String("");
                for (int i = 0; i < mail.indexOf("@"); i++) {
                    temp = temp + mail.charAt(i);
                }
                FrameLayout lock = findViewById(R.id.lock);
                final String finalTemp = temp;
                mDatabase.child("USERS").child(finalTemp).child("lock-unlock").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        obj.setLock_unlock(Integer.parseInt(dataSnapshot.getValue().toString()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                mDatabase.child("USERS").child(finalTemp).child("key").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        obj.setKey(dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                lock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIHandler = new Handler(Looper.getMainLooper());
                        TimerTask lockTask = new TimerTask() {
                            @Override
                            public void run() {
                                if (haveNetworkConnection()) {
                                    if (connected) {
                                        if (account != null) {
                                            if (valid) {
                                                if (obj.getLock_unlock() == 0) {
                                                    MyClientTask myClientTask = new MyClientTask(address);
                                                    myClientTask.execute("lock");
                                                } else {
                                                    lock_unlock.runOnUI(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getApplicationContext(), "DOOR IS ALREADY LOCKED", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            } else {
                                                lock_unlock.runOnUI(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "UNAUTHORIZED USER", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    } else {
                                        Intent i = new Intent(getApplicationContext(), reConnect.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
                        MyClientTask clientTask = new MyClientTask(address);
                        clientTask.execute("verifyUser?key=" + obj.getKey());
                        Timer lockTimer = new Timer();
                        lockTimer.schedule(lockTask, 2000);
                    }
                });
                FrameLayout unlock = findViewById(R.id.unlock);
                unlock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIHandler = new Handler(Looper.getMainLooper());
                        TimerTask unlockTask = new TimerTask() {
                            @Override
                            public void run() {
                                if (haveNetworkConnection()) {
                                    if (connected) {
                                        if (account != null) {
                                            if (valid) {
                                                if (obj.getLock_unlock() == 1) {
                                                    MyClientTask myClientTask = new MyClientTask(address);
                                                    myClientTask.execute("unlock");
                                                } else {
                                                    lock_unlock.runOnUI(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getApplicationContext(), "DOOR IS ALREADY UNLOCKED", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            } else {
                                                lock_unlock.runOnUI(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "UNAUTHORIZED USER", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    } else {
                                        Intent i = new Intent(getApplicationContext(), reConnect.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
                        MyClientTask clientTask = new MyClientTask(address);
                        clientTask.execute("verifyUser?key=" + obj.getKey());
                        Timer unlockTimer = new Timer();
                        unlockTimer.schedule(unlockTask, 2000);
                    }
                });
            }
        }
        else
        {
            Intent i = new Intent(getApplicationContext(),noInternet.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
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

    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
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
                    if(response.toString().equals("VALID"))
                    {
                        valid = true;
                    }
                    if(response.toString().equals("INVALID"))
                    {
                        valid = false;
                    }
                    if(response.toString().equals("lock"))
                    {
                        lock_unlock.runOnUI(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "LOCKED", Toast.LENGTH_SHORT).show();
                        }
                    });
                    }
                    if(response.toString().equals("unlock"))
                    {
                        lock_unlock.runOnUI(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "UNLOCKED", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
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
