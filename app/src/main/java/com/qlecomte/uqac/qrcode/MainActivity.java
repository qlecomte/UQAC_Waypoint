package com.qlecomte.uqac.qrcode;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemLongClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMenuItemClickListener,
        OnMenuItemLongClickListener {

    private FragmentManager fragmentManager;
    private DialogFragment mMenuDialogFragment;

    public static final String PREFS_NAME = "PrefRange";
    SharedPreferences.Editor editor;
    private static final int WAYPOINTMANAGER_REQUESTCODE = 698;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editor = getSharedPreferences(PREFS_NAME,0).edit();
        editor.putInt("rangeSize", 500).commit();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        fragmentManager = getSupportFragmentManager();
        initMenuFragment();

        MapsFragment mapsFragment = new MapsFragment();

        if (getIntent().hasExtra("latitude_notif") && getIntent().hasExtra("longitude_notif") ) {
            Bundle b = new Bundle();
            b.putDouble("latitude_notif", getIntent().getDoubleExtra("latitude_notif", -1000));
            b.putDouble("longitude_notif", getIntent().getDoubleExtra("longitude_notif", -1000));
            mapsFragment.setArguments(b);
        }

        addFragment(mapsFragment, true, R.id.container);

        }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_maps:
                Intent intent = new Intent(this, WaypointManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, WAYPOINTMANAGER_REQUESTCODE);
                break;
            case R.id.action_settings:
                intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_movementtype:
                if(item.getIcon()== getResources().getDrawable( R.drawable.car )){
                    item.setIcon(R.drawable.footmen);
                    editor.putInt("rangeSize", 1500).commit();
                }else {
                    item.setIcon(R.drawable.car);
                    editor.putInt("rangeSize", 500).commit();
                }
                break;
            case R.id.action_qrcode:
                intent = new Intent(this,QRCodeActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initMenuFragment() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.menu_size));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(false);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
    }

    private List<MenuObject> getMenuObjects() {
        // You can use any [resource, bitmap, drawable, color] as image:
        // item.setResource(...)
        // item.setBitmap(...)
        // item.setDrawable(...)
        // item.setColor(...)
        // You can set image ScaleType:
        // item.setScaleType(ScaleType.FIT_XY)
        // You can use any [resource, drawable, color] as background:
        // item.setBgResource(...)
        // item.setBgDrawable(...)
        // item.setBgColor(...)
        // You can use any [color] as text color:
        // item.setTextColor(...)
        // You can set any [color] as divider color:
        // item.setDividerColor(...)

        /*
         * Close
         * QR Code
         * Gestion Waypoints
         * Options
         */


        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject();
        close.setResource(R.drawable.close);

        MenuObject qrCode = new MenuObject();
        qrCode.setResource(R.drawable.qrcode);

        MenuObject waypoint = new MenuObject();
        waypoint.setResource(R.drawable.waypoint);

        MenuObject settings = new MenuObject();
        settings.setResource(R.drawable.settings);


        menuObjects.add(close);
        menuObjects.add(qrCode);
        menuObjects.add(waypoint);
        menuObjects.add(settings);

        return menuObjects;
    }

    private void addFragment(android.support.v4.app.Fragment fragment, boolean addToBackStack, int containerId) {
        invalidateOptionsMenu();
        String backStackName = fragment.getClass().getName();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(containerId, fragment, backStackName)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (mMenuDialogFragment != null && mMenuDialogFragment.isAdded()) {
            mMenuDialogFragment.dismiss();
        } else{
            finish();
        }
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        Intent i;
        switch (position){
            case 1:
                i = new Intent(this, QRCodeActivity.class);
                startActivity(i);
                break;

            case 2:
                i = new Intent(this, WaypointManagerActivity.class);
                startActivityForResult(i, WAYPOINTMANAGER_REQUESTCODE);
                break;

            case 3:
                i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
        }



    }

    @Override
    public void onMenuItemLongClick(View clickedView, int position) {
        Toast.makeText(this, "Long clicked on position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WAYPOINTMANAGER_REQUESTCODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if (data != null){
                    MapsFragment mapsFragment = (MapsFragment)getSupportFragmentManager().findFragmentById(R.id.container);
                    mapsFragment.moveMap(data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0));
                }
            }
        }

    }


}
