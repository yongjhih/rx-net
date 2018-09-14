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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

import java.util.Arrays;
import java.util.List;

import io.reactivex.observers.TestObserver;
import rx.wifi.BuildConfig;

import static android.net.wifi.WifiManager.ERROR_AUTHENTICATING;
import static android.net.wifi.WifiManager.EXTRA_NEW_STATE;
import static android.net.wifi.WifiManager.EXTRA_SUPPLICANT_ERROR;
import static android.net.wifi.WifiManager.EXTRA_WIFI_STATE;
import static android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_UNKNOWN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class RxWifiTest {
    @SuppressWarnings("MissingPermission")
    @Test
    public void scan() throws Exception {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        Context context = RuntimeEnvironment.application.getApplicationContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        shadowOf(wifiManager).setWifiEnabled(true);
        ScanResult scanResultFoo = Shadow.newInstanceOf(ScanResult.class);
        scanResultFoo.SSID = "\"foo\"";
        scanResultFoo.BSSID = "02:00:00:00:00:00";
        ScanResult scanResultBar = Shadow.newInstanceOf(ScanResult.class);
        scanResultBar.SSID = "\"bar\"";
        scanResultBar.BSSID = "02:00:00:00:00:01";
        List<ScanResult> scanResults = Arrays.asList(scanResultFoo, scanResultBar);
        shadowOf(wifiManager).setScanResults(scanResults);

        TestObserver<List<ScanResult>> tester = RxWifi.scan(context).test();
        Intent foobar = new Intent(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.sendBroadcast(foobar);
        tester.assertValues(scanResults);
    }

    @Test
    public void states() throws Exception {
        // TODO
    }

    @Test
    public void supplicantStates() throws Exception {
        // TODO

    }

    @Test
    public void connected() throws Exception {
        // TODO

    }

    @Test
    public void connect() throws Exception {
        WifiManager wifiManager = mock(WifiManager.class);
        ScanResult scanResultFoo = Shadow.newInstanceOf(ScanResult.class);
        scanResultFoo.SSID = "\"foo\"";
        scanResultFoo.BSSID = "02:00:00:00:00:00";
        scanResultFoo.capabilities = "";

        WifiConfiguration config = mock(WifiConfiguration.class);
        config.networkId = 0;
        config.SSID = "\"foo\"";

        WifiConfiguration configBar = mock(WifiConfiguration.class);
        configBar.networkId = 1;
        configBar.SSID = "\"bar\"";
        when(wifiManager.getConfiguredNetworks()).thenReturn(Arrays.asList(config, configBar));

        RxWifi.connect(wifiManager, scanResultFoo);

        verify(wifiManager).addNetwork(any(WifiConfiguration.class));
        verify(wifiManager).disconnect();
        verify(wifiManager).enableNetwork(config.networkId, true);
        verify(wifiManager).disableNetwork(configBar.networkId);
        verify(wifiManager).reconnect();
    }

    @Test
    public void connectWEP() throws Exception {
        WifiManager wifiManager = mock(WifiManager.class);
        ScanResult scanResultFoo = Shadow.newInstanceOf(ScanResult.class);
        scanResultFoo.SSID = "\"foo\"";
        scanResultFoo.BSSID = "02:00:00:00:00:00";
        scanResultFoo.capabilities = "WEP";

        WifiConfiguration config = mock(WifiConfiguration.class);
        config.networkId = 0;
        config.SSID = "\"foo\"";
        when(wifiManager.getConfiguredNetworks()).thenReturn(Arrays.asList(config));

        RxWifi.connect(wifiManager, scanResultFoo, "password");

        verify(wifiManager).addNetwork(any(WifiConfiguration.class));
        verify(wifiManager).disconnect();
        verify(wifiManager).enableNetwork(config.networkId, true);
        verify(wifiManager).reconnect();
    }

    @Test
    public void connectWPA() throws Exception {
        WifiManager wifiManager = mock(WifiManager.class);
        ScanResult scanResultFoo = Shadow.newInstanceOf(ScanResult.class);
        scanResultFoo.SSID = "\"foo\"";
        scanResultFoo.BSSID = "02:00:00:00:00:00";
        scanResultFoo.capabilities = "WPA";

        WifiConfiguration config = mock(WifiConfiguration.class);
        config.networkId = 0;
        config.SSID = "\"foo\"";
        when(wifiManager.getConfiguredNetworks()).thenReturn(Arrays.asList(config));

        RxWifi.connect(wifiManager, scanResultFoo, "password");

        verify(wifiManager).addNetwork(any(WifiConfiguration.class));
        verify(wifiManager).disconnect();
        verify(wifiManager).enableNetwork(config.networkId, true);
        verify(wifiManager).reconnect();
    }

    @Test
    public void isConnected() throws Exception {
        // TODO

    }

    @Test
    public void connects() throws Exception {
        // TODO

    }

    @Test
    public void scanFor() throws Exception {
        // TODO

    }

    @Test
    public void connectedFor() throws Exception {
        // TODO

    }

    @Test
    public void testScan() {
        // TODO
    }

    @Test
    public void testStates() {
        final Context context = RuntimeEnvironment.application.getApplicationContext();

        TestObserver<Integer> tester = RxWifi.states(context).test();

        Intent disable = new Intent(WIFI_STATE_CHANGED_ACTION)
                .putExtra(EXTRA_WIFI_STATE, WIFI_STATE_DISABLED);
        context.sendBroadcast(disable);
        tester.assertValues(WIFI_STATE_DISABLED);

        Intent unknown = new Intent(WIFI_STATE_CHANGED_ACTION)
                .putExtra(EXTRA_WIFI_STATE, WIFI_STATE_UNKNOWN);
        context.sendBroadcast(unknown);
        tester.assertValues(WIFI_STATE_DISABLED, WIFI_STATE_UNKNOWN);
    }

    @Test
    public void testSupplicantStates() {
        final Context context = RuntimeEnvironment.application.getApplicationContext();

        TestObserver<SupplicantState> tester = RxWifi.supplicantStates(context).test();

        Intent i = new Intent(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
                .putExtra(EXTRA_NEW_STATE, (Parcelable) SupplicantState.INACTIVE)
                .putExtra(EXTRA_SUPPLICANT_ERROR, ERROR_AUTHENTICATING);
        context.sendBroadcast(i);

        tester.assertValues(SupplicantState.INACTIVE);
    }
}
