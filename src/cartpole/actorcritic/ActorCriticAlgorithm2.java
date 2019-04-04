package cartpole.actorcritic;

/**
 * Translation from the C language of Sutton's actor-critic algorithm:
 * http://incompleteideas.net/sutton/book/code/pole.c
 * Paper: http://www.derongliu.org/adp/adp-cdrom/Barto1983.pdf
 */
import java.util.concurrent.ThreadLocalRandom;

import cartpole.Cartpole;
import rl.MathUtil;

/**
 * Extending the original actor-critic with multiple discrete actions.
 * Note that MAX_FAILURES has been changed to 200, indicating it takes longer than
 * its 2-action cousin to arrive at a policy that works.
 * 
 * @author budi kurniawan
 */
public class ActorCriticAlgorithm2 {
    private static final int NUM_BOXES = 162; /* Number of disjoint boxes of state space. */
    private static final int ALPHA = 1000; /* Learning rate for action weights, w. */
    private static final float BETA = 0.5F; /* Learning rate for critic weights, v. */
    private static final float GAMMA = 0.95F; /* Discount factor for critic. */
    private static final float LAMBDAw = 0.9F; /* Decay rate for w eligibility trace. */
    private static final float LAMBDAv = 0.8F; /* Decay rate for v eligibility trace. */
    private static final int MAX_FAILURES = 200; /* Termination criterion. */
    private static final int MAX_STEPS = 100_000;
    
    private int[] actions = {0, 1, 2, 3};
    private int action;
    private final int NUM_ACTIONS = actions.length;
    double[][] w = new double[NUM_BOXES][NUM_ACTIONS]; /* vector of action weights */
    double[] v = new double[NUM_BOXES]; /* vector of critic weights */
    double[][] e = new double[NUM_BOXES][NUM_ACTIONS]; /* vector of action weight eligibilities */
    double[] xbar = new double[NUM_BOXES]; /* vector of critic weight eligibilities */

    public void learn() {
        Cartpole cartpole = new Cartpole();

        float  r;
        int steps = 0, failures = 0;
        boolean failed;
        /*--- Find box in state space containing start state ---*/
        int box = cartpole.getBox();

        System.out.println("ac 2");
        /*--- Iterate through the action-learn loop. ---*/
        while (steps++ < MAX_STEPS && failures < MAX_FAILURES) {
            /*--- Choose action randomly, biased by current weight. ---*/
            //int y = (ThreadLocalRandom.current().nextFloat() < probPushRight(w[box])) ? 1 : 0;
            action = getAction(box);
            /*--- Update traces. ---*/
            e[box][action] += (1.0 - LAMBDAw) * 0.5;
            xbar[box] += (1.0 - LAMBDAv);

            /*--- Remember prediction of failure for current state ---*/
            double oldP = v[box];

            /*--- Apply action to the simulated cart-pole ---*/
            cartpole.applyAction(action);

            /*--- Get box of state space containing the resulting state. ---*/
            box = cartpole.getBox();
            double p;
            if (box < 0) { // failure
                failed = true;
                failures++;
                System.out.println("Actor critic w. multiple actions. Trial " + failures + " was " + steps + " steps.");
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
            double rHat = r + GAMMA * p - oldP;

            for (int i = 0; i < NUM_BOXES; i++) {
                /*--- Update all weights. ---*/
                for (int j = 0; j < NUM_ACTIONS; j++) {
                    w[i][j] += ALPHA * rHat * e[i][j];
                }

                v[i] += BETA * rHat * xbar[i];
                if (failed) {
                    /*--- If failure, zero all traces. ---*/
                    for (int j = 0; j < NUM_ACTIONS; j++) {
                        e[i][j] = 0.0;
                    }
                    xbar[i] = 0.0F;
                } else {
                    /*--- Otherwise, update (decay) the traces. ---*/
                    for (int j = 0; j < NUM_ACTIONS; j++) {
                        e[i][j] *= LAMBDAw;
                    }
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

//    public double probPushRight(float s) {
//        return (1.0 / (1.0 + Math.exp(-Math.max(-50.0, Math.min(s, 50.0)))));
//    }

    protected int getAction(int state) {
        //double[] pi = getPi();
        double random = ThreadLocalRandom.current().nextDouble();
        //System.out.println("random:" + random);
        double[] pi = MathUtil.softMax(w[state]);
//        int greedyAction = MathUtil.argMax(pi);
//        boolean exploit = random < pi[greedyAction];
//        if (exploit) {
//            return greedyAction;
//        } else {
//            return ThreadLocalRandom.current().nextInt(0, NUM_ACTIONS);
//        }

        double summedProb = pi[0];
        int index = 0;
        while (random > summedProb && index < NUM_ACTIONS - 1) {
            index++;
            summedProb += pi[index];
        }
        return index;
    }

    
    public static void main(String[] args) {
        ActorCriticAlgorithm2 algorithm = new ActorCriticAlgorithm2();
        algorithm.learn();
    }
}