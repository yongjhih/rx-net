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
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;
import android.support.annotation.WorkerThread;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import rx.receiver.android.RxReceiver;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;

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
    @CheckReturnValue
    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @RequiresPermission(ACCESS_NETWORK_STATE)
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
    @CheckReturnValue
    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @RequiresPermission(ACCESS_NETWORK_STATE)
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
    @CheckReturnValue
    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @RequiresPermission(ACCESS_NETWORK_STATE)
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
    @RequiresPermission(ACCESS_NETWORK_STATE)
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
    @SuppressWarnings("deprecation")
    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @RequiresPermission(ACCESS_NETWORK_STATE)
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

    @NonNull
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static Observable<Intent> connectivity(
            @NonNull final Context context) {
        return RxReceiver.receives(context,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static boolean isConnected(@NonNull final Context context) {
        return isConnected((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static boolean isConnected(@NonNull final ConnectivityManager connectivityManager) {
        final NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @NonNull
    @RequiresPermission(allOf = {INTERNET, ACCESS_NETWORK_STATE})
    public static Observable<Boolean> internectivity(
            @NonNull final Context context) {
        return connectivity(context).map(new Function<Intent, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull Intent intent) throws Exception {
                        return isResolvable();
                    }
                });
    }

    @RequiresPermission(INTERNET)
    @WorkerThread
    public static boolean isResolvable() {
        return isResolvable("connectivitycheck.android.com");
    }

    @RequiresPermission(INTERNET)
    @WorkerThread
    public static boolean isResolvable(@NonNull final String host) {
        try {
            InetAddress.getByName(host);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    @RequiresPermission(INTERNET)
    @WorkerThread
    public static boolean isReachable() {
        return isReachable("connectivitycheck.android.com");
    }

    @RequiresPermission(INTERNET)
    @WorkerThread
    public static boolean isReachable(@NonNull final String host) {
        return isReachable(host, 200);
    }

    @RequiresPermission(INTERNET)
    @WorkerThread
    public static boolean isReachable(@NonNull final String host,
                                      int timeout) {
        try {
            return InetAddress.getByName(host).isReachable(timeout);
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    @RequiresPermission(INTERNET)
    @WorkerThread
    public static boolean isConnectable(@NonNull final String host,
                                        int port, int timeout) {
        final Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
