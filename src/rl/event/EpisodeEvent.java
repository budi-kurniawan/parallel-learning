package rl.event;

import java.util.EventObject;
import java.util.List;

import rl.Agent;
import rl.QEntry;

public class EpisodeEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private int episode;
    private float epsilon;
    private String agentId;
    private QEntry[][] q;
    private QEntry[][] otherQ;
    private List<QEntry[][]> qTables;
    private Agent agent;
    
    public EpisodeEvent(Agent source, int episode, float epsilon, QEntry[][] q) {
        super(source);
        this.agent = agent;
        this.episode = episode;
        this.epsilon = epsilon;
        this.q = q;
    }

    public EpisodeEvent(Agent source, int episode, float epsilon, QEntry[][] q, QEntry[][] otherQ) {
        super(source);
        this.agent = source;
        this.episode = episode;
        this.epsilon = epsilon;
        this.q = q;
        this.otherQ = otherQ;
    }
    public EpisodeEvent(Agent source, int episode, float epsilon, List<QEntry[][]> qTables) {
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
    public QEntry[][] getOtherQ() {
        return otherQ;
    }
    public Agent getAgent() {
        return agent;
    }
}