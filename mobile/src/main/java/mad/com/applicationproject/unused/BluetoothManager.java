/*
 * Copyright (C) Kieran Hillier
 * All Rights Reserved
 */

package mad.com.applicationproject.unused;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import mad.com.applicationproject.activity.Settings;
import mad.com.applicationproject.misc.Constants;

/**
 * Does all the work for setting up and managing Bluetooth connections with other devices.
 * Uses threads for establishing connections and transmissions.
 */
public class BluetoothManager {

    private static final String TAG = BluetoothManager.class.getSimpleName();

    // Unique UUID for this application
    private static final UUID OBD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private ConnectionThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;


    private SharedPreferences mPreferences;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // Doing nothing
    public static final int STATE_CONNECTING = 1; // Initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  // Connected to a remote device

    /** Constructor. Prepares a new Obd Bluetooth session. */
    public BluetoothManager(Context context, Handler handler) {
        Log.i(TAG, "BluetoothManager created");
        mContext = context;
//        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    /** Sets the current connection state */
    private synchronized void setState(int state) {
        Log.d(TAG, "setServiceState() " + getStateName(mState) + " -> " + getStateName(state));
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    private synchronized String getStateName(int state) {
        switch (state) {
            case STATE_NONE:
                return "NONE";
            case STATE_CONNECTING:
                return "CONNECTING";
            case STATE_CONNECTED:
                return "CONNECTED";
        }
        return "?";
    }

    /** Returns the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /** Starts the bluetooth service. Called by the MainActivity onResume() */
    public synchronized void start() {
        Log.d(TAG, "start()");
        resetThreads();
        // Doesn'thread do anything at the moment
    }

    /** Stops threads and resets the connection state to NONE */
    public synchronized void stop() {
        Log.d(TAG, "stopService()");
        resetThreads();
        setState(STATE_NONE);
    }

    /** Returns true if not stopped */
    public synchronized boolean isStopped() {
        return mState == STATE_NONE;
    }

    /** Stops all threads */
    private synchronized void resetThreads() {
//        Log.d(TAG, "resetThreads()");
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

//    private BluetoothDevice getDevice() {
//        final String selectedDevice = mPreferences.getString(Settings.BLUETOOTH_DEVICE, null);
//        if (selectedDevice == null || "".equals(selectedDevice)) {
//            return requestDevice();
//        } else {
//            return mBluetoothAdapter.getRemoteDevice(selectedDevice);
//        }
//    }
//
//    private BluetoothDevice requestDevice() {
//        // Create an instance of the dialog fragment and show it;
//
//        Toast.makeText(mContext, mContext.getString(R.string.bluetooth_no_device),
//                Toast.LENGTH_LONG).show();
//        stopService();
//        return null;
//    }

    /** Starts ConnectionThread to initiate a connection to a remote device. */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "CONNECTING to: " +  device.getAddress() + ", " + device.getName());
        resetThreads();

        // Start the thread to connect with the given device
        mConnectThread = new ConnectionThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    /** Starts ConnectionThread to initiate a connection to a remote device. */
    public synchronized void connect() {

        // get MAC address from preferences
        String address = mPreferences.getString(Settings.BLUETOOTH_DEVICE, null);

        if (address != null) {

            // Get the BluetoothDevice object
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

            if (device != null) {
                Log.d(TAG, "CONNECTING to: " +  device.getAddress() + ", " + device.getName());

                // Start the ConnectionThread to attempt to connect to the device
                resetThreads();
                mConnectThread = new ConnectionThread(device);
                mConnectThread.start();
                setState(STATE_CONNECTING);
            }
        }
    }

    /** Starts ConnectedThread to begin managing Bluetooth connection */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "CONNECTED to: " +  device.getAddress() + ", " + device.getName());
        resetThreads();

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connectionSuccess device back to the UI Activity
        Message message = mHandler.obtainMessage(Constants.MESSAGE_DEVICE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.DEVICE, device);
        message.setData(bundle);
        mHandler.sendMessage(message);

        setState(STATE_CONNECTED);
    }
    /** Write to ConnectedThread in an unsynchronized manner */
    public void write(byte[] out) {
        Log.d(TAG, "write()");
        // Create temporary object
        ConnectedThread connectedThread;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            connectedThread = mConnectedThread;
        }
        // Perform the write unsynchronized
        connectedThread.write(out);
    }

    /** Notifies the UI Activity that the connection failed or was lost. */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message message = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Bluetooth connection Failed");
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    /**
     * Runs while attempting connect to remote a device. The connection either succeeds or fails.
     */
    private class ConnectionThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectionThread(BluetoothDevice device) {
            BluetoothSocket tmpSocket = null;
            mDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                tmpSocket = device.createRfcommSocketToServiceRecord(OBD_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Exception thrown while assigning UUID to Bluetooth socket");
            }
            mSocket = tmpSocket;
        }

        public void run() {
            Log.i(TAG, "BEGIN ConnectionThread");
            setName("ConnectionThread");

            // Cancel discovery because it's costly and we're about to connect
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // Blocks until success or throws an exception.
                mSocket.connect();
            } catch (IOException e) {
                //TODO: handle OBD exception
                //(https://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701details)

                // Close the socket
                try {
                    mSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // CONNECTION SUCCESS

            // Reset the ConnectionThread because we're done
            synchronized (BluetoothManager.this) {
                mConnectThread = null;
            }

            // Pass connectionSuccess socket elsewhere to be managed
            connected(mSocket, mDevice);
        }

        /** Close the connecting socket */
        public void cancel() {
            Log.i(TAG, "CLOSE ConnectionThread");
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * Runs while connectionSuccess with a remote device. Handles incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread{
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN ConnectedThread");

            byte[] buffer = new byte[1024];
            int bytes; // bytes returned from read()

            // Keeps listening to the InputStream while connectionSuccess (until an exception occurs)
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mInputStream.read(buffer);

                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionFailed();

                    // Start the service over to restart listening mode
                    BluetoothManager.this.start();
                    break;
                }
            }
        }

        /** Write to connectionSuccess OutStream */
        public void write(byte[] buffer) {
            try {
                mOutputStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        /** Close the socket connection */
        public void cancel() {
            Log.i(TAG, "CANCEL ConnectedThread");
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
