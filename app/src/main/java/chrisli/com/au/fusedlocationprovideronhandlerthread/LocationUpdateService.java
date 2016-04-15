package chrisli.com.au.fusedlocationprovideronhandlerthread;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


/**
 * Created by cli on 14/04/2016.
 */
public class LocationUpdateService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String IntentLocationUpdateStopService = "android.intent.extra._LOCATION_UPDATE_STOP_SERVICE";

    private static String logTag_ = LocationUpdateService.class.getSimpleName();
    private GoogleApiClient googleApiClient_ = null;
    private LocationListener locationListener_ = new LocationListener();
    private HandlerThread handlerThread_ = null;
    private LocationUpdateHandler locationUpdateHandler_ = null;
    private static final int START_LOCATION_UPDATE = 1;
    private static final int CHANGE_LOCATION_SETTINGS = 2;
    private static final int STOP_LOCATION_UPDATE = 3;

    //Description: a custom LocationListener
    //Author: Chris Li
    private class LocationListener implements com.google.android.gms.location.LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            App.log(App.LOG_INFO, logTag_, location.toString());

        }
    }

    //Description: a custom Handler class for location updates
    //Author: Chris Li
    private class LocationUpdateHandler extends Handler {

        public LocationUpdateHandler(Looper looper) {
            super(looper);
        }

        //Description: message handler
        //Author: Chris Li

        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1) {
                case START_LOCATION_UPDATE: {
                    App.log(App.LOG_INFO, logTag_, "START_LOCATION_UPDATE");
                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    locationRequest.setInterval(15000);
                    locationRequest.setFastestInterval(10000);
                    startLocationUpdates(locationRequest);
                    break;
                }
                case CHANGE_LOCATION_SETTINGS: {
                    App.log(App.LOG_INFO, logTag_, "CHANGE_LOCATION_SETTINGS");
                    LocationRequest locationRequest = (LocationRequest) msg.obj;
                    if (locationRequest != null) {
                        startLocationUpdates(locationRequest);
                    }
                }
                case STOP_LOCATION_UPDATE: {
                    App.log(App.LOG_INFO, logTag_, "STOP_LOCATION_UPDATE");
                    stopLocationUpdates();
                    if (handlerThread_ != null) {
                        handlerThread_.quit();
                    }
                }
            }
        }
    }

    //Description: the service life cycle callback
    //Author: Chris Li
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Description: the service life cycle callback
    //Author: Chris Li

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        App.log(App.LOG_INFO, logTag_, "");

        handlerThread_ = new HandlerThread(logTag_ + " thread", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread_.start();
        Looper serviceLooper = handlerThread_.getLooper();
        locationUpdateHandler_ = new LocationUpdateHandler(serviceLooper);

        googleApiClient_ = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
    }

    //Description: the service life cycle callback
    //Author: Chris Li
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        boolean stopService = false;
        if (intent != null) {
            stopService = intent.getBooleanExtra(IntentLocationUpdateStopService, false);
        }
        App.log(App.LOG_INFO, logTag_, "stopService = " + stopService);
        if (stopService) {
            if (locationUpdateHandler_ != null) {
                Message message = locationUpdateHandler_.obtainMessage();
                message.arg1 = STOP_LOCATION_UPDATE;
                if (handlerThread_ != null && handlerThread_.isAlive()) {
                    locationUpdateHandler_.sendMessage(message);
                }
            }
            stopSelf();
        } else {
            if (googleApiClient_ != null && !googleApiClient_.isConnected()) {
                googleApiClient_.connect();
            }
        }

        return START_STICKY;
    }

    //Description: the service life cycle callback
    //Author: Chris Li

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        App.log(App.LOG_INFO, logTag_, "");
        if (locationUpdateHandler_ != null) {
            Message message = locationUpdateHandler_.obtainMessage();
            message.arg1 = STOP_LOCATION_UPDATE;
            if (handlerThread_ != null && handlerThread_.isAlive()) {
                locationUpdateHandler_.sendMessage(message);
            }
        }
        super.onDestroy();
    }

    //Description: the Google API client connection status callback
    //Author: Chris Li
    @Override
    public void onConnected(Bundle bundle) {
        App.log(App.LOG_INFO, logTag_, "");
        if (locationUpdateHandler_ != null) {
            Message message = locationUpdateHandler_.obtainMessage();
            message.arg1 = START_LOCATION_UPDATE;
            if (handlerThread_ != null && handlerThread_.isAlive()) {
                locationUpdateHandler_.sendMessage(message);
            }
        }
    }

    //Description: the Google API client connection status callback
    //Author: Chris Li
    @Override
    public void onConnectionSuspended(int i) {
        App.log(App.LOG_INFO, logTag_, "i = " + i);
    }

    //Description: the Google API client connection status callback
    //Author: Chris Li
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        App.log(App.LOG_INFO, logTag_, connectionResult.toString());
    }

    //Description: a function to change location settings
    //Author: Chris Li
    public void changeLocationSettings(LocationRequest locationRequest) {
        App.log(App.LOG_INFO, logTag_, "");
        if (locationRequest != null) {
            if (locationUpdateHandler_ != null) {
                Message message = locationUpdateHandler_.obtainMessage();
                message.arg1 = CHANGE_LOCATION_SETTINGS;
                message.obj = locationRequest;
                if (handlerThread_ != null && handlerThread_.isAlive()) {
                    locationUpdateHandler_.sendMessage(message);
                }
            }
        }
    }


    //Description: a function to stop the location updates
    //Author: Chris Li
    public void stopLocationUpdates() {
        App.log(App.LOG_INFO, logTag_, "");
        if (googleApiClient_ != null && googleApiClient_.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient_, locationListener_);
            googleApiClient_.disconnect();
        }
    }

    //Description: a function to start the location updates
    //Author: Chris Li
    private void startLocationUpdates(LocationRequest locationRequest) {
        App.log(App.LOG_INFO, logTag_, "");
        if (locationRequest != null) {
            if (googleApiClient_ != null && googleApiClient_.isConnected()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient_, locationRequest, locationListener_);
            }
        }
    }
}
