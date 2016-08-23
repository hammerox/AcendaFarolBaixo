package com.hammerox.android.acendaofarolbaixo.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.hammerox.android.acendaofarolbaixo.R;

import butterknife.BindString;
import butterknife.ButterKnife;


public class PrefFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener{

    @BindString(R.string.pref_file_name) String fileName;
    @BindString(R.string.pref_alarm_type_key) String alarmTypeKey;
    @BindString(R.string.pref_alarm_sound_key) String alarmSoundKey;
    @BindString(R.string.pref_alarm_speech_key) String alarmSpeechKey;
    @BindString(R.string.pref_boot_key) String bootKey;

    private ListPreference alarmTypePref;
    private Preference alarmSoundPref;
    private Preference alarmSpeechPref;
    private Preference bootPref;
    private OnFragmentInteractionListener mListener;

    public static final String LOG_TAG = PrefFragment.class.getSimpleName();

    public PrefFragment() {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this, getActivity());

        getPreferenceManager().setSharedPreferencesName(fileName);
        addPreferencesFromResource(R.xml.preferences);

        // Set variables
        alarmTypePref = (ListPreference) findPreference(alarmTypeKey);
        alarmTypePref.setOnPreferenceChangeListener(this);

        alarmSoundPref = findPreference(alarmSoundKey);
        alarmSoundPref.setOnPreferenceChangeListener(this);

        alarmSpeechPref = findPreference(alarmSpeechKey);
        alarmSpeechPref.setOnPreferenceChangeListener(this);

        bootPref = findPreference(bootKey);
        bootPref.setOnPreferenceChangeListener(this);


        /* If needed to read the preference value on fragment creation,
        * call onPreferenceChange here*/
        onPreferenceChange(alarmTypePref, alarmTypePref.getValue());
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String key = preference.getKey();
        String value = o.toString();
        Log.v(LOG_TAG, key + " is " + value);

        if (key.equals(alarmTypeKey)) {
            switch (Integer.valueOf(value)) {
                case 0:
                    alarmSoundPref.setEnabled(false);
                    alarmSpeechPref.setEnabled(false);
                    break;
                case 1:
                    alarmSoundPref.setEnabled(true);
                    alarmSpeechPref.setEnabled(false);
                    break;
                case 2:
                    alarmSoundPref.setEnabled(false);
                    alarmSpeechPref.setEnabled(true);
                    break;
            }
        }
        return true;
    }

}
