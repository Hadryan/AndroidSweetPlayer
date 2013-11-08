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

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.streaming.sweetplayer.R;
import com.streaming.sweetplayer.api.Config;
import com.streaming.sweetplayer.utils.ImageLoader;
import com.streaming.sweetplayer.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;


public class CommonAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> mDataArrayList;
    private FragmentActivity mActivity;
    private ImageLoader mImageLoader;
    private int mImageToLoad = 1;

    public CommonAdapter(FragmentActivity activity, ArrayList<HashMap<String, String>> arrayList) {
        mActivity = activity;
        mDataArrayList = arrayList;
        mImageLoader = new ImageLoader(mActivity.getApplicationContext());
        mImageLoader.setResize(false);

        if(Config.deviceWidth >= 600) {
            mImageToLoad = 2;
        }
    }

    public int getCount() {
        return mDataArrayList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView caption;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.common_item, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image);
            viewHolder.caption = (TextView) convertView.findViewById(R.id.caption);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> dataList = mDataArrayList.get(position);
        viewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        viewHolder.imageView.setPadding(4, 4, 4, 4);
        viewHolder.caption.setText(dataList.get(Config.NAME));
        String image = dataList.get(Config.IMAGE);
        mImageLoader.DisplayImage(Utils.replaceImage(image, mImageToLoad), viewHolder.imageView);

        return convertView;
    }
}