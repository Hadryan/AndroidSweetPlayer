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

package com.streaming.sweetplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.streaming.sweetplayer.PlayerActivity;
import com.streaming.sweetplayer.api.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Class responsible to manage the MediaPlayer and all his functions (Stop, Play, Pause...)
 */
public class PlayerService extends Service {
    private static final String TAG = "PlayerService";
    private static int mCurrentIndex = 0;
    private static MediaPlayer mPlayer;
    private final Handler mHandler = new Handler();
    private boolean mBroadcastIsRegistered = false;
    private int mBufferPercent = 0;
    private Intent mBroadcastIntent;

    public static final String BROADCAST_ACTION = "com.streaming.sweetplayer";
    public static ArrayList<HashMap<String, String>> songList = null;
    public static boolean isCompleted = false;
    public static boolean isPlaying = false;
    public static boolean isRepeat = false;
    public static boolean isShuffle = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // Log.i(TAG, "Creating Service");
        Config.playerServiceStarted = true;
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mPlayer.setOnCompletionListener(mOnCompletionListener);
        mPlayer.setOnErrorListener(mOnErrorListener);
        mPlayer.setOnInfoListener(mOnInfoListener);
        mPlayer.setOnPreparedListener(mOnPreparedListener);
        mPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mBroadcastIntent = new Intent(BROADCAST_ACTION);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Log.i(TAG, "Starting Player Service");
        registerReceiver(broadcastReceiver, new IntentFilter(PlayerActivity.BROADCAST_SEEKBAR));
        mBroadcastIsRegistered = true;
        if (intent != null) {
            isCompleted = false;
            mCurrentIndex = intent.getExtras().getInt(Config.PLAYER_CURRENT_INDEX);
            songList = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra(Config.PLAYER_LIST);
            new PlayMusicTask().execute(mCurrentIndex);
        }

        return START_STICKY;
    }

    public static boolean checkSongsList() {
        return songList != null && songList.size() > 0;
    }

    public static class PlayMusicTask extends AsyncTask<Object, Void, String> {
        @Override
        protected void onPreExecute() {
            // Log.i(TAG, "Pre executing");
        }

        @Override
        protected String doInBackground(Object... params) {
            int songIndex = (Integer) params[0];
            if(checkSongsList()) {
                play(songIndex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // Log.i(TAG, "Post executing");
        }
    }

    /**
     * Once the song is ready to play, we can do some operations here, in this case send some updates to the UI.
     */
    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            // Log.i(TAG, "Completed");
            mPlayer.start();
            setupHandler();
            isCompleted = true;
        }
    };

    /**
     * This method is handled when there is an error in the mediaPlayer
     */
    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e(TAG, String.format("Error(%s%s)", what, extra));
            // showServerError();
            return false;
        }
    };

    /**
     * We set the percentage of the buffering being taken.
     */
    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            setBufferPercent(percent);
        }
    };

    /**
     * Once a song has finished playing, go to the next song
     */
    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer arg0) {
            // Log.i(TAG, "Playing next song");
            if (mPlayer != null && checkSongsList()) {
                next();
            }
        }
    };

    /**
     * These functions declarations, in the meantime, are just needed to avoid warnings.
     */
    private MediaPlayer.OnInfoListener mOnInfoListener = new MediaPlayer.OnInfoListener() {
        public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
            return false;
        }
    };

    private MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer arg0, int arg1, int arg2) {
        }
    };

    private static void updatePlayerInfo() {
        HashMap<String, String> songDetail = songList.get(mCurrentIndex);
        PlayerActivity.sSongId = Config.playerSongId = songDetail.get(Config.ID);
        PlayerActivity.sSongArtistName= Config.playerSongArtistName = songDetail.get(Config.ARTIST);
        PlayerActivity.sSongName  = Config.playerSongName = songDetail.get(Config.NAME);
        PlayerActivity.sSongMp3 = Config.playerSongMp3 = songDetail.get(Config.MP3);
        PlayerActivity.sSongDuration = Config.playerSongDuration = songDetail.get(Config.DURATION);
        PlayerActivity.sSongUrl = Config.playerSongUrl = songDetail.get(Config.URL);
        PlayerActivity.sSongDetailUrl= Config.playerSongDetailUrl = Config.SONG_DETAIL_URL + Config.playerSongId;

        if(songDetail.get(Config.IMAGE) != null) {
            PlayerActivity.sSongArtistImage = Config.playerSongArtistImage = songDetail.get(Config.IMAGE);
        }
    }

    /*private static void showServerError() {
        try {
            SongsFragment.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    if(!SongsFragment.mActivity.isFinishing()) {
                        Context context = SongsFragment.mActivity;
                        Utils.showAlertDialog(context, context.getString(R.string.no_server_title), context.getString(R.string.no_server_message));
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/

    /**
     * Go to the next song.
     */
    public static void next() {
        if (checkSongsList()) {
            int nextIndex;

            if(mCurrentIndex >= songList.size() - 1) {
                nextIndex = 0;
                mCurrentIndex = nextIndex;
            } else {
                if (isRepeat) {
                    nextIndex = mCurrentIndex;
                } else if (isShuffle) {
                    Random rand = new Random();
                    nextIndex = rand.nextInt(songList.size() - 1);
                    mCurrentIndex = nextIndex;
                } else {
                    nextIndex = mCurrentIndex + 1;
                    mCurrentIndex = nextIndex;
                }
            }

            // Log.d(TAG, "Next index: " + nextIndex);
            new PlayMusicTask().execute(nextIndex);
            updatePlayerInfo();
        }
    }

    /**
     * Go to the previous song.
     */
    public static void previous() {
        if (checkSongsList()) {
            int previousIndex;

            if(mCurrentIndex > songList.size() - 1) {
                previousIndex = 0;
                mCurrentIndex = previousIndex;
            } else {
                if (mCurrentIndex == 0) {
                    previousIndex = songList.size() - 1;
                    mCurrentIndex = previousIndex;
                } else {
                    previousIndex = mCurrentIndex - 1;
                    mCurrentIndex = previousIndex;
                }
            }

            // Log.d(TAG, "Previous index: " + previousIndex);
            new PlayMusicTask().execute(previousIndex);
            updatePlayerInfo();
        }
    }

    /**
     * Play a song, but before reset the MediaPlayer and set the right URL.
     *
     * @param songIndex Integer
     */
    private static void play(int songIndex) {
        try {
            if (mPlayer != null && checkSongsList()) {
                mPlayer.reset();
                mCurrentIndex = songIndex;
                String mp3File = songList.get(mCurrentIndex).get(Config.MP3);
                String mp3Final = mp3File.replaceAll(" ", "%20");
                // Log.d(TAG, "Current index: " + mCurrentIndex);
                // Log.d(TAG, "Songlist size: " + songList.size());
                mPlayer.setDataSource(mp3Final);
                mPlayer.prepare();
                isPlaying = true;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to use in the Pause/Play button
     */
    public static void playSong() {
        try {
            if (mPlayer != null) {
                if (isCompleted) {
                    isPlaying = true;
                    mPlayer.start();
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pauses a song only if it is playing.
     */
    public static void pause() {
        try {
            if (mPlayer != null) {
                if (mPlayer.isPlaying()) {
                    isPlaying = false;
                    mPlayer.pause();
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void setBufferPercent(int currentBufferPercent) {
        mBufferPercent = currentBufferPercent;
    }

    private int getBufferPercentage() {
        return mBufferPercent;
    }

    private void setupHandler() {
        mHandler.removeCallbacks(sendUpdatesToUI);
        mHandler.postDelayed(sendUpdatesToUI, 1000);
    }

    /**
     * Starts sending the data to update the player widgets.
     */
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            int duration = Integer.parseInt(songList.get(mCurrentIndex).get(Config.DURATION)); // check for IndexOutOfBoundsException
            mBroadcastIntent.putExtra("artistName", songList.get(mCurrentIndex).get(Config.ARTIST));
            mBroadcastIntent.putExtra("songName", songList.get(mCurrentIndex).get(Config.NAME));
            mBroadcastIntent.putExtra("artistImageName", songList.get(mCurrentIndex).get(Config.IMAGE));
            mBroadcastIntent.putExtra("currentDuration", mPlayer.getCurrentPosition());
            mBroadcastIntent.putExtra("totalDuration", duration);
            mBroadcastIntent.putExtra("bufferPercentProgress", getBufferPercentage());

            sendBroadcast(mBroadcastIntent);
            mHandler.postDelayed(this, 1000);
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSeekPos(intent);
        }
    };

    /**
     * Update seek position from Activity
     */
    private void updateSeekPos(Intent intent) {
        int seekPos = intent.getIntExtra("seekpos", 0);
        if (mPlayer.isPlaying()) {
            mHandler.removeCallbacks(sendUpdatesToUI);
            mPlayer.seekTo(seekPos);
            setupHandler();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Log.i(TAG, "Destroying Player service");
        Config.playerServiceStarted = false;
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
        }

        // Unregister seekbar receiver
        if (mBroadcastIsRegistered) {
            unregisterReceiver(broadcastReceiver);
            mBroadcastIsRegistered = false;
        }

        // Stop the seekbar handler from sending updates to UI
        mHandler.removeCallbacks(sendUpdatesToUI);
    }
}
