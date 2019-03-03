package cartpole.parallel;

import java.util.concurrent.locks.Lock;

import cartpole.QLearningCartpoleFactory;
import common.Environment;
import common.QEntry;
import common.agent.QLearningAgent;
import common.parallel.stopwalk.StopWalkAgent;

public class CartpoleStopWalkFactory extends QLearningCartpoleFactory {
    protected Lock[] locks;
    public CartpoleStopWalkFactory(QEntry[][] q, Lock[] locks) {
        super(q);
        this.locks = locks;
    }

    @Override
    public QLearningAgent createAgent(int index, Environment environment, int episode) {
        return new StopWalkAgent(index, environment, getStateActions(), getQ(), episode, locks);
    }
}
