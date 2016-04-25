package com.example.alexahern.raindrop;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

public class GetRainTask extends AsyncTask<String, Void, Double> {
    private final Callback resultCallback;
    private final String LOG_TAG = GetRainTask.class.getSimpleName();

    public GetRainTask(Callback resultCallback) {
        this.resultCallback = resultCallback;
    }

    public interface Callback {
        void displayLoading();

        void displayResult(int result);

        void setShareIntent(String input);

        String getTimeFrame();

        String getApiKey();

        String[] getLatitudeAndLongitude();
    }

    @Override
    protected void onPreExecute() {
        resultCallback.displayLoading();
        super.onPreExecute();
    }

    @Override
    protected Double doInBackground(String... params) {
        try {
            DataFetcher forecastWeatherDataFetcher = new ForecastDataFetcher(resultCallback.getLatitudeAndLongitude(), resultCallback.getApiKey());
            JsonParser parser = new JsonParser(forecastWeatherDataFetcher.fetchWeatherData());
            return parser.getPercentageChanceOfRain(resultCallback.getTimeFrame());
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return null;
    }

    protected void onPostExecute(Double result) {
        if (result != null) {
            resultCallback.displayResult(result.intValue());
            resultCallback.setShareIntent("There is a " + result.intValue() + "% " + resultCallback.getTimeFrame() + " chance of rain.");
        }
        else{
            //Do nothing and keep displayed data the same
        }
    }

}
