package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
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

public class create_new_account extends AppCompatActivity {

    private static final int RC_SIGN_IN=0;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    CharSequence text = "This Account already exists";
    int duration = Toast.LENGTH_SHORT;
    String address = "";
    static Boolean connected = false;
    static Boolean accnt = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_account);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
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
            myClientTask.execute("");
            FrameLayout frame = findViewById(R.id.frameLayout);
            frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Timer timer = new Timer();
                    TimerTask newTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (haveNetworkConnection()) {
                                if (account == null) {
                                    if (connected) {
                                        if (haveNetworkConnection()) {
                                            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                                            startActivityForResult(signInIntent, RC_SIGN_IN);
                                        } else {
                                            Intent i = new Intent(getApplicationContext(), noInternet.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(i);
                                            finish();
                                        }
                                    } else {
                                        Intent i = new Intent(getApplicationContext(), reConnect.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                        finish();
                                    }
                                }

                            } else {
                                Intent i = new Intent(getApplicationContext(), noInternet.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            }
                        }
                    };
                    timer.schedule(newTask, 100);
                }
            });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        String mail;
        try {
            final GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            mail=account.getEmail();
            String temp = new String("");
            for(int i=0;i<mail.indexOf("@");i++)
            {
                temp = temp +mail.charAt(i);
            }
            final String finalTemp = temp;
            mDatabase.child("USERS").child(finalTemp).addValueEventListener(new ValueEventListener() {
                int temp1 = 1;
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()==null)
                    {
                        temp1 = -1;
                        final MyClientTask myClientTask = new MyClientTask(address);
                        Timer timer = new Timer();
                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                if(accnt)
                                {
                                    MyClientTask ClientTask = new MyClientTask(address);
                                    ClientTask.execute("writeAccount?mail=" + finalTemp);
                                }
                                    mDatabase.child("USERS").child(finalTemp).child("key").setValue(generateKey());
                                    mDatabase.child("USERS").child(finalTemp).child("lock-unlock").setValue(1);
                                    /*Intent i = new Intent(getApplicationContext(), lock_unlock.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);*/
                                    finish();
                            }
                        };
                        myClientTask.execute("checkAccount");
                        timer.schedule(timerTask,100);
                    }
                    else if(temp1==1 && dataSnapshot.getValue() != null )
                    {
                        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                        mGoogleSignInClient.revokeAccess();
                        toast.show();
                        Intent i=new Intent(getApplicationContext(),sign_in_existing.class);
                        startActivity(i);
                        finish();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        catch (ApiException e) {
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
                    if(response.toString().equals("YES"))
                    {
                        accnt = true;
                    }
                    if(response.toString().equals("NO"))
                    {
                        accnt = false;
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