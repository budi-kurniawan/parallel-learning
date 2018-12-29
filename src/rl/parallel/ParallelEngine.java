package rl.parallel;

import java.util.List;

import rl.Agent;
import rl.Engine;
import rl.Environment;
import rl.QEntry;
import rl.event.EpisodeEvent;

public class ParallelEngine extends Engine {
    private int agentIndex;
    protected List<QEntry[][]> qTables;

    public ParallelEngine(int agentIndex, List<QEntry[][]> qTables) {
        super();
        this.agentIndex = agentIndex;
        this.qTables = qTables;
    }

    @Override
    protected Agent createAgent(Environment environment, int episode, int numEpisodes) {
        return new ParallelAgent(agentIndex, environment, stateActions, qTables, episode, numEpisodes);
    }
    
    @Override
    protected void fireAfterEpisodeEvent(Agent agent, int episode) {
        EpisodeEvent event = new EpisodeEvent(agent, episode, agent.getEffectiveEpsilon(), qTables);
        episodeListeners.forEach(listener -> listener.afterEpisode(event));
    }
}