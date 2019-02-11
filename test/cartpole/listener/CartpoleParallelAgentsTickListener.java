package cartpole.listener;

import common.CommonUtil;
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
            Thread.currentThread().interrupt();
        } else if (event.getTick() == CommonUtil.MAX_TICKS) {
            // policy found
            policyFound = true;
            event.getSource().reachedGoal = true;
            Thread.currentThread().interrupt();
        }
    }
}