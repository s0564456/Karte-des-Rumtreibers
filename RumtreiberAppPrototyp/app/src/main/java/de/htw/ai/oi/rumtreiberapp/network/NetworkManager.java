package de.htw.ai.oi.rumtreiberapp.network;

import android.util.Log;

import de.htw.ai.oi.rumtreiberapp.activities.RegistrationActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import de.htw.ai.oi.rumtreiberapp.data.LocationEntry;

import static de.htw.ai.oi.rumtreiberapp.network.UnixEpochDateTypeAdapter.getUnixEpochDateTypeAdapter;

public class NetworkManager {

    public final static String SERVER_URL_SEC = "https://rumtreiber.f4.htw-berlin.de:8080/rumtreiber/data/tunichtgut";
    //public final static String SERVER_URL = "http://rumtreiber.f4.htw-berlin.de:8080/rumtreiber/data/tunichtgut/testdb";
    public final static String SERVER_URL = "http://rumtreiber.f4.htw-berlin.de:8080/rumtreiber/data/tunichtgut";
    public static int TIMEOUT = 5000;
    private static final String TAG = NetworkManager.class.getSimpleName();

    public static synchronized boolean registrate(String username) {
        URL url;
        String line = null;
        String userNameURLEncoded = username;
        try {
            userNameURLEncoded = URLEncoder.encode(username, "UTF-8");
            url = new URL(SERVER_URL + "?userName=" + userNameURLEncoded);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();


            if (con != null) {
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(false);
                con.setDoInput(true);
                con.setConnectTimeout(TIMEOUT);
                DataOutputStream outputStream;

                // read data
                InputStream inputStream = con.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    /*
                    while ((line1= bufferedReader.readLine())  != null) {
                        line = line1;
                    }*/
                line = bufferedReader.readLine();
                Log.d(TAG, "registrate(): gelesen: " + line);
                bufferedReader.close();
                inputStream.close();
                con.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            line = "IOException";
            e.printStackTrace();
        }


        if (line != null && line.equalsIgnoreCase("IOException")) {
            Log.d(TAG, "registrate(): IOException: line ist = " + line);
            line = null;
        }

        if (line == null) {
            Log.d(TAG, "registrate(): line ist hoffentlich null? " + line);
            return false;
        } else {
            Log.d(TAG, "registrate(): line ist hoffentlich nicht null? " + line);
            Log.d(TAG, "registrate(): vom Server empfangen: " + Long.parseLong(line));
            RegistrationActivity.REGISTRATION_ID = Long.parseLong(line);
            RegistrationActivity.USER_NAME = username;
        }
        return true;
    }


    //----------------------------- POST LOCATION ----------------------------------------------------

    public static synchronized boolean postNewLocationEntry(LocationEntry locationEntry) {

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().
                registerTypeAdapter(Date.class, getUnixEpochDateTypeAdapter()).create();
        String json = gson.toJson(locationEntry);
        byte[] compressedData = json.getBytes();
        Log.d(TAG, "postNewLocationEntry(): JSON erstellt:  \n" + json);

        String http_url = SERVER_URL;
        URL url;
        try {
            url = new URL(http_url );
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String authHeader = URLEncoder.encode(String.valueOf(RegistrationActivity.REGISTRATION_ID), "UTF-8");

            if (con != null) {
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", authHeader);
                con.addRequestProperty("auth", authHeader);
                con.setDoOutput(true);
                con.setDoInput(false);
                con.setConnectTimeout(TIMEOUT);
                DataOutputStream outputStream;
                try {
                    // Send data
                    outputStream = new DataOutputStream(con.getOutputStream());
                    outputStream.write(compressedData);
                    outputStream.flush();
                    outputStream.close();
                    Log.d(TAG, "postNewLocationEntry(): JSON wurde in den outputStream geschrieben.");
                    Log.d(TAG, "Status erfolgreich?:    " + con.getResponseCode());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "postNewLocationEntry(): IOException");
                    return false;
                } finally {
                    if (con != null){
                        Log.d(TAG, "Status:    " + con.getResponseCode());
                        con.disconnect();
                    }
                    else {
                        Log.d(TAG, "Connection == null!");
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG, "postNewLocationEntry(): MalformedURLException");
            return false;
        } catch (IOException e) {
            Log.d(TAG, "postNewLocationEntry(): IOException 2");
            e.printStackTrace();
            return false;
        }
        return true;
    }



    //----------------------------- GET LOCATIONS ----------------------------------------------------

    // https://stackoverflow.com/questions/10500775/parse-json-from-httpurlconnection-object
    public static synchronized Collection<LocationEntry> getAllLocationEntries() {
        ArrayList<LocationEntry> res = new ArrayList<LocationEntry>();

        /*
        LocationEntry l = new LocationEntry("Max Tester", new Timestamp(new Date().getTime()) );
        l.setLastLongitude(13.4442);
        l.setLastLatitude(53.13442);
        res.add(l); */

        String jsonResponse = null;
        String http_url = SERVER_URL + "/locations";
        URL url;
        HttpURLConnection con = null;
        try {
            url = new URL(http_url);
            con = (HttpURLConnection) url.openConnection();
            String authHeader = URLEncoder.encode(String.valueOf(RegistrationActivity.REGISTRATION_ID), "UTF-8");
            Log.d(TAG, "getAllLocationEntries(): Authheader : "+authHeader );
            if (con != null) {

                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", authHeader);
                con.addRequestProperty("auth", authHeader);
                con.setRequestProperty("Accept", "application/json");
                Log.d(TAG, "getAllLocationEntries(): Requestproperties: "+ con.getRequestProperty("auth") + "\t" + con.getRequestProperty("Authorization") );
                con.setDoOutput(false);
                con.setDoInput(true);
                con.setConnectTimeout(TIMEOUT);
                con.setReadTimeout(TIMEOUT);

                con.connect();
                int status = con.getResponseCode();
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        jsonResponse = sb.toString();
                        Log.d(TAG, "getAllLocationEntries(): Connection Status 200 bzw 201, ausgelsen : " + jsonResponse);
                        break;
                    default:
                        Log.d(TAG, "getAllLocationEntries(): ungewöhnlicher Connection Status: " + status);

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(NetworkManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (con != null) {
                try {
                    con.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(NetworkManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                Log.d(TAG, "getAllLocationEntries(): Connection nicht aufgebaut. ");
            }
        }

        // JSON to LocationEntry
        List<LocationEntry> resList = null;
        if (jsonResponse != null){
            GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(Date.class, getUnixEpochDateTypeAdapter());
                    //.setDateFormat("yyyy-MM-dd hh:mm:ss.S");


            Type listType = new TypeToken<List<LocationEntry>>(){}.getType();
            //resList = new Gson().fromJson(jsonResponse, listType);
            resList = gsonBuilder.create().fromJson(jsonResponse, listType);
            Log.d(TAG, "getAllLocationEntries(): ermittelt wurde u.a. : "+resList.get(0).getName());
        }
        else{
            Log.d(TAG, "getAllLocationEntries(): keine JSON-Daten erhalten. ");
        }
        return resList;
    }


}
