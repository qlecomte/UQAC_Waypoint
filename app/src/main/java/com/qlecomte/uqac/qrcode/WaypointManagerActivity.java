package com.qlecomte.uqac.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;

import java.util.List;


public class WaypointManagerActivity extends Activity {

    public SwipeToDismissTouchListener<ListViewAdapter> touchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint_manager);
        init((ListView) findViewById(R.id.list_view));
    }

    @Override
    protected void onPause() {
        super.onPause();
        touchListener.processPendingDismisses();
    }

    private void init(ListView listView) {
        final MyBaseAdapter adapter = new MyBaseAdapter();
        listView.setAdapter(adapter);
        touchListener = new SwipeToDismissTouchListener<>(
                        new ListViewAdapter(listView),
                        new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListViewAdapter view, int position) {
                                adapter.remove(position);
                            }
                        });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (touchListener.existPendingDismisses()){
                    touchListener.processPendingDismisses();
                }
                else{

                    Intent data = new Intent();
                    data.putExtra("latitude", adapter.getItem(position).getLatitude());
                    data.putExtra("longitude", adapter.getItem(position).getLongitude());
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
            ViewHolder(View view) {
                dataTextView = ((TextView) view.findViewById(R.id.txt_data));
                view.setTag(this);
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = convertView == null
                    ? new ViewHolder(convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_dismissable, parent, false))
                    : (ViewHolder) convertView.getTag();

            viewHolder.dataTextView.setText(waypoints.get(position).getName());

            TextView undoBtn = (TextView)convertView.findViewById(R.id.txt_undo);

            undoBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //do something
                    touchListener.undoPendingDismiss();
                }
            });

            final ImageView fav = (ImageView)convertView.findViewById(R.id.favorite);
            fav.setImageResource(favoriteResource(waypoints.get(position).isFavorite()));
            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean futurFavoriteState = !waypoints.get(position).isFavorite();

                    waypoints.get(position).setFavorite(futurFavoriteState);
                    DatabaseManager.get().updateWaypoint(waypoints.get(position));
                    fav.setImageResource(favoriteResource(futurFavoriteState));

                }
            });


            return convertView;
        }

        public int favoriteResource(boolean b){
            if (b)
                return R.drawable.starfull;
            else
                return R.drawable.starempty;

        }
    }


}
