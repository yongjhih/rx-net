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
package rx.connectivity.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowConnectivityManager;

import rx.connectivity.BuildConfig;

import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class RxConnectivityTest {
    @Test
    public void defaultNetwork() {
        //ConnectivityManager connectivityManager = (ConnectivityManager) RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);
        //ShadowConnectivityManager shadowConnectivityManager = shadowOf(connectivityManager);
        // TODO RxConnectivity.defaultNetwork(connectivityManager, networkRequest, mockNetworkCallback);
        // mockNetworkCallback.onAvailable(mock(network))
        // verify(defaultNetowrk())
        // verify(ConnectivityManager.setProcessDefaultNetwork())
        // verify(ConnectivityManager.bindProcessToNetwork())
    }


}
