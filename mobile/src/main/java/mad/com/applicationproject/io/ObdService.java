/*
 * Copyright (C) Kieran Hillier
 * All Rights Reserved
 */

package mad.com.applicationproject.io;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.MisunderstoodCommandException;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.UnsupportedCommandException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import mad.com.applicationproject.activity.MainActivity;
import mad.com.applicationproject.activity.Settings;
import mad.com.applicationproject.io.ObdCommandJob.ObdCommandJobState;

/**
 * Connects and establishes communication with a Bluetooth OBD device
 */
public class ObdService extends Service {

    private static final String TAG = ObdService.class.getSimpleName();
    private static final UUID OBD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int COMMAND_TIME_OUT = 60;
    private static final int INITIALISATION_PAUSE = 60;

    private final IBinder mBinder = new ObdBinder();
    public class ObdBinder extends Binder {
        public ObdService getService() {
            return ObdService.this;
        }
    }

    private ObdServiceListener mObdServiceListener;

    private Context mContext;

    private int mQueueCounter = 0;
    private QueueThread mQueueThread;
    private final BlockingQueue<ObdCommandJob> mJobsQueue = new LinkedBlockingQueue<>();
    private boolean mIsRunning = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mDevice = null;
    private BluetoothSocket mConnectedSocket = null;
    private ConnectThread mConnectThread;

    /** The state of the service */
    private ObdServiceState mState;
    public enum ObdServiceState {
        CONNECTING,
        INITIALISING,
        RUNNING
    }

    public ObdServiceState getServiceState() {
        return mState;
    }

    private void setServiceState(ObdServiceState state) {
        mState = state;
    }

    private SharedPreferences mSharedPrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
                    Log.d(TAG, "onSharedPreferenceChanged()");
                    switch (key) {
                        case Settings.OBD_PROTOCOL:
                            // TODO: Live configuration
                            break;
                    }
                }
            };

    private List<ObdServiceListener> mListeners = new ArrayList<>();

    public void setObdServiceListener(ObdServiceListener listener) {
        mListeners.add(listener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        // get preferences and register a change listener
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPrefs.registerOnSharedPreferenceChangeListener(mPrefListener);

        // get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // create Bluetooth connection and queue threads
        mConnectThread = new ConnectThread();
        mQueueThread = new QueueThread();

        Log.v(TAG, "ObdService created");
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public boolean isQueueEmpty() {
        return mJobsQueue.isEmpty();
    }

    public void setContext(Context context) {
        mContext = context;
    }

//    public void connect(BluetoothDevice device) {
//        if (mDevice != device
//                && mBluetoothManager.getState() == BluetoothManager.STATE_NONE) {
//
//            if (mBluetoothManager == null) {
//                mBluetoothManager = new BluetoothManager(this, mHandler);
//            }
//            mBluetoothManager.connect(device);
//        } else {
//            toastShort("Already connected to " + device.getName());
//        }
//    }

    /** Gets the remote Bluetooth device then calls for the OBD connection to be started */
    public void start(){
        Log.i(TAG, "Starting ObdService");
        setServiceState(ObdServiceState.CONNECTING);

        // cancel discovery because it's costly and we're about to connect
        mBluetoothAdapter.cancelDiscovery();

        // get MAC address of selected Bluetooth device from preferences
        final String prefDeviceAddress = mSharedPrefs.getString(Settings.BLUETOOTH_DEVICE, null);

        // no device has been selected
        if (prefDeviceAddress == null || "".equals(prefDeviceAddress)) {
            Log.e(TAG, "No Bluetooth device has been selected.");
            Toast.makeText(mContext, "No Bluetooth Device selected", Toast.LENGTH_LONG).show();
            stopService();

        // a device has been selected
        } else {

            // get Bluetooth device object from the MAC address
            mDevice = mBluetoothAdapter.getRemoteDevice(prefDeviceAddress);

            // attempt to connect in a separate thread
            mConnectThread.start();
            // thread will call either connected() or connectionFailed()
        }
    }

    /**Stores connected socket and starts the OBD connection */
    private void connected(BluetoothSocket socket) {
        Log.i(TAG, "Connected socket with Bluetooth device: "
                + mDevice.getAddress() + ", " + mDevice.getName());
        mConnectedSocket = socket;
        startObd();
    }

    /** Connection failed. Stop service */
    private void connectionFailed() {
        Log.e(TAG, "Failed to connect with Bluetooth device: "
                + mDevice.getAddress() + ", " + mDevice.getName());
//        Toast.makeText(mContext, "Failed to connect with Bluetooth device", Toast.LENGTH_LONG).show();
        stopService();
    }

    /** Starts and configures the connection to the OBD interface */
    private void startObd() {
        setServiceState(ObdServiceState.INITIALISING);

        mQueueThread.start();
        mIsRunning = true;

        // Get OBD protocol from preferences
        final String protocol = mSharedPrefs.getString(Settings.OBD_PROTOCOL, "AUTO");

        // Send reset command then wait 500 milliseconds to give the adapter time to reset
        queueJob(new ObdCommandJob(new ObdResetCommand(), INITIALISATION_PAUSE));
        queueJob(new ObdCommandJob(new EchoOffCommand()));
        queueJob(new ObdCommandJob(new EchoOffCommand())); // sometimes needs to be sent twice
        queueJob(new ObdCommandJob(new LineFeedOffCommand()));
        queueJob(new ObdCommandJob(new TimeoutCommand(COMMAND_TIME_OUT))); // input (0-255) * 4 = timeout in milliseconds
        queueJob(new ObdCommandJob(new SelectProtocolCommand(ObdProtocols.valueOf(protocol))));

        // reset queue counter
        mQueueCounter = 0;
        Log.d(TAG, "Initialization commands queued");
    }

    /** Adds a command to be executed to the queue */
    public void queueJob(ObdCommandJob job) {
        mQueueCounter++;
        Log.v(TAG, "queueJob() - adding job[" + mQueueCounter + "] to queue");

        job.setId(mQueueCounter);
        try {
            mJobsQueue.put(job);
        } catch (InterruptedException e) {
            job.setState(ObdCommandJob.ObdCommandJobState.QUEUE_ERROR);
            Log.e(TAG, "queueJob() - Failed to queue job");
        }
    }

    /** Stops OBD / Bluetooth connection and clears queue */
    public void stopService() {
        Log.d(TAG, "Stopping ObdService...");

        // stop threads
        mQueueThread.interrupt();
        mConnectThread.interrupt();

        // clear any queued commands
        mJobsQueue.clear();
        mIsRunning = false;

        // if socket connected
        if (mConnectedSocket != null) {
            // close socket
            try {
                mConnectedSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "stopService() - unable to close socket - ", e);
            }
        }
        stopSelf();
        Log.d(TAG, "stopSelf() called");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    /**
     * Runs the queue until the service is stopped
     */
    private class QueueThread extends Thread {

        public void run() {
            Log.d(TAG, "QueueThread - Start thread");
            int previousJobId = 0;

//            // command execution time content_debug
//            float sum = 0L;
//            int count = 0;
//            Float max = 0f;
//            Float min = 10000f;

            while (!Thread.currentThread().isInterrupted()) {
                ObdCommandJob job = null;

                try {
                    // get next job to be executed
                    job = mJobsQueue.take();
                    Log.d(TAG, "QueueThread - Taking job[" + job.getId() + "] from queue...");

                    // if the previous job id is larger than the current
                    // it means the initialisation commands are finished
                    // and this is a data query command
                    if (previousJobId > job.getId()) {
                        setServiceState(ObdServiceState.RUNNING);
                    }
                    previousJobId = job.getId();


                    // if job is new, i.e. has not been executed before
                    if (job.getState().equals(ObdCommandJobState.NEW)) {
                        job.setState(ObdCommandJobState.RUNNING);

                        // if job is a command AND socket is connected
                        if (mConnectedSocket.isConnected()) {
                            // execute the command
                            job.getCommand().run(mConnectedSocket.getInputStream(),
                                    mConnectedSocket.getOutputStream());
//                            // command execution time content_debug
//                            Long now = job.getCommand().getEnd() - job.getCommand().getStart();
//                            sum += now;
//                            count++;
//                            Float avg = sum / count;
//                            if (now > max) max = now.floatValue();
//                            if (now < min) min = now.floatValue();
//
//                            final Float maxInt = max;
//                            final Float avgInt = avg;
//                            final Float minInt = min;
//                            final Float nowInt = now.floatValue();
//
////                            int percent = (int) (nowInt / maxInt * 100);
////                            Log.d(TAG,  " max: " + max.intValue()
////                                    + " | avg: " + avg.intValue()
////                                    + " | min: " + min.intValue()
////                                    + " | now: " + now.intValue()
////                                    + " | " + percent
////                                    +"% | completed command: " + job.getCommand().getName());
//
//                            final ObdCommandJob job2 = job;
//                            ((MainActivity) mContext).runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    ((MainActivity) mContext).debugCommandDelay(maxInt,avgInt,minInt,nowInt);
//                                }
//                            });
                        // if socket is not connected
                        } else {
                            job.setState(ObdCommandJobState.EXECUTION_ERROR);
                            Log.e(TAG, "QueueThread - Failed to run command -> socket is not connected");
                            stopService();
                        }
                        // if the job is telling the thread to wait
                        if (job.getWait() > 0) {
                            synchronized (this) {
                                //wait for as long as the job specifies
                                Log.d(TAG, "QueueThread - waiting " + job.getWait() + "ms...");
                                wait(job.getWait());
                            }
                        }

                    // (ERROR) if job is not new
                    } else {
                        Log.e(TAG, "QueueThread - Job state not new it shouldn't be in queue");
                        job.setState(ObdCommandJobState.EXECUTION_ERROR);
                    }

                // thread was interrupted
                } catch (InterruptedException i) {
                    interrupt();

                // command unknown/unsupported
                } catch (UnsupportedCommandException u) {
                    Log.w(TAG, "QueueThread - Command not supported -> " + u.getMessage());
                    if (job != null) {
                        job.setState(ObdCommandJobState.NOT_SUPPORTED);
                    }

                // broken pipe in the socket connection i.e. connection severed with remote device
                } catch (IOException io) {
                    Log.e(TAG, "QueueThread - IO error -> " + io.getMessage());
                    if (job != null) {
                        if (io.getMessage().contains("Broken pipe")) {
                            job.setState(ObdCommandJobState.BROKEN_PIPE);
                        } else {
                            job.setState(ObdCommandJobState.EXECUTION_ERROR);
                        }
                    }

                // command returned a "NODATA" message
                } catch (NoDataException nd) {
                    Log.d(TAG, "QueueThread - Unable to get result -> " + nd.getMessage());
                    if (job != null) {
                        job.setState(ObdCommandJobState.NO_DATA);
                    }

                // command returned a "?" message
                } catch (MisunderstoodCommandException ee) {
                    Log.v(TAG, "QueueThread - Unable to get result -> " + ee.getMessage());
                    if (job != null) {
                        job.setState(ObdCommandJobState.MISUNDERSTOOD);
                    }

                // command encountered any other errors
                } catch (Exception e) {
                    Log.e(TAG, "QueueThread - Failed to run command -> " + e.getMessage());
                    if (job != null) {
                        job.setState(ObdCommandJobState.EXECUTION_ERROR);
                    }
                }

                // send the completed command off to update the MainActivity
                if (job != null && job.getCommand() != null) {

                    // if no errors occurred, set stat to FINISHED
                    if (job.getState().equals(ObdCommandJobState.RUNNING)) {
                        job.setState(ObdCommandJobState.FINISHED);
                    }

                    final ObdCommandJob job2 = job;
                    ((MainActivity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            ((MainActivity) mContext).stateUpdate(job2);

                            for (ObdServiceListener listener : mListeners) {
                                listener.onCommandComplete(job2);
                            }
                        }
                    });
                }
            }
        }
    }


    /**
     * Attempts connection with a remote device
     */
    private class ConnectThread extends Thread {

        public void run() {
            Log.d(TAG, "ConnectThread - start thread");
            setName("ConnectThread");

            BluetoothSocket socket;
            BluetoothSocket fallback;

            try {
                // Get a BluetoothSocket for connecting with the Bluetooth device
                socket = mDevice.createRfcommSocketToServiceRecord(OBD_UUID);

                // attempt to connect the socket
                try {
                    socket.connect(); // blocks until successful or an exception is thrown

                // socket failed to connect. attempt to connect fallback socket
                } catch (IOException io) {
                    Log.w(TAG, "Error connecting. Attempting to connect fallback socket...", io);
                    /**
                     * This fix was sourced from here:
                     * http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701#18786701
                     */
                    Class<?> aClass = socket.getRemoteDevice().getClass();
                    Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
                    try {
                        // invoke the hidden method "createRfcommSocket" via reflections
                        Method m = aClass.getMethod("createRfcommSocket", paramTypes);
                        Object[] params = new Object[]{Integer.valueOf(1)};
                        fallback = (BluetoothSocket) m.invoke(socket.getRemoteDevice(), params);
                        fallback.connect();
                        socket = fallback;

                    // fallback socket also failed to connect. Stop service
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to connect fallback socket", e);

                        // CONNECTION FAILED
                        // Close the socket
                        try {
                            socket.close();

                            // unable to close socket
                        } catch (IOException ioClose) {
                            Log.e(TAG, "Error closing socket during connection failure", ioClose);
                        }
                        connectionFailed();
                        return;
                    }
                }

                // CONNECTION SUCCESS
                // Pass connected socket elsewhere to be managed
                connected(socket);

            // unable to get a socket
            } catch (IOException e) {
                Log.e(TAG, "Error while creating Bluetooth socket", e);
            }
        }
    }


//    /**
//     * Does all the work for setting up and managing Bluetooth connections with other devices.
//     * Uses threads for establishing connections and transmissions.
//     */
//    public class BluetoothManager {
//
//        // Member fields
//        private ConnectThread mConnectThread;
//        /**
//         * Constructor. Prepares a new Obd Bluetooth session.
//         */
//        public BluetoothManager() {
//            Log.i(TAG, "BluetoothManager created");
//        }
//
//        /** Stops / resets connectionThread */
//        public synchronized void stop() {
//            Log.d(TAG, "stopService()");
//            reset();
//        }
//
//        /**Stops all threads */
//        private synchronized void reset() {
//            // Cancel any thread attempting to make a connection
//            if (mConnectThread != null) {
//                mConnectThread.close();
//                mConnectThread = null;
//            }
//        }
//
//        /** Starts ConnectThread to initiate a connection to a remote device */
//        public synchronized void connect(BluetoothDevice device) {
//            Log.d(TAG, "CONNECTING to: " + device.getAddress() + ", " + device.getName());
//            reset();
//
//            // Start the thread to connect with the given device
//            mConnectThread = new ConnectThread(device);
//            mConnectThread.start();
//        }
//
//        /** Starts ConnectedThread to begin managing Bluetooth connection */
//        public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
//            Log.d(TAG, "CONNECTED to: " + device.getAddress() + ", " + device.getName());
//            reset();
//
//            mConnectedSocket = socket;
//        }
//
//        /** TODO: new comment
//         * Notifies the UI Activity that the connection failed or was lost*/
//        private void connectionFailed() {
//
//        }
//
//        /**
//         * Runs while attempting connect to remote a device. The connection either succeeds or fails
//         */
//        private class ConnectThread extends Thread {
//            private final BluetoothSocket socket;
//            private final BluetoothDevice mmDevice;
//
//            public ConnectThread(BluetoothDevice device) {
//                BluetoothSocket tmpSocket = null;
//                mmDevice = device;
//
//                // Get a BluetoothSocket to connect with the given BluetoothDevice
//                try {
//                    tmpSocket = device.createRfcommSocketToServiceRecord(OBD_UUID);
//                } catch (IOException e) {
//                    Log.e(TAG, "Exception thrown while assigning UUID to Bluetooth socket");
//                }
//                socket = tmpSocket;
//            }
//
//            public void run() {
//                Log.i(TAG, "BEGIN ConnectThread");
//                setName("ConnectThread");
//
//                // Cancel discovery because it's costly and we're about to connect
//                mBluetoothAdapter.cancelDiscovery();
//
//                // Make a connection to the BluetoothSocket
//                try {
//                    // Blocks until success or throws an exception.
//                    socket.connect();
//
//                } catch (IOException e) {
//                    //TODO: handle OBD exception
//                    //(https://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701details)
//
//                    // Close the socket
//                    try {
//                        socket.close();
//
//                    } catch (IOException e2) {
//                        Log.e(TAG, "unable to close() socket during connection failure", e2);
//                    }
//                    connectionFailed();
//                    return;
//                }
//
//                // CONNECTION SUCCESS
//
//                // Reset the ConnectThread because we're done
//                synchronized (BluetoothManager.this) {
//                    mConnectThread = null;
//                }
//
//                // Pass connected socket elsewhere to be managed
//                connected(socket, mmDevice);
//            }
//
//            /**
//             * Close the connecting socket
//             */
//            public void close() {
//                Log.i(TAG, "CLOSE ConnectThread");
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    Log.e(TAG, "close() of connect socket failed", e);
//                }
//            }
//        }
//    }
}
