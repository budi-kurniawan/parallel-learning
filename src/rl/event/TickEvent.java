package rl.event;

import java.util.EventObject;

import rl.Agent;

public class TickEvent extends EventObject {
    private static final long serialVersionUID = -2107107863268570674L;
    private int prevState;
    private int state;
    public TickEvent(Agent source, int prevState, int state) {
        super(source);
        this.prevState = prevState;
        this.state = state;
    }
    
    public int getPrevState() {
        return prevState;
    }
    
    public int getState() {
        return state;
    }

}
