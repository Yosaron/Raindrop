package com.example.alexahern.raindrop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A placeholder fragment containing a simple view.
 */
public class RainFragment extends Fragment implements GetRainTask.Callback {
    private ProgressBar spinningLoader;
    private TextView chanceOfRain;
    private TextView periodOfMeasurement;
    private TextView lastUpdated;
    private String timeFrame;
    private ShareActionProvider mShareActionProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public String getTimeFrame() {
        return timeFrame;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.rain_fragment, container, false);
        lastUpdated = ((TextView) rootView.findViewById(R.id.last_updated_textview));
        chanceOfRain = (TextView) rootView.findViewById(R.id.rain_textview);
        periodOfMeasurement = (TextView) rootView.findViewById(R.id.periodOfMeasurement);
        spinningLoader = (ProgressBar) rootView.findViewById(R.id.spinningLoader);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
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

    public SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    public void setTimeFrameFromPreference() {
        SharedPreferences prefs = getSharedPreferences();
        timeFrame = prefs.getString(getString(R.string.pref_timeframe_key),
                getString(R.string.pref_timeframe_daily));
    }

    public void displayNetworkErrorMessage() {
        Toast.makeText(getContext(), "NETWORK ERROR", Toast.LENGTH_SHORT).show();
    }

    private void updateWeather() {
        setTimeFrameFromPreference();

        if (thereIsANetwork()) {
            setLastUpdatedWithStoredTime();
            executeRainTaskWithApiKeyAndTimeFrame();
        } else {
            displayNetworkErrorMessage();
        }
    }

    public void executeRainTaskWithApiKeyAndTimeFrame() {
        GetRainTask rainTask = new GetRainTask(this, getString(R.string.apikey));
        rainTask.execute(timeFrame);
    }

    public boolean thereIsANetwork() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rainfragment, menu);
        MenuItem shareMenuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
    }

    public void setShareIntent(String textToShare) {
        Intent shareButtonIntent = new Intent();
        shareButtonIntent.setAction(Intent.ACTION_SEND);
        shareButtonIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        shareButtonIntent.setType("text/plain");
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareButtonIntent);
        }
    }

    @Override
    public void displayLoading() {
        chanceOfRain.setVisibility(View.GONE);
        spinningLoader.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayResult(int result) {
        hideLoading();
        chanceOfRain.setText(result + "");
        chanceOfRain.append("%");
        setLastUpdatedWithNewTime();
    }

    public void hideLoading() {
        spinningLoader.setVisibility(View.GONE);
        chanceOfRain.setVisibility(View.VISIBLE);
    }

    public void setLastUpdatedWithNewTime() {
        String currentTime = getCurrentTime();
        setTimeFrameMessage();
        lastUpdated.setText(getString(R.string.last_updated_message, currentTime));
        saveCurrentTimeToSharedPreferences(currentTime);
    }

    public void setLastUpdatedWithStoredTime() {
        SharedPreferences prefs = getSharedPreferences();
        lastUpdated.setText(getString(R.string.last_updated_message, prefs.getString("last_updated_key", "never")));
    }

    public String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(cal.getTime());
    }

    public void setTimeFrameMessage() {
        if (timeFrame.equals("hourly")) {
            periodOfMeasurement.setText(getString(R.string.hourly_timeframe_message));
        } else if (timeFrame.equals("daily")) {
            periodOfMeasurement.setText(getString(R.string.daily_timeframe_message));
        }
    }

    public void saveCurrentTimeToSharedPreferences(String currentTime) {
        SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_updated_key", currentTime);
        editor.apply();
    }

}
