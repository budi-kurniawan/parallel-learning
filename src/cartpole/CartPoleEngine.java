package cartpole;

import common.AbstractEngine;
import common.Agent;
import common.Environment;
import common.QEntry;

public class CartpoleEngine extends AbstractEngine {
    
    private QEntry[][] q;
    public CartpoleEngine(QEntry[][] q) {
        super();
        this.q = q;
    }
    
    @Override
    public QEntry[][] getQ() {
        return q;
    }
    
    @Override
    public int[] getActions() {
        return CartpoleUtil.actions;
    }
    
    @Override
    public Environment createEnvironment() {
        return new ActorCriticCartpoleEnvironment();
    }
    
    @Override
    public int[][] getStateActions() {
        return CartpoleUtil.getStateActions();
    }

    @Override
    public Agent createAgent(Environment environment, int episode) {
        return new Agent(environment, getStateActions(), q, episode);
    }
}