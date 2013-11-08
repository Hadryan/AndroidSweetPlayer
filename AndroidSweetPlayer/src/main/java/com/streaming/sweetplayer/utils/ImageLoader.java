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

package com.streaming.sweetplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.streaming.sweetplayer.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    private boolean mIsResized = false;
    private ExecutorService mExecutorService;
    private FileCache mFileCache;
    private Map<ImageView, String> mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private MemoryCache mMemoryCache = new MemoryCache();
    private final int mDefaultImage = R.drawable.ic_launcher;

    public ImageLoader(Context context) {
        mFileCache = new FileCache(context);
        mExecutorService = Executors.newFixedThreadPool(5);
    }

    public void setResize(boolean resized) {
        mIsResized = resized;
    }

    public void DisplayImage(String url, ImageView imageView) {
        mImageViews.put(imageView, url);
        Bitmap bitmap = mMemoryCache.get(url);

        if(bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if(Utils.validateString(url)) {
                queuePhoto(url, imageView);
            }
            imageView.setImageResource(mDefaultImage);
        }
    }
 
    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        mExecutorService.submit(new PhotosLoader(p));
    }
 
    public Bitmap getBitmap(String url) {
        File f = mFileCache.getFile(url);
 
        // From SD cache
        Bitmap bit = decodeFile(f);
        if(bit != null) {
            return bit;
        }
 
        // From web
        try {
            int TIMEOUT_VALUE = 60000;
            Bitmap bitmap;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(TIMEOUT_VALUE);
            conn.setReadTimeout(TIMEOUT_VALUE);
            conn.setInstanceFollowRedirects(true);

            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (IOException e) {
           e.printStackTrace();
           return null;
        }
    }

    public Bitmap getImageBitmap(String url) {
        Bitmap bitmap = null;
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bitmap = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
 
    // Decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            BitmapFactory.Options o2 = new BitmapFactory.Options();
 
            // Find the correct scale value. It should be the power of 2.
            if(mIsResized) {
                final int REQUIRED_SIZE = 70;
                int width_tmp = o.outWidth;
                int height_tmp = o.outHeight;
                int scale = 1;

                while(true) {
                    if(width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                        break;

                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale*= 2;
                }
                o2.inSampleSize = scale;
            }

            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // Log.d("File not found: ", e.getMessage());
        }
        return null;
    }

    private static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ;) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;

                os.write(bytes, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    // Task for the queue
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }
 
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }
 
        public void run() {
            if(imageViewReused(photoToLoad))
                return;

            Bitmap bmp = getBitmap(photoToLoad.url);
            mMemoryCache.put(photoToLoad.url, bmp);

            if(imageViewReused(photoToLoad))
                return;

            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
            Activity a = (Activity) photoToLoad.imageView.getContext();

            if (a != null) {
                a.runOnUiThread(bd);
            }
        }
    }
 
    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = mImageViews.get(photoToLoad.imageView);
        return tag == null || !tag.equals(photoToLoad.url);
    }
 
    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p){
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if(imageViewReused(photoToLoad))
                return;

            if(bitmap != null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(mDefaultImage);
        }
    }
 
    public void clearCache() {
        mMemoryCache.clear();
        mFileCache.clear();
    }
}
