package rl.parallel;

import java.util.List;

import rl.Agent;
import rl.Environment;
import rl.QEntry;

public class ParallelAgent extends Agent {
//    private QEntry[][] q;
//    private QEntry[][] otherQ;
    private List<QEntry[][]> qTables;
    
//    public ParallelAgent(int agentIndex, Environment environment, int[][] stateActions, QEntry[][] q, QEntry[][] otherQ, int episode, int numEpisodes) {
//        super(environment, stateActions, null, episode, numEpisodes);
//        this.index = agentIndex;
//        this.q = q;
//        this.otherQ = otherQ;
//    }
    
    public ParallelAgent(int agentIndex, Environment environment, int[][] stateActions, List<QEntry[][]> qTables, int episode, int numEpisodes) {
        super(environment, stateActions, null, episode, numEpisodes);
        this.index = agentIndex;
        this.qTables = qTables;
    }
    
    @Override
    public void updateQValue(int state, int action, double value) throws Exception {
        QEntry[][] q = qTables.get(index);
        if (q[state][action].value == -Double.MAX_VALUE) {
            throw new Exception("updating q, state: " + state + ", action:" + action + ", value:" + value);
        }
        QEntry qEntry = q[state][action];
        qEntry.value = value;
        qEntry.counter = qEntry.counter + 1;
    }
//    @Override
//    public void updateQValue(int state, int action, double value) throws Exception {
//        if (q[state][action].value == -Double.MAX_VALUE) {
//            throw new Exception("updating q, state: " + state + ", action:" + action + ", value:" + value);
//        }
//        QEntry qEntry = q[state][action];
//        qEntry.value = value;
//        qEntry.counter = qEntry.counter + 1;
//    }

    @Override
    protected double getQValue(int state, int action) {
        int counterTotal = 0;
        double avgWeightedValue = 0.0;
        for (QEntry[][] q : qTables) {
            QEntry qEntry = q[state][action];
            counterTotal += qEntry.counter;
            avgWeightedValue += qEntry.value * qEntry.counter;
        }
        if (counterTotal == 0) {
            return 0.0;
        } else {
            return avgWeightedValue / counterTotal;
        }
    }
//    @Override
//    protected double getQValue(int state, int action) {
//        QEntry[][] q = qTables.get(index);
//        QEntry qEntry = q[state][action];
//        QEntry otherQEntry = otherQ[state][action];
//        if (qEntry.counter == 0 && otherQEntry.counter == 0) {
//            return 0;
//        }
//        return (qEntry.value * qEntry.counter + otherQEntry.value * otherQEntry.counter) / (qEntry.counter + otherQEntry.counter);
//    }

    @Override
    public double getMaxQ(int state) {
        double maxValue = -Double.MAX_VALUE;
        for (QEntry[][] q : qTables) {
            for (int i = 0; i < stateActions[state].length; i++) {
                int action = stateActions[state][i];
                QEntry qEntry = q[state][action];
                if (qEntry.value > maxValue) {
                    maxValue = qEntry.value;
                }
//                QEntry otherQEntry = otherQ[state][action];
//                if (otherQEntry.value > maxValue) {
//                    maxValue = otherQEntry.value;
//                }
            }
        }
        return maxValue;
    }
}