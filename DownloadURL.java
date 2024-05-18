package com.example.cyawait;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadURL {
    //Accepts a URL and return its contents in JSON format
    public String retrieveURL(String url) throws IOException {
        String URLData = "";
        String line;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        try{
            URL getURL = new URL(url);
            httpURLConnection = (HttpURLConnection) getURL.openConnection();
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null){
                sb.append(line);
            }
            URLData = sb.toString();
            bufferedReader.close();
        }catch(Exception e){
            Log.d("Exception", e.toString());
        }finally{
            inputStream.close();
            httpURLConnection.disconnect();
        }
        //Returns Data in JSON format
        return URLData;
    }
}

