package gridworld;

import common.Environment;
import common.Factory;
import common.QEntry;
import common.QLearningAgent;

public class GridworldFactory implements Factory {
    protected QEntry[][] q;
    public GridworldFactory(QEntry[][] q) {
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
    public QLearningAgent createAgent(int index, Environment environment, int episode) {
        return new QLearningAgent(environment, GridworldUtil.getStateActions(), q, episode);
    }
}