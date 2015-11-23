package com.qlecomte.uqac.qrcode;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service
{
    private static final String TAG = "LocationService";

    private LocationManager mLocationManager = null;
    private LocationListener mLocationListener;

    private static final int LOCATION_INTERVAL_MS = 60*1000;
    private static final float LOCATION_DISTANCE_METERS = 50f;

    public static final String LOCATION = "com.qlecomte.uqac.qrcode.locationChanged";

    private class LocationListener implements android.location.LocationListener{
        Location mLastLocation;
        public LocationListener(String provider)
        {
            //Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
            //broadcastLocation();
        }
        @Override
        public void onLocationChanged(Location location)
        {
            //Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            broadcastLocation();
        }
        @Override
        public void onProviderDisabled(String provider)
        {
            //Log.e(TAG, "onProviderDisabled: " + provider);
        }
        @Override
        public void onProviderEnabled(String provider)
        {
            //Log.e(TAG, "onProviderEnabled: " + provider);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            //Log.e(TAG, "onStatusChanged: " + provider);
        }

    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        //Log.e(TAG, "onCreate");

        mLocationListener = new LocationListener(LocationManager.NETWORK_PROVIDER);

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL_MS, LOCATION_DISTANCE_METERS,
                    mLocationListener);
        } catch (SecurityException | IllegalArgumentException ex) {
            //Log.i(TAG, "fail to request location update, ignore", ex);
        }
        /*
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL_MS, LOCATION_DISTANCE_METERS,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            //Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            //Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        broadcastLocation();

        return START_STICKY;
    }


    @Override
    public void onDestroy()
    {
        //Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {

            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                //Log.i(TAG, "fail to remove location listners, ignore", ex);
            }

        }
    }
    private void initializeLocationManager() {
        //Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void broadcastLocation(){

        Intent data = new Intent(LOCATION);
        data.putExtra("latitude", mLocationListener.mLastLocation.getLatitude());
        data.putExtra("longitude", mLocationListener.mLastLocation.getLongitude());

        //Log.d(TAG, "Send Broadcast : " + mLocationListener.mLastLocation.toString());

        sendBroadcast(data);
    }
}