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

package com.streaming.sweetplayer.fragment;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.streaming.sweetplayer.R;
import com.streaming.sweetplayer.adapter.SongAdapter;
import com.streaming.sweetplayer.api.Config;
import com.streaming.sweetplayer.database.DataBaseHelper;
import com.streaming.sweetplayer.utils.ImageLoader;
import com.streaming.sweetplayer.utils.JSONParser;
import com.streaming.sweetplayer.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ArtistFragment extends Fragment implements OnItemSelectedListener {
    private static final String TAG = "Artist Fragment";
    private ArrayList<HashMap<String, String>> mAlbumsList = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> mAlbumSongsList = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> mArtistSongsList = new ArrayList<HashMap<String, String>>();
    private static ArrayList<HashMap<String, String>> mSongsList;
    private static DataBaseHelper mDataBase;
    private static FragmentActivity mActivity;
    private static ListView mListView;
    private static SongAdapter mSongAdapter;
    private boolean isSongsView = true;
    private boolean isTopView = true;
    private ArrayAdapter<CharSequence> mSpinnerAdapter;
    private LinearLayout mScrollLayout;
    private ImageLoader mImageLoader;
    private ImageView mArtistImageView;
    private ProgressDialog mProgressDialog;
    private TextView mArtistNameTextView;
    private TextView mAlbumNameTextView;
    private Spinner mSpinner;
    private String mJsonItem;
    private String mImageForDB;

    public static String artistImage;
    public String detailTab;
    public String artistName;
    public String artistSongsUrl;
    public String artistAlbumsUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        View view = inflater.inflate(R.layout.artists_list, container, false);
        mActivity = this.getActivity();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDataBase = new DataBaseHelper(mActivity);
        if(Utils.isConnectedToInternet(mActivity)) {
            mListView = (ListView) mActivity.findViewById(R.id.detail_list);
            mScrollLayout = (LinearLayout) mActivity.findViewById(R.id.scroll_layout);
            mSpinnerAdapter = ArrayAdapter.createFromResource(mActivity, R.array.artist_detail_array, android.R.layout.simple_spinner_item);
            mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner = (Spinner) mActivity.findViewById(R.id.detail_spinner);
            mSpinner.setAdapter(mSpinnerAdapter);
            mSpinner.setOnItemSelectedListener(this);
            mArtistNameTextView = (TextView) mActivity.findViewById(R.id.detail_artist_name);
            mArtistNameTextView.setText(artistName);
            mAlbumNameTextView = (TextView) mActivity.findViewById(R.id.detail_album_name);
            mImageLoader = new ImageLoader(mActivity.getApplicationContext());
            mImageLoader.setResize(true);
            mArtistImageView = (ImageView) mActivity.findViewById(R.id.detail_artist_image);
            mArtistImageView.setVisibility(View.VISIBLE);
            mImageLoader.DisplayImage(artistImage, mArtistImageView);
            mJsonItem = Config.SONGS_ITEM;
            mImageForDB = Utils.replaceImage(artistImage, 1);
            // Log.d(TAG, "Artist: " + artistSongsUrl + "-" + artistAlbumsUrl + "-" + artistImage + "-" + artistName);
            clearAllData();
            new GetSongsTask().execute(mArtistSongsList, artistSongsUrl, true);
        } else {
            Utils.showAlertDialog(mActivity, getString(R.string.no_internet_title), getString(R.string.no_internet_message));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.gaSendView(mActivity, Config.DETAIL_TAB);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();
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

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if(pos == 0) {
            mAlbumNameTextView.setVisibility(View.GONE);
            clearListView();
            mSongsList = mArtistSongsList;
            setSongAdapter(false);
            isTopView = true;
        } else if(pos == 1) {
            clearListView();
            if(isTopView) {
                Utils.sortArrayList(mArtistSongsList);
                mSongsList = mArtistSongsList;
            } else {
                Utils.sortArrayList(mAlbumSongsList);
                mSongsList = mAlbumSongsList;
            }
            setSongAdapter(false);
        } else {
            if(mAlbumsList.size() == 0) {
                mJsonItem = Config.ALBUMS_ITEM;
                mScrollLayout.setVisibility(View.VISIBLE);
                new GetSongsTask().execute(mAlbumsList, artistAlbumsUrl, false);
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void clearAllData() {
        clearListView();
        clearTopList();
        clearAlbumsList();
        clearAlbumSongsList();
        clearSongsList();
    }

    /**
     * Clear the current listview.
     */
    private void clearListView() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mListView.setAdapter(null);
                if(mSongAdapter != null) {
                    mSongAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void clearTopList() {
        mArtistSongsList.clear();
    }

    private void clearAlbumsList() {
        mAlbumsList.clear();
    }

    private void clearAlbumSongsList() {
        mAlbumSongsList.clear();
    }

    private void clearSongsList() {
        if(mSongsList != null) {
            mSongsList.clear();
        }
    }

    private class GetSongsTask extends AsyncTask<Object, Void, String> {
        private ArrayList<HashMap<String, String>> detailList = null;
        private String detailUrl;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage(getString(R.string.processing_text));
            mProgressDialog.show();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected String doInBackground(Object...params) {
            detailList = (ArrayList<HashMap<String, String>>) params[0];
            detailUrl = (String) params[1];
            isSongsView = (Boolean) params[2];

            if (detailUrl != null && detailList != null) {
                getSongsList(detailList, detailUrl);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            if (detailUrl != null && detailList != null) {
                if(isSongsView) {
                    mSongsList = detailList;
                    setSongAdapter(false);
                } else {
                    setAlbums();
                }
            }
        }
    }

    private void getSongsList(ArrayList<HashMap<String, String>> parseList, String url) {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray;

        try {
            JSONObject json = jsonParser.getJSONFromUrl(url);

            if(json != null) {
                jsonArray = json.getJSONArray(mJsonItem);
                int array_length = jsonArray.length();

                if(isSongsView) {
                    for(int i = 0; i < array_length; i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        map.put(Config.ID, jsonObject.getString(Config.ID));
                        map.put(Config.ARTIST, jsonObject.getString(Config.ARTIST));
                        map.put(Config.NAME, jsonObject.getString(Config.SONG));
                        map.put(Config.MP3, jsonObject.getString(Config.MP3));
                        map.put(Config.DURATION, jsonObject.getString(Config.DURATION));
                        map.put(Config.URL, jsonObject.getString(Config.URL));
                        map.put(Config.IMAGE, mImageForDB);
                        parseList.add(map);
                    }
                } else {
                    for(int i = 0; i < array_length; i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        map.put(Config.ID, jsonObject.getString(Config.ID));
                        map.put(Config.ALBUM, jsonObject.getString(Config.ALBUM));
                        map.put(Config.SONGS_ITEM, jsonObject.getString(Config.SONGS_ITEM));
                        map.put(Config.IMAGE, jsonObject.getString(Config.IMAGE));
                        map.put(Config.URL, jsonObject.getString(Config.URL));
                        parseList.add(map);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setSongAdapter(final boolean isMultiAdding) {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mSongAdapter = new SongAdapter(mActivity, mSongsList, Config.DETAIL_TAB, isMultiAdding);
                mListView.setAdapter(mSongAdapter);
                mSongAdapter.notifyDataSetChanged();
            }
        });

        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Utils.startPlayerService(mActivity, mSongsList, position, artistImage);
            }
        });
    }

    public static void addToPlaylist() {
        Utils.addMultipleSongsToPlaylist(mActivity, mDataBase, mSongAdapter, mSongsList, true);
    }

    private void setAlbums() {
        int albumsSize = mAlbumsList.size();
        mImageLoader.setResize(false);

        for(int i = 0; i < albumsSize; i++) {
            HashMap<String, String> albums = mAlbumsList.get(i);
            String albumId = albums.get(Config.ID);
            String albumName = albums.get(Config.ALBUM);
            String albumImage = albums.get(Config.IMAGE);
            new GetAlbumImageTask().execute(albumImage, albumId, albumName);
        }
    }

    private class GetAlbumImageTask extends AsyncTask<Object, Void, String> {
        Bitmap albumBitmap;
        ImageView albumImageView;
        String albumId;
        String albumName;

        @Override
        protected void onPreExecute() {
            albumImageView = new ImageView(mActivity.getApplicationContext());
        }

        @SuppressWarnings("unchecked")
        @Override
        protected String doInBackground(Object...params) {
            String image = (String) params[0];
            albumId = (String) params[1];
            albumName = (String) params[2];
            albumBitmap = mImageLoader.getImageBitmap(image);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(Config.deviceDensityDpi <= 120 && !mAlbumsList.isEmpty()) {
                mArtistImageView.setVisibility(View.GONE);
            }
            albumImageView.setLayoutParams(new LayoutParams(90, 90));
            albumImageView.setPadding(2, 2, 10, 2);
            albumImageView.setImageBitmap(albumBitmap);
            albumImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mJsonItem = Config.SONGS_ITEM;
                    mAlbumNameTextView.setVisibility(View.VISIBLE);
                    mAlbumNameTextView.setText(albumName);
                    clearListView();
                    clearAlbumSongsList();
                    String albumSongsUrl = Config.ALBUMS_DETAIL_URL + albumId;
                    new GetSongsTask().execute(mAlbumSongsList, albumSongsUrl, true);
                    isTopView = false;
                    mSpinner.setSelection(2);
                }
            });
            mScrollLayout.addView(albumImageView);
        }
    }
}
