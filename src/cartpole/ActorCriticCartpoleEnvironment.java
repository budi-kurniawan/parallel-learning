package cartpole;

import java.util.concurrent.ThreadLocalRandom;

import common.Environment;
import common.Result;

public class ActorCriticCartpoleEnvironment implements Environment {
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

    public float x;
    public float x_dot;
    public float theta;
    public float theta_dot;

    // typedef float vector[N_BOXES];
    float[] w = new float[N_BOXES]; /* vector of action weights */
    float[] v = new float[N_BOXES]; /* vector of critic weights */
    float[] e = new float[N_BOXES]; /* vector of action weight eligibilities */
    float[] xbar = new float[N_BOXES]; /* vector of critic weight eligibilities */

    float p, oldp, rhat, r;
    int i, failures = 0, failed;
    /*--- Find box in state space containing start state ---*/
    int box = Sutton.get_box(x, x_dot, theta, theta_dot);

    @Override
    public Result submit(int state, int action) {
        /*--- Choose action randomly, biased by current weight. ---*/
        //System.out.println("sutton submit");
        int y = (ThreadLocalRandom.current().nextFloat() < prob_push_right(w[box])) ? 1 : 0;

        /*--- Update traces. ---*/
        e[box] += (1.0 - LAMBDAw) * (y - 0.5);
        xbar[box] += (1.0 - LAMBDAv);

        /*--- Remember prediction of failure for current state ---*/
        oldp = v[box];

        /*--- Apply action to the simulated cart-pole ---*/
        cart_pole(y);

        /*--- Get box of state space containing the resulting state. ---*/
        box = Sutton.get_box(x, x_dot, theta, theta_dot);
        boolean terminal;
        if (box < 0) {
            /*--- Failure occurred. ---*/
            failed = 1;
            failures++;
            /*--- Reset state to (0 0 0 0).  Find the box. ---*/
            //x = x_dot = theta = theta_dot = 0.0F;
            //box = Sutton.get_box(x, x_dot, theta, theta_dot);

            /*--- Reinforcement upon failure is -1. Prediction of failure is 0. ---*/
            r = -1.0F;
            p = 0.0F;
            terminal = true;
        } else {
            terminal = false;
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

        int nextState = terminal? 162: box;//Sutton.get_box(x, x_dot, theta, theta_dot);
        int reward = terminal ? -1 : 0;
        return new Result(reward, nextState, terminal);
    }

    @Override
    public int getStartState() {
        return Sutton.get_box(x, x_dot, theta, theta_dot);
    }
    
    @Override
    public void reset() {
        x = x_dot = theta = theta_dot = 0.0F;
        box = 85;
    }
    
    public void cart_pole(int action) {
        float force = (action > 0) ? FORCE_MAG : -FORCE_MAG;
        float costheta = (float) Math.cos(theta);
        float sintheta = (float) Math.sin(theta);
        float temp = (force + POLEMASS_LENGTH * theta_dot * theta_dot * sintheta) / TOTAL_MASS;
        float thetaacc = (GRAVITY * sintheta - costheta * temp)
                / (LENGTH * (FOURTHIRDS - MASSPOLE * costheta * costheta / TOTAL_MASS));
        float xacc = temp - POLEMASS_LENGTH * thetaacc * costheta / TOTAL_MASS;

        /*** Update the four state variables, using Euler's method. ***/
        x += TAU * x_dot;
        x_dot += TAU * xacc;
        theta += TAU * theta_dot;
        theta_dot += TAU * thetaacc;
    }
    
    public double prob_push_right(float s) {
        return (1.0 / (1.0 + Math.exp(-Math.max(-50.0, Math.min(s, 50.0)))));
    }
}