package common.event;

import java.util.EventObject;

public class TrialEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private long startTime;
    private long endTime;
    
    public TrialEvent(Object source, long startTime, long endTime) {
        super(source);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
