package cartpole;

import common.Result;

public class QLearningCartpoleEnvironment extends AbstractCartpoleEnvironment {
//    private static final int TERMINAL_STATE = 162;
    int box = getBox(x, xDot, theta, thetaDot);

    @Override
    public Result submit(int state, int action) {
        updateInternalVariables(action);

        box = getBox(x, xDot, theta, thetaDot);
        //System.out.println("QLCartpoleEnv. submit() box:" + box + ", action: " + action + ", x:" + x + ", theta:" + theta);
        boolean terminal = box < 0;
        int nextState = terminal? 162 : box;
        int reward = terminal ? -1 : 0;
        return new Result(reward, nextState, terminal);
    }

    @Override
    public void reset() {
        x = xDot = theta = thetaDot = 0.0F;
        box = getBox(x, xDot, theta, thetaDot);
        //System.out.println("reset. box:" + box);
    }
    
}