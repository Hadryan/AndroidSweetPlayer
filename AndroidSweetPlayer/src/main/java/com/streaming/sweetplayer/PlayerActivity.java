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

package com.streaming.sweetplayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.streaming.sweetplayer.api.Config;
import com.streaming.sweetplayer.database.DataBaseHelper;
import com.streaming.sweetplayer.service.PlayerService;
import com.streaming.sweetplayer.utils.ImageLoader;
import com.streaming.sweetplayer.utils.JSONParser;
import com.streaming.sweetplayer.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerActivity extends ActionBarActivity implements OnSeekBarChangeListener {
    private static final String TAG = "Player Activity";
    private Activity mActivity;
    private ArrayList<HashMap<String, String>> mSongDetailList = null;
    private boolean mBroadcastIsRegistered;
    private DataBaseHelper mDataBase;
    private Intent mSeekbarIntent;
    private ImageButton mPlayPauseButton;
    private ImageButton mPreviousButton;
    private ImageButton mNextButton;
    private ImageButton mRepeatButton;
    private ImageButton mShuffleButton;
    private ImageLoader mImageLoader;
    private ImageView mArtistImageView;
    private MenuItem mAddMenuItem;
    private ProgressBar mLoadingBar;
    private ProgressDialog mProgressDialog;
    private ShareActionProvider mShareActionProvider;
    private SeekBar mProgressBar;
    private TextView mCurrentDurationTextView;
    private TextView mTotalDurationTextView;
    private TextView mSongNameTextView;
    private TextView mArtistNameTextView;

    public static String sSongId;
    public static String sSongArtistName;
    public static String sSongName;
    public static String sSongMp3;
    public static String sSongDuration;
    public static String sSongArtistImage;
    public static String sSongUrl;
    public static String sSongDetailUrl;
    public static final String BROADCAST_SEEKBAR = "com.streaming.sweetplayer.sendseekbar";

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        mActivity = this;
        mDataBase = new DataBaseHelper(mActivity);
        mImageLoader = new ImageLoader(mActivity.getApplicationContext());
        mLoadingBar = (ProgressBar) findViewById(R.id.loadingBar);

        Intent intent = getIntent();
        String action = intent.getAction();
        if(action != "see_player") {
                mLoadingBar.setVisibility(View.VISIBLE);
                Config.playerSongId = intent.getStringExtra(Config.ID);
                Config.playerSongArtistName = intent.getStringExtra(Config.ARTIST);
                Config.playerSongName = intent.getStringExtra(Config.NAME);
                Config.playerSongMp3 = intent.getStringExtra(Config.MP3);
                Config.playerSongDuration = intent.getStringExtra(Config.DURATION);
                Config.playerSongArtistImage = intent.getStringExtra(Config.IMAGE);
                Config.playerSongUrl = intent.getStringExtra(Config.URL);
                Config.playerSongDetailUrl = Config.SONG_DETAIL_URL + Config.playerSongId;
        }
        updatePlayerInfo();
        // logPlayerInfo();

        mSeekbarIntent = new Intent(BROADCAST_SEEKBAR);
        mProgressBar = (SeekBar) findViewById(R.id.progressBar);
        mCurrentDurationTextView = (TextView) findViewById(R.id.currentDuration);
        mTotalDurationTextView = (TextView) findViewById(R.id.totalDuration);
        mArtistNameTextView = (TextView) findViewById(R.id.playerArtistName);
        mArtistNameTextView.setText(sSongArtistName);
        mSongNameTextView = (TextView) findViewById(R.id.playerSongName);
        mSongNameTextView.setText(sSongName);
        mPlayPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        mPreviousButton = (ImageButton) findViewById(R.id.previousButton);
        mNextButton = (ImageButton) findViewById(R.id.nextButton);
        mRepeatButton = (ImageButton) findViewById(R.id.repeatButton);
        mShuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        mArtistImageView = (ImageView) findViewById(R.id.player_artist_image);
        loadImage(sSongArtistImage);
        setupPlayerButtons();
    }

    private void updatePlayerInfo() {
        sSongId = Config.playerSongId;
        sSongArtistName = Config.playerSongArtistName;
        sSongName = Config.playerSongName;
        sSongMp3 = Config.playerSongMp3;
        sSongDuration = Config.playerSongDuration;
        sSongArtistImage = Config.playerSongArtistImage;
        sSongUrl = Config.playerSongUrl;
        sSongDetailUrl = Config.playerSongDetailUrl;
    }

    private void logPlayerInfo() {
        Log.d(TAG, "Player id: " + sSongId);
        Log.d(TAG, "Player artist: " + sSongArtistName);
        Log.d(TAG, "Player name: " + sSongName);
        Log.d(TAG, "Player mp3: " + sSongMp3);
        Log.d(TAG, "Player duration: " + sSongDuration);
        Log.d(TAG, "Player image: " + sSongArtistImage);
        Log.d(TAG, "Player url: " + sSongUrl);
        Log.d(TAG, "Player detail url: " + sSongDetailUrl);
    }

    private void setupPlayerButtons() {
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (PlayerService.checkSongsList()) {
                    if (PlayerService.isPlaying) {
                        mPlayPauseButton.setImageResource(R.drawable.btn_play);
                        PlayerService.pause();
                    } else {
                        mPlayPauseButton.setImageResource(R.drawable.btn_pause);
                        PlayerService.playSong();
                    }
                }
            }
        });

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (PlayerService.checkSongsList()) {
                    mLoadingBar.setVisibility(View.VISIBLE);
                    PlayerService.previous();
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (PlayerService.checkSongsList()) {
                    mLoadingBar.setVisibility(View.VISIBLE);
                    PlayerService.next();
                }
            }
        });

        mRepeatButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (PlayerService.checkSongsList()) {
                    if (PlayerService.isRepeat) {
                        PlayerService.isRepeat = false;
                        Utils.showUserMessage(mActivity, getString(R.string.repeat_off));
                        mRepeatButton.setImageResource(R.drawable.btn_repeat);
                    } else {
                        PlayerService.isRepeat = true;
                        PlayerService.isShuffle = false;
                        Utils.showUserMessage(mActivity, getString(R.string.repeat_on));
                        mRepeatButton.setImageResource(R.drawable.btn_repeat_focused);
                        mShuffleButton.setImageResource(R.drawable.btn_shuffle);
                    }
                }
            }
        });

        mShuffleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (PlayerService.checkSongsList()) {
                    if (PlayerService.isShuffle) {
                        PlayerService.isShuffle = false;
                        Utils.showUserMessage(mActivity, getString(R.string.shuffle_off));
                        mShuffleButton.setImageResource(R.drawable.btn_shuffle);
                    } else {
                        PlayerService.isShuffle = true;
                        PlayerService.isRepeat = false;
                        Utils.showUserMessage(mActivity, getString(R.string.shuffle_on));
                        mShuffleButton.setImageResource(R.drawable.btn_shuffle_focused);
                        mRepeatButton.setImageResource(R.drawable.btn_repeat);
                    }
                }
            }
        });

        mProgressBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.gaSendView(this, "player_screen");
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(PlayerService.BROADCAST_ACTION));
        mBroadcastIsRegistered = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBroadcastIsRegistered) {
            unregisterReceiver(broadcastReceiver);
            mBroadcastIsRegistered = false;
        }

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDataBase != null) {
            mDataBase.close();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDataBase != null) {
            mDataBase.close();
        }

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_options_menu, menu);
        inflater.inflate(R.menu.share_menu, menu);

        MenuItem shareItem = menu.findItem(R.id.item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        mShareActionProvider.setShareIntent(getDefaultIntent());
        //mShareActionProvider.setShareHistoryFileName(null);

        MenuItem viewLyricsMenuItem = menu.findItem(R.id.item_view_lyrics);
        if(viewLyricsMenuItem != null) {
            viewLyricsMenuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (PlayerService.checkSongsList()) {
                        new GetSongDetailTask().execute();
                    }
                    return true;
                }
            });
        }

        mAddMenuItem = menu.findItem(R.id.item_add_to_playlist);
        if(mDataBase.isOnPlayList(sSongId)) {
            mAddMenuItem.setVisible(false);
        } else {
            mAddMenuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    addToPlaylist();
                    return true;
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    private Intent getDefaultIntent() {
        String urlToShare = Config.DOMAIN + sSongUrl;
        String sharingText = mActivity.getString(R.string.sharing_text) + " " + urlToShare + " " + mActivity.getString(R.string.sharing_android_app) + " " + Config.GOOGLE_PLAY_LINK;

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, sSongName);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        return sharingIntent;
    }

    private void loadImage(String artistImage) {
        if(Utils.validateString(artistImage)) {
            if(Config.deviceHeight > 320) {
                int imageToLoad = 2;
                if(Config.deviceDensityDpi <= 160 && Config.deviceHeight <= 480) {
                    imageToLoad = 1;
                }
                mImageLoader.DisplayImage(Utils.replaceImage(artistImage, imageToLoad), mArtistImageView);
            } else {
                mArtistImageView.setVisibility(View.GONE);
            }
        }
    }

    private void addToPlaylist() {
        if(Utils.validateSongData(sSongId, sSongName, sSongArtistName, sSongMp3,
                                  sSongDuration, sSongUrl, sSongArtistImage)) {
            if(mDataBase.isOnPlayList(sSongId)) {
                Utils.showUserMessage(mActivity.getApplicationContext(), mActivity.getString(R.string.playlist_already_added));
            } else {
                String artistImage = Utils.replaceImage(sSongArtistImage, 1);
                mDataBase.insert(sSongId, sSongName, sSongArtistName, sSongMp3,
                                 sSongDuration, sSongUrl, artistImage);
                mAddMenuItem.setVisible(false);
                Utils.showUserMessage(mActivity.getApplicationContext(), mActivity.getString(R.string.playlist_added));
                Utils.gaSendEvent(mActivity, "Playlist added", sSongName + "-" + sSongArtistName);
            }
        }
    }

    private class GetSongDetailTask extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage(mActivity.getString(R.string.processing_text));
            mProgressDialog.show();
            mSongDetailList = new ArrayList<HashMap<String, String>>();
        }

        @Override
        protected String doInBackground(Object... params) {
            if(Utils.validateString(sSongDetailUrl)) {
                getSongDetail();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            if(mSongDetailList != null && !mSongDetailList.isEmpty()) {
                showLyrics();
            }
        }
    }

    private void getSongDetail() {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray;

        try {
            JSONObject json = jsonParser.getJSONFromUrl(sSongDetailUrl);

            if(json != null) {
                jsonArray = json.getJSONArray(Config.SONGS_ITEM);
                int items_length = jsonArray.length();

                for(int i = 0; i < items_length; i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    map.put(Config.LYRICS, jsonObject.getString(Config.LYRICS));
                    try {
                        mSongDetailList.add(map);
                    } catch(NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showLyrics() {
        String lyrics = mSongDetailList.get(0).get(Config.LYRICS);
        final String lyrics_final = lyrics.replace("\r", "\n ");
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                if(!mActivity.isFinishing()) {
                    if (lyrics_final.length() > 0 && !lyrics_final.equals("null")) {
                        Utils.showAlertDialog(mActivity, sSongName, lyrics_final);
                    } else {
                        Utils.showAlertDialog(mActivity, sSongName, mActivity.getString(R.string.no_lyric));
                    }
                }
                Utils.gaSendEvent(mActivity, "lyrics", sSongName);
                mSongDetailList = null;
            }
        });
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null) {
                updateControlsTime(intent);
            }
        }
    };

    /**
     * Update the progressbar and time during playback.
     *
     * @param intent Intent
     */
    private void updateControlsTime(Intent intent) {
        if (PlayerService.isCompleted) {
            mLoadingBar.setVisibility(View.GONE);
            String artistName = intent.getStringExtra("artistName");
            String songName = intent.getStringExtra("songName");
            String artistImageName = intent.getStringExtra("artistImageName");
            loadImage(artistImageName);
            mArtistNameTextView.setText(artistName);
            mSongNameTextView.setText(songName);

            if(mDataBase != null) {
                if(!mDataBase.isOnPlayList(sSongId)) {
                    if(mAddMenuItem != null) {
                        mAddMenuItem.setVisible(true);
                    }
                }
            }
        }

        int currentDuration = intent.getIntExtra("currentDuration", 0);
        int totalDuration = intent.getIntExtra("totalDuration", 0);
        mCurrentDurationTextView.setText(Utils.getTotalDuration(Utils.toSeconds(currentDuration)));
        mTotalDurationTextView.setText(Utils.getTotalDuration(totalDuration));
        mProgressBar.setMax(totalDuration);
        mProgressBar.setProgress(Utils.toSeconds(currentDuration));
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int seekPos = seekBar.getProgress();
            mSeekbarIntent.putExtra("seekpos", seekPos);
            mActivity.sendBroadcast(mSeekbarIntent);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}