package cartpole;

import common.Result;

public class QLearningCartpoleEnvironment extends AbstractCartpoleEnvironment {
//    private static final int TERMINAL_STATE = 162;
    //int box = getBox(x, xDot, theta, thetaDot);

    @Override
    public Result submit(int state, int action) {
        updateInternalVariables(action);

        int box = getBox(x, xDot, theta, thetaDot);
        //System.out.println("QLCartpoleEnv. submit() box:" + box + ", action: " + action + ", x:" + x + ", theta:" + theta);
        boolean terminal = box < 0;
        int nextState = terminal? 162 : box;
        int reward = terminal ? -1 : 0;
        return new Result(reward, nextState, terminal);
    }

    @Override
    public void reset() {
        if (CartpoleUtil.randomizeStartingPositions) {
            // generate random between two numbers: double random = min + Math.random() * (max - min);
            // initialise theta to a number between -twelve_degrees to +twelve_degrees
            theta = (float) (-twelve_degrees + Math.random() * (twelve_degrees + twelve_degrees));
            x = xDot = thetaDot = 0.0F;
        } else {
            x = xDot = theta = thetaDot = 0.0F;
        }
    }
}