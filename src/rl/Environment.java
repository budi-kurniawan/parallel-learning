package rl;

import static rl.Action.DOWN;
import static rl.Action.LEFT;
import static rl.Action.RIGHT;
import static rl.Action.UP;

public class Environment {

    private int[][] gridworld;
    private int maxState;
    
    //public static int[] wells = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97};
    public static int[] wells = {3, 7, 13, 19, 29, 37, 43, 53, 61, 71, 79, 89};
    //public static int[] wells = {55};

    public Environment() {
        this.gridworld = new int[Util.numRows][Util.numCols];
        gridworld[Util.numRows - 1][Util.numCols - 1] = 1; // goal state
        int numBoxes = Util.numRows * Util.numCols;
        for (int state : wells) {
            if (state + 1 > numBoxes) {
                break;
            }
            int[] rowCol = Util.stateToRowColumn(state, Util.numCols);
            gridworld[rowCol[0]][rowCol[1]] = -1; // terminal state
        }
        maxState = Util.numRows * Util.numCols - 1;
    }

    public Result submit(int state, int action) {
        int nextState;
        switch (action) {
        case UP:
            nextState = state + Util.numCols;
            if (nextState > maxState) {
                nextState = state;
            }
            break;
        case DOWN:
            nextState = state - Util.numCols;
            if (nextState < 0) {
                nextState = state;
            }
            break;
        case LEFT:
            if (state % Util.numCols == 0) {
                nextState = state;
            } else {
                nextState = state - 1;
            }
            break;
        case RIGHT:
            if ((state + 1) % Util.numCols == 0) {
                nextState = state;
            } else {
                nextState = state + 1;
            }
            break;
        default:
            nextState = -1;
            System.err.println("Invalid Action");
        }
        
        int row = nextState / Util.numCols;
        int col = nextState % Util.numCols;
        float reward = gridworld[row][col];
        if (reward == 0.0) {
            reward = -0.1F;
        }
        boolean terminal = reward == 1 || reward == -1;
        return new Result(reward, nextState, terminal);
    }
    
    public int getStartState() {
        return 0;
    }
    
    public void reset() {
    }
}