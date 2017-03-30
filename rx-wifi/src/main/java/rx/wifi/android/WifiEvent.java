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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.annotation.TargetApi;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringDef;

/**
 * TODO: Move to wifi-android-annotations
 */
@TargetApi(Build.VERSION_CODES.M)
@StringDef(value = {
        WifiManager.EXTRA_BSSID,
        WifiManager.EXTRA_NEW_RSSI,
        WifiManager.EXTRA_NEW_STATE,
        WifiManager.EXTRA_NETWORK_INFO,
        WifiManager.EXTRA_PREVIOUS_WIFI_STATE,
        WifiManager.EXTRA_RESULTS_UPDATED,
        WifiManager.EXTRA_SUPPLICANT_CONNECTED,
        WifiManager.EXTRA_SUPPLICANT_ERROR,
        WifiManager.EXTRA_WIFI_INFO,
        WifiManager.EXTRA_WIFI_STATE,
})
@Retention(RetentionPolicy.SOURCE)
public @interface WifiEvent {
}

