package com.qlecomte.uqac.qrcode;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;

import java.util.Locale;

public class ResultQRCodeActivity extends Activity implements OnInitListener {

    private TextToSpeech mTts;
    private String contenuQR;
    private int MY_DATA_CHECK_CODE = 674;

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
