package com.hammerox.android.acendaofarolbaixo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import butterknife.BindString;
import butterknife.ButterKnife;


public class PrefFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener{

    @BindString(R.string.pref_file_name) String fileName;
    @BindString(R.string.pref_boot_key) String bootKey;

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

        findPreference(bootKey).setOnPreferenceChangeListener(this);

        /* If needed to read the preference value on fragment creation,
        * call onPreferenceChange here*/
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
        sendLog(preference.getKey(), o.toString());
        return true;
    }


    private static void sendLog(String key, String value) {
        Log.v(LOG_TAG, key + " is " + value);
    }
}
