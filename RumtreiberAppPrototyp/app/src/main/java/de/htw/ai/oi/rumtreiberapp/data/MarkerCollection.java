package de.htw.ai.oi.rumtreiberapp.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;

/**
 * adminstratetes received LocationEntry-Objects and Markes. Because Markers couldn't be added to
 * the GoogleMap, there is still some buisness logic to do in MapActivity.
 * First refreshLocation, then refreshMarkers (which produces a MarkerOptions object, that you can use to create a marker )
 * and last give the marker the updateActualMarker-method (needed for internal purposes)
 *
 */
public class MarkerCollection {

    private Context mapsContext;
    private LocationEntry locationEntry;
    private Marker actual, past=null;

    public MarkerCollection(LocationEntry locationEntry, Context context){
        this.locationEntry = locationEntry;
        this.mapsContext = context;
    }

    public String getName(){
        return locationEntry.getName();
    }

    public boolean isLocationEntryNewer(LocationEntry locationEntry){
        if (this.locationEntry.getLastTimestamp() != null && locationEntry.getLastTimestamp() != null){
            return this.locationEntry.getLastTimestamp().before(locationEntry.getLastTimestamp());
        }
        else if (this.locationEntry.getLastTimestamp() == null){
            return true;
        }
        return false;
    }

    public void refreshLocation(LocationEntry locationEntry){
        this.locationEntry = locationEntry;
    }

    /**
     * copies actual LoacationEntry values to the the secondLast Values.
     * @param locationEntry
     * @param flag:   indicates that LocationEntry only contains last values, but not the second lasts.
     */
    public void refreshLocation(LocationEntry locationEntry, int flag){
        if (locationEntry.getSecondLastTimestamp() == null){
            locationEntry.setSecondLastLongitude(this.locationEntry.getLastLongitude());
            locationEntry.setSecondLastLatitude(this.locationEntry.getLastLatitude());
            locationEntry.setSecondLastTimestamp(this.locationEntry.getLastTimestamp());
            locationEntry.setSecondLastDirection(this.locationEntry.getLastDirection());
        }
        this.locationEntry = locationEntry;
    }


    public void updateActualMarker(Marker marker){
        if (past != null){
            past.remove();
        }
        past = actual;
        if (past != null){
            past.setAlpha(0.5f);
        }
        this.actual = marker;
        actual.showInfoWindow();
    }

    public MarkerOptions refreshMarkers(){

        String pattern = "dd-MM-yyyy 'um' HH:mm:ss z";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        LatLng pos =  new LatLng( locationEntry.getLastLatitude(), locationEntry.getLastLongitude() );
        float rot = locationEntry.getLastDirection();
        String snippet = simpleDateFormat.format(locationEntry.getLastTimestamp());
        return new MarkerOptions().position(pos).title(locationEntry.getName()).snippet(snippet)
                .icon( BitmapDescriptorFactory.fromBitmap(resizeBitmap(
                        "footprints2", 75,75))
                )
                .flat(true)
                .anchor(0.5f,0.5f)
                .rotation(rot);
    }

    // Quelle: https://stackoverflow.com/questions/41509791/how-to-fix-custom-size-of-google-maps-marker-in-android
    public Bitmap resizeBitmap(String drawableName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(
                mapsContext.getResources(),
                mapsContext.getResources().getIdentifier(drawableName, "drawable", mapsContext.getPackageName())
        );
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }
}
