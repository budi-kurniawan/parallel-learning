package gridworld.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import common.QEntry;
import common.StateAction;
import common.event.EpisodeEvent;
import common.listener.EpisodeListener;
import gridworld.GridworldUtil;

public class SingleAgentEpisodeListener implements EpisodeListener {
    volatile boolean policyFound = false;
    private long totalProcessTime = 0L;
    
    @Override
    public void afterEpisode(EpisodeEvent event) {
        long start = System.nanoTime();
        int episode = event.getEpisode();
        if (episode == GridworldUtil.numEpisodes || policyFound) {
            return;
        }
        QEntry[][] qTable = event.getQ();
        List<StateAction> steps = new ArrayList<>();
        if (GridworldUtil.policyFound(qTable, steps)) {
            // policy found
            policyFound = true;
            StringBuilder sb = new StringBuilder(1000);
            sb.append("Policy found by single agent at episode " + event.getEpisode() + "\n");
//            for (StateAction step : steps) {
//                sb.append("(" + step.state + ", " + step.action + "), ");
//            }
//            sb.append("(" + Util.getGoalState() + ")\n");
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