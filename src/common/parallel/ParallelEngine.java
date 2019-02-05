package common.parallel;

import java.util.List;

import common.Agent;
import common.AbstractEngine;
import common.QEntry;
import common.event.EpisodeEvent;
import gridworld.GridworldEnvironment;

public abstract class ParallelEngine extends AbstractEngine {
    private int agentIndex;
    protected List<QEntry[][]> qTables;

    public ParallelEngine(int agentIndex, List<QEntry[][]> qTables) {
        super();
        this.agentIndex = agentIndex;
        this.qTables = qTables;
    }

//    @Override
//    protected Agent createAgent(GridworldEnvironment environment, int episode) {
//        return new ParallelAgent(agentIndex, environment, stateActions, qTables, episode);
//    }
    
    @Override
    protected void fireAfterEpisodeEvent(Agent agent, int episode) {
        EpisodeEvent event = new EpisodeEvent(agent, episode, agent.getEffectiveEpsilon(), qTables);
        episodeListeners.forEach(listener -> listener.afterEpisode(event));
    }
}