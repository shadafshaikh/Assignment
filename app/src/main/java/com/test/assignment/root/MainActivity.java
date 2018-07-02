package com.test.assignment.root;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.test.assignment.R;
import com.test.assignment.database.DatabaseHelper;
import com.test.assignment.maps.MapsActivity;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private final int REQUEST_PERMISSION = 100, REQUEST_PERMISSION_BACKUP = 101;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private NetworkReceiver networkReceiver;
    private LocationSetter locationSetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVariables();        /* Initialize all necessary objects*/
        initListeners();        /* Initialize all listeners (events and location updates)*/
        createLocationRequest();/* Checks for the necessary settings for location updates*/

        /* Update the UI according to the network status */
        updateUI(((MainApplication)getApplication()).checkNetworkConnection());

        /* Fetch the last known location for backup*/
        fetchLocation();

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(networkReceiver, new IntentFilter(MainApplication.NETWORK_RECEIVER));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    startLocationUpdates();
                } else {
                    Toast.makeText(this, "GPS is mandatory to fetch location. please tap on fetch button", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case REQUEST_PERMISSION_BACKUP: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    fetchLocation();
                } else {
                    Toast.makeText(this, "GPS is mandatory to fetch location. please tap on fetch button", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    /**
     * Initialize all necessary objects like FusedLocationProvider, network receiver,
     * LocationSetter and LocationRequest to define the criteria for location updates.
     * I have considered high accuracy as a priority, and in this case app will ask to turn on
     * GPS option.
     */
    @SuppressLint("RestrictedApi")
    private void initVariables() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        networkReceiver = new NetworkReceiver();
        locationSetter = new LocationSetter(0,0);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void initListeners() {
        findViewById(R.id.btFetch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationUpdates();
            }
        });
        findViewById(R.id.tvLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra(MainApplication.LATITUDE, locationSetter.getLatitude());
                intent.putExtra(MainApplication.LONGITUDE, locationSetter.getLongitude());
                startActivity(intent);

            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Toast.makeText(MainActivity.this, ""+location.getLongitude()+ " " +
                            location.getLongitude(), Toast.LENGTH_SHORT).show();
                    locationSetter = new LocationSetter(location.getLatitude(), location.getLongitude());
                    updateDB();/* Updating local DB*/
                    if(((MainApplication)getApplication()).checkNetworkConnection())
                    updateUI(true);
                    break;
                }
            }
        };
    }

    /**
     * Checks for the GPS setting, if not enabled it will ask user to enable it.
     */
    @SuppressLint("RestrictedApi")
    protected void createLocationRequest() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d("","");
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                10);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    /**
     * This will be called at very first time when user launches the app.
     * This will fetch last known location of the user. It is required to get this info because if user
     * disables GPS, system might clear cache and also will give updates after very long time. If I don't
     * receive any updates then I am storing last known location in the DB.
     */
    private void fetchLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSION_BACKUP);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                locationSetter = new LocationSetter(location.getLatitude(), location.getLongitude());
                                updateDB();
                            }
                        }
                    });
        }
    }

    /**
     * This will register location callback object with location request object defined in the beginning.
     * This will give updates of the location.
     */
    private void startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSION);
        }else{
            createLocationRequest();
            Toast.makeText(this, "Fetching location, please wait!", Toast.LENGTH_SHORT).show();
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }

    }

    /**
     * This will unregister the location updates.
     */
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * To update the UI according to the network status, I'm calling this method
     * @param network either true or false
     */
    private void updateUI(boolean network) {

        TextView tvStatus, tvLocation;
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvLocation = (TextView) findViewById(R.id.tvLocation);

        if(network){
            tvStatus.setText(getResources().getText(R.string.network_on));
            tvLocation.setVisibility(View.VISIBLE);
            DatabaseHelper helper = ((MainApplication)getApplication()).getDatabaseHelper();
            ArrayList<LocationSetter> setters = (ArrayList<LocationSetter>) helper.getData(DatabaseHelper.ModelType.LOCATION_DATA,
                    null,null,null,null,null,null,null);
            if(setters.size() == 0){
                tvLocation.setText(getResources().getText(R.string.lt_not_available));
                tvLocation.setEnabled(false);
            }else{
                locationSetter = setters.get(0);
                tvLocation.setEnabled(true);
                tvLocation.setText("Latitude: "+ setters.get(0).getLatitude() +
                        " Longitude: "+setters.get(0).getLongitude());
            }
        }else{
            tvStatus.setText(getResources().getText(R.string.network_off));
            tvLocation.setVisibility(View.GONE);
        }

    }

    /**
     * Update the SQL lite db using helper class methods
     */
    private void updateDB() {
        DatabaseHelper helper = ((MainApplication)getApplication()).getDatabaseHelper();
        helper.insertData(locationSetter, DatabaseHelper.ModelType.LOCATION_DATA);
        stopLocationUpdates();
    }

    /**
     * Receiver to receive updates of the network. It receives updates from the
     * NetworkReceiver class from the root package.
     */
    private class NetworkReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null && intent.getAction().equalsIgnoreCase(MainApplication.NETWORK_RECEIVER)){
                updateUI(intent.getBooleanExtra(MainApplication.NETWORK_STATUS, false));
            }
        }
    }



}
