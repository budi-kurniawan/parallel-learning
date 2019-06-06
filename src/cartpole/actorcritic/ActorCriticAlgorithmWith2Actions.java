package cartpole.actorcritic;

/**
 * Translation from the C language of Sutton's actor-critic algorithm:
 * http://incompleteideas.net/sutton/book/code/pole.c
 * Paper: http://www.derongliu.org/adp/adp-cdrom/Barto1983.pdf
 */
import java.util.concurrent.ThreadLocalRandom;

import cartpole.Cartpole;

public class ActorCriticAlgorithmWith2Actions {
    private static final int NUM_STATES = 162; /* Number of disjoint boxes of state space. */
    private static final int ALPHA = 1000; /* Learning rate for action weights, w. */
    private static final float BETA = 0.5F; /* Learning rate for critic weights, v. */
    private static final float GAMMA = 0.95F; /* Discount factor for critic. */
    private static final float LAMBDAw = 0.9F; /* Decay rate for w eligibility trace. */
    private static final float LAMBDAv = 0.8F; /* Decay rate for v eligibility trace. */
    private static final int MAX_FAILURES = 100; /* Termination criterion. */
    private static final int MAX_STEPS = 100_000;

    private static int numSuccesses = 0;
    
    public void learn() {
        Cartpole cartpole = new Cartpole();
        float[] w = new float[NUM_STATES]; /* vector of action weights */
        float[] v = new float[NUM_STATES]; /* vector of critic weights */
        float[] e = new float[NUM_STATES]; /* vector of action weight eligibilities */
        float[] xbar = new float[NUM_STATES]; /* vector of critic weight eligibilities */

        float  r;
        int steps = 0, failures = 0;
        boolean failed;
        /*--- Find box in state space containing start state ---*/
        int box = cartpole.getState();

        /*--- Iterate through the action-learn loop. ---*/
        while (steps++ < MAX_STEPS && failures < MAX_FAILURES) {
            /*--- Choose action randomly, biased by current weight. ---*/
            int y = (ThreadLocalRandom.current().nextFloat() < probPushRight(w[box])) ? 1 : 0;

            /*--- Update traces. ---*/
            e[box] += (1.0 - LAMBDAw) * (y - 0.5);
            xbar[box] += (1.0 - LAMBDAv);

            /*--- Remember prediction of failure for current state ---*/
            float oldP = v[box];

            /*--- Apply action to the simulated cart-pole ---*/
            cartpole.applyAction(y);

            /*--- Get box of state space containing the resulting state. ---*/
            box = cartpole.getState();
            float p;
            if (box < 0) { // failure
                failed = true;
                failures++;
                //System.out.println("Trial " + failures + " was " + steps + " steps.");
                steps = 0;
                /*--- Reset state to (0 0 0 0).  Find the box. ---*/
                cartpole.reset();
                box = cartpole.getState();
                /*--- Reinforcement upon failure is -1. Prediction of failure is 0. ---*/
                r = -1.0F;
                p = 0.0F;
            } else { // not a failure
                failed = false;
                /*--- Reinforcement is 0. Prediction of failure given by v weight. ---*/
                r = 0;
                p = v[box];
            }

            // Heuristic reinforcement = current reinforcement + gamma * new failure prediction - previous failure prediction
            float rHat = r + GAMMA * p - oldP;

            for (int i = 0; i < NUM_STATES; i++) {
                /*--- Update all weights. ---*/
                w[i] += ALPHA * rHat * e[i];
                v[i] += BETA * rHat * xbar[i];
                if (failed) {
                    /*--- If failure, zero all traces. ---*/
                    e[i] = 0.0F;
                    xbar[i] = 0.0F;
                } else {
                    /*--- Otherwise, update (decay) the traces. ---*/
                    e[i] *= LAMBDAw;
                    xbar[i] *= LAMBDAv;
                }
            }
        }
        if (failures == MAX_FAILURES) {
            System.out.println("Pole not balanced. Stopping after " + failures + " failures.");
        } else {
            numSuccesses++;
            System.out.println("Pole balanced successfully for at least " + steps + " steps");
        }
    }

    public double probPushRight(float s) {
        return (1.0 / (1.0 + Math.exp(-Math.max(-50.0, Math.min(s, 50.0)))));
    }

    public static void main(String[] args) {
        for (int i = 0; i < 20; i++) {
            ActorCriticAlgorithmWith2Actions algorithm = new ActorCriticAlgorithmWith2Actions();
            algorithm.learn();
        }
        System.out.println("num successes = " + numSuccesses);
    }
}