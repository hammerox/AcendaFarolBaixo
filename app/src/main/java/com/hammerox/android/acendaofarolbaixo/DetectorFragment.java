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


import com.skyfishjy.library.RippleBackground;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mehdi.sakout.fancybuttons.FancyButton;


public class DetectorFragment extends Fragment {

    @BindView(R.id.detector_button) FancyButton mDetectorButton;
    @BindView(R.id.detector_ripple) RippleBackground mDetectorRipple;

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
            mDetectorRipple.startRippleAnimation();
        } else {
            mDetectorRipple.stopRippleAnimation();
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


    @OnClick(R.id.detector_button)
    public void onToggleClicked(FancyButton button) {
        boolean switchOn = !findService(mServiceName);
        if (switchOn) {
            // Location permission not granted
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
                return;
            }

            // Enable detector
            Intent intent = new Intent(getActivity(), DetectorService.class);
            getActivity().startService(intent);
            mDetectorRipple.startRippleAnimation();
        } else {
            // Disable detector
            Intent intent = new Intent(getActivity(), DetectorService.class);
            getActivity().stopService(intent);
            mDetectorRipple.stopRippleAnimation();
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
