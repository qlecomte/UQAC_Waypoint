package com.qlecomte.uqac.qrcode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;

import java.util.List;


public class WaypointManagerActivity extends Activity {

    public SwipeToDismissTouchListener<ListViewAdapter> touchListener;

    private double m_currentLatitude = 0.0;
    private double m_currentLongitude = 0.0;

    final MyBaseAdapter myAdapter = new MyBaseAdapter();

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Log.d("WaypointManagerActivity", "Receive Broadcast" + bundle.toString());

                m_currentLatitude =  bundle.getDouble("latitude");
                m_currentLongitude = bundle.getDouble("longitude");

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
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent i = new Intent (this, LocationService.class);
        this.registerReceiver(receiver, new IntentFilter(LocationService.LOCATION));
        this.startService(i);
    }

    @Override
    protected void onPause() {
        super.onPause();
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

        private final List<Waypoint> waypoints = DatabaseManager.get().getWaypoints();

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

        class ViewHolder {
            TextView dataTextView;
            TextView secondaryDataTextView;

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
