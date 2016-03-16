package org.cerion.musicplayer;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.cerion.musicplayer.data.AudioFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Database extends SQLiteOpenHelper {

    private static final String TAG = "Database";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "data.db";

    //Singleton class
    private static Database mInstance;
    private Database(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public synchronized static Database getInstance(Context context)
    {
        if(mInstance == null)
            mInstance = new Database(context.getApplicationContext());

        return mInstance;
    }

    private static final String TABLE = "library";
    private static final String _PATH = "path";
    private static final String _ARTIST = "artist";
    private static final String _ALBUM = "album";
    private static final String _TITLE = "title";

    private static final String SQL_CREATE = "create table " + TABLE + "("
            + _PATH + " TEXT PRIMARY KEY NOT NULL, "
            + _ARTIST + " TEXT, "
            + _ALBUM + " TEXT, "
            + _TITLE + " TEXT"
            + ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        reset();
    }

    public void reset() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void add(AudioFile audioFile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(_PATH, audioFile.getPath());
        values.put(_ARTIST, audioFile.getArtist());
        values.put(_ALBUM, audioFile.getAlbum());
        values.put(_TITLE, audioFile.getTitle());

        db.insert(TABLE, null, values);
        db.close();
    }

    public Map<String,Integer> getArtists() {
        SQLiteDatabase db = getReadableDatabase();
        Map<String,Integer> result = new HashMap<>();

        String query = String.format("SELECT %s,count(*) FROM %s GROUP BY %s", _ARTIST, TABLE, _ARTIST);
        Cursor c = db.rawQuery(query, null);

        if(c != null) {
            while (c.moveToNext()) {
                result.put(c.getString(0), c.getInt(1) );
            }
            c.close();
        }

        db.close();
        return result;
    }

    public Map<String,AudioFile> getFilesInPath(String path) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM %s WHERE %s LIKE '%s/%%' AND %s NOT LIKE '%s/%%/%%'", TABLE, _PATH, path, _PATH, path);

        Map<String,AudioFile> map = new HashMap<>();
        Cursor c = db.rawQuery(query, null);

        if(c != null) {
            while (c.moveToNext()) {
                String filePath = c.getString(c.getColumnIndexOrThrow(_PATH));
                String artist = c.getString(c.getColumnIndexOrThrow(_ARTIST));
                String album = c.getString(c.getColumnIndexOrThrow(_ALBUM));
                String title = c.getString(c.getColumnIndexOrThrow(_TITLE));

                AudioFile af = new AudioFile(filePath, artist, album, title);
                map.put(filePath,af);
            }
            c.close();
        }

        db.close();
        return map;
    }

    public List<AudioFile> getFilesForArtist(String artist) {
        SQLiteDatabase db = getReadableDatabase();
        List<AudioFile> result = new ArrayList<>();

        String query = String.format("SELECT * FROM %s WHERE %s='%s' ORDER BY %s", TABLE, _ARTIST, artist, _TITLE);
        Cursor c = db.rawQuery(query, null);

        if(c != null) {
            while (c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(_PATH));
                String album = c.getString(c.getColumnIndexOrThrow(_ALBUM));
                String title = c.getString(c.getColumnIndexOrThrow(_TITLE));

                AudioFile af = new AudioFile(path, artist, album, title);
                result.add(af);
            }
            c.close();
        }

        db.close();
        return result;
    }

    public void log()
    {
        Map<String,Integer> artists = getArtists();

        Iterator it = artists.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String artist = (String)pair.getKey();
            int count = (Integer)pair.getValue();
            System.out.println(artist + ": " + count);
            it.remove();
        }

    }
}
