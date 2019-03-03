package common.event;

import java.util.EventObject;

import common.Agent;

public class EpisodeEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private int episode;
    private Agent agent;
    
    public EpisodeEvent(int episode, Agent source) {
        super(source);
        this.agent = source;
        this.episode = episode;
    }

    public int getEpisode() {
        return episode;
    }
    
    public Agent getSource() {
        return agent;
    }
}