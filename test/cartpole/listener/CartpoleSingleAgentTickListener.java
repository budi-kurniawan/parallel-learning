package cartpole.listener;

import common.CommonUtil;
import common.agent.QLearningAgent;
import common.event.TickEvent;
import common.listener.TickListener;

public class CartpoleSingleAgentTickListener implements TickListener {
    
    @Override
    public void afterTick(TickEvent event) {
        if (event.getTick() == CommonUtil.MAX_TICKS) {
            ((QLearningAgent) event.getSource()).reachedGoal = true;
            Thread.currentThread().interrupt();
        }
    }
}