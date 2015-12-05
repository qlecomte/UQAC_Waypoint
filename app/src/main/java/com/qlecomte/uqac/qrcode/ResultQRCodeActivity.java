package com.qlecomte.uqac.qrcode;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;

import java.util.Locale;

public class ResultQRCodeActivity extends AppCompatActivity implements OnInitListener {

    private TextToSpeech mTts;
    private String contenuQR;
    private int MY_DATA_CHECK_CODE = 674;

    private static final int WAYPOINTMANAGER_REQUESTCODE = 698;

    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_qrcode);

        contenuQR = getIntent().getStringExtra("MyQRCode");
        TextView textView = (TextView)findViewById(R.id.text);

        textView.setText(contenuQR);
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        editor = getPreferences(MODE_PRIVATE).edit();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_buttons, menu);

        if (getPreferences(MODE_PRIVATE).getBoolean("isCar", true)) {
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
                boolean isCar = getPreferences(MODE_PRIVATE).getBoolean("isCar", true);
                if (isCar){
                    item.setIcon(R.drawable.footmen);
                    editor.putBoolean("isCar", false).commit();
                    editor.putInt("rangeSize", 500).commit();
                }
                else {
                    item.setIcon( R.drawable.car );
                    editor.putBoolean("isCar", true).commit();
                    editor.putInt("rangeSize", 1500).commit();
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


    protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // Succès, au moins un moteur de TTS à été trouvé, on l'instancie
                mTts = new TextToSpeech(this, this);

                if (mTts.isLanguageAvailable(Locale.FRANCE) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    mTts.setLanguage(Locale.FRANCE);
                }


            } else {
                // Echec, aucun moteur n'a été trouvé, on propose à l'utilisateur d'en installer un depuis le Market
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            if (Build.VERSION.SDK_INT < 21) {
                mTts.speak(contenuQR, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                mTts.speak(contenuQR, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(mTts !=null){
            mTts.stop();
            mTts.shutdown();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTts != null)
            mTts.shutdown();
    }

}
