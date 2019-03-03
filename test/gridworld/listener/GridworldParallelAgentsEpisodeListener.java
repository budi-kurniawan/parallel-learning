package gridworld.listener;

import java.util.ArrayList;
import java.util.List;

import common.CommonUtil;
import common.QEntry;
import common.StateAction;
import common.agent.QLearningAgent;
import common.event.EpisodeEvent;
import common.listener.EpisodeListener;
import gridworld.GridworldUtil;

public class GridworldParallelAgentsEpisodeListener implements EpisodeListener {
    private volatile boolean policyFound = false;
    private int trialNumber;
    
    public GridworldParallelAgentsEpisodeListener(int trialNumber) {
        this.trialNumber = trialNumber;
    }

    @Override
    public void afterEpisode(EpisodeEvent event) {
        if (policyFound || event.getEpisode() == CommonUtil.numEpisodes) {
            return;
        }
        QLearningAgent agent = (QLearningAgent) event.getSource();
        QEntry[][] qTable = event.getQ();
        if (qTable == null) {
            qTable = event.getQTables().get(agent.getIndex());
        }
        List<StateAction> steps = new ArrayList<>();
        if (GridworldUtil.policyFound(qTable, steps)) {
            // policy found
            policyFound = true;
            agent.reachedGoal = true;
            Thread.currentThread().interrupt();
        }
    }
}