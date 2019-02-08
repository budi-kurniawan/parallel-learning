package common.event;

import java.util.EventObject;
import java.util.List;

import common.QLearningAgent;
import common.QEntry;

public class EpisodeEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private int episode;
    private float epsilon;
    private QEntry[][] q; // for single agent
    private List<QEntry[][]> qTables; // for multiple agents
    private QLearningAgent agent;
    
    public EpisodeEvent(QLearningAgent source, int episode, float epsilon, QEntry[][] q) {
        super(source);
        this.agent = source;
        this.episode = episode;
        this.epsilon = epsilon;
        this.q = q;
    }

    public EpisodeEvent(QLearningAgent source, int episode, float epsilon, List<QEntry[][]> qTables) {
        super(source);
        this.agent = source;
        this.episode = episode;
        this.epsilon = epsilon;
        this.qTables = qTables;
    }
    
    public int getEpisode() {
        return episode;
    }
    public float getEpsilon() {
        return epsilon;
    }
    public QEntry[][] getQ() {
        return q;
    }
    public QLearningAgent getAgent() {
        return agent;
    }
    public List<QEntry[][]> getQTables() {
        return qTables;
    }    
}