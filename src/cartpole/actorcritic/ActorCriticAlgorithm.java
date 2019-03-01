package cartpole.actorcritic;

/**
 * Translation from the C language of Sutton's actor-critic algorithm:
 * http://incompleteideas.net/sutton/book/code/pole.c
 * Paper: http://www.derongliu.org/adp/adp-cdrom/Barto1983.pdf
 */
import java.util.concurrent.ThreadLocalRandom;

public class ActorCriticAlgorithm {

    /**
     * This file contains a simulation of the cart and pole dynamic system and a
     * procedure for learning to balance the pole. Both are described in Barto,
     * Sutton, and Anderson, "Neuronlike Adaptive Elements That Can Solve Difficult
     * Learning Control Problems," IEEE Trans. Syst., Man, Cybern., Vol. SMC-13, pp.
     * 834--846, Sept.--Oct. 1983, and in Sutton, "Temporal Aspects of Credit
     * Assignment in Reinforcement Learning", PhD Dissertation, Department of
     * Computer and Information Science, University of Massachusetts, Amherst, 1984.
     * The following routines are included:
     * 
     * main: controls simulation interations and implements the learning system.
     * 
     * cart_and_pole: the cart and pole dynamics; given action and current state,
     * estimates next state
     * 
     * get_box: The cart-pole's state space is divided into 162 boxes. get_box
     * returns the index of the box into which the current state appears.
     * 
     * These routines were written by Rich Sutton and Chuck Anderson. Claude Sammut
     * translated parts from Fortran to C. Please address correspondence to
     * sutton@gte.com or anderson@cs.colostate.edu
     * ---------------------------------------
     * ----------------------------------------------------------------------
     */
    private static final int N_BOXES = 162; /* Number of disjoint boxes of state space. */
    private static final int ALPHA = 1000; /* Learning rate for action weights, w. */
    private static final float BETA = 0.5F; /* Learning rate for critic weights, v. */
    private static final float GAMMA = 0.95F; /* Discount factor for critic. */
    private static final float LAMBDAw = 0.9F; /* Decay rate for w eligibility trace. */
    private static final float LAMBDAv = 0.8F; /* Decay rate for v eligibility trace. */
    private static final int MAX_FAILURES = 100; /* Termination criterion. */
    private static final int MAX_STEPS = 100_000;

    public void learn() {
        Cartpole cartpole = new Cartpole();
        float[] w = new float[N_BOXES]; /* vector of action weights */
        float[] v = new float[N_BOXES]; /* vector of critic weights */
        float[] e = new float[N_BOXES]; /* vector of action weight eligibilities */
        float[] xbar = new float[N_BOXES]; /* vector of critic weight eligibilities */

        float p, oldP, r, rHat;
        int steps = 0, failures = 0;
        boolean failed;
        /*--- Find box in state space containing start state ---*/
        int box = cartpole.getBox();

        /*--- Iterate through the action-learn loop. ---*/
        while (steps++ < MAX_STEPS && failures < MAX_FAILURES) {
            /*--- Choose action randomly, biased by current weight. ---*/
            int y = (ThreadLocalRandom.current().nextFloat() < probPushRight(w[box])) ? 1 : 0;

            /*--- Update traces. ---*/
            e[box] += (1.0 - LAMBDAw) * (y - 0.5);
            xbar[box] += (1.0 - LAMBDAv);

            /*--- Remember prediction of failure for current state ---*/
            oldP = v[box];

            /*--- Apply action to the simulated cart-pole ---*/
            cartpole.applyAction(y);

            /*--- Get box of state space containing the resulting state. ---*/
            box = cartpole.getBox();
            if (box < 0) { // failure
                failed = true;
                failures++;
                System.out.println("Trial " + failures + " was " + steps + " steps.");
                steps = 0;
                /*--- Reset state to (0 0 0 0).  Find the box. ---*/
                cartpole.reset();
                box = cartpole.getBox();
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
            rHat = r + GAMMA * p - oldP;

            for (int i = 0; i < N_BOXES; i++) {
                /*--- Update all weights. ---*/
                w[i] += ALPHA * rHat * e[i];
                v[i] += BETA * rHat * xbar[i];
                if (v[i] < -1.0)
                    v[i] = v[i];

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
            System.out.println("Pole balanced successfully for at least " + steps + " steps");
        }
    }

    public double probPushRight(float s) {
        return (1.0 / (1.0 + Math.exp(-Math.max(-50.0, Math.min(s, 50.0)))));
    }

    public static void main(String[] args) {
        ActorCriticAlgorithm algorithm = new ActorCriticAlgorithm();
        algorithm.learn();
    }
}