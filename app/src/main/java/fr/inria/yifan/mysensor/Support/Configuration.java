package fr.inria.yifan.mysensor.Support;

import java.io.Serializable;

/**
 * This class stores global configuration parameters for the application.
 */

public class Configuration implements Serializable {

    // Parameters for audio sound signal sampling
    public static final int SAMPLE_RATE_IN_HZ = 8000;

    // Parameters for sensing sampling
    public static final int SAMPLE_DELAY_IN_MS = 100;

    // Permission request indicator code
    public static final int PERMS_REQUEST_RECORD = 1000;
    public static final int PERMS_REQUEST_LOCATION = 1002;
    public static final int ENABLE_REQUEST_LOCATION = 1003;
    static final int PERMS_REQUEST_STORAGE = 1001;
    // Minimum time interval between location updates (milliseconds)
    static final int LOCATION_UPDATE_TIME = 100;
    // Minimum distance between location updates (meters)
    static final int LOCATION_UPDATE_DISTANCE = 5;
}
