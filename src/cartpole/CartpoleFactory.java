package cartpole;

import common.Environment;
import common.Factory;
import common.QEntry;
import common.agent.QLearningAgent;

public abstract class CartpoleFactory implements Factory {
    protected QEntry[][] q;
    public CartpoleFactory(QEntry[][] q) {
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
    public int[][] getStateActions() {
        return CartpoleUtil.getStateActions();
    }

    @Override
    public QLearningAgent createAgent(int index, Environment environment, int episode) {
        return new QLearningAgent(environment, getStateActions(), q, episode);
    }

}
