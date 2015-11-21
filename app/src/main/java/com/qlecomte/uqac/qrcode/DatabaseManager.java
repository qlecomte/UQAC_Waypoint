package com.qlecomte.uqac.qrcode;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseManager extends SQLiteOpenHelper {

    /** Instance unique pré-initialisée */
    private static DatabaseManager INSTANCE = new DatabaseManager();

    /** Point d'accès pour l'instance unique du singleton */
    public static DatabaseManager getInstance()
    {
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

    // Order Table Column Names
    private static final String KEY_NAME = "name";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    // Table Create Statement
    private static final String CREATE_WAYPOINT_TABLE = "CREATE TABLE " + TABLE_WAYPOINTS + " ( "
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_NAME + " TEXT, "
            + KEY_LATITUDE +" INTEGER, "
            + KEY_LONGITUDE +" INTEGER  )";

    private DatabaseManager() {
        super(MyAppSingleton.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        Log.i(LOG, "Constructeur");
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        arg0.execSQL(CREATE_WAYPOINT_TABLE);
        Log.i(LOG, "Creation");

    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        arg0.execSQL("DROP TABLE IF EXISTS " + TABLE_WAYPOINTS);
        this.onCreate(arg0);

        Log.i(LOG, "Upgrade");
    }

}