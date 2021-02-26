package com.example.authasyncp1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static String readStream(InputStream is) {                      //Merci StackOverflow
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    public class MyThread extends Thread {                               //My thread allowing the asynchronous interactions

        public void run() {
            URL url = null;
            EditText loginText = (EditText) findViewById(R.id.Login);    //Login text field
            EditText pwdText = (EditText) findViewById(R.id.pwd);        //Password text field
            TextView textView = (TextView) findViewById(R.id.textView2);
            String str = loginText.getText() + ":" + pwdText.getText();  //concatenated un+pwd for the http call


            try {

                url = new URL("https://httpbin.org/basic-auth/bob/sympa");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {

                    String basicAuth = "Basic " + Base64.encodeToString(str.getBytes(), Base64.NO_WRAP);
                    urlConnection.setRequestProperty("Authorization", basicAuth);

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    String s = readStream(in);
                    Log.i("JFL", s);
                    JSONObject res = new JSONObject(s);


                    runOnUiThread(new Runnable() {            //Using a runOnUiThread in order to circumvent the interdiction of graphical element modification in the main thread
                        @Override
                        public void run() {
                            textView.setText(s);                 //Updating the textView with the inputStream
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button go;
        go = (Button) findViewById(R.id.go);

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyThread t = new MyThread();                    //Using a thread allows long processes in the main
                t.start();
            }
        });

    }

}