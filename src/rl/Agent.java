package rl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Agent {
    private int state = Integer.MIN_VALUE;
    private float reward;
    public boolean terminal;
    private QEntry[][] q;
    
    protected Environment environment;
    protected int[][] stateActions;
    protected static final float ALPHA = 0.7F;
    protected static final float GAMMA = 1;
    public static final float EPSILON = 0.3F;
    
    protected float effectiveEpsilon;
    
    public Agent(Environment environment, int[][] stateActions, QEntry[][] q, int episode, int numEpisodes) {
        this.environment = environment;
        this.stateActions = stateActions;
        this.q = q;
        if (numEpisodes == 1) {
            effectiveEpsilon = EPSILON;
        } else {
            effectiveEpsilon = (numEpisodes - episode) * EPSILON / (numEpisodes - 1);
        }
    }
    
    public void tick() {
        learn();
    }
    
    protected void learn() {
        if (state == Integer.MIN_VALUE) {
            state = 0;
        } else {
            int prevState = state;
            int action = getExploreExploitAction(prevState); 
            Result result = environment.submit(prevState, action);
            this.state = result.nextState;
            this.reward = result.reward;
            this.terminal = result.terminal;
            double oldValue = getQValue(prevState, action);
            double newValue = (1 - ALPHA) * oldValue + ALPHA * (reward + GAMMA * getMaxQ(state));
            try {
                updateQValue(prevState, action, newValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void updateQValue(int state, int action, double value) throws Exception {
        if (q[state][action].value == -Double.MAX_VALUE) {
            throw new Exception("updating q, state: " + state + ", action:" + action + ", value:" + value);
        }
        q[state][action].value = value;
    }
    
    protected int getRandomAction(int state) {
        // returns 0, 1, ... (n-1) randomly, where n is the number of elements in Action
        int index = ThreadLocalRandom.current().nextInt(0, stateActions[state].length);
        return stateActions[state][index];
    }
    
    public int getExploreExploitAction(int state) {
        // nextFloat returns a float x where 0 <= x < 1
        float random = ThreadLocalRandom.current().nextFloat(); 
        if (random < effectiveEpsilon) {
            // explore, return a random action
            return getRandomAction(state);
        } else {
            return getExploitAction(state);
        }
    }
    
    public int getExploitAction(int state) {
        // exploit, return an action with the highest value
        // if there is more than one action with the same highest value, randomly select from those actions
        double maxValue = -Double.MAX_VALUE; // Double.MIN_VALUE is positive;
        double[] actionValues = new double[stateActions[state].length];
        for (int i = 0; i < stateActions[state].length; i++) {
            int action = stateActions[state][i];
            double value = getQValue(state, action);
            actionValues[i] = value;
            if (value > maxValue) {
                maxValue = value;
            }
        }
        // find out how many actions have the same value;
        List<Integer> topActionIndexes = new ArrayList<>();
        for (int i = 0; i < stateActions[state].length; i++) {
            if (maxValue == actionValues[i]) {
                topActionIndexes.add(i);
            }
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(0, topActionIndexes.size());
        int randomActionWithHighestValue = stateActions[state][topActionIndexes.get(randomIndex)];
        return randomActionWithHighestValue;
    }
    
    protected double getQValue(int state, int action) {
        return q[state][action].value;
    }
    
    public double getMaxQ(int state) {
        double maxValue = 0;
        for (int i = 0; i < stateActions[state].length; i++) {
            int action = stateActions[state][i];
            double value = getQValue(state, action);
            if (value > maxValue) {
                maxValue = value;
            }
        }
        return maxValue;
    }
    
    public float getEffectiveEpsilon() {
        return effectiveEpsilon;
    }
    
    public int getState() {
        return state;
    }    
}