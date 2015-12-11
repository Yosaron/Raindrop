package com.example.alexahern.raindrop;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

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
        text.setText(getString(R.string.last_updated_message) + getString(R.string.last_updated_value));

        return rootView;
    }
}
