package common.parallel.stopwalk;

import java.util.concurrent.locks.Lock;

import common.AbstractEngine;

public abstract class StopWalkEngine extends AbstractEngine {
    private int agentIndex;
    private Lock[] locks;

//    public StopWalkEngine(int agentIndex, QEntry[][] q, Lock[] locks) {
//        super(q);
//        this.agentIndex = agentIndex;
//        this.locks = locks;
//    }

//    @Override
//    protected Agent createAgent(GridworldEnvironment environment, int episode) {
//        return new StopWalkAgent(agentIndex, environment, stateActions, q, episode, locks);
//    }    
}