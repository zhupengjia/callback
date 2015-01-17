package com.example.callback;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

public class webCallbackTask extends AsyncTask<String, String, String> {
    @Override
    protected String doInBackground(String... params) {
        String content;
        try {
            URL url = new URL(params[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            urlConnection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            content = reader.readLine().substring(2);

            urlConnection.disconnect();
        } catch (Exception e) {
            content = "网络超时" + e.getMessage();
            //Log.d(TAG,e.getMessage());
        }
        return content;
    }
}