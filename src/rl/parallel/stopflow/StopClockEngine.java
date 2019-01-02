package rl.parallel.stopflow;

import java.util.concurrent.locks.Lock;
import rl.Agent;
import rl.Engine;
import rl.Environment;
import rl.QEntry;

public class StopClockEngine extends Engine {
    private int agentIndex;
    private Lock[] locks;

    public StopClockEngine(int agentIndex, QEntry[][] q, Lock[] locks) {
        super(q);
        this.agentIndex = agentIndex;
        this.locks = locks;
    }

    @Override
    protected Agent createAgent(Environment environment, int episode, int numEpisodes) {
        return new StopClockAgent(agentIndex, environment, stateActions, q, episode, numEpisodes, locks);
    }    
}