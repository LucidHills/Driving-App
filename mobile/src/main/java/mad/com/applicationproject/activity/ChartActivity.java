package mad.com.applicationproject.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

import mad.com.applicationproject.R;

/**
 * Created by kiera on 8/11/2016.
 * TODO: Write class comment
 */
public class ChartActivity extends MainActivity {

    private static final String TAG = ChartActivity.class.getSimpleName();

    private LineChart mLineChart;
    private PieChart mPieChart;
    private ArrayList<PieEntry> mPieEntries = new ArrayList<>();
    private ArrayList<Entry> mLineEntries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_chart);

        mLineChart = (LineChart) findViewById(R.id.line_chart);
        mPieChart = (PieChart) findViewById(R.id.pie_chart);
        setupChart();
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

    private void setupChart() {
        if (mLineChart.isEmpty()) {
            //mPieEntries.add(new Entry(0f, 0f));
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
}
