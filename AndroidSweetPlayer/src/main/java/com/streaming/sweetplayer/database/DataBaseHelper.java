/*
 * Copyright (C) 2013 Ronny Yabar Aizcorbe <ronnycontacto@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.streaming.sweetplayer.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.streaming.sweetplayer.api.Config;
import com.streaming.sweetplayer.service.PlayerService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is responsible to provide CRUD functions to interact with the DataBase to manage the mPlayList.
 */
public class DataBaseHelper {
    private static final String TAG = "DATABASE";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = Config.SERVER;
    private static final String TABLE_NAME = "songs";
    private static final String SONG_ID = "song_id";
    private SQLiteDatabase mDB;
    private MainHelper mMainHelper = null;

    public ArrayList<HashMap<String, String>> playList = new ArrayList<HashMap<String, String>>();

    /**
     * Constructor, We need to create a MainHelper object. See below about the MainHelper class.
     *
     * @param context Context
     */
    public DataBaseHelper(Context context) {
        mMainHelper = new MainHelper(context);
    }

    void open() throws SQLException {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mMainHelper.getWritableDatabase();
        }
    }

    /**
     * Insert a row into the table Songs
     *
     */
    public void insert(String id, String name, String artist, String mp3,
                       String duration, String url, String image) {
        String songId = '"' + id + '"';
        String songName = '"' + name + '"';
        String songArtistName = '"' + artist + '"';
        String songMp3 = '"' + mp3 + '"';
        String songDuration = '"' + duration + '"';
        String songUrl = '"' + url + '"';
        String songArtistImage = '"' + image + '"';

        try {
            String sql = "INSERT INTO " + TABLE_NAME + "(song_id, name, artist, mp3, duration, url, image1)" + " VALUES (" + songId + "," + songName + "," + songArtistName + "," + songMp3 + "," + songDuration + "," + songUrl + "," + songArtistImage + ")";
            // Log.d(TAG, sql);
            mDB.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a single row from the table Songs
     *
     * @param songId Integer
     */
    public void delete(String songId, int position) {
        try {
            mDB.delete(TABLE_NAME, SONG_ID + "='"+songId+"'", null);
            if(PlayerService.songList != null) {
                if(!PlayerService.songList.isEmpty()) {
                    PlayerService.songList.remove(position);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a song is already in the playlist by the ID.
     *
     * @param playListItemId String
     */
    public Boolean isOnPlayList(String playListItemId) {
        Cursor playListCursor = null;
        int nro_results = 0;
        try {
            this.open();
            playListCursor = mDB.query(TABLE_NAME, new String[]{Config.ID}, SONG_ID + "=" + '"' + playListItemId + '"', null, null, null, null);
            nro_results = playListCursor.getCount();
        } finally {
            if (playListCursor != null && !playListCursor.isClosed()) {
                playListCursor.close();
            }
        }

        return nro_results > 0;
    }

    /**
     * Returns ALL rows from the table Songs
     */
    public ArrayList<HashMap<String, String>> selectAll() {
        Cursor cursor = null;
        /**
         * Once we select all rows from the table, next step is to add the data to the PlayList Array HashMap <Key, Value>.
         * When the selection finished, it's mandatory to close the cursor.
         */
        try {
            this.open();
            cursor = mDB.query(TABLE_NAME, new String[]{SONG_ID, Config.NAME, Config.ARTIST, Config.MP3, Config.DURATION, Config.URL, Config.IMAGE}, null, null, null, null, "id DESC");
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(Config.ID, cursor.getString(0));
                    map.put(Config.NAME, cursor.getString(1));
                    map.put(Config.ARTIST, cursor.getString(2));
                    map.put(Config.MP3, cursor.getString(3));
                    map.put(Config.DURATION, cursor.getString(4));
                    map.put(Config.URL, cursor.getString(5));
                    map.put(Config.IMAGE, cursor.getString(6));
                    playList.add(map);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return playList;
    }

    /**
     * Close the MainHelper and DataBase connection.
     */
    public void close() {
        if (mMainHelper != null) {
            mMainHelper.close();
        }

        if (mDB != null && mDB.isOpen()) {
            mDB.close();
        }
    }

    /**
     * Class responsible to create/update the DataBase and manage tables.
     */
    public class MainHelper extends SQLiteOpenHelper {
        MainHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + "(id INTEGER PRIMARY KEY autoincrement, song_id TEXT, name TEXT, artist TEXT, mp3 TEXT, duration TEXT, url TEXT, image1 TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Updating DataBase");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
