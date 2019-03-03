package common.agent;

import java.util.concurrent.ThreadLocalRandom;

import common.Agent;
import common.Environment;
import common.Result;

public class ActorCriticAgent implements Agent {
    
    private static final int NUM_BOXES = 162; /* Number of disjoint boxes of state space. */
    private static final int ALPHA = 1000; /* Learning rate for action weights, w. */
    private static final float BETA = 0.5F; /* Learning rate for critic weights, v. */
    private static final float GAMMA = 0.95F; /* Discount factor for critic. */
    private static final float LAMBDAw = 0.9F; /* Decay rate for w eligibility trace. */
    private static final float LAMBDAv = 0.8F; /* Decay rate for v eligibility trace. */
    
    private int index;
    private Environment environment;
    private int episode;
    private int state = Integer.MIN_VALUE;
    private int action;
    protected float reward;
    private boolean terminal;
    private float[] w = new float[NUM_BOXES]; /* vector of action weights */
    private float[] v = new float[NUM_BOXES]; /* vector of critic weights */
    private float[] e = new float[NUM_BOXES]; /* vector of action weight eligibilities */
    private float[] xbar = new float[NUM_BOXES]; /* vector of critic weight eligibilities */

    private float oldp;
    
    public ActorCriticAgent(Environment environment, int episode) {
        this.environment = environment;
        this.episode = episode;
    }
    
    public ActorCriticAgent(int index, Environment environment, int episode) {
        this(environment, episode);
        this.index = index;
    }
    
    @Override
    public void tick() {
        learn();
    }
    
    @Override
    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public int getState() {
        return state;
    }
    
    @Override
    public int getIndex() {
        return index;
    }
    
    protected void learn() {
        if (state == Integer.MIN_VALUE) {
            state = environment.getStartState();
        } else {
            action = (ThreadLocalRandom.current().nextFloat() < probPushRight(w[state])) ? 1 : 0;
            e[state] += (1.0 - LAMBDAw) * (action - 0.5);
            xbar[state] += (1.0 - LAMBDAv);

            /*--- Remember prediction of failure for current state ---*/
            oldp = v[state];

            Result result = environment.submit(state, action);
            this.reward = result.reward;
            this.terminal = result.terminal;
            this.state = result.nextState;
            float p = terminal? 0.0F : v[state];
            float rhat = reward + GAMMA * p - oldp;
            for (int i = 0; i < NUM_BOXES; i++) {
                /*--- Update all weights. ---*/
                w[i] += ALPHA * rhat * e[i];
                v[i] += BETA * rhat * xbar[i];
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
        }
    }
    
    private double probPushRight(float s) {
        return (1.0 / (1.0 + Math.exp(-Math.max(-50.0, Math.min(s, 50.0)))));
    }
}