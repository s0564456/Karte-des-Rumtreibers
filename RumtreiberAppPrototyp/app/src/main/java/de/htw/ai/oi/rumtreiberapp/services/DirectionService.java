package de.htw.ai.oi.rumtreiberapp.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class DirectionService extends Service implements SensorEventListener {

    private static final String TAG =
            DirectionService.class.getSimpleName();
    /*
    Direction kann Werte zwischen 0 und 360 beinhalten, 1000 wenn DirectionService nicht laeuft
     */
    public static float direction = 1000;

    //private final IBinder  directionServiceBinder = new DirectionServiceBinder();
    private SensorManager mSensorManager;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

   // private Handler mapsActivityCallbackHandler;
// https://developer.android.com/guide/topics/sensors/sensors_position#java

    public DirectionService() {
    }

    public void onCreate(){
        Log.d(TAG, "DirectionService->" +
                "onCreate(): aufgerufen...");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = null;
        try{
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } catch (NullPointerException e){
            Log.d(TAG, "DirectionService->" +
                    "onCreate(): getDefaultSensor wirft NullPointerException: " + e.getMessage());
        }
        if (accelerometer != null) {
            mSensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            mSensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void onDestroy(){
        mSensorManager.unregisterListener(this);
        direction = 1000;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "DirectionService->" +
                "onBind(): aufgerufen...");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "DirectionService->" +
                "onUnbind(): aufgerufen...");
        return super.onUnbind(intent);
    }


    /*
    * ----------------------------   SensorEventListener- Methoden ---------------------------------------
    * */

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    /**
     * Called when there is a new sensor event.  Note that "on changed"
     * is somewhat of a misnomer, as this will also be called if we have a
     * new reading from a sensor with the exact same sensor values (but a
     * newer timestamp).
     *
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     *
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading,
                    0, mAccelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading,
                    0, mMagnetometerReading.length);
        }
        updateOrientationAngles();
        direction = (float) Math.toDegrees(mOrientationAngles[0]);
        Log.d(TAG, "DirectionService->" +
                "onSensorChanged(): neue Richtung in Bogenmaß : " + mOrientationAngles[0]);
        Log.d(TAG, "DirectionService->" +
                "onSensorChanged(): neue Richtung in Grad: " + direction);
    }
    /**
     * Called when the accuracy of the registered sensor has changed.  Unlike
     * onSensorChanged(), this is only called when this accuracy value changes.
     *
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "DirectionService->" +
                "onAccuracyChanged(): Sensorgenauigkeit hat sich verändert zu " + accuracy);
    }



    // -----------------------------------------------------------------------------------------------------

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        Log.d(TAG, "DirectionService->" +
                "updateOrientationAngles(): aufgerufen...");
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);
        // "mRotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
        // "mOrientationAngles" now has up-to-date information.

        Log.d(TAG, "DirectionService->" +
                "updateOrientationAngles(): aufgerufen...");

    }



}

