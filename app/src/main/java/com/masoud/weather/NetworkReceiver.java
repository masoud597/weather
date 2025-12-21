package com.masoud.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkReceiver extends BroadcastReceiver {
    // Used to make a listener to be able to implement onNetworkAvailable and onNetworkLost method in main activity
    public interface NetworkStateListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }
    private final NetworkStateListener listener;
    public NetworkReceiver(NetworkStateListener listener) {
        this.listener = listener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (listener != null) {
            if (isConnected(context)) {
                listener.onNetworkAvailable();
            } else {
                listener.onNetworkLost();
            }
        }
    }

    // Check if phone is connected to internet or not
    private boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(network);
        return networkCapabilities != null && (
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}
