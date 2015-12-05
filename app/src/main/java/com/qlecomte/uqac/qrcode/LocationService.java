package com.qlecomte.uqac.qrcode;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnInitListener
{

    protected static final String TAG = "location-updates-sample";

    public static final String LOCATION = "com.qlecomte.uqac.qrcode.locationChanged";

    public static final double DISTANCE_WAYPOINT_ALERT_IN_METERS = 500;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 60000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Access to preferences
     */
    protected SharedPreferences prefs;

    /**
     * Boolean saying if notifications are activated
     */
    private boolean mNotificationsActivated;

    private NotificationManager mNotificationManager;
    private static final String GROUP_NOTIF_STR = "group_notif";
    private static final int BASE_NOTIF_ID = 2653;
    private int MY_DATA_CHECK_CODE = 674;
    private TextToSpeech mTts;
    private String notifVocale;

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            if (Build.VERSION.SDK_INT < 21) {
                mTts.speak(notifVocale, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                mTts.speak(notifVocale, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();

        mRequestingLocationUpdates = true;

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        prefs  = PreferenceManager.getDefaultSharedPreferences(this);
        mTts = new TextToSpeech(this, this);
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
    }

    @Override
    public void onStart(Intent intent, int startId) {

        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        mGoogleApiClient.connect();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        broadcastLocation();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
        mGoogleApiClient.disconnect();

        super.onDestroy();
    }


    public boolean getNotifActivated()
    {
        return prefs.getBoolean("notification_proximity",false);
    }

    public boolean getVocalActivated()
    {
        return prefs.getBoolean("vocal_synthesis", false);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        broadcastLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;

        broadcastLocation();
        if(getNotifActivated())
            notificationAlert();
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void broadcastLocation(){

        if (mCurrentLocation != null) {
            Intent data = new Intent(LOCATION);
            data.putExtra("latitude", mCurrentLocation.getLatitude());
            data.putExtra("longitude", mCurrentLocation.getLongitude());

            sendBroadcast(data);
        }
    }

    private void notificationAlert(){

        List<Waypoint> listeWaypoints = DatabaseManager.get().getWaypoints();
        List<Waypoint> listeCloseWaypoint = new ArrayList<>();

        for (Waypoint w : listeWaypoints) {

            if(calculDistance(mCurrentLocation, w) <= DISTANCE_WAYPOINT_ALERT_IN_METERS)
            {
                listeCloseWaypoint.add(w);
                /*Notification notif = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.waypointwhite)
                        .setContentTitle("Point d'intérêt proche !")
                        .setAutoCancel(true)
                        .setContentText("Point d'intérêt à moins de 500m : " + w.getName())
                        .setGroup(GROUP_NOTIF_STR)
                        .setGroupSummary(true)
                        .build();


                // mId allows you to update the notification later on.
                mNotificationManager.notify(BASE_NOTIF_ID+index, notif);*/



            }
        }

        if (listeCloseWaypoint.size() > 0) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for (Waypoint w : listeCloseWaypoint)
                inboxStyle.addLine(w.getName() + " - " + (int)calculDistance(mCurrentLocation, w)+"m");
            //inboxStyle.setBigContentTitle(listeCloseWaypoint.size() + " Waypoints proches");
            inboxStyle.setSummaryText("Waypoint");

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.putExtra("longitude_notif", mCurrentLocation.getLongitude());
            resultIntent.putExtra("latitude_notif", mCurrentLocation.getLatitude());

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );



            Notification summaryNotification = new NotificationCompat.Builder(this)
                    .setContentTitle(listeCloseWaypoint.size() + " Waypoints proches")
                    .setSmallIcon(R.drawable.waypointwhite)
                    .setStyle(inboxStyle)
                    .setGroup(GROUP_NOTIF_STR)
                    .setGroupSummary(true)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent)
                    .build();

            if (getVocalActivated())
            {
                notifVocale = "Il y a " + listeCloseWaypoint.size() + " points d'intérêts à proximité";
                mTts.speak(notifVocale, TextToSpeech.QUEUE_FLUSH, null);
            }

            mNotificationManager.notify(BASE_NOTIF_ID, summaryNotification);

        }
    }

    private double calculDistance(Location w1, Waypoint w2) {
        float[] resultat = new float[3];
        Location.distanceBetween(w1.getLatitude(), w1.getLongitude(), w2.getLatitude(), w2.getLongitude(), resultat);
        return (double)resultat[0];
    }


}