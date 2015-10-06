package com.qlecomte.uqac.qrcode;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ResultQRCodeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_qrcode);

        String str = getIntent().getStringExtra("MyQRCode");
        TextView textView = (TextView)findViewById(R.id.text);
        textView.setText(str);
    }

}
