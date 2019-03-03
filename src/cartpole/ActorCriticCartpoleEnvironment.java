package cartpole;

import common.Result;

public class ActorCriticCartpoleEnvironment extends AbstractCartpoleEnvironment {

    @Override
    public Result submit(int state, int action) {
        cartpole.applyAction(action);
        box = cartpole.getBox();
        boolean terminal = box < 0;
        float reward = terminal? -1F : 0;
        if (terminal) {
            cartpole.reset();
            box = cartpole.getBox();
        }
        int nextState = box;
        return new Result(reward, nextState, terminal);
    }
}