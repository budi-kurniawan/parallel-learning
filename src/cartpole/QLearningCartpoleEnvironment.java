package cartpole;

import common.Result;

public class QLearningCartpoleEnvironment extends AbstractCartpoleEnvironment {

    @Override
    public Result submit(int state, int action) {
        cartpole.applyAction(action);
        box = cartpole.getBox();//getBox(x, xDot, theta, thetaDot);
        //System.out.println("QLCartpoleEnv. submit() box:" + box + ", action: " + action + ", x:" + x + ", theta:" + theta);
        boolean terminal = box < 0;
        int nextState = terminal? 162 : box;
        int reward = terminal ? -1 : 0;
        return new Result(reward, nextState, terminal);
    }
}