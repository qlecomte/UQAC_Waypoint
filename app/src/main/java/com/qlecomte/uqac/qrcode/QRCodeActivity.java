package com.qlecomte.uqac.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

public class QRCodeActivity extends Activity {

    private Camera mCamera = null;
    private CameraPreview mPreview = null;
    private Handler autoFocusHandler = null;
    private FrameLayout preview = null;
    private ImageScanner mScanner = null;
    private boolean previewing = true;

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
    void openCamera() {
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
    void releaseCamera() {
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
        i.putExtra("MyQRCode", str);
        startActivity(i);

    }

}