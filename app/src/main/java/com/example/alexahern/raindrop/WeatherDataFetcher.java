package com.example.alexahern.raindrop;

import android.net.Uri;

import org.json.JSONException;

/**
 * Created by alexahern on 23/01/16.
 */

/*Interface for fetching the data from a Uri*/
public interface WeatherDataFetcher {
    Uri buildUri();

    String fetchUri(Uri builtUri);

    String downloadFromUri();

    Double getPercentageChanceOfRain(String timeFrame) throws JSONException;
}
