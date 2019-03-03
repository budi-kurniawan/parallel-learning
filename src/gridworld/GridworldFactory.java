package gridworld;

import common.Agent;
import common.Environment;
import common.Factory;
import common.QEntry;
import common.agent.QLearningAgent;

public class GridworldFactory implements Factory {
    protected QEntry[][] q;
    public GridworldFactory(QEntry[][] q) {
        this.q = q;
    }
    
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
    public Agent createAgent(int index, Environment environment, int episode) {
        return new QLearningAgent(environment, GridworldUtil.getStateActions(), q, episode);
    }
}