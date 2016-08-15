package com.hammerox.android.acendaofarolbaixo;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DetectorFragment extends Fragment {

    @BindView(R.id.toggle_detector) ToggleButton mToggleButton;

    private String mServiceName = DetectorService.class.getName();
    private ActivityManager mActivityManager;

    private static final int LOCATION_PERMISSION_ID = 1001;

    private OnFragmentInteractionListener mListener;

    public DetectorFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detector, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        // Find DetectorService running on background
        // If found, toggle button to ON. If not, toggle to OFF
        if (findService(mServiceName)) {
            Log.v(DetectorService.LOG_TAG, "Found Service running");
            mToggleButton.setChecked(true);
        } else {
            mToggleButton.setChecked(false);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    @OnClick(R.id.toggle_detector)
    public void onToggleClicked(ToggleButton button) {
        boolean on = button.isChecked();

        if (on) {
            // Location permission not granted
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
                return;
            }

            // Enable detector
            Intent intent = new Intent(getActivity(), DetectorService.class);
            getActivity().startService(intent);
        } else {
            // Disable detector
            Intent intent = new Intent(getActivity(), DetectorService.class);
            getActivity().stopService(intent);
        }
    }


    private boolean findService(String serviceName) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        }

        // Search
        for (ActivityManager.RunningServiceInfo service : mActivityManager.getRunningServices(Integer.MAX_VALUE)) {
            // Check
            if (serviceName.equals(service.service.getClassName())) {
                // If found, return true
                return true;
            }
        }

        // If not found, return false
        return false;
    }

}
