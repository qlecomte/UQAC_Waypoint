package com.qlecomte.uqac.qrcode;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper {

    /** Instance unique pré-initialisée */
    private static DatabaseManager INSTANCE;

    /** Point d'accès pour l'instance unique du singleton */
    public static DatabaseManager get()
    {
        if (INSTANCE == null)
            INSTANCE = new DatabaseManager();
        return INSTANCE;
    }

    // Logcat tag
    private static final String LOG = "Database Manager";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "ProjectDB";

    // Table names
    private static final String TABLE_WAYPOINTS = "waypointsTable";

    // Common column names
    private static final String KEY_ID = "id";

    // Waypoints Column Names
    private static final String KEY_NAME = "name";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ICON = "icon";

    // Table Create Statement
    private static final String CREATE_WAYPOINT_TABLE = "CREATE TABLE " + TABLE_WAYPOINTS + " ( "
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_NAME + " TEXT, "
            + KEY_LATITUDE +" REAL, "
            + KEY_LONGITUDE +" REAL, "
            + KEY_ICON + " INTEGER ) ";

    private DatabaseManager() {
        super(MyAppSingleton.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        arg0.execSQL(CREATE_WAYPOINT_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        arg0.execSQL("DROP TABLE IF EXISTS " + TABLE_WAYPOINTS);
        this.onCreate(arg0);
    }


    // Waypoint
    public void addWaypoint(Waypoint w) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, w.getName());
        values.put(KEY_LATITUDE, w.getLatitude());
        values.put(KEY_LONGITUDE, w.getLongitude());
        values.put(KEY_ICON, w.getIcon());

        db.insert(TABLE_WAYPOINTS, null, values);
    }
    public List<Waypoint> getWaypoints() {
        List<Waypoint> waypoints = new LinkedList<>();
        String query = "SELECT * FROM " + TABLE_WAYPOINTS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Waypoint w;
        if (cursor.moveToFirst()) {
            do {
                w = new Waypoint();
                w.setName(cursor.getString(1));
                w.setLatitude(Double.parseDouble(cursor.getString(2)));
                w.setLongitude(Double.parseDouble(cursor.getString(3)));
                w.setIcon(Float.parseFloat(cursor.getString(4)));
                waypoints.add(w);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return waypoints;
    }

    public void deleteWaypoint(Waypoint part) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WAYPOINTS,
                KEY_NAME + " = ?", new String[]{part.getName()});
    }
    public Waypoint deleteWaypoint(int id) {
        Waypoint w = getWaypoints().get(id);
        deleteWaypoint(w);
        return w;
    }
    public void deleteWaypoints(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WAYPOINTS);
        db.execSQL(CREATE_WAYPOINT_TABLE);
    }
}