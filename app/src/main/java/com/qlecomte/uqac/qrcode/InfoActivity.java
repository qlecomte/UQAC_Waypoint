package com.qlecomte.uqac.qrcode;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;
import android.widget.Toast;

import java.util.Locale;

public class InfoActivity extends AppCompatActivity{

    private String contenuQR;
    private final int MY_DATA_CHECK_CODE = 674;

    private static final int WAYPOINTMANAGER_REQUESTCODE = 698;

    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        contenuQR = getIntent().getStringExtra("MyQRCode");
        TextView textView = (TextView)findViewById(R.id.text);
        textView.setText(contenuQR);

        editor = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_buttons, menu);

        if (getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).getBoolean("isCar", true)) {
            menu.findItem(R.id.action_movementtype).setIcon(R.drawable.car);
        }
        else{
            menu.findItem(R.id.action_movementtype).setIcon(R.drawable.footmen);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_maps:
                intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.action_waypoints:
                intent = new Intent(this, WaypointManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(intent, WAYPOINTMANAGER_REQUESTCODE);
                break;
            case R.id.action_settings:
                intent = new Intent(this,SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                break;
            case R.id.action_movementtype:
                boolean isCar = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).getBoolean("isCar", true);
                if (isCar){
                    Toast.makeText(this, "Mode piéton activé", Toast.LENGTH_SHORT).show();
                    item.setIcon(R.drawable.footmen);
                    int distFoot = PreferenceManager.getDefaultSharedPreferences(this).getInt("rangedist_foot", 500);
                    editor.putBoolean("isCar", false).commit();
                    editor.putInt("rangedist", distFoot).commit();
                }
                else {
                    Toast.makeText(this, "Mode voiture activé", Toast.LENGTH_SHORT).show();
                    item.setIcon( R.drawable.car );
                    int distCar = PreferenceManager.getDefaultSharedPreferences(this).getInt("rangedist_car", 1500);
                    editor.putBoolean("isCar", true).commit();
                    editor.putInt("rangedist", distCar).commit();
                }
                break;
            case R.id.action_qrcode:
                intent = new Intent(this,QRCodeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
