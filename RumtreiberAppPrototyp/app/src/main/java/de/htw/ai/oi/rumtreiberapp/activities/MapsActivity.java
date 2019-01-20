package de.htw.ai.oi.rumtreiberapp.activities;

import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import de.htw.ai.oi.rumtreiberapp.R;
import de.htw.ai.oi.rumtreiberapp.data.LocationEntry;
import de.htw.ai.oi.rumtreiberapp.data.MarkerCollection;
import de.htw.ai.oi.rumtreiberapp.services.DirectionService;
import de.htw.ai.oi.rumtreiberapp.services.MyPositionUpdateService;
import de.htw.ai.oi.rumtreiberapp.services.OtherUsersTrackingService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    /** Tag für die LogCat. */
    private static final String TAG =
            MapsActivity.class.getSimpleName();
    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 122;


    private static HashMap<String, MarkerCollection> positions = new HashMap<>();
    //private static Collection<MarkerCollection> markers =
    private static MarkerCollection myMarker;

    //---------------- Callbackhandler --------------------------------------------------------------

    private Handler mapsActivityCallbackHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Handler->handleMessage(): Handler: Message empfangen. " + this);
            //Log.d(TAG, "Handler->handleMessage(): mMap: " + mMap);
            final Bundle bundle = msg.getData();
            if (bundle != null){
                final LatLng latLng = (LatLng) bundle.get("position");
                final String person = (String ) bundle.get("person");
                final LocationEntry locationEntry = (LocationEntry) bundle.get("locationEntry");
                if (locationEntry == null){
                    Log.e(TAG, "Handler->handleMessage(): Empfangener LocationEntry == null !!" );
                }
                else {
                    Log.d(TAG, "Handler->handleMessage(): Empfangene Position: " + locationEntry.getLastLatitude() + ", " + locationEntry.getLastLongitude());
                    boolean refresh = true;
                    if (person != null) {
                        MarkerOptions markerOptions;
                        MarkerCollection markerCollection;
                        if (person.equalsIgnoreCase("Ich")) {
                            // locationEntry enthält keinen alten Marker, da nicht bekannt
                            myMarker.refreshLocation(locationEntry, 1);
                            markerCollection = myMarker;
                            Log.d(TAG, "Handler->handleMessage(): eigene Position bearbeitet   ");

                            // Kamera ausrichten
                            LatLng pos1 = new LatLng(locationEntry.getLastLatitude(), locationEntry.getLastLongitude());
                            LatLng pos2 = new LatLng(locationEntry.getSecondLastLatitude(), locationEntry.getSecondLastLongitude());
                            moveCamera(new LatLng[]{pos1, pos2});
                        } else if (!positions.containsKey(person)) {
                            //positions.put(person, new LatLng[]{latLng, null});
                            positions.put(person, new MarkerCollection(locationEntry, MapsActivity.this));
                            markerCollection = positions.get(person);
                            Log.d(TAG, "Handler->handleMessage(): neue Person hinzugefuegt:   " + person);
                        } else {
                            markerCollection = positions.get(person);
                            if (markerCollection.isLocationEntryNewer(locationEntry)){
                                positions.get(person).refreshLocation(locationEntry);
                                Log.d(TAG, "Handler->handleMessage(): Position bekannter Person veraendert:   " + person);
                            }
                            else {
                                refresh = false;
                                Log.d(TAG, "Handler->handleMessage(): Position bekannter Person nicht veraendert, da nicht neu:   " + person);
                            }

                            /*
                            LatLng[] array = positions.get(person);
                            LatLng oldPos = array[0];
                            array[0] = latLng;
                            array[1] = oldPos;  */
                        }
                        if (refresh){
                            markerOptions = markerCollection.refreshMarkers();
                            markerCollection.updateActualMarker(setMarker(markerOptions));
                        }

                        /*
                        Log.d(TAG, "Handler->handleMessage(): latLng =  " + latLng);
                        setMarker(latLng, person); */
                    }
                }
            }
            super.handleMessage(msg);
        }
    };


    // ----------- ServiceConnection  -------------------------------------------------------------

    private ServiceConnection connectionToUpdateService = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "MapsActivity->" +
                    "connectionToUpdateService(): neue Service Connection erstellt.");
            ((MyPositionUpdateService.PositionServiceBinder) iBinder)
                    .setCallbackHandler(mapsActivityCallbackHandler);
            ((MyPositionUpdateService.PositionServiceBinder) iBinder).startProvider();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "MapsActivity->" +
                    "onServiceDisconnected(): Was geschieht mit Service Connection? .");
            // nichts zu tun, da :
        }
    };

    private ServiceConnection connectionToTrackingService = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "MapsActivity->" +
                    "connectionToTrackingService(): neue Service Connection erstellt.");
            ((OtherUsersTrackingService.TrackingServiceBinder) iBinder)
                    .setCallbackHandler(mapsActivityCallbackHandler);
            ((OtherUsersTrackingService.TrackingServiceBinder) iBinder).startProvider();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "MapsActivity->" + "connectionToTrackingService ->" +
                    "onServiceDisconnected(): Was geschieht mit Service Connection? .");
            // nichts zu tun, da :
        }
    };


    // -----------------  Grundlegende Activity Methoden ------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Rechte ueberpruefen
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Log.d(TAG, "MapsActivity->" +
                        "onCreate(): ACCESS_FINE_LOCATION shouldShowRequestPermissionRationale wahr.");
            }
            else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST_CODE);
                Log.d(TAG, "MapsActivity->" +
                        "onCreate(): requestPermissions aufgerufen.");
            }
        }
        else {
            Log.d(TAG, "MapsActivity->" +
                    "onCreate(): ACCESS_FINE_LOCATION Permission besteht.");
        }


        // Registrierung, falls erforderlich
        if (RegistrationActivity.REGISTRATION_ID == -1){
            Log.d(TAG, "MapsActivity->" +
                    "onCreate(): Registrierung erforderlich.");
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        }

        //init der MarkerCollections
        initHashMap();

        Log.d(TAG, "MapsActivity->" +
                "onCreate(): MarkerCollections initialisiert.");
        Log.d(TAG, "MapsActivity->" +
                "onCreate(): connectionToUpdateService == null ? " + (connectionToUpdateService == null));

        // Starten des Richtungsdienstes  -- vielleicht besser in onResume() aufgehoben?
        final Intent directionServiceIntent = new Intent(this, DirectionService.class);
        startService(directionServiceIntent);

        // Starten des UpdatePositionServices
        // ? notwendig beim allerersten Start, aber nicht mehr bei allen folgenden
        final Intent updatePositionIntent = new Intent(this, MyPositionUpdateService.class);
        Log.d(TAG, "MapsActivity->" +
                "onCreate(): updatePositionIntent == null ? " + (updatePositionIntent == null));
        bindService(updatePositionIntent, connectionToUpdateService, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume(){

        final Intent otherUsersTrackingIntent = new Intent(this, OtherUsersTrackingService.class);
        Log.d(TAG, "MapsActivity->" +
                "onResume(): otherUsersTrackingIntent == null ? " + (otherUsersTrackingIntent == null));
        bindService(otherUsersTrackingIntent, connectionToTrackingService, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause(){
         unbindService(connectionToTrackingService);
        stopService(new Intent(this, OtherUsersTrackingService.class));
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        stopService(new Intent(this, DirectionService.class));
        /*
        mapsActivityCallbackHandler.removeCallbacksAndMessages(null);
        unbindService(connectionToUpdateService);
        // hier: stopService(new Intent(this, MyPositionUpdateService.class))
        // eigentlich soll der Service doch auch noch laufen, wenn die App geschlossen ist
        // fragt sich nur, wann der Service überhaupt gestoppt werden soll und ob bei Neustart laufender Service wiedergefunden wird  */
        unbindService(connectionToUpdateService);
        super.onDestroy();
    }



    // -----------------  Weitergehende Methoden ------------------------------------------------------

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "MapsActivity->" +
                "onMapReady(): Karte bereit.");
        mMap = googleMap;
        Log.d(TAG, "MapsActivity->" +
                "Karte ist bereit...");
        // move the camera
        LatLng berlin = new LatLng(52.523434, 13.3270716);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(berlin));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13f));
    }


    private Marker setMarker(MarkerOptions markerOptions){
        Log.d(TAG, "MapsActivity->" + " setMarker(): Marker wird gesetzt." );
        return mMap.addMarker(markerOptions);
    }

    @Deprecated
    private Marker setMarker(LatLng pos, String name){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13f));
        return mMap.addMarker(new MarkerOptions().position(pos).title(name).snippet("Snippet")
                .icon( BitmapDescriptorFactory.fromBitmap(resizeBitmap(
                        "footprints2", 75,75))
                )
                .flat(true)
                .anchor(0.5f,0.5f)
                .rotation((float) 90.1) );
    }

    private void moveCamera(LatLng pos){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13f));
    }

    // Quelle: https://stackoverflow.com/questions/24812483/how-to-create-bounds-of-a-android-polyline-in-order-to-fit-the-screen
    private void moveCamera(LatLng[] pos ){
        mMap.setMaxZoomPreference(16f);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0; i < pos.length;i++){
            builder.include(pos[i]);
        }
        LatLngBounds bounds = builder.build();
        int padding = 200; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
        mMap.resetMinMaxZoomPreference();
    }

    // Quelle: https://stackoverflow.com/questions/41509791/how-to-fix-custom-size-of-google-maps-marker-in-android
    public Bitmap resizeBitmap(String drawableName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(drawableName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }


    private void initHashMap(){
        myMarker = new MarkerCollection(new LocationEntry("Ich", null), this);
        // --- positions.put("Ich", new MarkerCollection(new LocationEntry(),this));
    }
}


