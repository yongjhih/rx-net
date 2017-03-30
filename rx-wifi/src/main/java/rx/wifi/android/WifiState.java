/*
 * Copyright (C) 2017, Andrew Chen
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
package rx.wifi.android;

import android.net.wifi.WifiManager;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO: Move to wifi-android-annotations
 */
@IntDef(value = {
        WifiManager.WIFI_STATE_DISABLED,
        WifiManager.WIFI_STATE_DISABLING,
        WifiManager.WIFI_STATE_ENABLED,
        WifiManager.WIFI_STATE_ENABLING,
        WifiManager.WIFI_STATE_UNKNOWN,
})
@Retention(RetentionPolicy.SOURCE)
public @interface WifiState {
}

