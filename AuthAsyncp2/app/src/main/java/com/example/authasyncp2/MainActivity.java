package com.example.authasyncp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static String readStream(InputStream is) throws IOException { //Merci StackOverflow
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
        }/* Alternative readStream for debugging purposes
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is),9999999);
        for (String line = r.readLine(); line != null; line =r.readLine()){
            sb.append(line);
        }
        is.close();
        return sb.toString();*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b = (Button) findViewById(R.id.img);
        Button list = (Button) findViewById(R.id.la);

        b.setOnClickListener(new getImageOnClickListener()); //Using our custom getImage listener on the button

        list.setOnClickListener(new View.OnClickListener() { //Clicking the button sends the user to a new list activity
            @Override
            public void onClick(View v) {
                Intent k = new Intent(MainActivity.this, ListActivity.class);
                startActivity(k);
            }
        });
    }

    class getImageOnClickListener implements View.OnClickListener { //override of the onClick method to reach the api
        @Override
        public void onClick(View v) {
            AsyncFlickrJSONData bob = new AsyncFlickrJSONData();                                            //using the AsyncFlickrJSONData type allows the api call
            bob.execute("https://www.flickr.com/services/feeds/photos_public.gne?tags=trees&format=json");  //extracting the JSON data from our call
        }
    }

    class AsyncBitmapDownloader extends AsyncTask<String, Void, Bitmap> //we send a string, and get a bitmap
    {

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url;
            Bitmap bitmap = null;
            for (int i = 0; i < strings.length; i++) {
                try {
                    url = new URL(strings[i]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();
                    InputStream in = urlConnection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.i("err", e.toString());
                }
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView iv;

            try {
                iv = (ImageView) findViewById(R.id.image);
                iv.setImageBitmap(bitmap);                      //updating the image view to the new bitmap
            } catch (Exception e) {
                Log.i("err", e.toString());
            }
        }
    }


    class AsyncFlickrJSONData extends AsyncTask<String, Void, JSONObject> { //we send a string and get a JSONObject
        JSONObject obj;
        URL url = null;
        JSONObject data = null;

        @Override
        protected JSONObject doInBackground(String... strings) {

            for (int i = 0; i < strings.length; i++) { //since we have an array of string parameters, we can make it more safe by going through said array
                try {
                    url = new URL(strings[i]);

                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        String s = readStream(in);
                        // Log.i("S" , s);
                        s = s.subSequence(15, s.length() - 1).toString(); //We trim out the non-JSON part of the api return
                        //Log.i("Flickr Response", s);
                        try {
                            data = new JSONObject(s);
                            // Log.i("SETTING DATA AS JSON", data.toString());
                        } catch (Exception e) {
                            Log.i("ERROR", "ERROR");
                            data = new JSONObject();
                        }
                    } catch (IOException e) {
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
            // Log.i("SENDING DATA ", data.toString());
            return data;
        }


        @Override
        protected void onPostExecute(JSONObject obj2) {
            TextView url = (TextView) findViewById(R.id.url);
            super.onPostExecute(obj2);
            JSONObject tmp = null;
            String link = null;
            // Log.i("Log", obj2.toString());
            try {
                tmp = obj2.getJSONArray("items").getJSONObject(0).getJSONObject("media"); //We get the media part of the first element
                link = tmp.getString("m"); //we extract the url
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Log.i("url", link);
            url.setText(link);
        }
    }
}