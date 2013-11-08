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

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.streaming.sweetplayer.api.Config;
import com.streaming.sweetplayer.fragment.ArtistFragment;
import com.streaming.sweetplayer.utils.Utils;

/**
 * This FragmentActivity (Interface) is responsible to SHOW the detail of an element (Genre detail, Artist detail).
 * For example: Artists -> His/Her Songs.
 */
public class ArtistActivity extends ActionBarActivity {
    private ShareActionProvider mShareActionProvider;
    private String mDetailName;
    private String mUrl;
    private static MenuItem mAddItem;
    private static MenuItem mCancelItem;
    private static MenuItem mMultiAddItem;
    private static MenuItem mPlayerItem;
    private static MenuItem mShareItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        Intent in = getIntent();
        mDetailName = in.getStringExtra(Config.NAME);
        mUrl = in.getStringExtra(Config.URL);

        ArtistFragment artistFragment = new ArtistFragment();
        artistFragment.artistName = in.getStringExtra(Config.NAME);
        artistFragment.artistImage = in.getStringExtra(Config.IMAGE);
        artistFragment.artistSongsUrl = in.getStringExtra(Config.DETAIL_URL) + in.getStringExtra(Config.ID);
        artistFragment.artistAlbumsUrl = Config.ALBUMS_URL + in.getStringExtra(Config.ID);
        artistFragment.detailTab = in.getStringExtra(Config.DETAIL_TAB);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.attach(artistFragment);
        ft.add(R.id.realtabcontent, artistFragment);
        ft.commit();
        this.getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.multi_add_menu, menu);
        inflater.inflate(R.menu.player_menu, menu);
        inflater.inflate(R.menu.add_menu, menu);
        inflater.inflate(R.menu.share_menu, menu);

        mPlayerItem = menu.findItem(R.id.item_player);
        if (validateMenuItem(mPlayerItem)) {
            mPlayerItem.setVisible(true);
        }

        mAddItem = menu.findItem(R.id.item_add_to_playlist);
        if (validateMenuItem(mAddItem)) {
            mAddItem.setVisible(true);
        }

        mShareItem = menu.findItem(R.id.item_share);
        if (validateMenuItem(mShareItem)) {
            mShareItem.setVisible(true);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareItem);
            mShareActionProvider.setShareIntent(getDefaultIntent());
            //mShareActionProvider.setShareHistoryFileName(null);
        }

        mMultiAddItem = menu.findItem(R.id.item_multi_add);
        if(validateMenuItem(mMultiAddItem)) {
            mMultiAddItem.setVisible(false);
        }

        mCancelItem = menu.findItem(R.id.item_cancel);
        if(validateMenuItem(mCancelItem)) {
            mCancelItem.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_player:
                if(!Config.playerServiceStarted) {
                    Utils.showUserMessage(getApplicationContext(), getString(R.string.not_song));
                } else {
                    try {
                        Intent intent = new Intent(ArtistActivity.this, PlayerActivity.class);
                        intent.setAction("see_player");
                        startActivity(intent);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            case R.id.item_add_to_playlist:
                ArtistFragment.setSongAdapter(true);
                setMenuItemsVisibility(false, true);
                return true;
            case R.id.item_cancel:
                ArtistFragment.setSongAdapter(false);
                setMenuItemsVisibility(true, false);
                return true;
            case R.id.item_multi_add:
                ArtistFragment.addToPlaylist();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static boolean validateMenuItem(MenuItem item) {
        return item != null;
    }

    public static void setMenuItemsVisibility(boolean visible, boolean visible_2) {
        if(validateMenuItem(mPlayerItem)) {
            mPlayerItem.setVisible(visible);
        }
        if(validateMenuItem(mAddItem)) {
            mAddItem.setVisible(visible);
        }
        if(validateMenuItem(mShareItem)) {
            mShareItem.setVisible(visible);
        }
        if(validateMenuItem(mCancelItem)) {
            mCancelItem.setVisible(visible_2);
        }
        if(validateMenuItem(mMultiAddItem)) {
            mMultiAddItem.setVisible(visible_2);
        }
    }

    public static void setAddVisibility(boolean visibility) {
        if(validateMenuItem(mAddItem)) {
            mAddItem.setVisible(visibility);
        }
    }

    private Intent getDefaultIntent() {
        String urlToShare = Config.DOMAIN + mUrl;
        String sharingText = getString(R.string.sharing_text) + " " + urlToShare  + " " + getString(R.string.sharing_android_app) + " " + Config.GOOGLE_PLAY_LINK;

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, mDetailName);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        return sharingIntent;
    }
}
