package com.example.alexahern.raindrop;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by alexahern on 23/01/16.
 */

public class ForecastDataFetcher implements DataFetcher {
    private String[] latitudeAndLongitude;
    private final String API_KEY;
    private final String TAG;

    public ForecastDataFetcher(String[] latitudeAndLongitude, String api_key) {
        this.latitudeAndLongitude = latitudeAndLongitude;
        this.API_KEY = api_key;
        this.TAG = getClass().toString();
    }


    @Override
    public Uri buildUri() {
        final String BASE_URL = "https://api.forecast.io/forecast/";
        return Uri.parse(BASE_URL).buildUpon()
                .appendPath(API_KEY)
                .appendPath(this.latitudeAndLongitude[0] + "," + latitudeAndLongitude[1])
                .build();
    }

    @Override
    public String fetchUri(Uri builtUri) {
        String buffer;
        HttpURLConnection urlConnection = null;

        try {
            urlConnection =  makeConnection(builtUri);
            buffer = readData(urlConnection.getInputStream());
        } catch (IOException e) {
            Log.e(TAG, "Connection error", e);
            return null;
        } finally {
            closeConnection(urlConnection);
        }
        return buffer;
    }

    private void closeConnection(HttpURLConnection urlConnection) {
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    private String readData(InputStream inputStream) throws IOException {
        BufferedReader reader;
        StringBuilder buffer = new StringBuilder();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        return buffer.toString();
    }

    @NonNull
    private HttpURLConnection makeConnection(Uri builtUri) throws IOException {
        HttpURLConnection urlConnection;
        URL url = new URL(builtUri.toString());
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        return urlConnection;
    }

    @Override
    public String fetchWeatherData() {
        return fetchUri(buildUri());
    }
}
