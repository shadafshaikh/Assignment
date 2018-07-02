package com.test.assignment.root;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.test.assignment.database.DatabaseHelper;


/**
 * Created by shadaf on 1/7/18.
 */

public class MainApplication extends Application {

    private DatabaseHelper databaseHelper;
    private ConnectivityManager connectivityManager;

    public static final String NETWORK_RECEIVER = "network receiver";
    public static final String LATITUDE = "lat", LONGITUDE = "long", NETWORK_STATUS = "network_status";

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(this);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * It returns the current helper class object initialized with the Application
     * @return
     */
    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public boolean checkNetworkConnection() {

        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if(netInfo != null){

            if(netInfo.isConnected()){
                return true;
            }else return false;

        }else return false;
    }


}
