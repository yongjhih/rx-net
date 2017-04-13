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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import rx.receiver.android.RxReceiver;

public class RxWifi {
    @NonNull
    public static Observable<List<ScanResult>> scan(@NonNull final Context context) {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        return RxReceiver.receives(context, intentFilter)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        wifiManager.startScan(); // obs.dispose -> onCancel -> unregister -> stop scan
                    }
                })
                .map(new Function<Intent, List<ScanResult>>() {
                    @Override
                    public List<ScanResult> apply(final Intent intent) throws Exception {
                        return wifiManager.getScanResults();
                    }
                });
    }

    @NonNull
    public static Observable<Integer> states(@NonNull final Context context) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        return RxReceiver.receives(context, intentFilter)
                .map(new Function<Intent, Integer>() {
                    @Override
                    public Integer apply(Intent intent) throws Exception {
                        return intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                    }
                });
    }

    @NonNull
    public static Observable<SupplicantState> supplicantStates(@NonNull final Context context) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);

        return RxReceiver.receives(context, intentFilter)
                .map(new Function<Intent, SupplicantState>() {
                    @Override
                    public SupplicantState apply(Intent intent) throws Exception {
                        return intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    }
                });
    }

    @NonNull
    public static Observable<SupplicantState> connected(final Context context) {
        return supplicantStates(context)
                .filter(new Predicate<SupplicantState>() {
                    @Override
                    public boolean test(SupplicantState supplicantState) throws Exception {
                        return supplicantState == SupplicantState.COMPLETED;
                    }
                });
    }

    @SuppressLint("NewApi")
    public static void connect(@NonNull final Context context, @NonNull String ssid) {
        final WifiManager wifiManager = context.getSystemService(WifiManager.class);
        wifiManager.disconnect();
        for (WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
            String trimSsid = config.SSID.replaceAll("\"$", "").replaceAll("^\"", "");
            if (trimSsid.equals(ssid)) {
                wifiManager.enableNetwork(config.networkId, true);
            } else {
                wifiManager.disableNetwork(config.networkId);
            }
        }
        wifiManager.reconnect();
    }

    @SuppressLint("NewApi")
    public static boolean hasConnected(@NonNull final Context context, @NonNull String ssid) {
        final WifiManager wifiManager = context.getSystemService(WifiManager.class);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String trimSsid = wifiInfo.getSSID().replaceAll("\"$", "").replaceAll("^\"", "");
        if (!trimSsid.equals(ssid)) {
            return false;
        }

        if (wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            return false;
        }

        return true;
    }

    @NonNull
    public Observable<ScanResult> connects(@NonNull final Context context, @NonNull String ssid) {
        // TODO
        return Observable.empty();
    //     if ()
    //     connect(context, ssid)
    //     return supplicantStates(context)
    //             .filter(new Predicate<SupplicantState>() {
    //                 @Override
    //                 public boolean test(SupplicantState supplicantState) throws Exception {
    //                     return supplicantState == SupplicantState.COMPLETED;
    //                 }
    //             });
    }
}
