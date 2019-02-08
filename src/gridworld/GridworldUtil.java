package gridworld;

import static common.Action.DOWN;
import static common.Action.LEFT;
import static common.Action.RIGHT;
import static common.Action.UP;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import common.QLearningAgent;
import common.CommonUtil;
import common.QEntry;
import common.StateAction;

public class GridworldUtil {
    public static final int CAPTION_HEIGHT = 30;
    public static int numRows = 8;
    public static int numCols = 8;
    public static int[] actions = {UP, DOWN, LEFT, RIGHT};
    private static int[][] stateActions;
    
    public static int[] stateToRowColumn(int state, int numCols) {
        return new int[] {state / numCols, state % numCols};
    }
    
    public static int getGoalState() {
        return numRows * numCols - 1;
    }
    
    public static synchronized int[][] getStateActions() {
        if (stateActions != null) {
            return stateActions;
        }
        int numStates = numRows * numCols;
        int[] topBoxActions = {DOWN, LEFT, RIGHT};
        int[] bottomBoxActions = {UP, LEFT, RIGHT};
        int[] leftBoxActions = {UP, DOWN, RIGHT};
        int[] rightBoxActions = {UP, DOWN, LEFT};
        int[] topLeftBoxActions = {DOWN, RIGHT};
        int[] topRightBoxActions = {DOWN, LEFT};
        int[] bottomLeftBoxActions = {UP, RIGHT};
        int[] bottomRightBoxActions = {UP, LEFT};

        stateActions = new int[numRows * numCols][actions.length];
        stateActions[0] = bottomLeftBoxActions;
        stateActions[numCols - 1] = bottomRightBoxActions;
        stateActions[numStates - 1] = topRightBoxActions;
        stateActions[numStates - numCols] = topLeftBoxActions;
        
        for (int i = 0; i < numStates; i++) {
            if (i == 0 || i == numCols - 1 || i == numStates - 1 || i == numStates - numCols) {
                continue;
            }
            if (i < numCols - 1) {
                stateActions[i] = bottomBoxActions;
            } else if (i > numStates - numCols) {
                stateActions[i] = topBoxActions;
            } else if (i % numCols == 0) {
                stateActions[i] = leftBoxActions;
            } else if (i % numCols == numCols - 1) {
                stateActions[i] = rightBoxActions;
            } else {
                stateActions[i] = actions;
            }
        }
        return stateActions;
    }
    
    public static QEntry[][] createInitialQ() {
        int numStates = numRows * numCols;
        QEntry[][] q = new QEntry[numStates][actions.length];
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < actions.length; j++) {
                q[i][j] = new QEntry();
            }
        }
        int[][] stateActions = getStateActions();
        // set q[state][disallowedAction] to -Double.MAX_VALUE and q[state][allowedAction] to 0
        for (int state = 0; state < numStates; state++) {
            for (int i = 0; i < actions.length; i++) {
                q[state][i].value = -Double.MAX_VALUE;
            }
            int[] actions = stateActions[state];
            for (int a : actions) {
                q[state][a].value = 0;
            }
        }
        return q;
    }
    
    public static QEntry[][] combineQTables(List<QEntry[][]> qTables) {
        QEntry[][] firstQTable = qTables.get(0);// create a reference for the if below
        int numStates = numRows * numCols;
        QEntry[][] combined = new QEntry[numStates][actions.length];
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < GridworldUtil.actions.length; j++) {
                combined[i][j] = new QEntry();
                if (firstQTable[i][j].value == -Double.MAX_VALUE) {
                    combined[i][j].value = -Double.MAX_VALUE;
                } else {
                    int totalCounter = 0;
                    double combinedValue = 0.0;
                    for (QEntry[][] qTable : qTables) {
                        totalCounter += qTable[i][j].counter;
                        combinedValue += qTable[i][j].counter * qTable[i][j].value;
                    }
                    if (totalCounter == 0) {
                        combined[i][j].value = 0;
                    } else {
                        combined[i][j].value = combinedValue / totalCounter;
                    }
                }
            }
        }
        return combined;
    }
    
    public static boolean policyFound(QEntry[][] qTable, List<StateAction> steps) {
        GridworldEnvironment environment = new GridworldEnvironment();
        QLearningAgent agent = new QLearningAgent(environment, GridworldUtil.getStateActions(), qTable, 1);
        int maxStepsAllowed = numCols + numRows;
        
        int stepsToGoal = 0;
        while (stepsToGoal < maxStepsAllowed) {
            stepsToGoal++;
            int prevState = agent.getState();
            agent.test();
            int action = agent.getAction();
            if (prevState != Integer.MIN_VALUE) {
                steps.add(new StateAction(prevState, action));
            }
            //int state = agent.getState();
            if (agent.getState() == getGoalState()) {
                return true;
            }
            if (agent.terminal) {
                return false;
            }
        }
        return agent.getState() == getGoalState();
    }
}