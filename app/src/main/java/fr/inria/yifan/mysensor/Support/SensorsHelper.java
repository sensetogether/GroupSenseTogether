package fr.inria.yifan.mysensor.Support;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import static fr.inria.yifan.mysensor.Support.Configuration.ENABLE_REQUEST_LOCATION;
import static fr.inria.yifan.mysensor.Support.Configuration.LOCATION_UPDATE_DISTANCE;
import static fr.inria.yifan.mysensor.Support.Configuration.LOCATION_UPDATE_TIME;
import static fr.inria.yifan.mysensor.Support.Configuration.PERMS_REQUEST_LOCATION;

/**
 * This class provides functions including initialize and reading data from sensors.
 */

public class SensorsHelper {

    private static final String TAG = "Sensors helper";
    // Declare GPS permissions
    private static final String[] LOCATION_PERMS = {Manifest.permission.ACCESS_FINE_LOCATION};
    private Activity mActivity;
    // Declare sensors and managers
    private Sensor mSensorLight;
    private Sensor mSensorProxy;
    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    // Declare sensing variables
    private float mLight;
    private float mProximity;
    private Location mLocation;
    // Declare light sensor listener
    private SensorEventListener mListenerLight = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            mLight = sensorEvent.values[0];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //PASS
        }
    };

    // Declare proximity sensor listener
    private SensorEventListener mListenerProxy = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            mProximity = sensorEvent.values[0];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //PASS
        }
    };

    // Declare location service listener
    private LocationListener mListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //PASS
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onProviderEnabled(String provider) {
            mLocation = mLocationManager.getLastKnownLocation(provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            //PASS
        }
    };

    // Register the broadcast receiver with the intent values to be matched
    public SensorsHelper(Activity activity) {
        mActivity = activity;
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        assert mSensorManager != null;
        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorProxy = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        // Start to sense the light and proximity
        mSensorManager.registerListener(mListenerLight, mSensorLight, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mListenerProxy, mSensorProxy, SensorManager.SENSOR_DELAY_UI);
        mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        initialization();
    }

    // Unregister the broadcast receiver and listeners
    public void close() {
        mSensorManager.unregisterListener(mListenerLight);
        mSensorManager.unregisterListener(mListenerProxy);
        mLocationManager.removeUpdates(mListenerGPS);
    }

    // Get the most recent light density
    public float getLightDensity() {
        return mLight;
    }

    // Get thr most recent proximity value
    public float getProximity() {
        return mProximity;
    }

    // Get location information from GPS
    public Location getLocation() {
        Log.d(TAG, "Location information: " + mLocation);
        return mLocation;
    }

    // Check related user permissions
    public void initialization() {
        // Check GPS enable switch
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(mActivity, "Please enable the GPS", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mActivity.startActivityForResult(intent, ENABLE_REQUEST_LOCATION);
        } else {
            // Check user permission for GPS
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mActivity, "Requesting GPS permission", Toast.LENGTH_SHORT).show();
                // Request permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mActivity.requestPermissions(LOCATION_PERMS, PERMS_REQUEST_LOCATION);
                } else {
                    Toast.makeText(mActivity, "Please give GPS permission", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Start GPS and location service
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_TIME, LOCATION_UPDATE_DISTANCE, mListenerGPS);
            }
        }
    }
}
