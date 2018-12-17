package rl.event;

import java.util.EventObject;
import rl.QEntry;

public class TrialEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private long startTime;
    private long endTime;
    private QEntry[][] q;
    
    public TrialEvent(Object source, long startTime, long endTime, QEntry[][] q) {
        super(source);
        this.startTime = startTime;
        this.endTime = endTime;
        this.q = q;
    }
    
    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public QEntry[][] getQ() {
        return q;
    }
}
