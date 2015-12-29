package com.example.alexahern.raindrop;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetRainTask extends AsyncTask<String, Void, Double> {
    private final Callback resultCallback;
    private final String LOG_TAG = GetRainTask.class.getSimpleName();
    private final String API_KEY;

    public GetRainTask(Callback resultCallback, String api_key) {
        this.resultCallback = resultCallback;
        API_KEY = api_key;
    }

    public interface Callback {
        void displayLoading();

        void displayResult(int result);

        void setShareIntent(String input);

        String getTimeFrame();
    }

    @Override
    protected void onPreExecute() {
        resultCallback.displayLoading();
        super.onPreExecute();
    }

    @Override
    protected Double doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }

        String rainForecastJsonStr = downloadWeatherJson();

        try {
            return getProbabilityOfRainFromJson(rainForecastJsonStr, params[0]);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return null;
    }

    @Nullable
    private String downloadWeatherJson() {
        return fetchUri(buildUri());
    }

    private void connectToUrl(URL url) throws IOException {

    }

    private String fetchUri(Uri builtUri) {
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
            Log.e(LOG_TAG, "Error", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return buffer.toString();
    }

    private Uri buildUri() {
        final String BASE_URL = "https://api.forecast.io/forecast/";
        final String LATITUDE_PARAM = "53.483457";
        final String LONGITUDE_PARAM = "-2.263960";

        return Uri.parse(BASE_URL).buildUpon()
                .appendPath(API_KEY)
                .appendPath(LATITUDE_PARAM + "," + LONGITUDE_PARAM)
                .build();
    }

    private Double getProbabilityOfRainFromJson(String jsonStr, String timeFrame)
            throws JSONException {
        JSONObject fullForecast = new JSONObject(jsonStr);
        JSONObject forecastForTimeFrame = fullForecast.getJSONObject(timeFrame)
                .getJSONArray("data")
                .getJSONObject(0);

        Double probabilityOfRain = forecastForTimeFrame.getDouble("precipProbability");
        return formatToPercentage(probabilityOfRain);
    }

    private Double formatToPercentage(Double probabilityOfRain) {
        return probabilityOfRain * 100;
    }

    protected void onPostExecute(Double result) {
        if (result != null) {
            resultCallback.displayResult(result.intValue());
            resultCallback.setShareIntent("There is a " + result.intValue() + "% " + resultCallback.getTimeFrame() + " chance of rain.");
        }
    }

}
