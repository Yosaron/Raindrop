package com.example.alexahern.raindrop;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
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
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        //TextView lastDateTextView = ((TextView) rootView.findViewById(R.id.last_updated_texview));
        //lastDateTextView.setText(getString(R.string.last_updated_message, getString(R.string.last_updated_value)));
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
        setCurrentTime();
    }

    public void setCurrentTime(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String currentTime = sdf.format(cal.getTime());
        TextView lastDateTextView = ((TextView) getActivity().findViewById(R.id.last_updated_texview));
        lastDateTextView.setText(getString(R.string.last_updated_message, currentTime));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rainfragment, menu);
    }

    private void updateWeather(){
        getRainTask rainTask = new getRainTask();
        rainTask.execute();
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
        protected Double doInBackground(String... params) {
        //if (params.length==0){
        //    return null;
        //}
            HttpURLConnection urlConnection = null;
            String rainForecastJsonStr = null;
            BufferedReader reader = null;
            try {
                //Construct the URL for forecast.io and attempt to connect
                final String BASE_URL ="https://api.forecast.io/forecast/";
                final String API_KEY = "753e2243637bc7967d0f27a82560c14f";
                final String LATITUDE_PARAM = "53.483457";
                final String LONGITUDE_PARAM = "-2.263960";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(API_KEY)
                        .appendPath(LATITUDE_PARAM+","+LONGITUDE_PARAM)
                        .build();

                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }
                rainForecastJsonStr = buffer.toString();

            }
            catch(IOException e){
                Log.e(LOG_TAG,"Error",e);
                return null;
            }
            finally{
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
                if (reader != null){
                    try{
                        reader.close();
                    }
                    catch(IOException e){
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try{
                return getPrecipPercent(rainForecastJsonStr);
            }
            catch(JSONException e){
                Log.e(LOG_TAG,e.getMessage(),e);
            }
            return null;
        }

        private Double getPrecipPercent(String jsonStr)
                throws JSONException {
            JSONObject forecastObject = new JSONObject(jsonStr);
            JSONObject hourlyForecastObject = forecastObject.getJSONObject("daily")
                    .getJSONArray("data")
                    .getJSONObject(5);
            Double currentPrecipProb = hourlyForecastObject.getDouble("precipProbability");
            return currentPrecipProb*100;
        }

        protected void onPostExecute(Double result) {
            if (result != null){
                TextView text = (TextView) getActivity().findViewById(R.id.rain_textview);
                text.setText(Integer.toString(result.intValue()));
                text.append("%");
            }
        }
    }
}
