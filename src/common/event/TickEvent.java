package common.event;

import java.util.EventObject;

import common.Agent;
import common.Environment;

public class TickEvent extends EventObject {
    private static final long serialVersionUID = -2107107863268570674L;
    private Agent agent;
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
        this.agent = source;
        this.environment = environment;
        this.tick = tick;
        this.episode = episode;
        this.prevState = prevState;
        this.state = state;
    }
    
    public Agent getSource() {
        return agent;
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