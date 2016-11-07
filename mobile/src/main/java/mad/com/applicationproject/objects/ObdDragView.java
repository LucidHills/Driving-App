package mad.com.applicationproject.objects;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.pires.obd.commands.ObdCommand;

import java.util.ArrayList;

import mad.com.applicationproject.R;
import mad.com.applicationproject.io.ObdCommandJob;

/**
 * Created by kiera on 7/11/2016.
 * TODO: Write class comment
 */
public class ObdDragView extends ObdCommandObject {

    private static final String TAG = ObdCommandObject.class.getSimpleName();

    private ViewGroup mParent;
    private TextView mName;
    private TextView mData;
    private TextView mUnit;

    public ObdDragView(Context context, ViewGroup parent) {
        this(context, parent, null);
    }

    public ObdDragView(Context context, ViewGroup parent, ObdCommand command) {
        super(context, command);
        Log.d(TAG, "ObdDragView created");
//        DragFrameLayout.inflate(context, R.layout.drag_obd_item, root);
//        inflater.inflate(R.layout.drag_obd_item, root, false);

        mName = (TextView) parent.findViewById(R.id.name);
        mData = (TextView) parent.findViewById(R.id.data);
        mUnit = (TextView) parent.findViewById(R.id.unit);

        update();
    }

    @Override
    public void update(ObdCommandJob obdCommandJob) {
        super.update(obdCommandJob);

        mName.setText(getName());
        mData.setText(getData());
        mUnit.setText(getUnit());
    }

    private ArrayList<View> findAllTextViews(ViewGroup parent) {
        final ArrayList<View> array = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            if (child instanceof ViewGroup) {
                array.addAll(findAllTextViews((ViewGroup) child)); // to get nested children
            } else if (child instanceof TextView) {
                array.add(child);
            }
        }
        return array;
    }
}
