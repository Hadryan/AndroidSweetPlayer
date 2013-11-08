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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.streaming.sweetplayer.R;
import com.streaming.sweetplayer.api.Config;
import com.streaming.sweetplayer.database.DataBaseHelper;
import com.streaming.sweetplayer.utils.ImageLoader;
import com.streaming.sweetplayer.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This adapter class GETS the data from the provided songs URL. and SET the data info into the Widgets.
 */
public class PlaylistAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> mDataArrayList;
    private DataBaseHelper mDataBase;
    private FragmentActivity mActivity;
    private ImageLoader mImageLoader;
    private LayoutInflater mInflater;
    private ListView mPlaylistListView;

    /**
     * Setup the data needed for custom adapter to be used by the listview.
     *
     * @param activity  Activity
     * @param arrayList ArrayList<HashMap<String, String>>
     */
    public PlaylistAdapter(FragmentActivity activity, ArrayList<HashMap<String, String>> arrayList, DataBaseHelper dataBase) {
        mActivity = activity;
        mDataArrayList = arrayList;
        mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDataBase = dataBase;
        mImageLoader = new ImageLoader(mActivity.getApplicationContext());
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

    static class ViewHolder {
        ImageView artistImageView;
        TextView songNameTextView;
        TextView artistNameTextView;
        ImageButton deleteButton;
    }

    /**
     * Get the data of an element in the current listview.
     *
     * @param position Integer
     * @param convertView  View
     * @param parent   ViewGroup
     */
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.playlist_song_item, null);
            viewHolder = new ViewHolder();
            viewHolder.artistImageView = (ImageView) convertView.findViewById(R.id.artist_image_thumbnail);
            viewHolder.songNameTextView = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.artistNameTextView = (TextView) convertView.findViewById(android.R.id.text2);
            viewHolder.deleteButton = (ImageButton) convertView.findViewById(R.id.delete_button);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> song = mDataArrayList.get(position);
        String songName = song.get(Config.NAME);
        String songArtistName = song.get(Config.ARTIST);
        String songArtistImage = song.get(Config.IMAGE);
        final String songId = song.get(Config.ID);
        final String artistSongName = songName + "-" + songArtistName;

        mImageLoader.DisplayImage(songArtistImage, viewHolder.artistImageView);
        viewHolder.songNameTextView.setText(songName);
        viewHolder.artistNameTextView.setText(songArtistName);
        viewHolder.deleteButton.setVisibility(View.VISIBLE);
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Utils.gaSendEvent(mActivity, "Playlist deleted", artistSongName);
                mDataBase.delete(songId, position);
                mDataArrayList.remove(position);
                Utils.showUserMessage(mActivity.getApplicationContext(), mActivity.getString(R.string.playlist_deleted));
                final PlaylistAdapter playlistAdapter = new PlaylistAdapter(mActivity, mDataArrayList, mDataBase);
                mPlaylistListView = (ListView) mActivity.findViewById(R.id.list);
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        mPlaylistListView.setAdapter(playlistAdapter);
                        playlistAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        return convertView;
    }
}
