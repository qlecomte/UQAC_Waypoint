package com.qlecomte.uqac.qrcode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    // Database Name
    private static final String DATABASE_PATH = MyAppSingleton.getContext().getFilesDir().getPath() + "/../databases/";
    private static final String DATABASE_NAME = "WaypointDB";

    // Table names
    private static final String TABLE_WAYPOINTS = "waypointTable";
    private static final String TABLE_TEMPLATE = "templateTable";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    // Waypoints Column Names
    private static final String KEY_NAME = "name";
    private static final String KEY_ICON = "icon";
    private static final String KEY_ID_TEMPLATE = "id_template";

    // Template Column Names
    private static final String KEY_TITLE = "title";
    private static final String KEY_PATHIMAGE = "pathImage";
    private static final String KEY_SUMMARY = "summary";



    //The Android's default system path of your application database.

    private SQLiteDatabase myDataBase;

    private DatabaseManager() {
        super(MyAppSingleton.getContext(), DATABASE_NAME, null, 1);
        try {
            createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String myPath = DATABASE_PATH + DATABASE_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(!dbExist){
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }

        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DATABASE_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){
            //database does't exist yet.

        }

        if(checkDB != null){
            checkDB.close();
        }

        return checkDB != null;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = MyAppSingleton.getContext().getAssets().open(DATABASE_NAME);

        // Path to the just created empty db
        String outFileName = DATABASE_PATH + DATABASE_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    @Override
    public synchronized void close() {
        if(myDataBase != null)
            myDataBase.close();
        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
    }


    // Waypoint
    public void addWaypoint(Waypoint w) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, w.getName());
        values.put(KEY_LATITUDE, w.getLatitude());
        values.put(KEY_LONGITUDE, w.getLongitude());
        values.put(KEY_ICON, w.getIcon());
        values.put(KEY_ID_TEMPLATE, -1);

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
                w.setIdTemplate(Integer.parseInt(cursor.getString(5)));
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

    // Template
    public Template getTemplate(int id) {
        Template t = new Template();
        String query = "SELECT * FROM " + TABLE_TEMPLATE + " WHERE " + KEY_ID + " = " + id;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                t = new Template();
                t.setTitle(cursor.getString(1));
                t.setPathImage(cursor.getString(2));
                t.setLatitude(Double.parseDouble(cursor.getString(3)));
                t.setLongitude(Double.parseDouble(cursor.getString(4)));
                t.setSummary(cursor.getString(5));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return t;
    }
}