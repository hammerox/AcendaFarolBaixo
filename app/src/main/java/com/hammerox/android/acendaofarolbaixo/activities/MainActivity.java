package com.hammerox.android.acendaofarolbaixo.activities;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.hammerox.android.acendaofarolbaixo.R;
import com.hammerox.android.acendaofarolbaixo.fragments.DetectorFragment;
import com.hammerox.android.acendaofarolbaixo.fragments.PrefFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity
        implements DetectorFragment.OnFragmentInteractionListener,
                    PrefFragment.OnFragmentInteractionListener {

    @BindView(R.id.activity_main_toolbar) Toolbar toolbar;
    @BindView(R.id.activity_main_layout_container) SlidingUpPanelLayout layoutContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    @Override
    public void onBackPressed() {
        if (layoutContainer.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            layoutContainer.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }
}