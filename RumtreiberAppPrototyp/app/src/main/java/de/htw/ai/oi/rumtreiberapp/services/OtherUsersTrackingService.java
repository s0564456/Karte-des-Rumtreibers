package de.htw.ai.oi.rumtreiberapp.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.content.pm.PackageManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import de.htw.ai.oi.rumtreiberapp.activities.RegistrationActivity;
import de.htw.ai.oi.rumtreiberapp.data.LocationEntry;
import de.htw.ai.oi.rumtreiberapp.network.NetworkManager;


/**
 * Aufgaben:
 *  Ermitteln des eigenen Standorts
 *  Positionsübertragung an Server
 */
public class OtherUsersTrackingService extends Service {
    /** Tag für die LogCat. */
    private static final String TAG =
            OtherUsersTrackingService.class.getSimpleName();
    private static final int MIN_TIME = 20000;
    private static final int MIN_DISTANCE = 50; //meter
    private Handler mapsActivityCallbackHandler;
    private final IBinder trackingServiceBinder = new TrackingServiceBinder();

    public OtherUsersTrackingService() {
    }

    // ------------- Service Methods ------------------------------------------------------------

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "MyPositionUpdateService->" +
                "onBind(): aufgerufen...");
        return trackingServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "MyLocationListener->" +
                "onUnbind(): aufgerufen...");
        mapsActivityCallbackHandler = null;
        return super.onUnbind(intent);

    }


    // ------------- LocationListener anmelden  ------------------------------------------------------------

    private void startTracker(){
        Log.d(TAG, "OtherUsersTrackingService->" +
                "startTracker(): wurde aufgerufen.");
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean run  =true;
                while (run) {
                    Collection<LocationEntry> res = NetworkManager.getAllLocationEntries();
                    if (res != null){
                        for (LocationEntry entry : res) {
                            if (mapsActivityCallbackHandler != null
                                    && !entry.getName().equalsIgnoreCase(RegistrationActivity.USER_NAME)) {
                                final Bundle bundle = new Bundle();
                                bundle.putString("person", entry.getName());
                                bundle.putParcelable(
                                        "position",
                                        null);
                                bundle.putParcelable(
                                        "locationEntry",
                                        entry);
                                final Message msg = new Message();
                                msg.setData(bundle);

                                mapsActivityCallbackHandler.sendMessage(msg);

                            }
                            Log.d(TAG, "OtherUsersTrackingService->" +
                                    "startTracker(): alle empfangenen Einträge gesendet.");
                        }
                    }
                    else{
                        Log.d(TAG, "OtherUsersTrackingService->" +
                                "startTracker(): keine Daten vom Server erhalten!");
                    }
                    try {
                        Thread.currentThread().sleep(MIN_TIME);
                        //Thread.currentThread().wait(MIN_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        run = false;
                    }
                }
            }
        }).start();


    }


    /**
     * Binder ermoeglichen es, auf Attribute und Methoden des Services zuzugreifen. Via RPC
     */
    public class TrackingServiceBinder extends Binder {

        public void setCallbackHandler(Handler handler){
            mapsActivityCallbackHandler = handler;
        }

        public void startProvider(){
            startTracker();
        }
    }


    // ------------------ Netzwerk ----------------------------------------------------------------



    private void sendLocationEntry(LocationEntry locationEntry){



    }


}
