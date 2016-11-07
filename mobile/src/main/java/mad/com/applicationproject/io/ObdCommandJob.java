package mad.com.applicationproject.io;

import com.github.pires.obd.commands.ObdCommand;

/**
 * This class represents a job that ObdService will have to execute and
 * maintain until the job is finished. It is, thereby, the application
 * representation of an ObdCommand instance plus a state that will be
 * interpreted and manipulated by ObdService.
 */
public class ObdCommandJob {

    private int mId;
    private ObdCommand mCommand;
    private ObdCommandJobState mState;

    private int mWait = 0;

    public ObdCommandJob(ObdCommand command) {
        this(command, 0);
    }

    public ObdCommandJob(ObdCommand command, int waitAfterExecution) {
        mCommand = command;
        mState = ObdCommandJobState.NEW;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public ObdCommand getCommand() {
        return mCommand;
    }

    public ObdCommandJobState getState() {
        return mState;
    }

    public void setState(ObdCommandJobState state) {
        mState = state;
    }

    public int getWait() {
        return mWait;
    }

    public void setWait(int wait) {
        mWait = wait;
    }

    /** The state of the command */
    public enum ObdCommandJobState {
        NEW,
        RUNNING,
        FINISHED,
        NO_DATA,
        MISUNDERSTOOD,
        EXECUTION_ERROR,
        BROKEN_PIPE,
        QUEUE_ERROR,
        NOT_SUPPORTED
    }

}

