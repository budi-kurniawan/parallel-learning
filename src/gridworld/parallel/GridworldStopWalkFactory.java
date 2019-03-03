package gridworld.parallel;

import java.util.concurrent.locks.Lock;

import common.Agent;
import common.Environment;
import common.QEntry;
import common.parallel.stopwalk.StopWalkQLearningAgent;
import gridworld.GridworldFactory;

public class GridworldStopWalkFactory extends GridworldFactory {
    protected Lock[] locks;
    public GridworldStopWalkFactory(QEntry[][] q, Lock[] locks) {
        super(q);
        this.locks = locks;
    }

    @Override
    public Agent createAgent(int index, Environment environment, int episode) {
        return new StopWalkQLearningAgent(index, environment, getStateActions(), getQ(), episode, locks);
    }
}
