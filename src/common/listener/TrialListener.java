package common.listener;

import common.event.TrialEvent;

public interface TrialListener {
    void beforeTrial(TrialEvent event);
    void afterTrial(TrialEvent event);
}
