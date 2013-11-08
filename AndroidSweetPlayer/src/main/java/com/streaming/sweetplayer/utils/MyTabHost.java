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
import android.util.AttributeSet;
import android.widget.TabHost;

public class MyTabHost extends TabHost {

    public MyTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTabHost(Context context) {
        super(context);
    }

    @Override
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        if(getCurrentView() != null){
            super.dispatchWindowFocusChanged(hasFocus);
        }
    }
}
