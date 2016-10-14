package mad.com.applicationproject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

/**
 *
 */
public class MySensorEventListener extends MainActivity implements SensorEventListener{
    private Context mContext;

    public MySensorEventListener(Context context) {
        mContext = context;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null) {
            Log.e("  ACCEL-X: ", " " + String.valueOf(event.values[0]));
            Log.e("  ACCEL-Y: ", " " + String.valueOf(event.values[1]));
            Log.e("  ACCEL-Z: ", " " + String.valueOf(event.values[2]));
            //displayAccelerometer(eventToString(event));
        }
    }

    public String[] eventToString(SensorEvent e) {
        int num = e.values.length;
        String[] array = new String[num];
        for (int i = 0; i < num; i++) {
            array[i] = String.valueOf(e.values[i]);
        }
        return array;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
