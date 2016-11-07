package mad.com.applicationproject.misc;

import mad.com.applicationproject.unused.BluetoothManager;

/**
 * Defines several constants used between {@link BluetoothManager} and the UI.
 */

public interface Constants {

    // Message types sent from the BluetoothManager Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_DEVICE = 6;
    public static final int MESSAGE_SOCKET = 7;

    // Key names received from the BluetoothManager Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String DEVICE = "device_name";
    public static final String SOCKET = "device_name";
}