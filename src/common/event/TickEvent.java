package common.event;

import java.util.EventObject;

import common.Environment;
import common.QEntry;
import common.QLearningAgent;

public class TickEvent extends EventObject {
    private static final long serialVersionUID = -2107107863268570674L;
    private QLearningAgent agent;
    private int prevState;
    private int state;
    private Environment environment;
    private QEntry[][] q;
    private int tick;
    private int episode;
    public TickEvent(QLearningAgent source) {
        super(source);
    }
    
    public TickEvent(QLearningAgent source, Environment environment, QEntry[][] q, int tick, int episode, int prevState, int state) {
        super(source);
        this.agent = source;
        this.environment = environment;
        this.q = q;
        this.tick = tick;
        this.episode = episode;
        this.prevState = prevState;
        this.state = state;
    }
    
    public QLearningAgent getSource() {
        return agent;
    }
    public Environment getEnvironment() {
        return environment;
    }
    public QEntry[][] getQ() {
        return q;
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