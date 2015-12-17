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
    private ProgressBar spinner; //new
    private TextView rainText;
    private TextView timeFrameText;
    private TextView lastDateTextView;
    private String timeFrame;
    private ShareActionProvider mShareActionProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public String getTimeFrame(){
        return timeFrame;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.rain_fragment, container, false);
        lastDateTextView = ((TextView) rootView.findViewById(R.id.last_updated_textview));
        rainText = (TextView) rootView.findViewById(R.id.rain_textview);
        timeFrameText = (TextView) rootView.findViewById(R.id.timeframeTextView);
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar); //new
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

    private void updateWeather() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        timeFrame = prefs.getString(getString(R.string.pref_timeframe_key),
                getString(R.string.pref_timeframe_daily));

        lastDateTextView.setText(getString(R.string.last_updated_message, prefs.getString("last_updated_key", "never")));

        if (networkInfo != null && networkInfo.isConnected()){
            GetRainTask rainTask = new GetRainTask(this,  getString(R.string.apikey));
            rainTask.execute(timeFrame);
        }
        else{
            Toast.makeText(getContext(),"NETWORK ERROR", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void displayLoading(){
        rainText.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayResult(int result){
        spinner.setVisibility(View.GONE); //new
        rainText.setVisibility(View.VISIBLE);

        rainText.setText(result+"");
        rainText.append("%");

        lastDateTextView.setText(getString(R.string.last_updated_message , getCurrentTime()));

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

}
