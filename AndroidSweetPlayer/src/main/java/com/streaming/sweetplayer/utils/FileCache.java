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

import android.content.Context;

import java.io.File;

/**
 * Class to manage the images files caching. Get and clear image files.
*/
class FileCache {
    private File cacheDir;
 
    /**
     * Find the cache directory to save cached images.
    */
    public FileCache(Context context){
        
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            String app_dir = "/Android/data/" + context.getApplicationContext().getPackageName();
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), app_dir);
        } else {
            cacheDir = context.getCacheDir();
        }

        if (cacheDir != null) {
            if(!cacheDir.exists())
                cacheDir.mkdirs();
        }
    }
 
    public File getFile(String url){
        String filename = String.valueOf(url.hashCode());
        return new File(cacheDir, filename);
    }
 
    public void clear() {
        File[] files = cacheDir.listFiles();
        
        if(files == null)
            return;
        
        for(File f:files)
            f.delete();
    }
}
