package com.qlecomte.uqac.qrcode;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

public class QRCodeActivity extends AppCompatActivity {

    private Camera mCamera = null;
    private CameraPreview mPreview = null;
    private Handler autoFocusHandler = null;
    private FrameLayout preview = null;
    private ImageScanner mScanner = null;
    private boolean previewing = true;

    SharedPreferences.Editor editor;
    private static final int WAYPOINTMANAGER_REQUESTCODE = 698;

    static {
        System.loadLibrary("iconv");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /* Instanciation du mScanner QR Code */
        mScanner = new ImageScanner();

        mScanner.setConfig(0, Config.X_DENSITY, 3);
        mScanner.setConfig(0, Config.Y_DENSITY, 3);

        mScanner.setConfig(0, Config.ENABLE, 0); //Disable all the Symbols
        mScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1); //Only QRCODE is enable

        autoFocusHandler = new Handler();

        preview = (FrameLayout)findViewById(R.id.camera_preview);

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
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    /**
     * Get an instance of the Camera object.
     */
    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            Log.e("Camera", "Camera can not be opened");
        }
        return c;
    }

    /**
     * A safe way to open the camera.
     */
    private void openCamera() {
        if (mCamera == null){
            mCamera = getCameraInstance();
            mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
            preview.removeAllViews();
            preview.addView(mPreview);
            preview.setVisibility(View.VISIBLE);
        }

    }

    /**
     * A safe way to release an instance of the Camera object.
     */
    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            preview.setVisibility(View.INVISIBLE);
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        }

    }



    private final Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing) {
                try {
                    mCamera.autoFocus(autoFocusCB);
                } catch (Exception e) {
                    Log.e("Autofocus", "The autofocus has failed");
                }
            }
        }
    };

    private final Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters;
            Camera.Size size;
            Image barcode;
            int result;

            SymbolSet syms;

            parameters = camera.getParameters();
            size = parameters.getPreviewSize();
            String url = "Bouton de test";

            barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            result = mScanner.scanImage(barcode);

            if (result != 0) {
                syms = mScanner.getResults();
                for (Symbol sym : syms) {
                    url= sym.getData();
                }

                handleResult(url);
            }
        }
    };

    /**
     * Mimic continuous auto-focusing
     */
    private final Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 2000);
        }
    };

    private void handleResult(final String str){
        Intent i = new Intent(this, ResultQRCodeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.putExtra("MyQRCode", str);
        startActivity(i);

    }

}