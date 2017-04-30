package edu.csulb.android.bluetoothmessenger.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.csulb.android.bluetoothmessenger.R;

public class WifiFragment extends Fragment {

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_wifi, container, false);
        return rootView;
    }

    public void open() {
        rootView.setVisibility(View.VISIBLE);
    }

    public void close() {
        rootView.setVisibility(View.GONE);
    }
}