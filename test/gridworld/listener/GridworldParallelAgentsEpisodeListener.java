package gridworld.listener;

import java.util.ArrayList;
import java.util.List;

import common.CommonUtil;
import common.QEntry;
import common.StateAction;
import common.event.EpisodeEvent;
import common.listener.EpisodeListener;
import gridworld.GridworldUtil;

public class GridworldParallelAgentsEpisodeListener implements EpisodeListener {
    private volatile boolean policyFound = false;
    private long totalProcessTime = 0L;
    private int trialNumber;
    
    public GridworldParallelAgentsEpisodeListener(int trialNumber) {
        this.trialNumber = trialNumber;
    }

    @Override
    public void afterEpisode(EpisodeEvent event) {
        long start = System.nanoTime();
        if (event.getEpisode() == CommonUtil.numEpisodes || policyFound) {
            return;
        }
        int agentIndex = event.getSource().getIndex();
        QEntry[][] qTable = event.getQ();
        if (qTable == null) {
            qTable = event.getQTables().get(event.getSource().getIndex());
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