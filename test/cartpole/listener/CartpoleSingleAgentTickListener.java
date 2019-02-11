package cartpole.listener;

import common.CommonUtil;
import common.event.TickEvent;
import common.listener.TickListener;

public class CartpoleSingleAgentTickListener implements TickListener {
    
    @Override
    public void afterTick(TickEvent event) {
        if (event.getTick() == CommonUtil.MAX_TICKS) {
            event.getSource().reachedGoal = true;
            Thread.currentThread().interrupt();
        }
    }
}