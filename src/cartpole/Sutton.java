package cartpole;

import java.util.concurrent.ThreadLocalRandom;

class Params {
    public float x;
    public float x_dot;
    public float theta;
    public float theta_dot;
}

public class Sutton {

    /**
     * /*---------------------------------------------------------------------- This
     * file contains a simulation of the cart and pole dynamic system and a
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
    /**
     * #include <math.h>
     * 
     * #define min(x, y) ((x <= y) ? x : y) #define max(x, y) ((x >= y) ? x : y)
     * #define prob_push_right(s) (1.0 / (1.0 + exp(-max(-50.0, min(s, 50.0)))))
     * #define random ((float) rand() / (float)((1 << 31) - 1))
     */
    private static final int N_BOXES = 162; /* Number of disjoint boxes of state space. */
    private static final int ALPHA = 1000; /* Learning rate for action weights, w. */
    private static final float BETA = 0.5F; /* Learning rate for critic weights, v. */
    private static final float GAMMA = 0.95F; /* Discount factor for critic. */
    private static final float LAMBDAw = 0.9F; /* Decay rate for w eligibility trace. */
    private static final float LAMBDAv = 0.8F; /* Decay rate for v eligibility trace. */
    private static final int MAX_FAILURES = 100; /* Termination criterion. */
    private static final int MAX_STEPS = 100_000;

    private static final float GRAVITY = 9.8F;
    private static final float MASSCART = 1.0F;
    private static final float MASSPOLE = 0.1F;
    private static final float TOTAL_MASS = (MASSPOLE + MASSCART);
    private static final float LENGTH = 0.5F; /* actually half the pole's length */
    private static final float POLEMASS_LENGTH = (MASSPOLE * LENGTH);
    private static final float FORCE_MAG = 10.0F;
    private static final float TAU = 0.02F; /* seconds between state updates */
    private static final float FOURTHIRDS = 1.3333333333333F;
    private static final float one_degree = 0.0174532F; /* 2pi/360 */
    private static final float six_degrees = 0.1047192F;
    private static final float twelve_degrees = 0.2094384F;
    private static final float fifty_degrees = 0.87266F;

    public static void main(String[] args) {
        // float x, /* cart position, meters */
        // x_dot, /* cart velocity */
        // theta, /* pole angle, radians */
        // theta_dot; /* pole angular velocity */
        Params params = new Params();
        // typedef float vector[N_BOXES];
        float[] w = new float[N_BOXES]; /* vector of action weights */
        float[] v = new float[N_BOXES]; /* vector of critic weights */
        float[] e = new float[N_BOXES]; /* vector of action weight eligibilities */
        float[] xbar = new float[N_BOXES]; /* vector of critic weight eligibilities */

        float p, oldp, rhat, r;
        int i, steps = 0, failures = 0, failed;
        /*--- Find box in state space containing start state ---*/
        int box = get_box(params.x, params.x_dot, params.theta, params.theta_dot);

        /*--- Iterate through the action-learn loop. ---*/
        while (steps++ < MAX_STEPS && failures < MAX_FAILURES) {
            /*--- Choose action randomly, biased by current weight. ---*/
            int y = (ThreadLocalRandom.current().nextFloat() < prob_push_right(w[box])) ? 1 : 0;

            /*--- Update traces. ---*/
            e[box] += (1.0 - LAMBDAw) * (y - 0.5);
            xbar[box] += (1.0 - LAMBDAv);

            /*--- Remember prediction of failure for current state ---*/
            oldp = v[box];

            /*--- Apply action to the simulated cart-pole ---*/
            cart_pole(y, params);

            /*--- Get box of state space containing the resulting state. ---*/
            box = get_box(params.x, params.x_dot, params.theta, params.theta_dot);
            if (box < 0) {
                /*--- Failure occurred. ---*/
                failed = 1;
                failures++;
                System.out.println("Trial " + failures + " was " + steps + " steps.");
                steps = 0;

                /*--- Reset state to (0 0 0 0).  Find the box. ---*/
                params.x = params.x_dot = params.theta = params.theta_dot = 0.0F;
                box = get_box(params.x, params.x_dot, params.theta, params.theta_dot);

                /*--- Reinforcement upon failure is -1. Prediction of failure is 0. ---*/
                r = -1.0F;
                p = 0.0F;
            } else {
                /*--- Not a failure. ---*/
                failed = 0;
                /*--- Reinforcement is 0. Prediction of failure given by v weight. ---*/
                r = 0;
                p = v[box];
            }

            /*--- Heuristic reinforcement is:   current reinforcement
                + gamma * new failure prediction - previous failure prediction ---*/
            rhat = r + GAMMA * p - oldp;

            for (i = 0; i < N_BOXES; i++) {
                /*--- Update all weights. ---*/
                w[i] += ALPHA * rhat * e[i];
                v[i] += BETA * rhat * xbar[i];
                if (v[i] < -1.0)
                    v[i] = v[i];

                if (failed==1) {
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
        if (failures == MAX_FAILURES)
            System.out.println("Pole not balanced. Stopping after " + failures + " failures.");
        else
            System.out.println("Pole balanced successfully for at least " + steps + " steps");
    }

    /*----------------------------------------------------------------------
       cart_pole:  Takes an action (0 or 1) and the current values of the
     four state variables and updates their values by estimating the state
     TAU seconds later.
    ----------------------------------------------------------------------*/

    /*** Parameters for simulation ***/
    public static void cart_pole(int action, Params params) {
        float force = (action > 0) ? FORCE_MAG : -FORCE_MAG;
        float costheta = (float) Math.cos(params.theta);
        float sintheta = (float) Math.sin(params.theta);
        float temp = (force + POLEMASS_LENGTH * params.theta_dot * params.theta_dot * sintheta) / TOTAL_MASS;
        float thetaacc = (GRAVITY * sintheta - costheta * temp)
                / (LENGTH * (FOURTHIRDS - MASSPOLE * costheta * costheta / TOTAL_MASS));
        float xacc = temp - POLEMASS_LENGTH * thetaacc * costheta / TOTAL_MASS;

        /*** Update the four state variables, using Euler's method. ***/
        params.x += TAU * params.x_dot;
        params.x_dot += TAU * xacc;
        params.theta += TAU * params.theta_dot;
        params.theta_dot += TAU * thetaacc;
    }

    /*----------------------------------------------------------------------
       get_box:  Given the current state, returns a number from 1 to 162
      designating the region of the state space encompassing the current state.
      Returns a value of -1 if a failure state is encountered.
    ----------------------------------------------------------------------*/
    public static int get_box(float x, float x_dot, float theta, float theta_dot) {
        int box = 0;

        if (x < -2.4 || x > 2.4 || theta < -twelve_degrees || theta > twelve_degrees)
            return (-1); /* to signal failure */

        if (x < -0.8)
            box = 0;
        else if (x < 0.8)
            box = 1;
        else
            box = 2;

        if (x_dot < -0.5)
            ;
        else if (x_dot < 0.5)
            box += 3;
        else
            box += 6;

        if (theta < -six_degrees)
            ;
        else if (theta < -one_degree)
            box += 9;
        else if (theta < 0)
            box += 18;
        else if (theta < one_degree)
            box += 27;
        else if (theta < six_degrees)
            box += 36;
        else
            box += 45;

        if (theta_dot < -fifty_degrees)
            ;
        else if (theta_dot < fifty_degrees)
            box += 54;
        else
            box += 108;
        return (box);
    }

    public static double prob_push_right(float s) {
        return (1.0 / (1.0 + Math.exp(-Math.max(-50.0, Math.min(s, 50.0)))));
    }
}