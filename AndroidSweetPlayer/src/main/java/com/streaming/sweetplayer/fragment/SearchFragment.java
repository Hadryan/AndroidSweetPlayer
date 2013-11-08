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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.streaming.sweetplayer.MainActivity;
import com.streaming.sweetplayer.R;
import com.streaming.sweetplayer.adapter.SongAdapter;
import com.streaming.sweetplayer.api.Config;
import com.streaming.sweetplayer.database.DataBaseHelper;
import com.streaming.sweetplayer.utils.JSONParser;
import com.streaming.sweetplayer.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


public class SearchFragment extends Fragment {
    private static ArrayList<HashMap<String, String>> mSearchList = new ArrayList<HashMap<String, String>>();
    private static DataBaseHelper mDataBase;
    private static FragmentActivity mActivity;
    private static ListView mListView;
    private static SongAdapter mSongAdapter;
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        View view = inflater.inflate(R.layout.songs_list, container, false);
        mActivity = this.getActivity();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDataBase = new DataBaseHelper(mActivity);
        mListView = (ListView) mActivity.findViewById(R.id.list);
        if(!Utils.isConnectedToInternet(mActivity)) {
            Utils.showAlertDialog(mActivity, getString(R.string.no_internet_title), getString(R.string.no_internet_message));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.gaSendView(mActivity, Config.SEARCH_TAB);
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

    /**
     * Clear the current listview.
     */
    private void clearListView() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mSearchList.clear();
                mListView.setAdapter(null);
                if(mSongAdapter != null) {
                    mSongAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void searchSongs(String searchInputText) {
        if (searchInputText.equals("") || searchInputText.length() == 0) {
            Utils.showUserMessage(mActivity.getApplicationContext(), mActivity.getString(R.string.search_text_empty));
            return;
        }

        Utils.gaSendEvent(mActivity, "search", searchInputText);
        String encodedSearchText = "";
        try {
            encodedSearchText = URLEncoder.encode(searchInputText, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        clearListView();
        String searchUrl = Config.SEARCH_SONGS_URL + encodedSearchText;
        if(mSearchList != null) {
            MainActivity.currentTab = Config.SEARCH_TAB;
            new GetSongsTask().execute(searchUrl);
        }
    }

    private class GetSongsTask extends AsyncTask<Object, Void, String> {
        private String searchUrl;

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
            searchUrl = (String) params[0];
            getSongsList(searchUrl);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            setSongAdapter(false);
        }
    }

    private void getSongsList(String url) {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray;

        try {
            JSONObject json = jsonParser.getJSONFromUrl(url);
            if(json != null) {
                jsonArray = json.getJSONArray(Config.SONGS_ITEM);
                int array_length = jsonArray.length();
                if(array_length > 0) {
                    for(int i = 0; i < array_length; i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        map.put(Config.ID, jsonObject.getString(Config.ID));
                        map.put(Config.ARTIST, jsonObject.getString(Config.ARTIST));
                        map.put(Config.NAME, jsonObject.getString(Config.SONG));
                        map.put(Config.MP3, jsonObject.getString(Config.MP3));
                        map.put(Config.DURATION, jsonObject.getString(Config.DURATION));
                        map.put(Config.URL, jsonObject.getString(Config.URL));
                        map.put(Config.IMAGE, jsonObject.getString(Config.IMAGE));
                        mSearchList.add(map);
                    }
                } else {
                    // Showing a message should be done in the UI thread.
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Utils.showUserMessage(mActivity.getApplicationContext(), mActivity.getString(R.string.search_empty));
                        }
                    });
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setSongAdapter(final boolean isMultiAdding) {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mSongAdapter = new SongAdapter(mActivity, mSearchList, Config.SEARCH_TAB, isMultiAdding);
                mListView.setAdapter(mSongAdapter);
                mSongAdapter.notifyDataSetChanged();
                if(!mSearchList.isEmpty()) {
                    MainActivity.setAddVisibility(true);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Utils.startPlayerService(mActivity, mSearchList, position, Config.SONGS_ITEM);
            }
        });
    }

    public static void addToPlaylist() {
        Utils.addMultipleSongsToPlaylist(mActivity, mDataBase, mSongAdapter, mSearchList, false);
    }
}
