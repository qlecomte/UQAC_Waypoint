package com.qlecomte.uqac.qrcode;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences prefs;
    private static final int WAYPOINTMANAGER_REQUESTCODE = 698;

    public static final String PREFS_NAME = "PrefRange";
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        editor = getSharedPreferences(PREFS_NAME,0).edit();
        editor.putInt("rangeSize", 500).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_maps:
                Intent intent = new Intent(this, WaypointManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, WAYPOINTMANAGER_REQUESTCODE);
                break;
            case R.id.action_settings:
                intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_movementtype:
                if(item.getIcon()== getResources().getDrawable( R.drawable.car )){
                    item.setIcon(R.drawable.footmen);
                    editor.putInt("rangeSize", 1500).commit();
                }else {
                    item.setIcon(R.drawable.car);
                    editor.putInt("rangeSize", 500).commit();
                }
                break;
            case R.id.action_qrcode:
                intent = new Intent(this,QRCodeActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
