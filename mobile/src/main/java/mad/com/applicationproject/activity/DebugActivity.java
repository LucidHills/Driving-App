//package mad.com.applicationproject.activity;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v7.widget.Toolbar;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.ViewGroup;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TableLayout;
//import android.widget.TableRow;
//import android.widget.TextView;
//
//import com.github.pires.obd.commands.ObdCommand;
//
//import mad.com.applicationproject.R;
//
///**
// * Created by kiera on 8/11/2016.
// * TODO: Write class comment
// */
//public class DebugActivity extends MainActivity {
//
//    private static final String TAG = DebugActivity.class.getSimpleName();
//
//    // content_debug UI
//    protected RelativeLayout mRootLayout;
//    private ProgressBar mProgressBar;
//    private TextView mBtStatusTextView;
//    private TextView mObdStatusTextView;
//    private TableLayout mTableLayout;
//    private static final int TABLE_ROW_MARGIN = 7;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        mRootLayout = (RelativeLayout) findViewById(R.id.content_main);
//        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
//
//        mBtStatusTextView = (TextView) findViewById(R.id.bt_status);
//        mObdStatusTextView = (TextView) findViewById(R.id.obd_status);
//        mTableLayout = (TableLayout) findViewById(R.id.data_table);
//
//    }
//
//    @Override
//    protected void enableBluetooth() {
//        super.enableBluetooth();
//        mBtStatusTextView.setText(R.string.status_ready);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_OK) {
//            mBtStatusTextView.setText(getString(R.string.status_enabled));
//
//        } else {
//            mBtStatusTextView.setText(getString(R.string.status_disabled));
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    @Override
//    protected void bluetoothDiscoveryStarted() {
//        super.bluetoothDiscoveryStarted();
//        mBtStatusTextView.setText(R.string.status_discovering);
//    }
//
//    @Override
//    protected void bluetoothDiscoveryFinished() {
//        super.bluetoothDiscoveryFinished();
//        if (mBluetoothAdapter.isEnabled()) {
//            mBtStatusTextView.setText(R.string.status_ready);
//        }
//    }
//
//    @Override
//    protected void doBindService() {
//        super.doBindService();
//        if (!mBound) {
//            mTableLayout.removeAllViews(); //start fresh
//            mBtStatusTextView.setText(getString(R.string.status_connecting));
//        }
//    }
//
//    @Override
//    protected void onServiceConnected() {
//        super.onServiceConnected();
//        mBtStatusTextView.setText(R.string.status_connected);
//        invalidateOptionsMenu();
//    }
//
//    @Override
//    protected void checkServiceState() {
//        super.checkServiceState();
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
//    }
//
//    @Override
//    protected void onCommandResult(String cmdId, String cmdName, String cmdResult) {
//        super.onCommandResult(cmdId, cmdName, cmdResult);
//
//
//        // get row for this command if it has been created
//        TextView row = (TextView) mRootLayout.findViewWithTag(cmdId);
//
//        // if there is a row for this command, set it's new value
//        if (row != null) row.setText(cmdResult);
//
//            // else create and populate a new row
//        else addTableRow(cmdId, cmdName, cmdResult);
//
//        // record the result for logging elsewhere
//        mCommandResult.put(cmdId, cmdResult);
//    }
//
//    @Override
//    protected void removeCommand(ObdCommand command) {
//        super.removeCommand(command);
//        // get row for this command if it has been created
//        String id = LookUpObdCommand(command.getName());
//        TextView view = (TextView) mRootLayout.findViewWithTag(id);
//
//        // if there is a row for this command, remove it
//        if (view != null) mTableLayout.removeView(view);
//    }
//
//    @Override
//    protected void doUnbindService() {
//        if (mBound) {
//            mObdStatusTextView.setText(getString(R.string.status_disconnected));
//            mBtStatusTextView.setText(R.string.status_ready);
//        }
//        super.doUnbindService();
//    }
//
//    /** Adds a row to display the output result of a command */
//    private void addTableRow(String id, String name, String val) {
//        Log.v(TAG, "addTableRow()");
//
//        TableRow row = new TableRow(this);
//        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        params.setMargins(TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN);
//        row.setLayoutParams(params);
//
//        TextView command = new TextView(this);
//        command.setGravity(Gravity.RIGHT);
//        command.setText(name + ": ");
//        row.addView(command);
//
//        TextView result = new TextView(this);
//        result.setGravity(Gravity.LEFT);
//        result.setText(val);
//        result.setTag(id);
//        row.addView(result);
//
//        mTableLayout.addView(row, params);
//    }
//}
//
//
//
