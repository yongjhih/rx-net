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
import android.support.annotation.RequiresPermission;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import rx2.receiver.android.RxReceiver;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;

public class RxWifi {
    /**
     *
     * @param context
     * @return
     */
    @NonNull
    //@RequiresPermission(ACCESS_WIFI_STATE)
    @RequiresPermission(anyOf = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION})
    @SuppressLint("MissingPermission")
    @CheckReturnValue
    public static Observable<List<ScanResult>> scan(@NonNull final Context context) {
        // Move into obs.<IntentFilter>fromCallable()?
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        // The WifiManager.startScan() usage is limited to:
        //
        // - Each foreground app is restricted to 4 scans every 2 minutes.
        // - All background apps combined are restricted to one scan every 30 minutes."
        //
        // So we don't call wifiManager.startScan() anymore
        //
        // Observable.interval(30, TimeUnit.SECONDS)
        //   .doOnNext { wifiManager.startScan() }
        //   .subscribe();
        //
        // RxWifi.scan(context)
        //   .subscribe();
        //
        // ref. https://stackoverflow.com/questions/49178307/startscan-in-wifimanager-deprecated-in-android-p
        return RxReceiver.receives(context, intentFilter)
                .filter(new Predicate<Intent>() {
                    @Override
                    public boolean test(@NonNull final Intent intent) throws Exception {
                        return intent.getBooleanExtra(
                                WifiManager.EXTRA_RESULTS_UPDATED, false);
                    }
                })
                .map(new Function<Intent, List<ScanResult>>() {
                    @Override
                    public List<ScanResult> apply(@NonNull final Intent intent) throws Exception {
                        return wifiManager.getScanResults();
                    }
                })
                .startWith(wifiManager.getScanResults());
    }

    /**
     * @param context
     * @return Observable<@WifiState Integer>
     */
    @NonNull
    @CheckReturnValue
    @RequiresPermission(ACCESS_WIFI_STATE)
    public static Observable<Integer> states(@NonNull final Context context) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        return RxReceiver.receives(context, intentFilter)
                .map(new Function<Intent, Integer>() {
                    @Override
                    @WifiState
                    public Integer apply(Intent intent) throws Exception {
                        @WifiState
                        int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                        return state;
                    }
                });
    }

    /**
     * @param context
     * @return
     */
    @NonNull
    @CheckReturnValue
    @RequiresPermission(ACCESS_WIFI_STATE)
    public static Observable<SupplicantState> supplicantStates(@NonNull final Context context) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);

        return RxReceiver.receives(context, intentFilter)
                .map(new Function<Intent, SupplicantState>() {
                    @Override
                    public SupplicantState apply(Intent intent) throws Exception {
                        // necessary validation?
                        //.flatMap {
                        //  return ((supplicantState != null) && SupplicantState.isValidState(supplicantState)) ?
                        //      Observable.just(supplicantState) : Observable.empty();
                        //}
                        return intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    }
                });
    }

    /**
     *
     * @param context
     * @return
     */
    @NonNull
    @CheckReturnValue
    @RequiresPermission(ACCESS_WIFI_STATE)
    public static Observable<SupplicantState> connected(@NonNull final Context context) {
        return supplicantStates(context)
                .filter(new Predicate<SupplicantState>() {
                    @Override
                    public boolean test(SupplicantState supplicantState) throws Exception {
                        return supplicantState == SupplicantState.COMPLETED;
                    }
                });
    }

    /**
     *
     * @param context
     * @param scanResult
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public static void connect(@NonNull final Context context, @NonNull final ScanResult scanResult) {
        connect(context, scanResult, null);
    }

    /**
     *
     * @param context
     * @param scanResult
     * @param password
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    public static void connect(@NonNull final Context context,
                               @NonNull final ScanResult scanResult,
                               @Nullable final String password) {
        connect((WifiManager) context.getSystemService(Context.WIFI_SERVICE), scanResult, password);
    }

    @RequiresPermission(CHANGE_WIFI_STATE)
    public static void connect(@NonNull final WifiManager wifimanager,
                               @NonNull final ScanResult scanResult) {
        connect(wifimanager, scanResult, null);
    }

    @RequiresPermission(CHANGE_WIFI_STATE)
    public static void connect(@NonNull final WifiManager wifiManager,
                               @NonNull final ScanResult scanResult,
                               @Nullable final String password) {
        final WifiConfiguration newConfig = new WifiConfiguration();
        final String trimSsid = scanResult.SSID.replaceAll("\"$", "").replaceAll("^\"", "");
        newConfig.SSID = "\""+ trimSsid + "\"";
        newConfig.status = WifiConfiguration.Status.ENABLED;
        newConfig.priority = 100;

        if (scanResult.capabilities.toUpperCase().contains("WEP")) {
            newConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            newConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            newConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            newConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            newConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            newConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            newConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            newConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            newConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

            if (password != null) {
                newConfig.wepKeys[0] = "\"" + password + "\"";
            }

            newConfig.wepTxKeyIndex = 0;

        } else if (scanResult.capabilities.toUpperCase().contains("WPA")) {
            newConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            newConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            newConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            newConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            newConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            newConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            newConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            newConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            newConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            if (password != null) {
                newConfig.preSharedKey = "\"" + password + "\"";
            }
        } else {
            newConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            newConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            newConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            newConfig.allowedAuthAlgorithms.clear();
            newConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            newConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            newConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            newConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            newConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            newConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        }

        wifiManager.addNetwork(newConfig); // networkId

        wifiManager.disconnect();
        for (final WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
            final String trimConfigSsid = config.SSID.replaceAll("\"$", "").replaceAll("^\"", "");
            if (trimConfigSsid.equals(trimSsid)) {
                wifiManager.enableNetwork(config.networkId, true);
            } else {
                wifiManager.disableNetwork(config.networkId); // Force connect even if no-internet wifi
            }
        }
        wifiManager.reconnect();
    }

    /**
     *
     * @param context
     * @param ssid
     * @return
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    public static boolean isConnected(@NonNull final Context context, @NonNull final String ssid) {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String trimSsid = wifiInfo.getSSID().replaceAll("\"$", "").replaceAll("^\"", "");

        return trimSsid.equals(ssid) && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED;
    }

    /**
     *
     * @param context
     * @param ssid
     * @return
     */
    @NonNull
    @RequiresPermission(anyOf = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION})
    //@RequiresPermission(CHANGE_WIFI_STATE)
    @CheckReturnValue
    public static Maybe<String> connects(@NonNull final Context context, @NonNull final String ssid) {
        return connects(context, ssid, null);
    }

    /**
     *
     * @param context
     * @param ssid
     * @param password
     * @return
     */
    @NonNull
    @RequiresPermission(anyOf = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION})
    //@RequiresPermission(CHANGE_WIFI_STATE)
    @CheckReturnValue
    public static Maybe<String> connects(@NonNull final Context context, @NonNull final String ssid, @Nullable final String password) {
        if (isConnected(context, ssid)) return Maybe.just(ssid);

        return scanFor(context, ssid)
            .doOnSuccess(new Consumer<ScanResult>() {
                @Override
                public void accept(ScanResult scanResult) throws Exception {
                    connect(context, scanResult, password);
                }
            }).flatMap(new Function<ScanResult, MaybeSource<SupplicantState>>() {
                @Override
                public MaybeSource<SupplicantState> apply(ScanResult scanResult) throws Exception {
                    return connectedFor(context, ssid);
                }
            }).map(new Function<SupplicantState, String>() {
                @Override
                public String apply(SupplicantState supplicantState) throws Exception {
                    return ssid;
                }
            });
    }

    /**
     *
     * @param context
     * @param ssid
     * @return
     */
    @CheckReturnValue
    @RequiresPermission(anyOf = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION})
    @NonNull
    public static Maybe<ScanResult> scanFor(@NonNull final Context context, @NonNull final String ssid) {
        return scan(context).flatMap(new Function<List<ScanResult>, ObservableSource<ScanResult>>() {
            @Override
            public ObservableSource<ScanResult> apply(List<ScanResult> scanResults) throws Exception {
                return Observable.fromIterable(scanResults);
            }
        }).filter(new Predicate<ScanResult>() {
            @Override
            public boolean test(ScanResult scanResult) throws Exception {
                String trimSsid = scanResult.SSID.replaceAll("\"$","").replaceAll("^\"","");
                return trimSsid.equals(ssid);
            }
        }).firstElement();
    }

    /**
     *
     * @param context
     * @param ssid
     * @return
     */
    @RequiresPermission(ACCESS_WIFI_STATE)
    @CheckReturnValue
    @NonNull
    public static Maybe<SupplicantState> connectedFor(@NonNull final Context context, @NonNull final String ssid) {
        return supplicantStates(context).filter(new Predicate<SupplicantState>() {
            @Override
            public boolean test(SupplicantState state) throws Exception {
                return isConnected(context, ssid);
            }
        }).firstElement();
    }
}
