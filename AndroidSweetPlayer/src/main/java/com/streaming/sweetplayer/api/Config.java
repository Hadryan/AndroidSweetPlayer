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

package com.streaming.sweetplayer.api;

import android.content.Intent;

public class Config {
    // SERVER Urls
    public static final String SERVER = "yourserver.com";
    private static final String BASE_URL = "http://api." + SERVER + "/";
    public static final String DOMAIN = "http://www." + SERVER;

    // JSON Urls
    public static final String ALBUMS_URL = BASE_URL + "albums.json?artistid=";
    public static final String ALBUMS_DETAIL_URL = BASE_URL + "album-songs.json?albumid=";
    public static final String ARTISTS_URL = BASE_URL + "artists.json";
    public static final String ARTISTS_DETAIL_URL = BASE_URL + "artist-songs.json?artistid=";
    public static final String GENRES_URL = BASE_URL + "genres.json";
    public static final String GENRE_ARTISTS_URL = BASE_URL + "genre-artists.json?genreid=";
    public static final String GENRE_SONGS_URL = BASE_URL + "genre-songs.json?genreid=";
    public static final String SEARCH_ARTISTS_URL = BASE_URL + "artists-search.json?q=";
    public static final String SEARCH_SONGS_URL = BASE_URL + "search.json?q=";
    public static final String SONG_DETAIL_URL = BASE_URL + "song-detail.json?id=";
    public static final String TOP_URL = BASE_URL + "top.json";

    // JSON item variables
    public static final String ALBUM = "album";
    public static final String ALBUMS_ITEM = "albums";
    public static final String ARTIST = "artist";
    public static final String ARTISTS_ITEM = "artists";
    public static final String DETAIL_URL = "detail_url";
    public static final String DURATION = "duration";
    public static final String GENRE = "genre";
    public static final String GENRES_ITEM = "genres";
    public static final String ID = "id";
    public static final String IMAGE = "image1";
    public static final String LYRICS = "lyrics";
    public static final String MP3 = "mp3";
    public static final String NAME = "name";
    public static final String SONG = "song";
    public static final String SONGS_ITEM = "songs";
    public static final String URL = "url";

    // TAB NAMES
    public static final String ARTISTS_TAB = "artists_screen";
    public static final String DETAIL_TAB = "detail_screen";
    public static final String GENRES_TAB = "genres_screen";
    public static final String PLAYLIST_TAB = "playlist_screen";
    public static final String SEARCH_TAB = "search_screen";
    public static final String TOP_TAB = "top_screen";

    // Device Metrics
    public static int deviceDensityDpi;
    public static int deviceHeight;
    public static int deviceWidth;

    // Player Config data
    public static final String PLAYER_CURRENT_INDEX = "player_current_index";
    public static final String PLAYER_LIST = "player_list";

    public static boolean playerServiceStarted = false;
    public static Intent serviceIntent;
    public static String playerSongId;
    public static String playerSongArtistName;
    public static String playerSongName;
    public static String playerSongMp3;
    public static String playerSongDuration;
    public static String playerSongArtistImage;
    public static String playerSongUrl;
    public static String playerSongDetailUrl;

    // Google Play link
    public static final String GOOGLE_PLAY_LINK = "https://play.google.com/store/apps/details?id=YOUR_APP_HERE";

    // UI action send to Google Analytics
    public static final String GOOGLE_ANALYTICS_ACTION = "ui_action";
}
