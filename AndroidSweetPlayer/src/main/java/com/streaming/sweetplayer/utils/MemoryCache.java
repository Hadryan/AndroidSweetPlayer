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

import android.graphics.Bitmap;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible to do the caching for images.
*/
public class MemoryCache {
    private Map<String, SoftReference<Bitmap>> mMemCache = Collections.synchronizedMap(new HashMap<String, SoftReference<Bitmap>>());
 
    public Bitmap get(String id) {
        if (!mMemCache.containsKey(id)) {
            return null;
        }
        
        SoftReference<Bitmap> reference = mMemCache.get(id);
        return reference.get();
    }
 
    public void put(String id, Bitmap bitmap) {
        mMemCache.put(id, new SoftReference<Bitmap>(bitmap));
    }

    public void clear() {
        mMemCache.clear();
    }
}