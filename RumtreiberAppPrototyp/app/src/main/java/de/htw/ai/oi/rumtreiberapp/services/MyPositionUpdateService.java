package de.htw.ai.oi.rumtreiberapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import java.sql.Timestamp;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import de.htw.ai.oi.rumtreiberapp.data.LocationEntry;
import de.htw.ai.oi.rumtreiberapp.network.NetworkManager;


/**
 * Aufgaben:
 *  Ermitteln des eigenen Standorts
 *  Positionsübertragung an Server
 */
public class MyPositionUpdateService extends Service implements LocationListener {
    /** Tag für die LogCat. */
    private static final String TAG =
            MyPositionUpdateService.class.getSimpleName();
    private static final int MIN_TIME = 10000;
    private static final int MIN_DISTANCE = 50; //meter
    private Handler mapsActivityCallbackHandler;
    private final IBinder  positionServiceBinder = new PositionServiceBinder();
    private SendLocationEntryTask mSendLocationTask = null;
    /*
    for reducing network trafic, send only every third time
     */
    private int count = 0;
    //private LatLng lastPosition = new LatLng()

    public MyPositionUpdateService() {
    }


    // ------------- Service Methods ------------------------------------------------------------

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "MyPositionUpdateService->" +
                "onBind(): aufgerufen...");
        return positionServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "MyLocationListener->" +
                "onUnbind(): aufgerufen...");
        mapsActivityCallbackHandler = null;
        //return super.onUnbind(intent);
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "MyLocationListener->" +
                "onRebind(): aufgerufen...");
        super.onRebind(intent);
    }

    // ------------- LocationListener Methods ------------------------------------------------------------

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "MyPositionUpdateService->" +
                "onLocationChanged(): entered...");
        Date date = new Date();
        LocationEntry locationEntry = new LocationEntry("Ich", new Timestamp(date.getTime()));
        if (location != null) {
            Log.d(TAG,
                    "MyPositionUpdateService->onLocationChanged(): " +
                            "Längengrad: " + location.getLongitude());
            Log.d(TAG,
                    "MyPositionUpdateService->onLocationChanged(): " +
                            "Breitengrad: " + location.getLatitude());

            // mGpsData = new GpsData(location);
            LatLng latLng = new LatLng( location.getLatitude(), location.getLongitude());

            // insert
            locationEntry.setLastLatitude(location.getLatitude());
            locationEntry.setLastLongitude(location.getLongitude());
            locationEntry.setLastDirection(DirectionService.direction);
            // don't show 0,0
            locationEntry.setSecondLastLatitude(location.getLatitude()+0.5);
            locationEntry.setSecondLastLongitude(location.getLongitude());


            // nur wenn gerade die Activity MapsActivity
            // angezeigt wird:
            Log.d(TAG,
                    "MyPositionUpdateService->onLocationChanged(): " +
                            "mapsActivityCallbackHandler == null?: " + (mapsActivityCallbackHandler == null));
            if (mapsActivityCallbackHandler != null) {
                final Bundle bundle = new Bundle();
                bundle.putString("person", "Ich");
                bundle.putParcelable(
                        "position",
                        latLng);
                bundle.putParcelable(
                        "locationEntry",
                        locationEntry
                );
                Log.d(TAG,
                        "MyPositionUpdateService->onLocationChanged(): " +
                                "Bundle erstellt, ist Position eingetragen?: " + bundle.get("position"));
                final Message msg = new Message();
                msg.setData(bundle);
                /*
                msg.what = KarteAnzeigen.TYP_EIGENE_POSITION; */

                mapsActivityCallbackHandler.sendMessage(msg);
            }

            // Upload (nur via WIFI) ...
            ConnectivityManager cm =
                    (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if (isConnected){
                switch (activeNetwork.getType()){
                    case ConnectivityManager.TYPE_WIFI:
                        // alle >30s oder nach >150m
                        if (count % 3 == 0) {
                            sendLocationEntry(locationEntry);
                        }
                        break;
                        // alle >90s oder nach >450m Distanz
                    case ConnectivityManager.TYPE_MOBILE:
                        if (count % 9 == 0) {
                            sendLocationEntry(locationEntry);
                        }
                }
            }
            else{
                // wenn sie an der Reihe waren zu senden, aber keine Verbindung bestand, bleiben sie an der Reihe
                if  (count % 3 == 0){
                    count--;
                }
            }
            count++;
            if (count > 1000000){
                count = 1;
            }

        }
    }

    // ------------- LocationListener anmelden  ------------------------------------------------------------

    private void startGeoProvider(){
        Log.d(TAG, "MyPositionUpdateService->" +
                "startGeoProvider(): wurde aufgerufen.");
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d(TAG, "MyPositionUpdateService->" +
                "startGeoProvider(): LocationManager == null ? " + (locationManager == null));
        final Criteria locationCriteria = new Criteria();
        locationCriteria.setAccuracy(Criteria.NO_REQUIREMENT);

        String provider = locationManager.getBestProvider(locationCriteria, true);
        // im Buch
        if (LocationManager.PASSIVE_PROVIDER.equalsIgnoreCase(provider)){
            provider = LocationManager.NETWORK_PROVIDER;
        }
        Log.d(TAG, "MyPositionUpdateService->" +
                "startGeoProvider(): provider == null ? " + (provider == null));
        if (provider == null){
            provider = LocationManager.NETWORK_PROVIDER;
            Log.d(TAG, "MyPositionUpdateService->" +
                    "startGeoProvider(): provider immer noch == null ? " + (provider == null));
        }


        try {
            locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, this);
            Log.d(TAG, "MyPositionUpdateService->" +
                    "startGeoProvider(): locationManager.requestLocationUpdates(provider...) hat geklappt ");
        }
        catch (SecurityException s){
            Log.d(TAG, "MyPositionUpdateService->" +
                    "startGeoProvider(): Security Exception gefangen.");
            s.printStackTrace();
        }
    }


    /**
     * Binder ermoeglichen es, auf Attribute und Methoden des Services zuzugreifen. Via RPC
     */
    public class PositionServiceBinder extends Binder{

        public LatLng myPosition;

        public void setMyPosition(LatLng position){
            myPosition = position;
        }

        public void setCallbackHandler(Handler handler){
            mapsActivityCallbackHandler = handler;
        }

        public void startProvider(){
            startGeoProvider();
        }
    }


    // ------------------ Netzwerk ----------------------------------------------------------------



    private void sendLocationEntry(LocationEntry locationEntry){
        // only setted to avoid NullPointerException in UnixEpochDateTypeAdapter:
        locationEntry.setSecondLastTimestamp(new Timestamp(0));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String myUserName = sharedPreferences.getString("userName","Ich");
        locationEntry.setName(myUserName);
        mSendLocationTask = new SendLocationEntryTask();
        mSendLocationTask.execute(locationEntry);
    }

    /**
     *
     */
    public class SendLocationEntryTask extends AsyncTask<LocationEntry, Void, Boolean> {

        SendLocationEntryTask() {
        }

        @Override
        protected Boolean doInBackground(LocationEntry... locationEntries) {
            return NetworkManager.postNewLocationEntry(locationEntries[0]);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSendLocationTask = null;

            if (success) {
                Log.d(TAG, "SendLocationEntryTask->onPostExecute(): AsyncTask erfolgreich, LocationEntry sollte gesendet sein.");
                // finish(); Loaderclass elemetn
            } else {
            }
        }

        @Override
        protected void onCancelled() {
            mSendLocationTask = null;
            //  showProgress(false);  Loaderclass elemetn
        }
    }

}
