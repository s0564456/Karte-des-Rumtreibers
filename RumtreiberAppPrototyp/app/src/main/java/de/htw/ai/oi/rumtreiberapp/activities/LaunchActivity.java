package de.htw.ai.oi.rumtreiberapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import de.htw.ai.oi.rumtreiberapp.R;

public class LaunchActivity extends AppCompatActivity {

    private static final String TAG =
            LaunchActivity.class.getSimpleName();
    private static final int ACCESS_NETWORK_STATE_REQUEST_CODE = 123;
    private static final int INTERNET_REQUEST_CODE = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // --------- Permissions sichern -----------------------------------------------------------------------------------------
        if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE)){
                Log.d(TAG, "LaunchActivity->" +
                        "onCreate(): ACCESS_NETWORK_STATE) shouldShowRequestPermissionRationale wahr.");
            }
            else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, ACCESS_NETWORK_STATE_REQUEST_CODE);
                Log.d(TAG, "LaunchActivity->" +
                        "onCreate(): requestPermissions Network aufgerufen.");
            }
        }
        else {
            Log.d(TAG, "LaunchActivity->" +
                    "onCreate(): ACCESS_INTERNET Permission besteht.");
        }
        if (checkSelfPermission(Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)){
                Log.d(TAG, "LaunchActivity->" +
                        "onCreate(): INTERNET) shouldShowRequestPermissionRationale wahr.");
            }
            else{
                requestPermissions(new String[]{Manifest.permission.INTERNET}, INTERNET_REQUEST_CODE);
                Log.d(TAG, "LaunchActivity->" +
                        "onCreate(): requestPermissions INTERNET aufgerufen.");
            }
        }
        else {
            Log.d(TAG, "MapsActivity->" +
                    "onCreate(): INTERNET Permission besteht.");
        }

        // deleteSharedPreferences(); /*
        handleSharedPreferences();



        // Registrierung, falls erforderlich
        if (RegistrationActivity.REGISTRATION_ID == -1){
            Log.d(TAG, "LaunchActivity->" +
                    "onCreate(): Registrierung erforderlich.");
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        }
        else {
            Log.d(TAG, "LaunchActivity->" +
                    "onCreate(): Registrierung nicht erforderlich.");
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }

        // */
    }


    private void handleSharedPreferences(){
        // --------------- Registrierungsid aus Ressource strings einlesen
        // SharedPreferences sharedPreferences = this.getPreferences(MODE_PRIVATE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(TAG, "LaunchActivity->" +
                "handleSharedPreferences(): existiert Registrierungid?: "+ sharedPreferences.contains("registrationId"));
        String registrationNumber = sharedPreferences.getString("registrationId","-1");
        Long registrationId = Long.parseLong(registrationNumber);
        String userName = sharedPreferences.getString("userName", "Ich");
        Log.d(TAG, "LaunchActivity->" +
                "handleSharedPreferences(): welcher Username ist in den sharedPreferences?: "+ userName);

        if (registrationId != -1){
            Log.d(TAG, "LaunchActivity->" +
                    "handleSharedPreferences(): bestehende (!=-1) Registrierungid ausgelsen: " +
                    registrationId + "\nwird jetzt in Registration.USER_NAME gespeichert." );
            RegistrationActivity.REGISTRATION_ID = registrationId;
        }
        // eigentlich nur noetig bei Neustart der Anwendung, aber sicherhaltshalber
        if (!userName.equalsIgnoreCase("Ich")){
            Log.d(TAG, "LaunchActivity->" +
                    "handleSharedPreferences(): bestehenden (!=Ich) Usernamen ausgelsen: "
                    + userName + "\nwird jetzt in Registration.USER_NAME gespeichert  ");
            RegistrationActivity.USER_NAME = userName;
        }

    }


    private void deleteSharedPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userId");
        editor.remove("registrationId");
        editor.apply();
    }
}
