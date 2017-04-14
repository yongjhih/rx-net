@file:Suppress("NOTHING_TO_INLINE")

package rx.connectivity.android

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.support.annotation.RequiresApi
import io.reactivex.Observable

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
inline fun ConnectivityManager.networks(networkRequest: NetworkRequest)
        : Observable<Network>
        = RxConnectivity.networks(this, networkRequest)
