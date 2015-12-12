package com.example.alexahern.raindrop;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class RainFragment extends Fragment {

    public RainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        TextView text = ((TextView) rootView.findViewById(R.id.last_updated_texview));
        text.setText(getString(R.string.last_updated_message, getString(R.string.last_updated_value)));
        updateWeather();
        return rootView;
    }

    private void updateWeather(){
        getRainTask rainTask = new getRainTask();
        rainTask.execute();
    }

    public class getRainTask extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = getRainTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
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
                Log.d(LOG_TAG, rainForecastJsonStr);

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

        return null;
        }

    }
}
