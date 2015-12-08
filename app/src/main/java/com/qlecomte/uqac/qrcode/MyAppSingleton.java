package com.qlecomte.uqac.qrcode;

import android.app.Application;
import android.content.Context;

public class MyAppSingleton extends Application {

    private static Context mContext;
    private static String m_PrefName;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
    public static String getPrefName() {
        return m_PrefName;
    }
}