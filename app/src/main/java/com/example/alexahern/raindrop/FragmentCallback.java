package com.example.alexahern.raindrop;

/**
 * Created by aherna01 on 25/04/16.
 */
public interface FragmentCallback {
    void displayLoading();

    void displayResult(int result);

    void setShareIntent(String input);

    String getTimeFrame();

    String getApiKey();

    String[] getLatitudeAndLongitude();
}
