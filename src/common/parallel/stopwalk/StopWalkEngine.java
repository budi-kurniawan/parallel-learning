package common.parallel.stopwalk;

import java.util.concurrent.locks.Lock;

import common.Agent;
import common.Engine;
import common.Environment;
import common.QEntry;

public class StopWalkEngine extends Engine {
    private int agentIndex;
    private Lock[] locks;

    public StopWalkEngine(int agentIndex, QEntry[][] q, Lock[] locks) {
        super(q);
        this.agentIndex = agentIndex;
        this.locks = locks;
    }

    @Override
    protected Agent createAgent(Environment environment, int episode, int numEpisodes) {
        return new StopWalkAgent(agentIndex, environment, stateActions, q, episode, numEpisodes, locks);
    }    
}