package cartpole.listener;

import common.CommonUtil;
import common.event.TickEvent;
import common.listener.TickListener;

public class CartpoleParallelAgentsTickListener implements TickListener {
    private volatile boolean policyFound = false;
    private long totalProcessTime = 0L;
    private int trialNumber;
    
    public CartpoleParallelAgentsTickListener(int trialNumber) {
        this.trialNumber = trialNumber;
    }

    @Override
    public void afterTick(TickEvent event) {
        long start = System.nanoTime();
        if (event.getEpisode() == CommonUtil.numEpisodes || policyFound) {
            return;
        }
        if (event.getTick() == CommonUtil.MAX_TICKS) {
            // policy found
            policyFound = true;
            
            StringBuilder sb = new StringBuilder(1000);
            sb.append("Policy found by agent " + event.getSource().getIndex() + " at episode " 
                    + event.getEpisode() + " (trial #" + trialNumber + ")\n");
//            for (StateAction step : steps) {
//                sb.append("(" + step.state + ", " + step.action + "), ");
//            }
//            sb.append("(" + Util.getGoalState() + ")\n");
            CommonUtil.printMessage(sb.toString());
            Thread.currentThread().interrupt();
        }
        long end = System.nanoTime();
        totalProcessTime += end - start;
    }

    public long getTotalProcessTime() {
        return totalProcessTime;
    }
}