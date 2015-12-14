package com.example.alexahern.raindrop;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A placeholder fragment containing a simple view.
 */
public class RainFragment extends Fragment {
    private ProgressBar spinner; //new
    private TextView rainText;
    private TextView timeFrameText;
    private TextView lastDateTextView;
    String timeFrame;

    public RainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.rain_fragment, container, false);
        rainText = (TextView) rootView.findViewById(R.id.rain_textview);
        timeFrameText = (TextView) rootView.findViewById(R.id.timeframeTextView);
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar1); //new
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    public String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String currentTime = sdf.format(cal.getTime());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_updated_key", currentTime);
        editor.apply();

        if(timeFrame.equals("hourly")){
            timeFrameText.setText(getString(R.string.hourly_timeframe_message));
        }
        else if(timeFrame.equals("daily")){
            timeFrameText.setText(getString(R.string.daily_timeframe_message));
        }

        return currentTime;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rainfragment, menu);
    }

    private void updateWeather() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        timeFrame = prefs.getString(getString(R.string.pref_timeframe_key),
                getString(R.string.pref_timeframe_daily));

        lastDateTextView = ((TextView) getActivity().findViewById(R.id.last_updated_textview));
        lastDateTextView.setText(getString(R.string.last_updated_message , prefs.getString("last_updated_key", "never")));

        if (networkInfo != null && networkInfo.isConnected()){
            getRainTask rainTask = new getRainTask();
            rainTask.execute(timeFrame);
        }
        else{
            Toast.makeText(getContext(),"NETWORK ERROR", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class getRainTask extends AsyncTask<String, Void, Double> {
        private final String LOG_TAG = getRainTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            rainText.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE); //new
            super.onPreExecute();
        }

        @Override
        protected Double doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            String rainForecastJsonStr = null;
            BufferedReader reader = null;
            try {
                //Construct the URL for forecast.io and attempt to connect
                final String BASE_URL = "https://api.forecast.io/forecast/";
                final String API_KEY = getString(R.string.apikey);
                final String LATITUDE_PARAM = "53.483457";
                final String LONGITUDE_PARAM = "-2.263960";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(API_KEY)
                        .appendPath(LATITUDE_PARAM + "," + LONGITUDE_PARAM)
                        .build();

                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                rainForecastJsonStr = buffer.toString();

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

            try {
                return getPrecipPercent(rainForecastJsonStr, params[0]);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            return null;
        }

        private Double getPrecipPercent(String jsonStr, String timeFrame)
                throws JSONException {
            JSONObject forecastObject = new JSONObject(jsonStr);
            JSONObject timeframeForecastObject;
            timeframeForecastObject = forecastObject.getJSONObject(timeFrame)
                    .getJSONArray("data")
                    .getJSONObject(0);

            Double currentPrecipProb = timeframeForecastObject.getDouble("precipProbability");
            return currentPrecipProb * 100;
        }

        protected void onPostExecute(Double result) {
            if (result != null) {
                spinner.setVisibility(View.GONE); //new
                rainText.setVisibility(View.VISIBLE);
                getCurrentTime();

                rainText.setText(Integer.toString(result.intValue()));
                rainText.append("%");

                lastDateTextView.setText(getString(R.string.last_updated_message , getCurrentTime()));

            }
        }
    }
}
