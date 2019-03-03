package cartpole.listener;

import common.CommonUtil;
import common.agent.QLearningAgent;
import common.event.TickEvent;
import common.listener.TickListener;

public class CartpoleParallelAgentsTickListener implements TickListener {
    private volatile boolean policyFound = false;
    private int trialNumber;
    
    public CartpoleParallelAgentsTickListener(int trialNumber) {
        this.trialNumber = trialNumber;
    }

    @Override
    public void afterTick(TickEvent event) {
        if (policyFound || event.getEpisode() == CommonUtil.numEpisodes) {
            return;
        } else if (event.getTick() == CommonUtil.MAX_TICKS) {
            // policy found
            policyFound = true;
            ((QLearningAgent) event.getSource()).reachedGoal = true;
            Thread.currentThread().interrupt();
        }
    }
}