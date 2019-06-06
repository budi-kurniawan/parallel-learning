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
public class OneStepActorCriticAlgorithm0 {
    private static final int NUM_STATES = 162; /* Number of disjoint boxes of state space. */
    private static final int ALPHA = 1000; /* Learning rate for action weights, w. */
    private static final float BETA = 0.5F; /* Learning rate for critic weights, v. */
    private static final float GAMMA = 0.95F; /* Discount factor for critic. */
    private static final float LAMBDAw = 0.9F; /* Decay rate for w eligibility trace. */
    private static final float LAMBDAv = 0.8F; /* Decay rate for v eligibility trace. */
    private static final int MAX_FAILURES = 100; /* Termination criterion. */
    private static final int MAX_STEPS = 100_000;
    
    private static final float ALPHA_THETA = 0.5F;
    private static final float ALPHA_W = 0.9F;
    
    
    private int[] actions = {0, 1, 2, 3};
    private int action;
    private final int NUM_ACTIONS = actions.length;
    private double[][] theta = new double[NUM_STATES][NUM_ACTIONS]; // actor's weights
    private double[] w = new double[NUM_STATES]; // critic's weights
    private double[][] zTheta = new double[NUM_STATES][NUM_ACTIONS]; /* vector of action weight eligibilities */
    private double[] zW = new double[NUM_STATES]; /* vector of critic weight eligibilities */
    
    private int numSuccesses = 0;

    public void learn() {
        Cartpole cartpole = new Cartpole();

        int steps = 0, failures = 0;
        /*--- Find box in state space containing start state ---*/
        int state = cartpole.getState();

        /*--- Iterate through the action-learn loop. ---*/
        double[][] I = MathUtil.ones(NUM_STATES);
        while (steps++ < MAX_STEPS && failures < MAX_FAILURES) {
            // Choose action randomly, biased by current weights.
            action = getAction(state);
            cartpole.applyAction(action);
            int prevState = state;
            state = cartpole.getState();
            boolean failed = state < 0;
            float r = failed ? -1.0F : 0F;
            double prediction = failed ? 0 : w[state];
            double oldPrediction = w[prevState];
            double delta = r + GAMMA * prediction - oldPrediction;
            
            double alphaW_delta = ALPHA_W * delta;
            // update w
            for (int i = 0; i < NUM_STATES; i++) {
//                w[i] += 
            }
            // update theta
            
            I = MathUtil.multiply(I, GAMMA);
            
            if (failed) {
                failures++;
                System.out.println("Actor critic w. multiple actions. Trial " + failures + " was " + steps + " steps.");
                steps = 0;
                /*--- Reset state to (0 0 0 0).  Find the box. ---*/
                cartpole.reset();
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
        
        OneStepActorCriticAlgorithm0 algorithm = new OneStepActorCriticAlgorithm0();
        for (int i = 0; i < 20; i++) {
            algorithm.learn();
        }
        System.out.println("num successes = " + algorithm.numSuccesses);
    }
}