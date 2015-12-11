package com.qlecomte.uqac.qrcode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;

import java.util.List;


public class WaypointManagerActivity extends AppCompatActivity {


    private SwipeToDismissTouchListener<ListViewAdapter> touchListener;

    private double m_currentLatitude = 0.0;
    private double m_currentLongitude = 0.0;

    private SharedPreferences.Editor editor;

    private final MyBaseAdapter myAdapter = new MyBaseAdapter();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Log.d("WaypointManagerActivity", "Receive Broadcast" + bundle.toString());

                m_currentLatitude =  bundle.getDouble("latitude");
                m_currentLongitude = bundle.getDouble("longitude");

                Location l = new Location("DummyProvider");
                l.setLatitude(m_currentLatitude);
                l.setLongitude(m_currentLongitude);
                Waypoint.sortList(myAdapter.waypoints, l);

                /* Refresh the listView using the new location */
                myAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint_manager);
        init((ListView) findViewById(R.id.list_view));

        editor = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_buttons, menu);

        if (getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).getBoolean("isCar", true)) {
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
                break;
            case R.id.action_settings:
                intent = new Intent(this,SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                break;
            case R.id.action_movementtype:
                boolean isCar = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).getBoolean("isCar", true);
                if (isCar){
                    Toast.makeText(this, "Mode piéton activé", Toast.LENGTH_SHORT).show();
                    item.setIcon(R.drawable.footmen);
                    int distFoot = PreferenceManager.getDefaultSharedPreferences(this).getInt("rangedist_foot", 500);
                    editor.putBoolean("isCar", false).commit();
                    editor.putInt("rangedist", distFoot).commit();
                }
                else {
                    Toast.makeText(this, "Mode voiture activé", Toast.LENGTH_SHORT).show();
                    item.setIcon( R.drawable.car );
                    int distCar = PreferenceManager.getDefaultSharedPreferences(this).getInt("rangedist_car", 1500);
                    editor.putBoolean("isCar", true).commit();
                    editor.putInt("rangedist", distCar).commit();
                }
                break;
            case R.id.action_qrcode:
                intent = new Intent(this,QRCodeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent i = new Intent (this, LocationService.class);
        this.registerReceiver(receiver, new IntentFilter(LocationService.LOCATION));
        this.startService(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        touchListener.processPendingDismisses();

        this.unregisterReceiver(receiver);
    }

    private void init(ListView listView) {
        listView.setAdapter(myAdapter);
        touchListener = new SwipeToDismissTouchListener<>(
                        new ListViewAdapter(listView),
                        new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListViewAdapter view, int position) {
                                myAdapter.remove(position);
                            }
                        });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (touchListener.existPendingDismisses()) {
                    touchListener.processPendingDismisses();
                } else {

                    Intent data = new Intent();
                    data.putExtra("latitude", myAdapter.getItem(position).getLatitude());
                    data.putExtra("longitude", myAdapter.getItem(position).getLongitude());
                    setResult(RESULT_OK, data);

                    finish();
                }
            }
        });
    }

    class MyBaseAdapter extends BaseAdapter {

        private final List<Waypoint> waypoints;

        public MyBaseAdapter(){
            waypoints = DatabaseManager.get().getWaypoints();


        }


        @Override
        public int getCount() {
            return waypoints.size();
        }

        @Override
        public Waypoint getItem(int position) {
            return waypoints.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void remove(int position) {
            DatabaseManager.get().deleteWaypoint(waypoints.get(position));
            waypoints.remove(position);
            notifyDataSetChanged();
        }

        final class ViewHolder {
            TextView dataTextView;
            final TextView secondaryDataTextView;

            ViewHolder(View view) {
                dataTextView = ((TextView) view.findViewById(R.id.txt_data));
                secondaryDataTextView = ((TextView) view.findViewById(R.id.txt_secondaryData));
                view.setTag(this);
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = convertView == null
                    ? new ViewHolder(convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_dismissable, parent, false))
                    : (ViewHolder) convertView.getTag();

            Waypoint wp = waypoints.get(position);

            /* Compute the distance from the current position to the waypoint */
            float[] distanceFromPosition = new float[3];
            Location.distanceBetween(m_currentLatitude, m_currentLongitude, wp.getLatitude(), wp.getLongitude(), distanceFromPosition);

            String distanceString = (int)distanceFromPosition[0] + " m";

            /* Set some infos into the viewHolder */
            viewHolder.dataTextView.setText(wp.getName());
            viewHolder.secondaryDataTextView.setText(distanceString);

            TextView undoBtn = (TextView)convertView.findViewById(R.id.txt_undo);

            undoBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //do something
                    touchListener.undoPendingDismiss();
                }
            });

            return convertView;
        }

    }


}
