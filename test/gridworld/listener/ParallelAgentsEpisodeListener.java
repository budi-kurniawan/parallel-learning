package gridworld.listener;

import java.util.ArrayList;
import java.util.List;

import common.QEntry;
import common.StateAction;
import common.event.EpisodeEvent;
import common.listener.EpisodeListener;
import gridworld.GridworldUtil;

public class ParallelAgentsEpisodeListener implements EpisodeListener {
    private volatile boolean policyFound = false;
    private long totalProcessTime = 0L;
    private int trialNumber;
    
    public ParallelAgentsEpisodeListener(int trialNumber) {
        this.trialNumber = trialNumber;
    }

    @Override
    public void afterEpisode(EpisodeEvent event) {
        long start = System.nanoTime();
        if (event.getEpisode() == GridworldUtil.numEpisodes || policyFound) {
            return;
        }
        int agentIndex = event.getAgent().getIndex();
        QEntry[][] qTable = event.getQ();
        if (qTable == null) {
            qTable = event.getQTables().get(event.getAgent().getIndex());
        }
        List<StateAction> steps = new ArrayList<>();
        if (GridworldUtil.policyFound(qTable, steps)) {
            // policy found
            policyFound = true;
            
            StringBuilder sb = new StringBuilder(1000);
            sb.append("Policy found by agent " + agentIndex + " at episode " + event.getEpisode() + " (trial #" + trialNumber + ")\n");
//            for (StateAction step : steps) {
//                sb.append("(" + step.state + ", " + step.action + "), ");
//            }
//            sb.append("(" + Util.getGoalState() + ")\n");
            GridworldUtil.printMessage(sb.toString());
            Thread.currentThread().interrupt();
        }
        long end = System.nanoTime();
        totalProcessTime += end - start;
    }

    public long getTotalProcessTime() {
        return totalProcessTime;
    }
}