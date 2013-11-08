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
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.streaming.sweetplayer.api.Config;
import com.streaming.sweetplayer.fragment.CommonFragment;
import com.streaming.sweetplayer.fragment.PlaylistFragment;
import com.streaming.sweetplayer.fragment.SearchFragment;
import com.streaming.sweetplayer.fragment.TopFragment;
import com.streaming.sweetplayer.service.PlayerService;
import com.streaming.sweetplayer.utils.MyTabHost;
import com.streaming.sweetplayer.utils.Utils;

import java.util.HashMap;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "Main";
    private static ActionBar mActionBar;
    private static MenuItem mAboutItem;
    private static MenuItem mAddItem;
    private static MenuItem mCancelItem;
    private static MenuItem mMultiAddItem;
    private static MenuItem mPlayerItem;
    private static MenuItem mPreferencesItem;
    private static MenuItem mSearchItem;
    private FragmentActivity mActivity;
    private MyTabHost mTabHost;
    private TabManager mTabManager;
    public static String currentTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        handleIntent(getIntent());
        mActionBar = getSupportActionBar();
        mActivity = this;
        getDensityDpi(mActivity);
        mTabHost = (MyTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);
        mTabManager.addTab(mTabHost.newTabSpec(Config.GENRES_TAB).setIndicator(mActivity.getString(R.string.genres)),
                CommonFragment.class, null);
        mTabManager.addTab(mTabHost.newTabSpec(Config.ARTISTS_TAB).setIndicator(mActivity.getString(R.string.artists)),
                CommonFragment.class, null);
        mTabManager.addTab(mTabHost.newTabSpec(Config.TOP_TAB).setIndicator(mActivity.getString(R.string.top)),
                TopFragment.class, null);
        mTabManager.addTab(mTabHost.newTabSpec(Config.PLAYLIST_TAB).setIndicator(mActivity.getString(R.string.playlist)),
                PlaylistFragment.class, null);
        adjustTabs();

        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.multi_add_menu, menu);
        inflater.inflate(R.menu.player_menu, menu);
        inflater.inflate(R.menu.add_menu, menu);
        inflater.inflate(R.menu.general_options_menu, menu);

        mPlayerItem = menu.findItem(R.id.item_player);
        if (validateMenuItem(mPlayerItem)) {
            mPlayerItem.setVisible(true);
        }

        mSearchItem = menu.findItem(R.id.item_search);
        if (validateMenuItem(mSearchItem)) {
            mSearchItem.setVisible(true);
        }

        mPreferencesItem = menu.findItem(R.id.item_preferences);
        if(validateMenuItem(mPreferencesItem)) {
            mPreferencesItem.setVisible(true);
        }

        mAboutItem = menu.findItem(R.id.item_about);
        if(validateMenuItem(mAboutItem)) {
            mAboutItem.setVisible(true);
        }

        mMultiAddItem = menu.findItem(R.id.item_multi_add);
        if(validateMenuItem(mMultiAddItem)) {
            mMultiAddItem.setVisible(false);
        }

        mCancelItem = menu.findItem(R.id.item_cancel);
        if(validateMenuItem(mCancelItem)) {
            mCancelItem.setVisible(false);
        }

        mAddItem = menu.findItem(R.id.item_add_to_playlist);
        if (validateMenuItem(mAddItem)) {
            mAddItem.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_player:
                if(!Config.playerServiceStarted) {
                    Utils.showUserMessage(mActivity.getApplicationContext(), getString(R.string.not_song));
                } else {
                    try {
                        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                        intent.setAction("see_player");
                        startActivity(intent);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            case R.id.item_about:
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.item_search:
                onSearchRequested();
                return true;
            case R.id.item_add_to_playlist:
                // Log.d(TAG, "current tab: " + currentTab);
                if(currentTab.equals(Config.TOP_TAB)) {
                    TopFragment.setSongAdapter(true);
                } else {
                    SearchFragment.setSongAdapter(true);
                }
                setMenuItemsVisibility(false, true);
                mActionBar.setDisplayShowTitleEnabled(false);
                return true;
            case R.id.item_cancel:
                // Log.d(TAG, "current tab: " + currentTab);
                if(currentTab.equals(Config.TOP_TAB)) {
                    TopFragment.setSongAdapter(false);
                } else {
                    SearchFragment.setSongAdapter(false);
                }
                setMenuItemsVisibility(true, false);
                mActionBar.setDisplayShowTitleEnabled(true);
                return true;
            case R.id.item_multi_add:
                if(currentTab.equals(Config.TOP_TAB)) {
                    TopFragment.addToPlaylist();
                } else {
                    SearchFragment.addToPlaylist();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Log.i(TAG, "Destroying main");
        if (Config.serviceIntent != null && !PlayerService.isPlaying) {
            stopService(Config.serviceIntent);
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
        if(validateMenuItem(mSearchItem)) {
            mSearchItem.setVisible(visible);
        }
        if(validateMenuItem(mPreferencesItem)) {
            mPreferencesItem.setVisible(visible);
        }
        if(validateMenuItem(mAboutItem)) {
            mAboutItem.setVisible(visible);
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

    /**
     * For Android fragmentation, it's so important to know the device details like width, height and densityDpi.
     */
    private void getDensityDpi(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Config.deviceDensityDpi = metrics.densityDpi;
        Config.deviceHeight = metrics.heightPixels;
        Config.deviceWidth = metrics.widthPixels;
    }

    private void adjustTabs() {
        if(mTabHost != null) {
            int tabHeight = 50;
            int tabsCount = mTabHost.getTabWidget().getTabCount();
            for(int i = 0; i < tabsCount; i++) {
                TextView tabTextView = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                tabTextView.setTextSize(9);
                tabTextView.setPadding(2, 0, 2, 0);
                tabTextView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
                mTabHost.getTabWidget().getChildAt(i).getLayoutParams().height = (int) (tabHeight * getResources().getDisplayMetrics().density);
            }
        }
    }

    private void handleIntent(Intent intent) {
        try {
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                // Log.d(TAG, "Search: " + query);
                SearchFragment searchFragment = new SearchFragment();
                addFragment(searchFragment);
                searchFragment.searchSongs(query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (TabManager.mLastTab.fragment != null && !TabManager.mLastTab.fragment.isDetached()) {
            ft.detach(TabManager.mLastTab.fragment);
        }

        if(TabManager.searchFragment != null && !TabManager.searchFragment.isDetached()) {
            ft.detach(TabManager.searchFragment);
        }

        TabManager.searchFragment = fragment;
        ft.add(R.id.realtabcontent, TabManager.searchFragment, Config.SEARCH_TAB);
        ft.attach(TabManager.searchFragment);
        ft.commit();
        this.getSupportFragmentManager().executePendingTransactions();
    }

    public static class TabManager implements MyTabHost.OnTabChangeListener {
        private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
        private final int mContainerId;
        private final FragmentActivity mActivity;
        private final MyTabHost mTabHost;
        public static Fragment searchFragment;
        public static TabInfo mLastTab;

        static final class TabInfo {
            private final Bundle args;
            private final Class<?> clss;
            private Fragment fragment;
            private final String tag;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class TabFactory implements MyTabHost.TabContentFactory {
            private final Context mContext;

            public TabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabManager(FragmentActivity activity, MyTabHost tabHost, int containerId) {
            mActivity = activity;
            mTabHost = tabHost;
            mTabHost.setOnTabChangedListener(this);
            mContainerId = containerId;
        }

        public void addTab(MyTabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new TabFactory(mActivity));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            info.fragment = mActivity.getSupportFragmentManager().findFragmentByTag(tag);
            if (info.fragment != null && !info.fragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(info.fragment);
                ft.commit();
            }

            mTabs.put(tag, info);
            mTabHost.addTab(tabSpec);
        }

        public boolean checkFragment(Fragment fragment) {
            return fragment != null;
        }

        @Override
        public void onTabChanged(String tabId) {
            TabInfo newTab = mTabs.get(tabId);
            if (mLastTab != newTab) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                if (mLastTab != null) {
                    if (checkFragment(mLastTab.fragment)) {
                        ft.detach(mLastTab.fragment);
                    }
                    if (checkFragment(searchFragment)) {
                        ft.detach(searchFragment);
                    }
                }
                if (newTab != null) {
                    if (!checkFragment(newTab.fragment)) {
                        newTab.fragment = Fragment.instantiate(mActivity, newTab.clss.getName(), newTab.args);
                        if(!newTab.fragment.isAdded()) {
                            ft.add(mContainerId, newTab.fragment, newTab.tag);
                        }
                    } else {
                        if(!newTab.fragment.isAdded()) {
                            ft.attach(newTab.fragment);
                        }

                        if (mLastTab != null) {
                            if (checkFragment(mLastTab.fragment)) {
                                ft.detach(mLastTab.fragment);
                            }
                            if (checkFragment(searchFragment)) {
                                ft.detach(searchFragment);
                            }
                        }
                    }
                }

                mLastTab = newTab;
                if(mLastTab != null) {
                    currentTab = mLastTab.fragment.getTag();
                }
                hideOrShowAddItem();
                ft.commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        }

        private void hideOrShowAddItem() {
            if(mLastTab != null ) {
                setMenuItemsVisibility(true, false);
                mActionBar.setDisplayShowTitleEnabled(true);
                if(mLastTab.fragment.getTag().equals(Config.TOP_TAB) || mLastTab.fragment.getTag().equals(Config.SEARCH_TAB)) {
                    setAddVisibility(true);
                } else {
                    setAddVisibility(false);
                }
            }
        }
    }
}