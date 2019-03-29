package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class settings extends AppCompatActivity {

    String address = "";
    static Boolean connected = false;
    static Boolean success = false;
    public static Handler UIHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Key obj =new Key();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final GoogleSignInClient mGoogleSignInClient;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
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
        UIHandler = new Handler(Looper.getMainLooper());
            if (account != null) {
                String mail;
                mail = account.getEmail();
                String temp = new String("");
                for (int i = 0; i < mail.indexOf("@"); i++) {
                    temp = temp + mail.charAt(i);
                }
                mDatabase.child("USERS").child(temp).child("key").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        obj.setKey(dataSnapshot.getValue().toString());
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        Button localSignOut = findViewById(R.id.localsignout);
        localSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account != null) {
                            if(haveNetworkConnection()) {
                                 mGoogleSignInClient.signOut();
                                        lock_unlock.runOnUI(new Runnable() {
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "Sign out Successful ", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
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
            }
        });
            Button signOut = findViewById(R.id.signout);
            signOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (account != null) {
                                if(haveNetworkConnection()) {
                                    if (connected) {
                                        if (success)
                                        {
                                            mGoogleSignInClient.signOut();
                                            lock_unlock.runOnUI(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), "Sign out Successful from both Devices", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(i);
                                            finish();
                                        }
                                        else
                                        {
                                            lock_unlock.runOnUI(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), "UNAUTHORIZED USER", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                    else
                                    {
                                        Intent i = new Intent(getApplicationContext(),reConnect.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                        finish();
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
                        }
                    };
                    Timer timer = new Timer();
                    timer.schedule(timerTask,1000);
                    MyClientTask myClientTask = new MyClientTask(address);
                    myClientTask.execute("");
                    MyClientTask ClientTask = new MyClientTask(address);
                    ClientTask.execute("signout?key="+obj.getKey());

                }
            });
            Button reset = findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(haveNetworkConnection())
                {
                    if (account != null) {
                        String mail;
                        mail = account.getEmail();
                        String temp = new String("");
                        for (int i = 0; i < mail.indexOf("@"); i++) {
                            temp = temp + mail.charAt(i);
                        }
                        mDatabase.child("USERS").child(temp).child("key").setValue(generateKey());
                        Toast toast = Toast.makeText(getApplicationContext(), "Key Reset Successful " + account.getDisplayName(), Toast.LENGTH_SHORT);
                        toast.show();
                        Intent i = new Intent(getApplicationContext(),lock_unlock.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
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
        });
        }
    }
    public String generateKey()
    {
        String key=new String("");
        Random random=new Random();
        for(int i=0;i<32;i++)
        {
            int x=random.nextInt(26);
            int y=random.nextInt(2);
            if(y==0)
            {
                x = x + 65;
            }
            if(y==1)
            {
                x = x + 97;
            }
            char z=(char)x;
            key=key+z;
        }
        return key;
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
                    if(response.toString().equals("SUCCESS"))
                    {
                        success = true;
                    }
                    if(response.toString().equals("FAILURE") && !success)
                    {
                        success = false;
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
