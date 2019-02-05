package common;

import static common.Action.DOWN;
import static common.Action.LEFT;
import static common.Action.RIGHT;
import static common.Action.UP;

import gridworld.GridworldUtil;

public class Environment {

    private int[][] gridworld;
    private int maxState;
    
    //public static int[] wells = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97};
    public static int[] wells = {3, 7, 13, 19, 29, 37, 43, 53, 61, 71, 79, 89};
    //public static int[] wells = {55};

    public Environment() {
        this.gridworld = new int[GridworldUtil.numRows][GridworldUtil.numCols];
        gridworld[GridworldUtil.numRows - 1][GridworldUtil.numCols - 1] = 1; // goal state
        int numBoxes = GridworldUtil.numRows * GridworldUtil.numCols;
        for (int state : wells) {
            if (state + 1 > numBoxes) {
                break;
            }
            int[] rowCol = GridworldUtil.stateToRowColumn(state, GridworldUtil.numCols);
            gridworld[rowCol[0]][rowCol[1]] = -1; // terminal state
        }
        maxState = GridworldUtil.numRows * GridworldUtil.numCols - 1;
    }

    public Result submit(int state, int action) {
        int nextState;
        switch (action) {
        case UP:
            nextState = state + GridworldUtil.numCols;
            if (nextState > maxState) {
                nextState = state;
            }
            break;
        case DOWN:
            nextState = state - GridworldUtil.numCols;
            if (nextState < 0) {
                nextState = state;
            }
            break;
        case LEFT:
            if (state % GridworldUtil.numCols == 0) {
                nextState = state;
            } else {
                nextState = state - 1;
            }
            break;
        case RIGHT:
            if ((state + 1) % GridworldUtil.numCols == 0) {
                nextState = state;
            } else {
                nextState = state + 1;
            }
            break;
        default:
            nextState = -1;
            System.err.println("Invalid Action");
        }
        
        int row = nextState / GridworldUtil.numCols;
        int col = nextState % GridworldUtil.numCols;
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