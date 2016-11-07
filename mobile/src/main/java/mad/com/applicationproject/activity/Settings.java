package mad.com.applicationproject.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.util.ArrayList;
import java.util.Set;

import mad.com.applicationproject.R;
import mad.com.applicationproject.io.availableObdCommands;

/**
 * Created by kiera on 1/11/2016.
 * TODO: Write class comment
 */
public class Settings extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    private static final String TAG = Settings.class.getSimpleName();

    public static final String UPLOAD_URL =                "upload_url_preference";
    public static final String UPLOAD_DATA =               "upload_data_preference";
    public static final String UPLOAD_VEHICLE_ID =         "upload_vehicle_id_preference";

    public static final String BLUETOOTH_ENABLE =          "bluetooth_enable_preference";
    public static final String BLUETOOTH_DEVICE =          "bluetooth_list_preference";

    public static final String GPS_ENABLE =                "gps_enable_preference";
    public static final String GPS_UPDATE_PERIOD =         "gps_update_period_preference";
    public static final String GPS_DISTANCE_PERIOD =       "gps_distance_period_preference";

    public static final String OBD_PROTOCOL =              "obd_protocols_list_preference";
    public static final String OBD_COMMANDS_SCREEN =       "obd_commands_screen";
    public static final String OBD_IMPERIAL_UNITS =        "obd_imperial_units_preference";
    public static final String OBD_UPDATE_PERIOD =         "obd_update_period_preference";
    public static final String OBD_MAX_FUEL_ECONOMY =      "obd_max_fuel_economy_preference";
    public static final String OBD_VOLUMETRIC_EFFICIENCY = "obd_volumetric_efficiency_preference";
    public static final String OBD_ENGINE_DISPLACEMENT =   "obd_engine_displacement_preference";
    public static final String OBD_CONFIG_READER =         "obd_reader_config_preference";

    public static final String LOGGING_ENABLE =            "logging_enable";
    public static final String LOGGING_DIRECTORY =         "logging_dirname";


    /** Validates a preference's new value when it's changed */
    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        switch (pref.getKey()) {
            case Settings.BLUETOOTH_ENABLE:
                break;
            case OBD_UPDATE_PERIOD:
            case OBD_VOLUMETRIC_EFFICIENCY:
            case OBD_ENGINE_DISPLACEMENT:
            case OBD_MAX_FUEL_ECONOMY:
            case GPS_UPDATE_PERIOD:
            case GPS_DISTANCE_PERIOD:
                try {
                    Double.parseDouble(newValue.toString().replace(",", "."));
                    return true;
                } catch (Exception e) {
                    Toast.makeText(this,
                            "Couldn't parse '" + newValue.toString() + "' as a number.",
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);


        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String[] preferenceKeys = new String[]{
                OBD_ENGINE_DISPLACEMENT,
                OBD_VOLUMETRIC_EFFICIENCY,
                OBD_UPDATE_PERIOD,
                OBD_MAX_FUEL_ECONOMY};

        for (String preferenceKey : preferenceKeys) {
            EditTextPreference editTextPreference = (EditTextPreference) getPreferenceScreen()
                    .findPreference(preferenceKey);
            editTextPreference.setOnPreferenceChangeListener(this);
        }


        // Available OBD commands. TODO: This should be read from mPreferences database
        final PreferenceScreen cmdScreen = (PreferenceScreen) getPreferenceScreen()
                .findPreference(OBD_COMMANDS_SCREEN);
//        Preference
        for (ObdCommand cmd : availableObdCommands.getCommands()) {
            final CheckBoxPreference cpref = new CheckBoxPreference(this);
            cpref.setTitle(cmd.getName());
            cpref.setKey(cmd.getName());
            cpref.setChecked(sharedPrefs.getBoolean(cmd.getName(), true));
            cmdScreen.addPreference(cpref);
        }


        final ArrayList<CharSequence> protocolStrings = new ArrayList<>();
        final ListPreference listProtocols = (ListPreference) getPreferenceScreen()
                .findPreference(OBD_PROTOCOL);

        // Available OBD protocols
        for (ObdProtocols protocol : ObdProtocols.values()) {
            protocolStrings.add(protocol.name());
        }
        listProtocols.setEntries(protocolStrings.toArray(new CharSequence[0]));
        listProtocols.setEntryValues(protocolStrings.toArray(new CharSequence[0]));


        // use Bluetooth adapter to select which paired OBD-II compliant device we'll use.
        final BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        final ArrayList<CharSequence> pairedDeviceStrings = new ArrayList<>();
        final ArrayList<CharSequence> values = new ArrayList<>();
        final ListPreference listBluetooth = (ListPreference) getPreferenceScreen()
                .findPreference(BLUETOOTH_DEVICE);

        // if Bluetooth not supported
        if (mBtAdapter == null) {
            listBluetooth.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
            listBluetooth.setEntryValues(values.toArray(new CharSequence[0]));

            // we shouldn't get here, still warn user
            Toast.makeText(this, "This device does not support Bluetooth.",
                    Toast.LENGTH_LONG).show();

            return;
        }

        // listen for preference click. TODO there are so many repeated validations :-/
        final Activity thisActivity = this;
        listBluetooth.setEntries(new CharSequence[1]);
        listBluetooth.setEntryValues(new CharSequence[1]);
        listBluetooth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // see what I mean in the previous comment?
                if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
                    Toast.makeText(thisActivity,
                            "This device does not support Bluetooth or it is disabled.",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

        // get paired devices and populate preference list.
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceStrings.add(device.getName() + "\n" + device.getAddress());
                values.add(device.getAddress());
            }
        }
        listBluetooth.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
        listBluetooth.setEntryValues(values.toArray(new CharSequence[0]));
    }

    @Override
    public void onResume() {
        super.onResume();
//        getPreferenceScreen().getSharedPreferences()
//                .registerOnSharedPreferenceChangeListener(mChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
//        getPreferenceScreen().getSharedPreferences()
//                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public static int getObdUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs.getString(OBD_UPDATE_PERIOD, "1"); // (seconds)

        int period = (int) (Double.parseDouble(periodString) * 1000);

        if (period < 0) {
            period = 2000; // default (milliseconds)
        }

        return period;
    }

    public static double getVolumetricEfficieny(SharedPreferences prefs) {
        String veString = prefs.getString(OBD_VOLUMETRIC_EFFICIENCY, ".85");
        double ve = 0.85;
        try {
            ve = Double.parseDouble(veString);
        } catch (Exception e) {
        }
        return ve;
    }

    public static double getEngineDisplacement(SharedPreferences prefs) {
        String edString = prefs.getString(OBD_ENGINE_DISPLACEMENT, "1.6");
        double ed = 1.6;
        try {
            ed = Double.parseDouble(edString);
        } catch (Exception e) {
        }
        return ed;
    }

    public static ArrayList<ObdCommand> getObdCommands(SharedPreferences prefs) {
        // create new list of commands to populate and return
        ArrayList<ObdCommand> commands = new ArrayList<>();

        // loop through available commands adding those enabled in preferences
        for (ObdCommand cmd : availableObdCommands.getCommands()) {
            if (prefs.getBoolean(cmd.getName(), true)) {
                commands.add(cmd);
            }
        }
        return commands;
    }

    public static double getMaxFuelEconomy(SharedPreferences prefs) {
        String maxStr = prefs.getString(OBD_MAX_FUEL_ECONOMY, "70");
        double max = 70;
        try {
            max = Double.parseDouble(maxStr);
        } catch (Exception e) {
        }
        return max;
    }

    public static String[] getReaderConfigCommands(SharedPreferences prefs) {
        String cmdsStr = prefs.getString(OBD_CONFIG_READER, "atsp0\natz");
        String[] cmds = cmdsStr.split("\n");
        return cmds;
    }
}

