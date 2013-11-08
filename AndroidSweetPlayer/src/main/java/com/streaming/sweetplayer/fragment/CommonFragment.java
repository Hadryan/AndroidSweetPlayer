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
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.streaming.sweetplayer.ArtistActivity;
import com.streaming.sweetplayer.R;
import com.streaming.sweetplayer.adapter.CommonAdapter;
import com.streaming.sweetplayer.api.Config;
import com.streaming.sweetplayer.utils.ImageLoader;
import com.streaming.sweetplayer.utils.JSONParser;
import com.streaming.sweetplayer.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class CommonFragment extends Fragment {
    private static final String TAG = "Common Fragment";
    private ArrayAdapter<CharSequence> mSpinnerAdapter;
    private ArrayList<HashMap<String, String>> mArtistsList = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> mGenresList = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> mGenreArtistsList = new ArrayList<HashMap<String, String>>();
    private Button mSearchButton;
    private EditText mSearchEditText;
    private FragmentActivity mActivity;
    private GridView mGridview;
    private ImageLoader mImageLoader;
    private ImageView mGenreImageView;
    private int mImageToLoad;
    private ProgressDialog mProgressDialog;
    private Spinner mGenreSpinner;
    private TextView mGenreNameTextView;
    private String mCurrentTab;
    private String mDetailURL;
    private String mJsonItem;
    private String mJsonName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        View view = inflater.inflate(R.layout.common_list, container, false);
        mActivity = this.getActivity();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getRightImage();
        if(Utils.isConnectedToInternet(mActivity)) {
            mCurrentTab = this.getTag();
            mImageLoader = new ImageLoader(mActivity.getApplicationContext());
            mImageLoader.setResize(true);
            mGridview = (GridView) mActivity.findViewById(R.id.gridview);
            mGenreImageView = (ImageView) mActivity.findViewById(R.id.genre_image);
            mGenreImageView.setVisibility(View.GONE);
            mGenreNameTextView = (TextView) mActivity.findViewById(R.id.genre_name);
            mGenreNameTextView.setVisibility(View.GONE);
            mSearchButton = (Button) mActivity.findViewById(R.id.searchButton);
            mSearchEditText = (EditText) mActivity.findViewById(R.id.searchEditText);
            mSearchEditText.setText("");
            mSearchEditText.setHint(R.string.search_artist_hint);
            setupGenresAndArtists();
            setupArtistSearch();
            setupSpinner();
        } else {
            Utils.showAlertDialog(mActivity, getString(R.string.no_internet_title), getString(R.string.no_internet_message));
        }
    }

    private void getRightImage() {
        if (Config.deviceDensityDpi <= 120) {
            mImageToLoad = 1;
        } else {
            mImageToLoad = 3;
        }
    }

    private void setupGenresAndArtists() {
        if(mCurrentTab.equals(Config.ARTISTS_TAB)) {
            mDetailURL = Config.ARTISTS_DETAIL_URL;
            mJsonItem = Config.ARTISTS_ITEM;
            mJsonName = Config.ARTIST;
            mSearchButton.setVisibility(View.VISIBLE);
            mSearchEditText.setVisibility(View.VISIBLE);
            if (mArtistsList.size() == 0) {
                new GetGenreArtistsTask().execute(mArtistsList, Config.ARTISTS_URL);
            } else {
                setCommonAdapter(mArtistsList);
            }
        } else {
            mDetailURL = Config.GENRE_SONGS_URL;
            mJsonItem = Config.GENRES_ITEM;
            mJsonName = Config.GENRE;
            mSearchButton.setVisibility(View.GONE);
            mSearchEditText.setVisibility(View.GONE);
            if (mGenresList.size() == 0) {
                new GetGenreArtistsTask().execute(mGenresList, Config.GENRES_URL);
            } else {
                setCommonAdapter(mGenresList);
            }
        }
    }

    private void setupArtistSearch() {
        mSearchButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchArtists();
            }
        });
    }

    public void searchArtists() {
        String searchInputText = mSearchEditText.getText().toString();
        if (searchInputText.equals("") || searchInputText.length() == 0) {
            Utils.showUserMessage(mActivity.getApplicationContext(), mActivity.getString(R.string.search_text_empty));
            return;
        }

        Utils.gaSendEvent(mActivity, "Search Artist", searchInputText);
        String encodedSearchText = "";
        try {
            encodedSearchText = URLEncoder.encode(searchInputText, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        clearListView();
        clearGenreArtistsList();
        String searchUrl = Config.SEARCH_ARTISTS_URL + encodedSearchText;
        if(mGenreArtistsList != null) {
            new GetGenreArtistsTask().execute(mGenreArtistsList, searchUrl);
        }
    }

    private void setupSpinner() {
        mSpinnerAdapter = ArrayAdapter.createFromResource(mActivity, R.array.genre_detail_array, android.R.layout.simple_spinner_item);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenreSpinner = (Spinner) mActivity.findViewById(R.id.genre_spinner);
        mGenreSpinner.setAdapter(mSpinnerAdapter);
        mGenreSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if(pos == 1) {
                    clearListView();
                    mGenreNameTextView.setVisibility(View.GONE);
                    mGenreImageView.setVisibility(View.GONE);
                    mGenreSpinner.setVisibility(View.GONE);
                    setCommonAdapter(mGenresList);
                    mCurrentTab = Config.GENRES_TAB;
                } else if (pos == 2) {
                    clearListView();
                    Utils.sortArrayList(mGenreArtistsList);
                    setCommonAdapter(mGenreArtistsList);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.gaSendView(mActivity, mCurrentTab);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void clearListView() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mGridview.setAdapter(null);
            }
        });
    }

    private void clearGenreArtistsList() {
        mGenreArtistsList.clear();
    }

    private class GetGenreArtistsTask extends AsyncTask<Object, Void, String> {
        private ArrayList<HashMap<String, String>> itemsList = null;
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
            itemsList  = (ArrayList<HashMap<String, String>>) params[0];
            detailUrl = (String) params[1];
            if (detailUrl != null) {
                getGenreArtistsList(itemsList, detailUrl);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            if (detailUrl != null) {
                setCommonAdapter(itemsList);
            }
        }
    }

    private void getGenreArtistsList(ArrayList<HashMap<String, String>> itemsList, String url) {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray;

        try {
            JSONObject json = jsonParser.getJSONFromUrl(url);

            if(json != null) {
                jsonArray = json.getJSONArray(mJsonItem);
                int array_length = jsonArray.length();
                if(array_length > 0) {
                    for(int i = 0; i < array_length; i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        map.put(Config.ID, jsonObject.getString(Config.ID));
                        map.put(Config.IMAGE, jsonObject.getString(Config.IMAGE));
                        map.put(Config.NAME, jsonObject.getString(mJsonName));
                        map.put(Config.URL, jsonObject.getString(Config.URL));
                        itemsList.add(map);
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

    private void setCommonAdapter(final ArrayList<HashMap<String, String>> arrayList) {
        final CommonAdapter adapter = new CommonAdapter(mActivity, arrayList);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mGridview.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });

        mGridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                HashMap<String, String> itemsDataList = arrayList.get(position);
                if(mCurrentTab.equals(Config.ARTISTS_TAB)) {
                    setIntent(itemsDataList);
                } else {
                    clearListView();
                    clearGenreArtistsList();
                    String genreId = itemsDataList.get(Config.ID);
                    String genreName = itemsDataList.get(Config.NAME);
                    String genreImage = itemsDataList.get(Config.IMAGE);
                    String genreFinalImage = Utils.replaceImage(genreImage, mImageToLoad);
                    String genreArtistsUrl = Config.GENRE_ARTISTS_URL + genreId;

                    mGenreNameTextView.setVisibility(View.VISIBLE);
                    mGenreNameTextView.setText(genreName);
                    mGenreImageView.setVisibility(View.VISIBLE);
                    mImageLoader.DisplayImage(genreFinalImage, mGenreImageView);
                    mGenreSpinner.setVisibility(View.VISIBLE);
                    mGenreSpinner.setSelection(0);
                    mCurrentTab = Config.ARTISTS_TAB;
                    mDetailURL = Config.ARTISTS_DETAIL_URL;
                    mJsonItem = Config.ARTISTS_ITEM;
                    mJsonName = Config.ARTIST;
                    new GetGenreArtistsTask().execute(mGenreArtistsList, genreArtistsUrl);
                }
            }
        });
    }

    private void setIntent(HashMap<String, String> itemsDataList) {
        String detailImage = Utils.replaceImage(itemsDataList.get(Config.IMAGE), mImageToLoad);

        Intent intent = new Intent(mActivity.getApplicationContext(), ArtistActivity.class);
        intent.putExtra(Config.ID, itemsDataList.get(Config.ID));
        intent.putExtra(Config.NAME, itemsDataList.get(Config.NAME));
        intent.putExtra(Config.URL, itemsDataList.get(Config.URL));
        intent.putExtra(Config.DETAIL_URL, mDetailURL);
        intent.putExtra(Config.IMAGE, detailImage);
        intent.putExtra(Config.DETAIL_TAB, mCurrentTab);
        startActivity(intent);
    }
}