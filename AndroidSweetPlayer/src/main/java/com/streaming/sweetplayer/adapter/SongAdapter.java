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

package com.streaming.sweetplayer.adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.streaming.sweetplayer.R;
import com.streaming.sweetplayer.api.Config;
import com.streaming.sweetplayer.utils.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This adapter class GETS the data from the provided songs URL. and SET the data info into the Widgets.
 */
public class SongAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> mDataArrayList;
    private boolean mIsMultiAdding;
    private FragmentActivity mActivity;
    private ImageLoader mImageLoader;
    private LayoutInflater mInflater;
    private String mTab;

    public boolean boolean_values[];

    /**
     * Setup the data needed for custom adapter to be used by the listview.
     *
     * @param activity  Activity
     * @param arrayList ArrayList<HashMap<String, String>>
     */
    public SongAdapter(FragmentActivity activity, ArrayList<HashMap<String, String>> arrayList, String tab, boolean multiAdding) {
        mActivity = activity;
        mDataArrayList = arrayList;
        mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTab = tab;
        mImageLoader = new ImageLoader(mActivity.getApplicationContext());
        int arraySize = getCount();
        boolean_values = new boolean[arraySize];
        mIsMultiAdding = multiAdding;
    }

    /**
     * Returns the number of items in the list.
     */
    public int getCount() {
        return mDataArrayList.size();
    }

    /**
     * Returns an object in the specified position
     *
     * @param position Integer
     */
    public Object getItem(int position) {
        return position;
    }

    /**
     * Returns a long id in the specified position
     *
     * @param position Integer
     */
    public long getItemId(int position) {
        return position;
    }

    public void setSelected(Boolean selected, int position) {
        boolean_values[position] = selected;
    }

    static class ViewHolder {
        TextView songNameTextView;
        TextView artistNameTextView;
        ImageView artistImageView;
        CheckBox songCheckBox;
    }

    /**
     * Get the data of an element in the current listview.
     *
     * @param position Integer
     * @param convertView     View
     * @param parent   ViewGroup
     */
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.artist_song_item, null);
            viewHolder = new ViewHolder();
            if(mTab.equals(Config.TOP_TAB) || mTab.equals(Config.SEARCH_TAB)) {
                convertView = mInflater.inflate(R.layout.song_item, null);
                viewHolder.artistNameTextView = (TextView) convertView.findViewById(android.R.id.text2);
                viewHolder.artistImageView = (ImageView) convertView.findViewById(R.id.song_artist_image);
            }
            viewHolder.songNameTextView = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.songCheckBox = (CheckBox) convertView.findViewById(R.id.song_checkbox);
            if(mIsMultiAdding) {
                viewHolder.songCheckBox.setVisibility(View.VISIBLE);
            } else {
                viewHolder.songCheckBox.setVisibility(View.GONE);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> song = mDataArrayList.get(position);
        String songName = song.get(Config.NAME);
        viewHolder.songNameTextView.setText(songName);

        if(mTab.equals(Config.TOP_TAB) || mTab.equals(Config.SEARCH_TAB)) {
            String artistName = song.get(Config.ARTIST);
            String songArtistImage = song.get(Config.IMAGE);
            viewHolder.artistNameTextView.setText(artistName);
            mImageLoader.DisplayImage(songArtistImage, viewHolder.artistImageView);
        }

        //viewHolder.songCheckBox.setTag(position);
        viewHolder.songCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    setSelected(true, position);
                } else {
                    setSelected(false, position);
                }
            }
        });

        if (position < boolean_values.length) {
            viewHolder.songCheckBox.setChecked(boolean_values[position]);
        }
        return convertView;
    }
}
