package cartpole;

import common.QEntry;;

public class CartpoleUtil {
    public static final int NUM_STATES = 163;
    public static int[] actions = {0, 1};
    private static int[][] stateActions;

    
    public static synchronized int[][] getStateActions() {
        if (stateActions != null) {
            return stateActions;
        }
        int[][] stateActions = new int[NUM_STATES][actions.length];
        for (int i = 0; i < NUM_STATES; i++) {
            stateActions[i] = actions;
        }
        return stateActions;
    }
    
    public static QEntry[][] createInitialQ() {
        QEntry[][] q = new QEntry[NUM_STATES][actions.length];
        for (int i = 0; i < NUM_STATES; i++) {
            for (int j = 0; j < actions.length; j++) {
                q[i][j] = new QEntry();
            }
        }
        return q;
    }    
}
