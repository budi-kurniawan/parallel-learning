package rl.event;

import java.util.EventObject;

import rl.QEntry;

public class EpisodeEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private int episode;
    private float epsilon;
    private QEntry[][] q;
    private QEntry[][] otherQ;
    
    public EpisodeEvent(Object source, int episode, float epsilon, QEntry[][] q) {
        super(source);
        this.episode = episode;
        this.epsilon = epsilon;
        this.q = q;
    }

    public EpisodeEvent(Object source, int episode, float epsilon, QEntry[][] q, QEntry[][] otherQ) {
        super(source);
        this.episode = episode;
        this.epsilon = epsilon;
        this.q = q;
        this.otherQ = otherQ;
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

}
