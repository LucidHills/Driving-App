///*
// * Copyright (C) Kieran Hillier
// * All Rights Reserved
// */
//
//package mad.com.applicationproject;
//
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.ArrayAdapter;
//import android.widget.GridLayout;
//import android.widget.Toast;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Set;
//import java.util.UUID;
//
//public class Copy extends AppCompatActivity {
//
//    private static final String TAG = MainActivity.class.getName();
//
////    PieChart mPieChart;
////    GridLayout mGridLayout;
////    MyLocation mLocation;
////    private ArrayList<PieEntry> mEntries = new ArrayList<>();
////    private List<GridItem> mGridList = new ArrayList<>();
////    private List<View> mGridViews = new ArrayList<>();
//
//    // OLD BT
//    private static final String OBD_UUID = "00001101-0000-1000-8000-00805F9B34FB";
//    private final ArrayList<String> mDeviceNames = new ArrayList<>();
//    private final ArrayList<String> mDevices = new ArrayList<>();
//
//    // Intent request codes
//    private static final int REQUEST_ENABLE_BT = 1;
//
//    // Local Bluetooth adapter
//    private BluetoothAdapter mBluetoothAdapter = null;
//
//    // Member object for the chat services
//    private BluetoothManager mBluetoothService = null;
//
//    // Name of the connectionSuccess device
//    private String mConnectedDeviceName = null;
//
//    private Boolean mUseBluetooth = false;
//    private Boolean mBluetoothSupported = true;
//
//    // String buffer for outgoing messages
//    private StringBuffer mOutStringBuffer;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        // Get local Bluetooth adapter
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        if (mBluetoothAdapter == null) { // If Bluetooth is not supported
//            mBluetoothSupported = false;
//            toastShort("Bluetooth not supported by device");
//        } else {
//            mBluetoothSupported = true;
//            registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
//            registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
//            toastShort("Bluetooth broadcast receiver registered...");
//        }
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    private void resumeBluetooth() {
//        if (mUseBluetooth) {
//            if (!mBluetoothAdapter.isEnabled()) {   // If Bluetooth is OFF
//                requestBluetooth();                  // Request to turn Bluetooth ON
//
//                // setupBluetoothService() will then be called during onActivityResult
//
//            } else if (mBluetoothService == null) {// Else if no BluetoothManager yet
//
//                // Otherwise, setupBluetoothService the chat session
//
//                // Initialize the BluetoothChatService to perform bluetooth connections
//                mBluetoothService = new BluetoothManager(this, mHandler);
//
//                // Initialize the buffer for outgoing messages
//                mOutStringBuffer = new StringBuffer("");
//            } else {
//                // Only if the state is STATE_NONE, do we know that we haven't started already
//                if (mBluetoothService.getState() == BluetoothManager.STATE_NONE) {
//                    // Start the Bluetooth services
//                    mBluetoothService.start();
//                }
//            }
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mBluetoothService != null) mBluetoothService.stopService();
//        unregisterReceiver(mReceiver);
//    }
//
//    private void startBluetooth() {
//        mUseBluetooth = true;
//        resumeBluetooth();
//    }
//
//    private void setupBluetoothService() {
//        // Initialize the BluetoothChatService to perform bluetooth connections
//        mBluetoothService = new BluetoothManager(mHandler);
//
//        // Initialize the buffer for outgoing messages
//        mOutStringBuffer = new StringBuffer("");
//    }
//
//    private void setupBluetooth() {
////        if (!mBluetoothAdapter.startDiscovery()) requestBluetooth();
//
//        if (!mBluetoothAdapter.isEnabled()) {
//            requestBluetooth();
//        } else {
//            mBluetoothAdapter.startDiscovery();
//            pairedBluetoothDevices();
//            selectDevice();
//        }
//    }
//
//    /** Establish connection with other device */
//    private void connectDevice(Intent data) {
//        // Get the device MAC address
//        String address = data.getExtras().getString(BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);
//        // Get the BluetoothDevice object
//        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        // Attempt to connect to the device
//        mBluetoothService.connect(device);
//    }
//
//    private void bluetoothStateChanged(Intent intent) {
//        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
//            case BluetoothAdapter.STATE_ON:
//                toastShort("Bluetooth enabled");
//                setupBluetooth();
//                break;
//            case BluetoothAdapter.STATE_OFF:
//                toastShort("Bluetooth disabled");
//                break;
//        }
//    }
//
//    public void requestBluetooth() {
//        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case REQUEST_ENABLE_BT:
//                // When the request to enable Bluetooth returns
//                if (resultCode == RESULT_OK) {
//                    // Bluetooth is now enabled, so set up a chat session
//                    setupBluetoothService();
//                } else if (resultCode == RESULT_CANCELED) {
//                    // User did not enable Bluetooth or an error occurred
//                    Log.d(TAG, "BT not enabled");
//                }
//                break;
//        }
//    }
//
//    public void pairedBluetoothDevices() {
//        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//        if (pairedDevices.size() > 0) {
//            for(BluetoothDevice device : pairedDevices) {
//                mDeviceNames.add(device.getName() + "\n" + device.getAddress());
//                mDevices.add(device.getAddress());
//            }
//        }
//    }
//
//    private void bluetoothDeviceFound(Intent intent) {
//        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//        if(!mDevices.contains(device.getAddress())) {
//            mDeviceNames.add(device.getName() + "\n" + device.getAddress());
//            mDevices.add(device.getAddress());
//        }
//    }
//
//    public void selectDevice() {
//        if (mDeviceNames.size() > 0) {
//            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//
//            ArrayAdapter arrayAdapter = new ArrayAdapter<>(this,
//                    android.R.layout.select_dialog_singlechoice,
//                    mDeviceNames.toArray(new String[mDeviceNames.size()]));
//
//            alertDialog.setSingleChoiceItems(arrayAdapter, -1, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                    int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
//                    String deviceAddress = mDevices.get(position);
//                    // TODO save deviceAddress
//
//                    //TODO: below code should be executed in a non-UI thread
//                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                    BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
//                    UUID uuid = UUID.fromString(OBD_UUID);
//                    BluetoothSocket socket = null;
//                    try {
//                        socket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
//                        socket.connect();
//                    } catch (IOException e) {
//                        //TODO: handle exception
//                        //(https://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701details)
//                        e.printStackTrace();
//                    }
//                }
//            });
//
//            alertDialog.setTitle("Choose Bluetooth device");
//            alertDialog.show();
//        }
//    }
//
//    /** Sends a message */
//    private void sendMessage(String message) {
//        // Check for connection before proceeding
//        if (mBluetoothService.getState() != BluetoothManager.STATE_CONNECTED) {
////            toast(R.string.not_connected);
//            return;
//        }
//
//        // Check for something to send
//        if (message.length() > 0) {
//            // Convert message string to bytes and tell the BluetoothManager to write
//            byte[] send = message.getBytes();
//            mBluetoothService.write(send);
//
//            // Reset out string buffer to zero
//            mOutStringBuffer.setLength(0);
//        }
//    }
//
//    /** Gets information back from the BluetoothManager */
//    private final Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case Constants.MESSAGE_STATE_CHANGE:
//                    switch (msg.arg1) {
//                        case BluetoothManager.STATE_CONNECTED:
//                            break;
//                        case BluetoothManager.STATE_CONNECTING:
//                            break;
//                        case BluetoothManager.STATE_NONE:
//                            break;
//                    }
//                    break;
//                case Constants.MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String writeMessage = new String(writeBuf);
//                    break;
//                case Constants.MESSAGE_READ:
//                    byte[] readBuf = (byte[]) msg.obj;
//                    // construct a string from the valid bytes in the buffer
//                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    break;
//                case Constants.MESSAGE_DEVICE_NAME:
//                    // save the connectionSuccess device's name
//                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
//                    toast("Connected to " + mConnectedDeviceName);
//                    break;
//                case Constants.MESSAGE_TOAST:
//                    toast(msg.getData().getString(Constants.TOAST));
//                    break;
//            }
//        }
//    };
//
//    /** Listens for system broadcasts related to Bluetooth */
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            switch (intent.getAction()) {
//                case BluetoothAdapter.ACTION_STATE_CHANGED:
//                    bluetoothStateChanged(intent);
//                    break;
//                case BluetoothDevice.ACTION_FOUND:
//                    bluetoothDeviceFound(intent);
//                    break;
//            }
//        }
//    };
//
//
//    private GridLayout.LayoutParams gridParams(int columnPos, int columnSpan, float columnWeight,
//                                               int rowPos, int rowSpan, float rowWeight) {
//        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
//        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
//        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
//        param.setMargins(0,0,0,0);
//        param.columnSpec = GridLayout.spec(columnPos, columnSpan, columnWeight);
//        param.rowSpec = GridLayout.spec(rowPos, rowSpan, rowWeight);
//        param.setGravity(Gravity.FILL);
//        return param;
//    }
//
//    private GridLayout.LayoutParams gridParams(int columnPos, float columnWeight,
//                                               int rowPos, float rowWeight) {
//        int noSpan = 1;
//        return gridParams(columnPos, noSpan, columnWeight, rowPos, noSpan, rowWeight);
//    }
//
//    private GridLayout.LayoutParams gridParams(int columnPos, int rowPos) {
//        float defaultWeight = 1f;
//        return gridParams(columnPos, defaultWeight, rowPos, defaultWeight);
//    }
//
//    private GridLayout.LayoutParams gridParams() {
////        int childCount = mGridLayout.getChildCount();
////        int columnPos = childCount / mGridLayout.getRowCount();
////        int rowPos = childCount / mGridLayout.getColumnCount();
////        return gridParams(columnPos, rowPos);
//        return null;
//    }
//
//    private void setupPieChart() {
////        int value = 100;
////        mPieChart = (PieChart)findViewById(R.id.pie_chart);
////        mPieChart.setCenterText(String.valueOf(value));
////        mPieChart.setMaxAngle((float) value);
////        mPieChart.setDrawEntryLabels(false);
////        mPieChart.setDescription("");
////        mPieChart.setHoleRadius(90f);
////        mPieChart.setHoleColor(Color.TRANSPARENT);
////        mPieChart.setCenterTextSize(100);
////        mPieChart.setCenterTextColor(Color.WHITE);
////
////
////        if (mPieChart.isEmpty()) {
////            mEntries.add(new PieEntry(value));
////            PieDataSet dataSet = new PieDataSet(mEntries, "Data");
////            PieData pieData = new PieData(dataSet);
////            mPieChart.setData(pieData);
////            mPieChart.invalidate();
////        }
//    }
//
//    private void toast(String message) {
//        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
//    }
//
//    private void toastShort(String message) {
//        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        switch(item.getItemId()) {
//            case R.id.action_settings:
//                return true;
//            case R.id.action_bluetooth:
//                mUseBluetooth = true;
//                resumeBluetooth();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//}
