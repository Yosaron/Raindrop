package com.example.alexahern.raindrop;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by alexahern on 23/01/16.
 */

/*Class to get JSON with the users current location from the forecast.io API, and parse it in order to obtain the
precipitation percentage
 */

public class ForecastWeatherDataFetcher implements WeatherDataFetcher {
    private String[] latitudeAndLongitude;
    private final String API_KEY;
    private final String TAG;

    public ForecastWeatherDataFetcher(String[] latitudeAndLongitude, String api_key) {
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
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuilder buffer = new StringBuilder();

        try {
            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Connection error", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
        return buffer.toString();
    }

    @Override
    public String downloadFromUri() {
        return fetchUri(buildUri());
    }

    @Override
    public Double getPercentageChanceOfRain(String timeFrame)
            throws JSONException {
        String jsonStr = downloadFromUri();
        JSONObject fullForecast = new JSONObject(jsonStr);
        JSONObject forecastForTimeFrame = fullForecast.getJSONObject(timeFrame)
                .getJSONArray("data")
                .getJSONObject(0);

        Double probabilityOfRain = forecastForTimeFrame.getDouble("precipProbability");
        return probabilityOfRain * 100;
    }
}
