package cartpole;

import common.Environment;

public abstract class AbstractCartpoleEnvironment implements Environment {
    protected static final int NUM_BOXES = 162; /* Number of disjoint boxes of state space. */
    public Cartpole cartpole = new Cartpole();

    protected int box = cartpole.getState();
    
    @Override
    public int getStartState() {
        return cartpole.getState();
    }
    
    @Override
    public void reset() {
        cartpole.reset();
        box = cartpole.getState();
    }
}