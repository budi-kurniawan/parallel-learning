package cartpole;

import common.AbstractEngine;
import common.Agent;
import common.Environment;
import common.QEntry;

public class CartpoleEngine extends AbstractEngine {
    
    private QEntry[][] q;
    private Class environmentClass;
    public CartpoleEngine(QEntry[][] q, Class environmentClass) {
        super();
        this.q = q;
        this.environmentClass = environmentClass;
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
        Environment environment = null;
        try {
            environment = (Environment) environmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return environment;
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