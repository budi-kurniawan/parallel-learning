package gridworld.listener;

import java.util.ArrayList;
import java.util.List;

import common.QEntry;
import common.StateAction;
import common.agent.QLearningAgent;
import common.event.EpisodeEvent;
import common.listener.EpisodeListener;
import gridworld.GridworldUtil;

public class GridworldSingleAgentEpisodeListener implements EpisodeListener {
    
    @Override
    public void afterEpisode(EpisodeEvent event) {
        QLearningAgent agent = (QLearningAgent) event.getSource();
        QEntry[][] qTable = agent.getQ();
        List<StateAction> steps = new ArrayList<>();
        if (GridworldUtil.policyFound(qTable, steps)) {
            // policy found
            agent.reachedGoal = true;
            Thread.currentThread().interrupt();
        }
    }
}