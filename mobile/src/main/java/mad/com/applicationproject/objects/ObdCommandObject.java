package mad.com.applicationproject.objects;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import com.github.pires.obd.commands.ObdCommand;

import mad.com.applicationproject.io.ObdCommandJob;

/**
 * Created by kiera on 7/11/2016.
 * TODO: Write class comment
 */
public class ObdCommandObject extends FrameLayout {

    private static final String TAG = ObdCommandObject.class.getSimpleName();

    private String mName;
    private String mData;
    private String mUnit;

    private ObdCommand mCommand;

    public ObdCommandObject(Context context) {
        this(context, null);
    }

    public ObdCommandObject(Context context, ObdCommand command) {
        super(context, null, 0, 0);
        setCommand(command);
    }

    public void update() {
        if (mCommand!= null) {
            setName(mCommand.getName());
            setData(mCommand.getCalculatedResult());
            setUnit(mCommand.getResultUnit());
        } else {
            setName("Command Name");
            setData("Data");
            setUnit("Unit");
        }
    }

    public void update(ObdCommandJob obdCommandJob) {
        if (obdCommandJob != null) {
            setCommand(obdCommandJob.getCommand());
            setName(mCommand.getName());
            setUnit(mCommand.getResultUnit());
            switch (obdCommandJob.getState()) {
                case NO_DATA:
                case EXECUTION_ERROR:
                    setData(mCommand.getResult());
                    break;
                case NOT_SUPPORTED:
                    setData("N/A");
                    break;
                case MISUNDERSTOOD:
                case BROKEN_PIPE:
                    setData("ERROR");
                    break;
                default:
                    setData(mCommand.getCalculatedResult());
            }
        } else Log.e(TAG, "obdCommandJob is null", new NullPointerException());
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = (name != null) ? name : "";
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        mData = (data != null) ? data : "";
    }

    public String getUnit() {
        return mUnit;
    }

    public void setUnit(String unit) {
        mUnit = (unit != null) ? unit : "";
    }

    public ObdCommand getCommand() {
        return mCommand;
    }

    public void setCommand(ObdCommand command) {
        mCommand = command;
        update();
    }
}
