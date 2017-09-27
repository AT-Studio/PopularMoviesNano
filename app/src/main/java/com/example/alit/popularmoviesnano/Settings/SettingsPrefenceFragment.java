package com.example.alit.popularmoviesnano.Settings;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.example.alit.popularmoviesnano.R;

/**
 * Created by AliT on 9/16/17.
 */

public class SettingsPrefenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
