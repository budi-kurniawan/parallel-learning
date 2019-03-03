package common.parallel;

import java.util.List;

import common.Environment;
import common.QEntry;
import common.agent.QLearningAgent;

public class ParallelQLearningAgent extends QLearningAgent {
    private List<QEntry[][]> qTables;
    private boolean sharingQTable;
    
    public ParallelQLearningAgent(int agentIndex, Environment environment, int[][] stateActions, List<QEntry[][]> qTables, int episode) {
        super(environment, stateActions, null, episode);
        this.index = agentIndex;
        this.qTables = qTables;
        this.sharingQTable = qTables.get(0) == qTables.get(qTables.size() - 1);
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

    @Override
    protected double getQValue(int state, int action) {
        if (sharingQTable) {
            QEntry[][] q = qTables.get(index);
            return q[state][action].value;
        }
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
            }
            if (sharingQTable) {
                break;
            }
        }
        return maxValue;
    }
}