package common.event;

import java.util.EventObject;
import java.util.List;

import common.QEntry;

public class TrialEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private long startTime;
    private long endTime;
    private QEntry[][] q;
    private List<QEntry[][]> qTables;
    
    public TrialEvent(Object source, long startTime, long endTime, QEntry[][] q) {
        super(source);
        this.startTime = startTime;
        this.endTime = endTime;
        this.q = q;
    }

    public TrialEvent(Object source, long startTime, long endTime, List<QEntry[][]> qTables) {
        super(source);
        this.startTime = startTime;
        this.endTime = endTime;
        this.qTables = qTables;
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
    
    public List<QEntry[][]> getQTables() {
        return qTables;
    }
}
