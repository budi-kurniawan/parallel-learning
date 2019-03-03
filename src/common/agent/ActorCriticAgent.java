package common.agent;

import common.Agent;

public class ActorCriticAgent implements Agent {
    
    private int index;
    
    @Override
    public void tick() {
        
    }
    
    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public int getState() {
        return 0;
    }
    
    @Override
    public int getIndex() {
        return index;
    }
}
