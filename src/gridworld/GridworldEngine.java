package gridworld;

import common.Agent;
import common.AbstractEngine;
import common.Environment;
import common.QEntry;

public class GridworldEngine extends AbstractEngine {
    
    private QEntry[][] q;
    public GridworldEngine(QEntry[][] q) {
        super();
        this.q = q;
    }
    
    @Override
    public QEntry[][] getQ() {
        return q;
    }
    
    @Override
    public int[] getActions() {
        return GridworldUtil.actions;
    }
    
    @Override
    public Environment createEnvironment() {
        return new GridworldEnvironment();
    }
    
    @Override
    public int[][] getStateActions() {
        return GridworldUtil.getStateActions();
    }

    @Override
    public Agent createAgent(Environment environment, int episode) {
        return new Agent(environment, GridworldUtil.getStateActions(), q, episode);
    }
    

}
