package com.test.assignment.root;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by shadaf on 1/7/18.
 */

public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        Intent sendIntent = new Intent(MainApplication.NETWORK_RECEIVER);

        if (networkInfo != null && networkInfo.isConnected())
            sendIntent.putExtra(MainApplication.NETWORK_STATUS, true);
        else
            sendIntent.putExtra(MainApplication.NETWORK_STATUS, false);

        context.sendBroadcast(sendIntent);
    }
}
