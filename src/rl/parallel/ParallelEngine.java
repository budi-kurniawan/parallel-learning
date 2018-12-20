package rl.parallel;

import rl.Agent;
import rl.Engine;
import rl.Environment;
import rl.QEntry;
import rl.event.EpisodeEvent;

public class ParallelEngine extends Engine {
    protected QEntry[][] otherQ;
    private int agentId;

    public ParallelEngine(int agentId, QEntry[][] q, QEntry[][] otherQ) {
        super();
        this.agentId = agentId;
        this.q = q;
        this.otherQ = otherQ;
    }

    @Override
    protected Agent createAgent(Environment environment, int episode, int numEpisodes) {
        return new ParallelAgent(agentId, environment, stateActions, q, otherQ, episode, numEpisodes);
    }
    
    @Override
    protected void fireAfterEpisodeEvent(Agent agent, int episode) {
        EpisodeEvent event = new EpisodeEvent(agent, episode, agent.getEffectiveEpsilon(), q, otherQ);
        episodeListeners.forEach(listener -> listener.afterEpisode(event));
    }

}
