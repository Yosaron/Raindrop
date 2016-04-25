package com.example.alexahern.raindrop;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

public class GetRainTask extends AsyncTask<String, Void, Double> {
    private final FragmentCallback rainFragmentCallback;
    private final String LOG_TAG = GetRainTask.class.getSimpleName();

    public GetRainTask(FragmentCallback rainFragmentCallback) {
        this.rainFragmentCallback = rainFragmentCallback;
    }

    @Override
    protected void onPreExecute() {
        rainFragmentCallback.displayLoading();
        super.onPreExecute();
    }

    @Override
    protected Double doInBackground(String... params) {
        try {
            DataFetcher forecastWeatherDataFetcher = new ForecastDataFetcher(rainFragmentCallback.getLatitudeAndLongitude(), rainFragmentCallback.getApiKey());
            JsonParser parser = new JsonParser(forecastWeatherDataFetcher.fetchWeatherData());
            return parser.getPercentageChanceOfRain(rainFragmentCallback.getTimeFrame());
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return null;
    }

    protected void onPostExecute(Double result) {
        if (result != null) {
            rainFragmentCallback.displayResult(result.intValue());
            rainFragmentCallback.setShareIntent("There is a " + result.intValue() + "% " + rainFragmentCallback.getTimeFrame() + " chance of rain.");
        } else {
            //Do nothing and keep displayed data the same
        }
    }

}
