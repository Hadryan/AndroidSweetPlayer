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

package com.streaming.sweetplayer.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.widget.Toast;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.streaming.sweetplayer.ArtistActivity;
import com.streaming.sweetplayer.MainActivity;
import com.streaming.sweetplayer.PlayerActivity;
import com.streaming.sweetplayer.R;
import com.streaming.sweetplayer.adapter.SongAdapter;
import com.streaming.sweetplayer.api.Config;
import com.streaming.sweetplayer.database.DataBaseHelper;
import com.streaming.sweetplayer.fragment.ArtistFragment;
import com.streaming.sweetplayer.fragment.SearchFragment;
import com.streaming.sweetplayer.fragment.TopFragment;
import com.streaming.sweetplayer.service.PlayerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Class to manage common functions needed across the whole application.
 */
public class Utils {
    private static final String TAG = "Utils";
    private static final String CHARSET_NAME = "UTF-8";

    public static boolean isConnectedToInternet(Context context) {
        CheckInternetConnection internetConnection = new CheckInternetConnection(context);
        return internetConnection.isConnectingToInternet();
    }

    private static Comparator<HashMap<String, String>> hashMapComparator = new Comparator<HashMap<String, String>>() {
        public int compare(final HashMap<String, String> m1, final HashMap<String, String> m2) {
            return m1.get(Config.NAME).compareTo(m2.get(Config.NAME));
        }
    };

    public static void sortArrayList(ArrayList<HashMap<String, String>> list) {
        if(!list.isEmpty()) {
            Collections.sort(list, hashMapComparator);
        }
    }

    /**
     * Function to show a dialog to the user about a current problem or inform about something happening.
     *
     * @param context Context
     * @param title   String
     * @param message String
     */
    public static void showAlertDialog(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    public static void showAlertDialogItems(Context context, String title, CharSequence[] items) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.create();
        alertDialog.setTitle(title);
        alertDialog.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog_2, int which) {
            }
        });
        alertDialog.show();
    }

    /**
     * Function to show a message to the user.
     * It looks good to center the message.
     *
     * @param context Context
     * @param message CharSequence
     */
    public static void showUserMessage(Context context, CharSequence message) {
        Toast playingMessage = Toast.makeText(context, message, Toast.LENGTH_LONG);
        playingMessage.setGravity(Gravity.CENTER, 0, 0);
        playingMessage.show();
    }

    public static void startPlayerService(FragmentActivity activity, ArrayList<HashMap<String, String>> arrayList,
                                          int position, String image) {
        HashMap<String, String> songHashMap = arrayList.get(position);
        if(isConnectedToInternet(activity)) {
            Intent intent = new Intent(activity.getApplicationContext(), PlayerActivity.class);
            intent.putExtra(Config.ID, songHashMap.get(Config.ID));
            intent.putExtra(Config.ARTIST, songHashMap.get(Config.ARTIST));
            intent.putExtra(Config.NAME, songHashMap.get(Config.NAME));
            intent.putExtra(Config.MP3, songHashMap.get(Config.MP3));
            intent.putExtra(Config.DURATION, songHashMap.get(Config.DURATION));
            intent.putExtra(Config.URL, songHashMap.get(Config.URL));

            if(image.equals(Config.SONGS_ITEM)) {
                intent.putExtra(Config.IMAGE, songHashMap.get(Config.IMAGE));
            } else {
                intent.putExtra(Config.IMAGE, image);
            }
            activity.startActivity(intent);

            if(!songHashMap.get(Config.ID).equals(Config.playerSongId)) {
                // Log.d("Player Service", "Song Id: " + songHashMap.get(Config.ID) + "Current Player Song Id: " + Config.playerSongId);
                Config.serviceIntent = new Intent(activity, PlayerService.class);
                Config.serviceIntent.putExtra(Config.PLAYER_CURRENT_INDEX, position);
                Config.serviceIntent.putExtra(Config.PLAYER_LIST, arrayList);
                activity.startService(Config.serviceIntent);
            }
        } else {
            showAlertDialog(activity, activity.getString(R.string.no_internet_title), activity.getString(R.string.no_internet_message));
        }

        String artistSongName = songHashMap.get(Config.NAME) + " - " + songHashMap.get(Config.ARTIST);
        gaSendEvent(activity, "Song click", artistSongName);
    }

    /**
     * Send information to Google Analytics
     */
    public static void gaSendEvent(Context context, String action, String textTracked) {
        try {
            GoogleAnalytics appInstance = GoogleAnalytics.getInstance(context);
            Tracker tracker = appInstance.getTracker(context.getString(R.string.ga_trackingId));

            try {
                if(tracker != null) {
                    tracker.sendEvent(Config.GOOGLE_ANALYTICS_ACTION, action, textTracked, null);
                }
            } catch(NullPointerException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gaSendView(Context context, String view) {
        try {
            GoogleAnalytics appInstance = GoogleAnalytics.getInstance(context);
            Tracker tracker = appInstance.getTracker(context.getString(R.string.ga_trackingId));

            if(tracker != null && view != null) {
                try {
                    tracker.sendView(view);
                } catch(NullPointerException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean validateString(String myString) {
        return myString != null && myString.length() > 0;
    }

    public static boolean validateSongData(String songId, String songName,
                                           String songArtistName, String songMp3,
                                           String songDuration, String songUrl,
                                           String songArtistImage) {
        return  validateString(songId) &&
                validateString(songName) &&
                validateString(songArtistName) &&
                validateString(songMp3) &&
                validateString(songDuration) &&
                validateString(songUrl) &&
                validateString(songArtistImage);
    }

    public static void addMultipleSongsToPlaylist(Context context, DataBaseHelper dataBase,  SongAdapter adapter,
                                                  ArrayList<HashMap<String, String>> arrayList, boolean isArtist) {
        List<CharSequence> stringList = new ArrayList<CharSequence>();
        List<CharSequence> alreadyAddedList = new ArrayList<CharSequence>();
        CharSequence[] songItems;

        for (int i = 0; i < arrayList.size(); i++) {
            if(adapter.boolean_values[i]) {
                String songId = arrayList.get(i).get(Config.ID);
                String songName = arrayList.get(i).get(Config.NAME);
                String songArtistName = arrayList.get(i).get(Config.ARTIST);
                String songMp3 = arrayList.get(i).get(Config.MP3);
                String songDuration = arrayList.get(i).get(Config.DURATION);
                String songUrl = arrayList.get(i).get(Config.URL);
                String songArtistImage = arrayList.get(i).get(Config.IMAGE);
                if(validateSongData(songId, songName, songArtistName, songMp3,
                                    songDuration, songUrl, songArtistImage)) {
                    if(!dataBase.isOnPlayList(songId)) {
                        String artistImage = Utils.replaceImage(songArtistImage, 1);
                        dataBase.insert(songId, songName, songArtistName, songMp3,
                                        songDuration, songUrl, artistImage);
                        stringList.add(songName);
                        if(PlayerService.songList != null) {
                            if(!PlayerService.songList.isEmpty()) {
                                HashMap<String, String> songHashMap = arrayList.get(i);
                                PlayerService.songList.add(songHashMap);
                            }
                        }
                    } else {
                        alreadyAddedList.add(songName);
                    }
                }
            }
        }

        int listSize = stringList.size();
        int addedListSize = alreadyAddedList.size();

        if(listSize > 0) {
            songItems = new String[listSize];
            for(int i = 0; i < listSize; i++) {
                songItems[i] = stringList.get(i);
            }
            showAlertDialogItems(context, context.getString(R.string.playlist_multiple_added), songItems);
            if(isArtist) {
                ArtistActivity.setMenuItemsVisibility(true, false);
                ArtistFragment.setSongAdapter(false);
            } else {
                MainActivity.setMenuItemsVisibility(true, false);
                if(MainActivity.currentTab.equals(Config.TOP_TAB)) {
                    TopFragment.setSongAdapter(false);
                } else {
                    SearchFragment.setSongAdapter(false);
                }
            }
            gaSendEvent(context, "Multi Playlist added", Arrays.toString(songItems));
        } else if(addedListSize > 0) {
            songItems = new String[addedListSize];
            for(int i = 0; i < addedListSize; i++) {
                songItems[i] = alreadyAddedList.get(i);
            }
            showAlertDialogItems(context, context.getString(R.string.playlist_already_added), songItems);
        } else {
            showAlertDialog(context, context.getString(R.string.add_to_playlist), context.getString(R.string.not_selected));
        }
    }

    public static String replaceImage(String artistImage, int value) {
        String finalImage = "";
        String image_1 = "-1.jpg";
        String image_2 = "-2.jpg";
        String image_3 = "-3.jpg";

        if(validateString(artistImage)) {
            switch (value) {
                case 1:
                    finalImage = artistImage.replace(image_3, image_1);
                    return finalImage;
                case 2:
                    String image = artistImage.replace(image_3, image_2);
                    finalImage = image.replace(image_1, image_2);
                    return finalImage;
                case 3:
                    finalImage = artistImage.replace(image_1, image_3);
                    return finalImage;
            }
        }
        return finalImage;
    }

    public static String getTotalDuration(int duration) {
        String secondsPref = "";
        int minutes;
        int seconds;

        if (duration < 60) {
            minutes = 0;
            seconds = duration;
        } else {
            minutes = duration / 60;
            seconds = duration % 60;
        }

        if (seconds < 10) {
            secondsPref = "0";
        }

        return minutes + ":" + secondsPref + seconds;
    }

    public static int toSeconds(int miliseconds) {
        return miliseconds / 1000;
    }
}

