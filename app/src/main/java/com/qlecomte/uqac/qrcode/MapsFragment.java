package com.qlecomte.uqac.qrcode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsFragment extends Fragment implements GoogleMap.OnMapLongClickListener, LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static final LatLng SAGUENAY = new LatLng(48.427976,-71.068516);
    private static final int ZOOM = 14;
    private String nomMarqueur;
    private Marker myLocation;

    private LocationManager manager;


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

        manager = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }


        try {
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            manager.removeUpdates(this);
        }catch (SecurityException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onMapLongClick(final LatLng point)
    {
        //Entrer le nom du marqueur
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        final EditText boxText = new EditText(getActivity());
        boxText.setHint("Nom du Marqueur");

        alert.setView(boxText);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                nomMarqueur = boxText.getText().toString();

                mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(nomMarqueur)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            }
        }).show();


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


        mMap.setOnMapLongClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SAGUENAY, ZOOM));
        mMap.addMarker(new MarkerOptions().position(new LatLng(48.420128, -71.052198)).title("Université du Québec à Chicoutimi"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(48.404702, -71.055185)).title("Place du Royaume"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(48.429142, -71.052359)).title("Tour à Bières"));
        myLocation = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
    }


    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        //double altitude = location.getAltitude();
        //float accuracy = location.getAccuracy();

        myLocation.setPosition(new LatLng(latitude, longitude));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), ZOOM));


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

}
