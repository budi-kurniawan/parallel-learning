package gridworld.parallel;

import java.util.concurrent.locks.Lock;

import common.Environment;
import common.QEntry;
import common.agent.QLearningAgent;
import common.parallel.stopwalk.StopWalkAgent;
import gridworld.GridworldFactory;

public class GridworldStopWalkFactory extends GridworldFactory {
    protected Lock[] locks;
    public GridworldStopWalkFactory(QEntry[][] q, Lock[] locks) {
        super(q);
        this.locks = locks;
    }

    @Override
    public QLearningAgent createAgent(int index, Environment environment, int episode) {
        return new StopWalkAgent(index, environment, getStateActions(), getQ(), episode, locks);
    }
}
