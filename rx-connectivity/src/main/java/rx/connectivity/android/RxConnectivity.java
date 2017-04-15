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
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;

public class RxConnectivity {

    /**
     * @return available network
     *
     * Usage:
     *
     * <pre>
     * networks(context, NetworkRequest.Builder()
     *                                 .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
     *                                 .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
     *                                 .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
     *                                 .build())
     * </pre>
     *
     * ref. https://github.com/eryngii-mori/android-developer-preview/issues/2218
     */
    @CheckResult
    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Observable<Network> networks(@NonNull final Context context,
                                        @NonNull final NetworkRequest networkRequest) {
        return networks(
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE),
                networkRequest);
    }

    /**
     * @param connectivityManager
     * @param networkRequest
     * @return
     */
    @CheckResult
    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Observable<Network> networks(
            @NonNull final ConnectivityManager connectivityManager,
                                        @NonNull final NetworkRequest networkRequest) {
        return Observable.create(new ObservableOnSubscribe<Network>() {
            @Override
            public void subscribe(final ObservableEmitter<Network> emit) throws Exception {
                final ConnectivityManager.NetworkCallback networkCallback =
                        new ConnectivityManager.NetworkCallback() {
                            @Override
                            public void onAvailable(Network network) {
                                emit.onNext(network);
                            }
                        };
                emit.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        connectivityManager.unregisterNetworkCallback(networkCallback);
                    }
                });
                connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
            }
        });
    }

    /**
     * @param connectivityManager
     * @param networkRequest
     * @return
     */
    @CheckResult
    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Maybe<Network> defaultNetwork(
            @NonNull final ConnectivityManager connectivityManager,
            @NonNull final NetworkRequest networkRequest) {
        return networks(connectivityManager, networkRequest).doOnNext(new Consumer<Network>() {
            @Override
            public void accept(Network network) throws Exception {
                defaultNetwork(connectivityManager, network);

            }
        }).singleElement();
    }

    /**
     * @param context
     * @param networkRequest
     * @return
     */
    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Maybe<Network> defaultNetwork(
            @NonNull final Context context,
            @NonNull final NetworkRequest networkRequest) {
        return defaultNetwork((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE),
                networkRequest);
    }

    /**
     * @param connectivityManager
     * @param network
     */
    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void defaultNetwork(
            @NonNull final ConnectivityManager connectivityManager,
            @NonNull final Network network) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            ConnectivityManager.setProcessDefaultNetwork(network);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.bindProcessToNetwork(network);
        }
    }
}
