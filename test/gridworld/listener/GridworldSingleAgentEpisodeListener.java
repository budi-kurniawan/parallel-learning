package gridworld.listener;

import java.util.ArrayList;
import java.util.List;

import common.CommonUtil;
import common.QEntry;
import common.StateAction;
import common.event.EpisodeEvent;
import common.listener.EpisodeListener;
import gridworld.GridworldUtil;

public class GridworldSingleAgentEpisodeListener implements EpisodeListener {
    volatile boolean policyFound = false;
    private long totalProcessTime = 0L;
    
    @Override
    public void afterEpisode(EpisodeEvent event) {
        long start = System.nanoTime();
        int episode = event.getEpisode();
        if (episode == CommonUtil.numEpisodes || policyFound) {
            return;
        }
        QEntry[][] qTable = event.getQ();
        List<StateAction> steps = new ArrayList<>();
        if (GridworldUtil.policyFound(qTable, steps)) {
            // policy found
            policyFound = true;
            Thread.currentThread().interrupt();
        }
        long end = System.nanoTime();
        totalProcessTime += end - start;
    }

    public long getTotalProcessTime() {
        return totalProcessTime;
    }
}