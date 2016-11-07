/*
 * Copyright (C) Kieran Hillier
 * All Rights Reserved
 */

package mad.com.applicationproject.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;
import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.enums.AvailableCommandNames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mad.com.applicationproject.R;
import mad.com.applicationproject.io.ObdCommandJob;
import mad.com.applicationproject.io.ObdService;
import mad.com.applicationproject.io.ObdServiceListener;
import mad.com.applicationproject.io.availableObdCommands;
import mad.com.applicationproject.misc.TimeRegulator;
import mad.com.applicationproject.unused.GridItem;
import mad.com.applicationproject.unused.MyLocation;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();




    // content_debug UI
    private TextView mBtStatusTextView;
    private TextView mObdStatusTextView;
    private TableLayout mTableLayout;
    private static final int TABLE_ROW_MARGIN = 7;



    // UI
    private RelativeLayout mRootLayout;
    private ProgressBar mProgressBar;
    private PieChart mPieChart;
    private GridLayout mGridLayout;
    private MyLocation mLocation;
    private ArrayList<PieEntry> mEntries = new ArrayList<>();
    private List<GridItem> mGridList = new ArrayList<>();
    private List<View> mGridViews = new ArrayList<>();

    // Bluetooth
    protected static final int REQUEST_ENABLE_BLUETOOTH = 1;
    protected BluetoothAdapter mBluetoothAdapter = null;
    protected boolean mBluetoothDefaultIsEnable = false;
    protected final ArrayList<BluetoothDevice> mAvailableDevices = new ArrayList<>();
    private final TimeRegulator mLastDiscovery = new TimeRegulator(60000); // wait to recalling discovery
    protected boolean mDiscovering = false;

    // OBD Service
    protected ObdService mObdService;
    protected boolean mBound = false;
    protected boolean mIsObdRunning = false;
    public Map<String, String> mCommandResult = new HashMap<>(); // TODO: used for logging
    private ArrayList<ObdCommand> commands; // TODO: us list rather than pull from preferences

    // Preferences
    protected SharedPreferences mSharedPrefs;
    protected SharedPreferences.OnSharedPreferenceChangeListener mPrefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
                    Log.d(TAG, "onSharedPreferenceChanged()");
                    switch (key) {
                        case Settings.BLUETOOTH_ENABLE:
                            if (sharedPrefs.getBoolean(key, false)) {
                                startBluetooth();
                            } else {
                                stopBluetooth();
                            }
                            break;
                    }
                }
            };

/*------------------------------------------------------------------------------------------------*/
    // Activity


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // initialize xml views

        mRootLayout = (RelativeLayout) findViewById(R.id.content_main);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // set default values to preferences on very first launch
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // get preferences and register the change listener
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPrefs.registerOnSharedPreferenceChangeListener(mPrefListener);





        mRootLayout = (RelativeLayout) findViewById(R.id.content_main);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mBtStatusTextView = (TextView) findViewById(R.id.bt_status);
        mObdStatusTextView = (TextView) findViewById(R.id.obd_status);
        mTableLayout = (TableLayout) findViewById(R.id.data_table);




        // get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // if Bluetooth supported
        if (mBluetoothAdapter != null) {

            // register Bluetooth broadcast receivers
            registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
            registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
            registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

            // record bluetooth state to be reverted onDestroy
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothDefaultIsEnable = true;
//                mBtStatusTextView.setText(R.string.status_ready);

            // if bluetooth is off and hasn't been granted, request to enable it
            } else if (!isBluetoothGranted()) {
                requestBluetooth();
            }

        // if Bluetooth not supported
        } else {
            mSharedPrefs.edit().putBoolean(Settings.BLUETOOTH_ENABLE, false).apply();
            Log.w(TAG, "onCreate() - Bluetooth not supported by device");
            toastShort("onCreate() - Bluetooth not supported by device");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        stopBluetooth();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();

        doUnbindService();
        disableBluetooth();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_bluetooth_toggle)
                .setTitle(mBluetoothAdapter.isEnabled()
                        ? getString(R.string.action_bluetooth_disable)
                        : getString(R.string.action_bluetooth_enable));

        menu.findItem(R.id.action_bluetooth_discovery)
                .setTitle(mDiscovering
                        ? getString(R.string.action_bluetooth_discovering)
                        : getString(R.string.action_bluetooth_discovery))
                .setEnabled(!mDiscovering);

        menu.findItem(R.id.action_start_stop)
                .setIcon(mBound
                        ? getDrawable(R.drawable.ic_action_stop)
                        : getDrawable(R.drawable.ic_action_start));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_start_stop:
                toggleService();
                break;
            case R.id.action_bluetooth_toggle:
                toggleBluetooth();
                return true;
            case R.id.action_bluetooth_discovery:
                startDiscovery();
                return true;
            case R.id.action_bluetooth_connect:
                showDeviceListDialog();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, Settings.class));
                return true;
            case R.id.action_chart:
                startActivity(new Intent(this, ChartActivity.class));
                return true;
            case R.id.action_debug_screen:
//                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_drag_fragment:
                showDragFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDragFragment() {
        // drag_view_fragment fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_main, new DragFragment())
                .commit();
    }


    // Activity
/*------------------------------------------------------------------------------------------------*/
    // UI

    private GridLayout.LayoutParams gridParams(int columnPos, int columnSpan, float columnWeight,
                                               int rowPos, int rowSpan, float rowWeight) {
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.setMargins(0,0,0,0);
        param.columnSpec = GridLayout.spec(columnPos, columnSpan, columnWeight);
        param.rowSpec = GridLayout.spec(rowPos, rowSpan, rowWeight);
        param.setGravity(Gravity.FILL);
        return param;
    }

    private GridLayout.LayoutParams gridParams(int columnPos, float columnWeight,
                                               int rowPos, float rowWeight) {
        int noSpan = 1;
        return gridParams(columnPos, noSpan, columnWeight, rowPos, noSpan, rowWeight);
    }

    private GridLayout.LayoutParams gridParams(int columnPos, int rowPos) {
        float defaultWeight = 1f;
        return gridParams(columnPos, defaultWeight, rowPos, defaultWeight);
    }

    private GridLayout.LayoutParams gridParams() {
//        int childCount = mGridLayout.getChildCount();
//        int columnPos = childCount / mGridLayout.getRowCount();
//        int rowPos = childCount / mGridLayout.getColumnCount();
//        return gridParams(columnPos, rowPos);
        return null;
    }

    private void setupPieChart() {
//        int value = 100;
//        mPieChart = (PieChart)findViewById(R.id.pie_chart);
//        mPieChart.setCenterText(String.valueOf(value));
//        mPieChart.setMaxAngle((float) value);
//        mPieChart.setDrawEntryLabels(false);
//        mPieChart.setDescription("");
//        mPieChart.setHoleRadius(90f);
//        mPieChart.setHoleColor(Color.TRANSPARENT);
//        mPieChart.setCenterTextSize(100);
//        mPieChart.setCenterTextColor(Color.WHITE);
//
//
//        if (mPieChart.isEmpty()) {
//            mEntries.add(new PieEntry(value));
//            PieDataSet dataSet = new PieDataSet(mEntries, "Data");
//            PieData pieData = new PieData(dataSet);
//            mPieChart.setData(pieData);
//            mPieChart.invalidate();
//        }
    }

    protected void toastLong(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    protected void toastShort(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    // UI
/*------------------------------------------------------------------------------------------------*/
    // Bluetooth


    /** Returns true if Bluetooth is supported and use is granted in preferences */
    private boolean isBluetoothGranted() {
        return mBluetoothAdapter != null &&
                mSharedPrefs.getBoolean(Settings.BLUETOOTH_ENABLE, false);
    }
    private void toggleBluetooth() {
        if (mBluetoothAdapter.isEnabled()) {
            stopBluetooth();
        } else {
            startBluetooth();
        }
    }

    /** Enables Bluetooth. Called when user clicked the enable bluetooth menu item*/
    private void startBluetooth() {
        Log.v(TAG, "startBluetooth()");
        enableBluetooth();

//        if (isBluetoothGranted()) {
//
//            if (!mBluetoothAdapter.isEnabled()) {
//                mBluetoothAdapter.enable();
//                toastShort("Turning Bluetooth on");
//            } else {
//                Log.d(TAG, "startBluetooth() - Bluetooth already on");
//            }
//            mBtStatusTextView.setText(R.string.status_ready);
//        } else {
//            stopBluetooth();
//        }
    }

    protected void enableBluetooth() {
        if(!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            toastShort("Turning Bluetooth on");
            mSharedPrefs.edit().putBoolean(Settings.BLUETOOTH_ENABLE, true).apply();
        }
        mBtStatusTextView.setText(R.string.status_ready);
    }

    public void requestBluetooth() {
        Log.d(TAG, "requestBluetooth()");
        // TODO: (MAYBE) create custom dialog?
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        if (requestCode == REQUEST_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_OK) {
            // set use bluetooth preference TRUE
            mSharedPrefs.edit().putBoolean(Settings.BLUETOOTH_ENABLE, true).apply();
            mBtStatusTextView.setText(getString(R.string.status_enabled));

        } else {
            mSharedPrefs.edit().putBoolean(Settings.BLUETOOTH_ENABLE, false).apply();
            Toast.makeText(this, R.string.text_bluetooth_disabled, Toast.LENGTH_LONG).show();
            mBtStatusTextView.setText(getString(R.string.status_disabled));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void stopBluetooth() {
        Log.d(TAG, "stopBluetooth()");
//        if (!isBluetoothGranted()) {
//            mBtStatusTextView.setText(R.string.status_enabled);
//        } else {
//            mBtStatusTextView.setText(R.string.status_disabled);
//        }
        stopLiveData();
        disableBluetooth();
    }

    /** Disables bluetooth only if it was disabled when we started */
    private void disableBluetooth() {
        if (!mBluetoothDefaultIsEnable) mBluetoothAdapter.disable();
    }

    private void startDiscovery() {
        if (isBluetoothGranted()) {
            mAvailableDevices.clear();
            if (mLastDiscovery.checkTime()) {
                if (!mBluetoothAdapter.startDiscovery()) {
                    Log.w(TAG, "startDiscovery() - unable to discover");
                    mLastDiscovery.undoUpdate();
                } else {
                    toastLong("startDiscovery() - Discovering Bluetooth devices...");
                    mDiscovering = true;
                }
            } else {
                toastLong("startDiscovery() - Discovery preformed recently");
            }
        } else {
            // TODO request to use bluetooth dialog
        }
    }

    /** Listens for system broadcasts related to Bluetooth */
    protected final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    bluetoothStateChanged(intent);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    bluetoothDiscoveryStarted();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    bluetoothDiscoveryFinished();
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    foundBluetoothDevice(intent);
                    break;
            }
        }
    };

    protected void bluetoothDiscoveryStarted() {
        Log.d(TAG, "onReceive() - START  bluetooth discovery");
        mProgressBar.setVisibility(View.VISIBLE);
        mBtStatusTextView.setText(R.string.status_discovering);
    }

    protected void bluetoothDiscoveryFinished() {
        Log.d(TAG, "onReceive() - FINISH bluetooth discovery");
        mDiscovering = false;
        invalidateOptionsMenu();
        if (mBluetoothAdapter.isEnabled() && !mBound) {
            mProgressBar.setVisibility(View.GONE);
            mBtStatusTextView.setText(R.string.status_ready);
        }
    }

    /** Handles Bluetooth state change broadcast */
    private void bluetoothStateChanged(Intent intent) {
        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
            case BluetoothAdapter.STATE_ON:
                Log.d(TAG, "bluetoothStateChanged() - ON");
                toastShort("Bluetooth ON");
                startBluetooth();
                break;
            case BluetoothAdapter.STATE_OFF:
                Log.d(TAG, "bluetoothStateChanged() - OFF");
                toastShort("Bluetooth OFF");
                stopBluetooth();
                break;
        }
    }

    /** Called to list a Bluetooth device found during discovery if not already listed */
    private void foundBluetoothDevice(Intent intent) {
        Log.d(TAG, "foundBluetoothDevice()");
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        // if not already listed, add it
        if(!mAvailableDevices.contains(device)) mAvailableDevices.add(device);

        Log.v(TAG, "foundBluetoothDevice() - " +
                device.getAddress() + ", " + device.getName());
    }

    /** Gets and lists paired Bluetooth devices if not already listed */
    public void pairedBluetoothDevices() {
        Log.d(TAG, "pairedBluetoothDevices()");
        Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();

        if (paired.size() > 0) {
            for (BluetoothDevice device : paired) {

                // if not already listed, add it
                if(!mAvailableDevices.contains(device)) mAvailableDevices.add(device);

                Log.v(TAG, "pairedBluetoothDevices() - " +
                        device.getAddress() + ", " + device.getName());
            }
        }
    }

    /** Shows a dialog of available Bluetooth devices and saves selection to preferences
     * TODO: Replace with ListPreference dialog using a PreferenceFragment */
    public void showDeviceListDialog() {
        Log.d(TAG, "showDeviceListDialog()");

//        if (prefDeviceAvailable()) {
//            // checks if device from preferences is available
//        }

        // populate available devices list
        pairedBluetoothDevices();

        // prepare list of optional bluetooth devices
        final int length = mAvailableDevices.size();
        CharSequence[] list = new CharSequence[length];
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                final BluetoothDevice device = mAvailableDevices.get(i);

                // if device has no name display address
                if (device.getName() == null) {
                    list[i] = device.getAddress();

                } else {
                    list[i] = device.getName();
                }
            }
        }

        // create onClickListener to handle selection
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                setSelectedDevice(which);

            }
        };

        // build and show the dialog
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setItems(list, listener)
                .setTitle("Choose Bluetooth device")
                .show();
    }

    private void setSelectedDevice(int selected) {
        Log.d(TAG, "Bluetooth device selected: " + mAvailableDevices.get(selected).getAddress()
                + ", " + mAvailableDevices.get(selected).getName());

        final String prefDevice = mSharedPrefs.getString(Settings.BLUETOOTH_DEVICE, null);

        // if selected device is different from preference device
        if (!mAvailableDevices.get(selected).getAddress().equals(prefDevice)) {

            // reset all command preferences
            for (ObdCommand cmd : availableObdCommands.getCommands()) {
                mSharedPrefs.edit().putBoolean(cmd.getName(), true).apply();
            }

            // save selected device to preferences
            mSharedPrefs.edit().putString(Settings.BLUETOOTH_DEVICE,
                    mAvailableDevices.get(selected).getAddress()).apply();

            toastLong("New device selected\nResetting all OBD commands");
        }
    }

    /** Checks if the device from preferences is available */
    private boolean prefDeviceAvailable() {
        Log.d(TAG, "prefDeviceAvailable()");

        // get MAC address of selected Bluetooth device from preferences
        final String prefDevice = mSharedPrefs.getString(Settings.BLUETOOTH_DEVICE, null);

        // if no device is selected in preferences
        if (prefDevice == null || "".equals(prefDevice)) return false;

        // get Bluetooth device object from the MAC address
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(prefDevice);

        // if device from preferences is available
        return mAvailableDevices.contains(device);
    }


    // Bluetooth
/*------------------------------------------------------------------------------------------------*/
    // OBD service

    private void toggleService() {
        if (!mBound) {
            startLiveData();
        } else {
            stopLiveData();
            mProgressBar.setVisibility(View.GONE);
        }
    }


    private void startLiveData() {
        if (!mBound && mBluetoothAdapter.isEnabled() && isBluetoothGranted()) {
            Log.v(TAG, "Starting live data...");

            doBindService();

            // start command execution
            new Handler().post(mQueueCommands);
        }
    }

    protected void doBindService() {
        if (!mBound) {
            Log.d(TAG, "Binding ObdService...");
            mProgressBar.setVisibility(View.VISIBLE);

            mTableLayout.removeAllViews(); //start fresh
            mBtStatusTextView.setText(getString(R.string.status_connecting));

            Intent serviceIntent = new Intent(this, ObdService.class);
            bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    protected ServiceConnection mServiceConnection = new ServiceConnection() {

        /** Called when the connection with the service is established */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "service is bound");
            ObdService.ObdBinder binder = (ObdService.ObdBinder) service;
            mObdService = binder.getService();
            mObdService.setObdServiceListener(mObdListener);
            mObdService.setContext(MainActivity.this);
            mObdService.start();
            mBound = true;

            MainActivity.this.onServiceConnected();
        }

        /** Called when the connection with the service disconnects unexpectedly */
        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "service disconnected unexpectedly");
            stopLiveData();
        }
    };

    protected void onServiceConnected() {
        invalidateOptionsMenu();
    }

    private int mCycleCounter = 0;
    private final Runnable mQueueCommands = new Runnable() {
        public void run() {
            if (mBound && mObdService.isRunning() && mObdService.isQueueEmpty()) {
                int commandCount = 0;
                for (ObdCommand command : availableObdCommands.getCommands()) {
                    if (mSharedPrefs.getBoolean(command.getName(), true)) {
                        Log.v(TAG, command.getName() + " command's pref is true, add to queue");
                        mObdService.queueJob(new ObdCommandJob(command));
                    } else {
                        Log.v(TAG, command.getName() + " command's pref is false, removing");
                        removeCommand(command);
                    }
                    commandCount++;
                }
                mCommandResult.clear();
                mCycleCounter++;
                Log.d(TAG, commandCount + " Query commands queued (cycle: " + mCycleCounter + ")");
            }
            // run again in period defined in preferences
            new Handler().postDelayed(mQueueCommands, Settings.getObdUpdatePeriod(mSharedPrefs));
        }
    };

    protected void checkServiceState() {
        if (mBound) {
            switch (mObdService.getServiceState()) {
                case INITIALISING:
                    // if the service is still starting
                    mBtStatusTextView.setText(R.string.status_connected);
                    mObdStatusTextView.setText(R.string.status_initialising);
                    break;
                case RUNNING:
                    // clear the initialisation commands
                    if (!mIsObdRunning) {
                        clearServiceConnecting();
                        mIsObdRunning = true; // boolean so it's only true once
                    }

                    // show that we are receiving data
                    mObdStatusTextView.setText(R.string.status_receiving);
                    break;
            }
        }}

    protected void clearServiceConnecting() {
        if (mBound) {
            mBtStatusTextView.setText(R.string.status_connected);
        } else if (mBluetoothAdapter.isEnabled()) {
            mBtStatusTextView.setText(R.string.status_ready);
        } else {
            mBtStatusTextView.setText(R.string.status_enabled);
        }
        mProgressBar.setVisibility(View.GONE);
        mTableLayout.removeAllViews(); //start fresh
    }

    protected ObdServiceListener mObdListener = new ObdServiceListener() {
        @Override
        public void onCommandComplete(ObdCommandJob cmdJob) {
            Log.v(TAG, "onCommandComplete()");

            checkServiceState();

            // get the name, id from command
            final String cmdName = cmdJob.getCommand().getName();
            final String cmdId = LookUpObdCommand(cmdName);


            // get result from the command
            String cmdResult = "";
            switch (cmdJob.getState()) {
                case NO_DATA:
                case MISUNDERSTOOD:
                    // set command's preference to false
                    mSharedPrefs.edit().putBoolean(cmdName, false).apply();
                    // it will be removed from view elsewhere
                    return;
                case EXECUTION_ERROR:
                    cmdResult = cmdJob.getCommand().getResult();
                    break;
                case NOT_SUPPORTED:
                    cmdResult = "NA";
                    break;
                case BROKEN_PIPE:
                    if (mBound) stopLiveData();
                    break;
                default:
                    cmdResult = cmdJob.getCommand().getFormattedResult();
            }

            onCommandResult(cmdId, cmdName, cmdResult);
        }
    };

    protected void onCommandResult(String cmdId, String cmdName, String cmdResult) {

        // get row for this command if it has been created
        TextView row = (TextView) mRootLayout.findViewWithTag(cmdId);

        // if there is a row for this command, set it's new value
        if (row != null) row.setText(cmdResult);

            // else create and populate a new row
        else addTableRow(cmdId, cmdName, cmdResult);

        // record the result for logging elsewhere
        mCommandResult.put(cmdId, cmdResult);
    }

    /** Adds a row to display the output result of a command */
    private void addTableRow(String id, String name, String val) {
        Log.v(TAG, "addTableRow()");

        TableRow row = new TableRow(this);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN);
        row.setLayoutParams(params);

        TextView command = new TextView(this);
        command.setGravity(Gravity.RIGHT);
        command.setText(name + ": ");
        row.addView(command);

        TextView result = new TextView(this);
        result.setGravity(Gravity.LEFT);
        result.setText(val);
        result.setTag(id);
        row.addView(result);

        mTableLayout.addView(row, params);
    }

    /** Gets the output result from a command, displays it and stores it to be logged */
//    @Override
//    public void stateUpdate(final ObdCommandJob commandJob) {
//        Log.v(TAG, "onCommandComplete()");
//
//        if (mBound) {
//            switch (mObdService.getServiceState()) {
//                case INITIALISING:
//                    // if the service is still starting
//                    mBtStatusTextView.setText(R.string.status_connected);
//                    mObdStatusTextView.setText(R.string.status_initialising);
//                    break;
//                case RUNNING:
//                    // clear the initialisation commands
//                    if (!mIsObdRunning) {
//                        mTableLayout.removeAllViews(); //start fresh
//                        mIsObdRunning = true;
//                    }
//
//                    // show that we are receiving data
//                    mObdStatusTextView.setText(R.string.status_receiving);
//                    break;
//            }
//        }
//
//        // get the name, id from command
//        final String commandName = commandJob.getCommand().getName();
//        final String commandId = LookUpObdCommand(commandName);
//
//        // get row for this command if it has been created
//        TextView row = (TextView) mRootLayout.findViewWithTag(commandId);
//
//        // get result from the command
//        String commandResult = "";
//        switch (commandJob.getState()) {
//            case NO_DATA:
//            case MISUNDERSTOOD:
//                // set command's preference to false
//                mSharedPrefs.edit().putBoolean(commandName, false).apply();
//                // it will be removed from view elsewhere
//                return;
//            case EXECUTION_ERROR:
//                commandResult = commandJob.getCommand().getResult();
//                break;
//            case NOT_SUPPORTED:
//                commandResult = "NA";
//                break;
//            case BROKEN_PIPE:
//                if (mBound) stopLiveData();
//                break;
//            default:
//                commandResult = commandJob.getCommand().getFormattedResult();
//        }
//
//        // if there is a row for this command, set it's new value
//        if (row != null) row.setText(commandResult);
//
//        // else create and populate a new row
//        else addTableRow(commandId, commandName, commandResult);
//
//        // record the result for logging elsewhere
//        mCommandResult.put(commandId, commandResult);
//    }

    /** Removes command from UI and sets it's preference to false to stop it being called again */
    protected void removeCommand(ObdCommand command) {
        // get row for this command if it has been created
        String id = LookUpObdCommand(command.getName());
        TextView view = (TextView) mRootLayout.findViewWithTag(id);

        // if there is a row for this command, remove it
        if (view != null) mTableLayout.removeView(view);}

    /** Looks up and compares a command name to the list of available commands */
    public static String LookUpObdCommand(String QueryCommand) {
        for (AvailableCommandNames availableCommand : AvailableCommandNames.values()) {
            if (availableCommand.getValue().equals(QueryCommand)) {
                return availableCommand.name();
            }
        }
        return QueryCommand;
    }


    private void stopLiveData() {
        Log.v(TAG, "Stopping live data...");
        doUnbindService();
    }

    public void doUnbindService() {
        if (mBound) {
            Log.d(TAG, "Unbinding ObdService...");

            if (mObdService.isRunning()) mObdService.stopService();

            unbindService(mServiceConnection);

            mObdStatusTextView.setText(getString(R.string.status_disconnected));

            mBound = false;
            mIsObdRunning = false;

            invalidateOptionsMenu();
        }
    }
}
