package common.parallel.stopwalk;

import java.util.concurrent.locks.Lock;

import common.Engine;
import common.Factory;
import common.listener.EpisodeListener;

public class StopWalkEngine extends Engine {
    private Lock[] locks;

    public StopWalkEngine(int index, Factory factory, Lock[] locks) {
        super(index, factory);
        this.locks = locks;
    }
    
    public StopWalkEngine(int index, Factory factory, EpisodeListener episodeListener, Lock[] locks) {
        super(index, factory, episodeListener);
        this.locks = locks;
    }
}