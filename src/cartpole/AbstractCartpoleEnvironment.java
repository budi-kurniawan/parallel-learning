package cartpole;

import common.Environment;

public abstract class AbstractCartpoleEnvironment implements Environment {
    protected static final int NUM_BOXES = 162; /* Number of disjoint boxes of state space. */
    public Cartpole cartpole = new Cartpole();

    /*--- Find box in state space containing start state ---*/
    protected int box = cartpole.getBox(); //getBox(x, xDot, theta, thetaDot);
    
    @Override
    public int getStartState() {
        return cartpole.getBox();//getBox(x, xDot, theta, thetaDot);
    }
    
    @Override
    public void reset() {
        cartpole.reset();
        box = cartpole.getBox();
    }
}