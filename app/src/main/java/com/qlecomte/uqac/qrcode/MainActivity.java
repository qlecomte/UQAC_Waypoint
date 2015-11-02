package com.qlecomte.uqac.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private TextToSpeech mTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b1 = (Button)findViewById(R.id.button);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, QRCodeActivity.class);
                startActivity(i);
            }
        });

        Button b2 = (Button)findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, LocationActivity.class);
                startActivity(i);
            }
        });

        Button b3 = (Button)findViewById(R.id.button3);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });

        Button b4 = (Button)findViewById(R.id.button4);
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent checkIntent = new Intent();
                checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                startActivityForResult(checkIntent, 0x01);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTts != null)
            mTts.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x01) {
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
        String tts = "Text To Speech";
        if (status == TextToSpeech.SUCCESS) {

            if (Build.VERSION.SDK_INT < 21) {
                mTts.speak(tts, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                mTts.speak(tts, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }
    }
}

