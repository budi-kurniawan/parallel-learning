package rl.listener;

import rl.event.TrialEvent;

public interface TrialListener {
    void beforeTrial(TrialEvent event);
    void afterTrial(TrialEvent event);
}
