package rl.parallel;

import rl.Agent;
import rl.Environment;
import rl.QEntry;

public class ParallelAgent2 extends Agent {
    private QEntry[][] q;
    private QEntry[][] otherQ;
    private String agentId;
    
    public ParallelAgent2(String agentId, Environment environment, int[][] stateActions, QEntry[][] q, QEntry[][] otherQ, int episode, int numEpisodes) {
        super(environment, stateActions, null, episode, numEpisodes);
        this.agentId = agentId;
        this.q = q;
        this.otherQ = otherQ;
    }

    @Override
    public void updateQValue(int state, int action, double value) throws Exception {
        if (q[state][action].value == -Double.MAX_VALUE) {
            throw new Exception("updating q, state: " + state + ", action:" + action + ", value:" + value);
        }
        QEntry qEntry = q[state][action];
        qEntry.value = value;
        qEntry.counter = qEntry.counter + 1;
    }

    @Override
    protected double getQValue(int state, int action) {
        QEntry qEntry = q[state][action];
        return qEntry.value;
//        QEntry otherQEntry = otherQ[state][action];
//        if (qEntry.counter == 0 && otherQEntry.counter == 0) {
//            return 0;
//        }
//        return (qEntry.value * qEntry.counter + otherQEntry.value * otherQEntry.counter) / (qEntry.counter + otherQEntry.counter);
    }

    @Override
    public double getMaxQ(int state) {
        double maxValue = -Double.MAX_VALUE;
        for (int i = 0; i < stateActions[state].length; i++) {
            int action = stateActions[state][i];
            QEntry qEntry = q[state][action];
//            QEntry otherQEntry = otherQ[state][action];
            if (qEntry.value > maxValue) {
                maxValue = qEntry.value;
            }
//            if (otherQEntry.value > maxValue) {
//                maxValue = otherQEntry.value;
//            }
        }
        return maxValue;
    }
    
    public String getAgentId() {
        return agentId;
    }
}