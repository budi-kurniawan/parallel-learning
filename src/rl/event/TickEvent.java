package rl.event;

import java.util.EventObject;

import rl.Agent;
import rl.Environment;

public class TickEvent extends EventObject {
    private static final long serialVersionUID = -2107107863268570674L;
    private int prevState;
    private int state;
    private Environment environment;
    private int tick;
    private int episode;
    public TickEvent(Agent source) {
        super(source);
    }
    
    public TickEvent(Agent source, Environment environment, int tick, int episode, int prevState, int state) {
        super(source);
        this.environment = environment;
        this.tick = tick;
        this.episode = episode;
        this.prevState = prevState;
        this.state = state;
    }
    
    public Environment getEnvironment() {
        return environment;
    }
    public int getPrevState() {
        return prevState;
    }
    
    public int getState() {
        return state;
    }
    
    public int getTick() {
        return tick;
    }
    
    public int getEpisode() {
        return episode;
    }
}