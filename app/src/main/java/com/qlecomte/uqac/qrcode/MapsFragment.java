package com.qlecomte.uqac.qrcode;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;


public class MapsFragment extends Fragment implements GoogleMap.OnMapLongClickListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private static final LatLng SAGUENAY = new LatLng(48.427976,-71.068516);
    private static final int ZOOM = 14;

    private static final float LOCATION_MARKER = BitmapDescriptorFactory.HUE_YELLOW;
    private static final float WAYPOINT_MARKER = BitmapDescriptorFactory.HUE_CYAN;

    private String nomMarqueur;
    private Marker myLocation;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Log.d("MapsFragment", "Receive Broadcast" + bundle.toString());

                double latitude = bundle.getDouble("latitude");
                double longitude = bundle.getDouble("longitude");

                myLocation.setPosition(new LatLng(latitude, longitude));
            }
        }
    };



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_maps, container, false);

        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.myMapContainer, new SupportMapFragment())
                .addToBackStack(null)
                .commit();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        setUpMapIfNeeded();
        updateMarkersMap();

        Intent i = new Intent (getActivity(), LocationService.class);

        getActivity().registerReceiver(receiver, new IntentFilter(LocationService.LOCATION));
        getActivity().startService(i);

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onMapLongClick(final LatLng point)
    {
        final EditText boxText = new EditText(getActivity());
        boxText.setHint("Nom du Marqueur");

        new AlertDialog.Builder(getActivity())
                .setView(boxText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        nomMarqueur = boxText.getText().toString();

                        mMap.addMarker(new MarkerOptions()
                                .position(point)
                                .title(nomMarqueur)
                                .snippet("-1")
                                .icon(BitmapDescriptorFactory.defaultMarker(WAYPOINT_MARKER)));

                        Waypoint w = new Waypoint(nomMarqueur, point.latitude, point.longitude, WAYPOINT_MARKER);
                        w.setIdTemplate(-1);

                        DatabaseManager.get().addWaypoint(w);


                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();


    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link MapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.myMapContainer))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }

    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        Log.i("Map", "setUpMap");

        mMap.setOnMapLongClickListener(this);
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(this);

        if (getArguments() == null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SAGUENAY, ZOOM));
        else
            moveMap(getArguments().getDouble("latitude_notif"),getArguments().getDouble("longitude_notif"), ZOOM);
    }

    private void updateMarkersMap(){
        List<Waypoint> waypoints = DatabaseManager.get().getWaypoints();

        mMap.clear();

        for (Waypoint w : waypoints){
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(w.getLatitude(), w.getLongitude()))
                    .title(w.getName())
                    .snippet(String.valueOf(w.getIdTemplate()))
                    .icon(BitmapDescriptorFactory.defaultMarker(w.getIcon())));
        }

        myLocation = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Position")
                .icon(BitmapDescriptorFactory.defaultMarker(LOCATION_MARKER)));
    }

    public void moveMap(double latitude, double longitude){
        moveMap(latitude, longitude, mMap.getCameraPosition().zoom);
    }

    public void moveMap(double latitude, double longitude, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View myContentsView = LayoutInflater.from(getContext()).inflate(R.layout.infobulle, null);
        private AddressResultReceiver resultReceiver;
        private Marker markerShowingInfoWindow;

        private boolean updateInfo;
        private String title;

        MyInfoWindowAdapter(){
            updateInfo = false;
            title = "";
        }

        @Override
        public View getInfoContents(final Marker marker) {
            markerShowingInfoWindow = marker;

            resultReceiver = new AddressResultReceiver(new Handler());
            // Fetch Address
            Location loc = new Location("dummyprovider");
            loc.setLatitude(marker.getPosition().latitude);
            loc.setLongitude(marker.getPosition().longitude);

            Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);

            // Pass the result receiver as an extra to the service.
            intent.putExtra(Constants.RECEIVER, resultReceiver);

            // Pass the location data as an extra to the service.
            intent.putExtra(Constants.LOCATION_DATA_EXTRA, loc);

            // Start the service. If the service isn't already running, it is instantiated and started
            // (creating a process for it if needed); if it is running then it remains running. The
            // service kills itself automatically once all intents are processed.
            getActivity().startService(intent);

            ((TextView)myContentsView.findViewById(R.id.title)).setText(marker.getTitle());

            if (marker.getSnippet() == null || Integer.parseInt(marker.getSnippet()) == -1){
                myContentsView.findViewById(R.id.button_plus).setVisibility(View.GONE);
            }
            else{
                myContentsView.findViewById(R.id.button_plus).setVisibility(View.VISIBLE);
            }

            // Crade, mais l'API google Maps V2 est moisie, donc on fait avec ce qu'on a...
            if (!title.equals(marker.getTitle())){
                title = marker.getTitle();
                updateInfo = true;

                ((TextView)myContentsView.findViewById(R.id.address)).setText("");
            }

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        class AddressResultReceiver extends ResultReceiver {

            public AddressResultReceiver(Handler handler) {
                super(handler);
            }

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                // Display the address string or an error message sent from the intent service.
                String mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

                if (resultCode == Constants.SUCCESS_RESULT) {
                    ((TextView)myContentsView.findViewById(R.id.address)).setText(mAddressOutput);
                }

                if (markerShowingInfoWindow != null && updateInfo) {
                    updateInfo = false;
                    //markerShowingInfoWindow.hideInfoWindow();
                    markerShowingInfoWindow.showInfoWindow();
                }
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.getSnippet() != null && Integer.parseInt(marker.getSnippet()) != -1){
            Intent i = new Intent(getActivity(), InfoActivity.class);
            i.putExtra("CodeTemplate", marker.getSnippet());
            i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
        }

    }
}
