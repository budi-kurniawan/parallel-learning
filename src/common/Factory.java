package common;

import common.agent.QLearningAgent;

/*
 * Injected to an Engine, an instance of Factory is responsible for creating objects needed by an Engine
 * 
 */
public interface Factory {
    QEntry[][] getQ();
    int[] getActions();
    Environment createEnvironment();
    int[][] getStateActions();
    QLearningAgent createAgent(int index, Environment environment, int episode);
}
