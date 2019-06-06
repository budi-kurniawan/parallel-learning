package cartpole.actorcritic;

import java.util.stream.IntStream;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import cartpole.Cartpole;
import rl.MathUtil;

/**
 * Extending the original actor-critic with multiple discrete actions.
 * 
 * @author budi kurniawan
 */
public class ActorCriticAlgorithmWithMultiActions {
    private static final int NUM_STATES = 162; /* Number of disjoint boxes of state space. */
    private static final int ALPHA = 1000; /* Learning rate for action weights, w. */
    private static final float BETA = 0.5F; /* Learning rate for critic weights, v. */
    private static final float GAMMA = 0.95F; /* Discount factor for critic. */
    private static final float LAMBDAw = 0.9F; /* Decay rate for w eligibility trace. */
    private static final float LAMBDAv = 0.8F; /* Decay rate for v eligibility trace. */
    private static final int MAX_FAILURES = 100; /* Termination criterion. */
    private static final int MAX_STEPS = 100_000;
    
    private int[] actions = {0, 1, 2, 3};
    private int action;
    private final int NUM_ACTIONS = actions.length;
    private double[][] theta = new double[NUM_STATES][NUM_ACTIONS]; // actor's weights
    private double[] w = new double[NUM_STATES]; // critic's weights
    private double[][] zTheta = new double[NUM_STATES][NUM_ACTIONS]; /* vector of action weight eligibilities */
    private double[] zW = new double[NUM_STATES]; /* vector of critic weight eligibilities */
    
    private static int numSuccesses = 0;

    public void learn() {
        Cartpole cartpole = new Cartpole();

        int steps = 0, failures = 0;
        /*--- Find box in state space containing start state ---*/
        int state = cartpole.getState();

        /*--- Iterate through the action-learn loop. ---*/
        while (steps++ < MAX_STEPS && failures < MAX_FAILURES) {
            // Choose action randomly, biased by current weights.
            action = getAction(state);
            /*--- Update traces. ---*/
            zTheta[state][action] += (1.0 - LAMBDAw) * 0.5;
            zW[state] += (1.0 - LAMBDAv);

            /*--- Remember prediction of failure for current state ---*/
            double oldP = w[state];

            /*--- Apply action to the simulated cart-pole ---*/
            cartpole.applyAction(action);
            state = cartpole.getState();
            double p;
            float  r;
            boolean failed = state < 0;
            if (failed) {
                failures++;
                //System.out.println("Actor critic w. multiple actions. Trial " + failures + " was " + steps + " steps.");
                steps = 0;
                /*--- Reset state to (0 0 0 0).  Find the box. ---*/
                cartpole.reset();
                state = cartpole.getState();
                /*--- Reinforcement upon failure is -1. Prediction of failure is 0. ---*/
                r = -1.0F;
                p = 0.0F;
            } else { // not a failure
                /*--- Reinforcement is 0. Prediction of failure given by w weight. ---*/
                r = 0;
                p = w[state];
            }

            // Heuristic reinforcement = current reinforcement + gamma * new failure prediction - previous failure prediction
            double rHat = r + GAMMA * p - oldP;

            for (int i = 0; i < NUM_STATES; i++) {
                /*--- Update all weights. ---*/
                for (int j = 0; j < NUM_ACTIONS; j++) {
                    theta[i][j] += ALPHA * rHat * zTheta[i][j];
                }
                w[i] += BETA * rHat * zW[i];
                if (failed) {
                    /*--- If failure, zero all traces. ---*/
                    for (int j = 0; j < NUM_ACTIONS; j++) {
                        zTheta[i][j] = 0.0;
                    }
                    zW[i] = 0.0F;
                } else {
                    /*--- Otherwise, update (decay) the traces. ---*/
                    for (int j = 0; j < NUM_ACTIONS; j++) {
                        zTheta[i][j] *= LAMBDAw;
                    }
                    zW[i] *= LAMBDAv;
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

    protected int getAction(int state) {
        double[] pi = MathUtil.softMax(theta[state]);
        EnumeratedIntegerDistribution dist = new EnumeratedIntegerDistribution(actions, pi);
        return dist.sample();
    }
    
    public static void main(String[] args) {
        Cartpole.FORCE_MAG_1 = 12.0F;
        Cartpole.FORCE_MAG_2 = -12.0F;
        Cartpole.FORCE_MAG_3 = 8.0F;
        Cartpole.FORCE_MAG_4 = -8.0F;
        
        for (int i = 0; i < 20; i++) {
            ActorCriticAlgorithmWithMultiActions algorithm = new ActorCriticAlgorithmWithMultiActions();
            algorithm.learn();
        }
        System.out.println("num successes = " + numSuccesses);
    }
}