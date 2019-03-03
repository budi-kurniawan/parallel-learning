package cartpole;

import common.Agent;
import common.Environment;
import common.QEntry;
import common.agent.QLearningAgent;

public class QLearningCartpoleFactory extends CartpoleFactory {
    protected QEntry[][] q;
    public QLearningCartpoleFactory(QEntry[][] q) {
        super();
        this.q = q;
    }

    @Override
    public Environment createEnvironment() {
        return new QLearningCartpoleEnvironment();
    }

    @Override
    public Agent createAgent(int index, Environment environment, int episode) {
        return new QLearningAgent(environment, getStateActions(), q, episode);
    }
    
    public QEntry[][] getQ() {
        return q;
    }
}
