package q.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import rl.QEntry;
import rl.StateAction;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.listener.EpisodeListener;

public class SingleAgentEpisodeListener implements EpisodeListener {
    volatile boolean policyFound = false;
    private long totalProcessTime = 0L;
    
    public SingleAgentEpisodeListener() {
    }

    @Override
    public void beforeEpisode(EpisodeEvent event) {
    }
    
    @Override
    public void afterEpisode(EpisodeEvent event) {
        long start = System.nanoTime();
        int episode = event.getEpisode();
        if (episode == Util.numEpisodes) {
            //System.out.println("max episode reached by single agent ");
            return;
        }
        if (policyFound) {
            return;
        }
//        if (episode != Util.numEpisodes - 1) {
//            return;
//        }
        QEntry[][] qTable = event.getQ();
        List<StateAction> steps = new ArrayList<>();
        if (Util.policyFound(qTable, steps)) {
            // policy found
            policyFound = true;
//            for (Future<?> future : futures) {
//                future.cancel(true);
//            }
            StringBuilder sb = new StringBuilder(1000);
            sb.append("Policy found by single agent at episode " + event.getEpisode() + "\n");
//            for (StateAction step : steps) {
//                sb.append("(" + step.state + ", " + step.action + "), ");
//            }
//            sb.append("(" + Util.getGoalState() + ")\n");
            System.out.println(sb.toString());
            Thread.currentThread().interrupt();
//            latch.countDown();
        }
        long end = System.nanoTime();
        totalProcessTime += end - start;
    }

    public long getTotalProcessTime() {
        return totalProcessTime;
    }
}
