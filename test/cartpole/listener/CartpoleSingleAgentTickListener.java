package cartpole.listener;

import common.CommonUtil;
import common.event.TickEvent;
import common.listener.TickListener;

public class CartpoleSingleAgentTickListener implements TickListener {
    volatile boolean policyFound = false;
    private long totalProcessTime = 0L;
    
    @Override
    public void afterTick(TickEvent event) {
        long start = System.nanoTime();
        if (event.getTick() == CommonUtil.MAX_TICKS) {
            // policy found
            policyFound = true;
            StringBuilder sb = new StringBuilder(1000);
            sb.append("Goal reached by single agent at episode " + event.getEpisode() + "\n");
            System.out.println(sb.toString());
            Thread.currentThread().interrupt();
        }
        long end = System.nanoTime();
        totalProcessTime += end - start;
    }

    public long getTotalProcessTime() {
        return totalProcessTime;
    }
}