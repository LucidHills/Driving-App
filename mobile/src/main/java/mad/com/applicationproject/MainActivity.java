package mad.com.applicationproject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;

    GridLayout mGridLayout;

    private TextView mLatitudeText, mLongitudeText, mAcceleration;
    private LineChart mLineChart;
    MyLocation mLocation;

    private List<Entry> mLineEntries = new ArrayList<>();
    private List<GridItem> mGridList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        mGridList.add(new GridItem("Item 1"));
//        mGridList.add(new GridItem("Item 2"));
//        mGridList.add(new GridItem("Item 3"));
//        mGridList.add(new GridItem("Item 4"));
//        mGridList.add(new GridItem("Item 5"));
//        mGridList.add(new GridItem("Item 6"));
//        mGridList.add(new GridItem("Item 7"));
//        mGridList.add(new GridItem("Item 8"));
//        mGridList.add(new GridItem("Item 9"));
//        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//        mAdapter = new MyAdapter(this, mGridList);
//        mLayoutManager = new GridLayoutManager(this, 3); //int is columns
//        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.setLayoutManager(mLayoutManager);


        mGridLayout = (GridLayout) findViewById(R.id.content_main);
        TextView textView = new TextView(this);
        textView.setText("Text View");
//        textView.

//        mGridLayout.addView(textView, gridParams());


        //Declare views
        mLatitudeText = (TextView) findViewById(R.id.latitude_text);
        mLongitudeText = (TextView) findViewById(R.id.longitude_text);
        mAcceleration = (TextView) findViewById(R.id.acceleration_view);

        mLineChart = (LineChart) findViewById(R.id.line_chart);
        setupChart();

        //mLocation = new MyLocation(this, this);
    }

    private GridLayout.LayoutParams gridParams() {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = GridLayout.LayoutParams.WRAP_CONTENT;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        //margins
        //row (undefined, span, alignment, weight)
//        params.rowSpec = GridLayout.LayoutParams.
        //column (undefined, span, alignment, weight)
        GridLayout.Spec column = GridLayout.spec(1, GridLayout.BASELINE);
        GridLayout.Spec row = GridLayout.spec(2);
        params = new GridLayout.LayoutParams();
        return params;
    }

    private void setupChart() {
        if (mLineChart.isEmpty()) {
            //mLineEntries.add(new Entry(0f, 0f));
            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(createLineDataSet());
            LineData lineData = new LineData(dataSets);
            mLineChart.setData(lineData);
            mLineChart.invalidate();

            //Formatting
            mLineChart.setDescription("");
            mLineChart.getLegend().setEnabled(false);
            mLineChart.setScaleEnabled(false);

            XAxis x1 = mLineChart.getXAxis();
            x1.setPosition(XAxis.XAxisPosition.BOTTOM);
            x1.setDrawLabels(false);
            x1.setDrawGridLines(false);

            YAxis y1 = mLineChart.getAxisRight();
            y1.setTextColor(Color.WHITE);
            y1.setAxisMaxValue(100f);
            y1.setAxisMinValue(0f);
            y1.setDrawGridLines(true);

            mLineChart.getAxisLeft().setEnabled(false);
        }
    }

    private LineDataSet createLineDataSet() {
        LineDataSet dataSet = new LineDataSet(mLineEntries, "Data");
        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        int color = ContextCompat.getColor(this, R.color.colorAccent);
        int alpha = 50; // out of 255

        //Formatting
        //dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(1);
        dataSet.setDrawFilled(true);
        dataSet.setColor(color);
        dataSet.setFillColor(color);
//        dataSet.setCubicIntensity(0.9f);
//        dataSet.setColor(ColorTemplate.getHoloBlue());
//        dataSet.setCircleColor(ColorTemplate.getHoloBlue());
//        dataSet.setLineWidth(2f);
//        dataSet.setCircleRadius(4f);
//        dataSet.setFillAlpha(65);
//        dataSet.setFillColor(ColorTemplate.getHoloBlue());
//        dataSet.setHighLightColor(Color.rgb(244,117,177));
//        dataSet.setValueTextColor(Color.WHITE);
//        dataSet.setValueTextSize(10f);

        return dataSet;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLineChart != null) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < 1000; i++) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addLineEntry();
                            }
                        });

                        try {
                            Thread.sleep(125);
                        } catch (InterruptedException e) {
                            Log.i(TAG, "thread sleep FAILED");
                        }
                    }
                }
            };
            thread.start();
        }
    }

    private void addLineEntry() {
        LineData data = mLineChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                Log.i(TAG, "addLineEntry() set == NULL");
                set = createLineDataSet();
                data.addDataSet(set);
            }

//            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 100)), 0);
            data.addEntry(new Entry(set.getEntryCount(), (float) ((Math.sin( (float) set.getEntryCount() / 3) * 50) + 50)), 0);
            data.notifyDataChanged();
            mLineChart.notifyDataSetChanged();

            mLineChart.setVisibleXRangeMaximum(50);
            mLineChart.moveViewToX(data.getEntryCount());
//            mLineChart.moveViewToAnimated(data.getEntryCount(), 0, YAxis.AxisDependency.RIGHT, 100);

//            mLineChart.invalidate(); //Refresh

        } else {
            Log.i(TAG, "addLineEntry() data == NULL");
        }
    }


    public void displayLocation(Context context, Location lastLocation) {
        Resources res = context.getResources();
        String latitude = res.getString(R.string.latitude) + " "
                + String.valueOf(lastLocation.getLatitude());
        String longitude = res.getString(R.string.longitude) + " "
                + String.valueOf(lastLocation.getLongitude());
        mLatitudeText.setText(latitude);
        mLongitudeText.setText(longitude);
    }

    /*
    public void setupSensors() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(new MySensorEventListener(this),
                accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void displayAccelerometer(String[] data) {
        //mAcceleration.setText(arrayToString(data, R.array.accel));
    }

    public void setupLocation() {
        LocationManager locationManager;
        MyLocationListener locationListener;
        locationListener = new MyLocationListener(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    public void displayLocation(String[] data) {
        //mLatitudeText.setText(arrayToString(data, R.array.loc));
    }

    public String arrayToString(String[] data, int id) {
        String string = "";
        Resources res = getResources();
        String[] ui = res.getStringArray(id);
        for (int i = 0; i < data.length; i++) {
            string += ui[i] + data[i];
        }
        return string;
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
