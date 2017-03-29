# RxWifi

## Usage

```kt
RxWifi(context).connect(ssid)
  .subscribe({ scanResult ->
   }, { e -> {
    // ssid not found
    // auth failure
    // connection failure
    // no internet?
  })
```

```kt
RxWifi(context).scan().subscribe {}
```

```kt
RxWifi(context).scan().filter { scanResult -> scanResult.SSID == ssid }.first().flatMap { RxWifi(context).connect() }
```

## Issue - Connection failure to a no-internet Wi-Fi with smart network routing enabled and mobile data enabled

* It not works on Samsung Note Edge, Android 6.0.1
* It does work on Nexus Pixel, Android 7.1.1 (can also reproduce)
* It does work on Nexus 5, Android 6.0.1

```kt
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                ConnectivityManager.setProcessDefaultNetwork(network)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.bindProcessToNetwork(network)
            }
        }
    }
    //connectivityManager.requestNetwork(NetworkRequest.Builder()
    connectivityManager.registerNetworkCallback(NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build(), netCallback)
}

// connectivityManager.unregisterNetworkCallback(netCallback)
```

## Installation

```gradle
repositories {
    jcenter()

}

dependencies {
    compile 'com.github.yongjhih:rx-wifi:0.0.1'
}
```

## LICENSE

Apache 2.0
