package rl.parallel;

import rl.Agent;
import rl.Engine;
import rl.Environment;
import rl.QEntry;
import rl.event.EpisodeEvent;

public class ParallelEngine extends Engine {
    protected QEntry[][] otherQ;

    public ParallelEngine(QEntry[][] q, QEntry[][] otherQ) {
        super();
        this.q = q;
        this.otherQ = otherQ;
    }

    @Override
    protected Agent createAgent(Environment environment, int episode, int numEpisodes) {
        return new ParallelAgent(environment, stateActions, q, otherQ, episode, numEpisodes);
    }
    
    @Override
    protected void fireAfterEpisodeEvent(Agent agent, int episode) {
        EpisodeEvent event = new EpisodeEvent(agent, episode, agent.getEffectiveEpsilon(), q, otherQ);
        episodeListeners.forEach(listener -> listener.afterEpisode(event));
    }

}
