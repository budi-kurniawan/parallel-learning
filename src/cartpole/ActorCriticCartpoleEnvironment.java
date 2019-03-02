package cartpole;

/**
 * http://incompleteideas.net/sutton/book/code/pole.c
 * Paper: http://www.derongliu.org/adp/adp-cdrom/Barto1983.pdf
 */
import java.util.concurrent.ThreadLocalRandom;

import common.Result;

public class ActorCriticCartpoleEnvironment extends AbstractCartpoleEnvironment {
    private static final float LAMBDAw = 0.9F; /* Decay rate for w eligibility trace. */
    private static final float LAMBDAv = 0.8F; /* Decay rate for v eligibility trace. */
    private static final int ALPHA = 1000; /* Learning rate for action weights, w. */
    private static final float BETA = 0.5F; /* Learning rate for critic weights, v. */
    private static final float GAMMA = 0.95F; /* Discount factor for critic. */

    private float[] w = new float[NUM_BOXES]; /* vector of action weights */
    private float[] v = new float[NUM_BOXES]; /* vector of critic weights */
    private float[] e = new float[NUM_BOXES]; /* vector of action weight eligibilities */
    private float[] xbar = new float[NUM_BOXES]; /* vector of critic weight eligibilities */

    private float p, oldp, r;
    private int i;

    @Override
    public Result submit(int state, int action) {
        /*--- Choose action randomly, biased by current weight. ---*/
        int y = (ThreadLocalRandom.current().nextFloat() < probPushRight(w[box])) ? 1 : 0;

        /*--- Update traces. ---*/
        e[box] += (1.0 - LAMBDAw) * (y - 0.5);
        xbar[box] += (1.0 - LAMBDAv);

        /*--- Remember prediction of failure for current state ---*/
        oldp = v[box];

        cartpole.applyAction(y);
        box = cartpole.getBox();
        boolean terminal = box < 0;
        if (terminal) {
            /*--- Reinforcement upon failure is -1. Prediction of failure is 0. ---*/
            r = -1.0F;
            p = 0.0F;
        } else {
            /*--- Reinforcement is 0. Prediction of failure given by v weight. ---*/
            r = 0;
            p = v[box];
        }

        float rhat = r + GAMMA * p - oldp;

        for (i = 0; i < NUM_BOXES; i++) {
            /*--- Update all weights. ---*/
            w[i] += ALPHA * rhat * e[i];
            v[i] += BETA * rhat * xbar[i];
            if (v[i] < -1.0)
                v[i] = v[i];

            if (terminal) {
                /*--- If failure, zero all traces. ---*/
                e[i] = 0.0F;
                xbar[i] = 0.0F;
            } else {
                /*--- Otherwise, update (decay) the traces. ---*/
                e[i] *= LAMBDAw;
                xbar[i] *= LAMBDAv;
            }
        }

        int nextState = terminal? 162 : box;
        int reward = terminal ? -1 : 0;
        return new Result(reward, nextState, terminal);
    }
    
    private double probPushRight(float s) {
        return (1.0 / (1.0 + Math.exp(-Math.max(-50.0, Math.min(s, 50.0)))));
    }
}