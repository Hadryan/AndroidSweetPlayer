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

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.streaming.sweetplayer.R;
import com.streaming.sweetplayer.adapter.PlaylistAdapter;
import com.streaming.sweetplayer.api.Config;
import com.streaming.sweetplayer.database.DataBaseHelper;
import com.streaming.sweetplayer.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistFragment extends Fragment {
    private ArrayList<HashMap<String, String>> playlistList = new ArrayList<HashMap<String, String>>();
    private DataBaseHelper mDataBase;
    private FragmentActivity mActivity;
    private ListView mListview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        View view = inflater.inflate(R.layout.songs_list, container, false);
        mActivity = this.getActivity();
        mDataBase = new DataBaseHelper(mActivity);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(Utils.isConnectedToInternet(mActivity)) {
            mListview = (ListView) mActivity.findViewById(R.id.list);
            mDataBase.selectAll();
            playlistList = mDataBase.playList;
            if(playlistList.size() == 0) {
                Utils.showUserMessage(mActivity, getString(R.string.playlist_empty));
            }
            getAdapter();
        } else {
            Utils.showAlertDialog(mActivity, getString(R.string.no_internet_title), getString(R.string.no_internet_message));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.gaSendView(mActivity, Config.PLAYLIST_TAB);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
    }

    private void getAdapter() {
        PlaylistAdapter adapter = new PlaylistAdapter(mActivity, playlistList, mDataBase);
        mListview.setAdapter(adapter);
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Utils.startPlayerService(mActivity, playlistList, position, Config.SONGS_ITEM);
            }
        });
    }
}