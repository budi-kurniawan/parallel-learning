package common.event;

import java.util.EventObject;
import java.util.List;

import common.Agent;
import common.QEntry;

public class EpisodeEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private int episode;
    private QEntry[][] q; // for single agent
    private List<QEntry[][]> qTables; // for multiple agents
    private Agent agent;
    
    public EpisodeEvent(Agent source, int episode, QEntry[][] q) {
        super(source);
        this.agent = source;
        this.episode = episode;
        this.q = q;
    }

    public EpisodeEvent(Agent source, int episode, List<QEntry[][]> qTables) {
        super(source);
        this.agent = source;
        this.episode = episode;
        this.qTables = qTables;
    }
    
    public int getEpisode() {
        return episode;
    }
    public QEntry[][] getQ() {
        return q;
    }
    public Agent getSource() {
        return agent;
    }
    public List<QEntry[][]> getQTables() {
        return qTables;
    }    
}