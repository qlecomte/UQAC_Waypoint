package com.qlecomte.uqac.qrcode;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        PreferenceFragment fragment = (PreferenceFragment)getFragmentManager().findFragmentById(android.R.id.content);
        Preference customPref = fragment.findPreference("language");
        customPref.setSummary(prefs.getString("language", ""));

        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case "language":
                PreferenceFragment fragment = (PreferenceFragment)getFragmentManager().findFragmentById(android.R.id.content);
                Preference customPref = fragment.findPreference("language");
                customPref.setSummary(sharedPreferences.getString(key, ""));
                break;
            case "notification_proximity":
                Toast toast = Toast.makeText(getApplicationContext(), "Notif changées", Toast.LENGTH_SHORT);
                toast.show();
                break;
            default:
                break;
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }



    /*
     * Langue
     * Notifications
     * A propos
     * Noter l'application
     *
     */

}
