package rl.listener;

import rl.event.EpisodeEvent;

public interface EpisodeListener {
    public default void beforeEpisode(EpisodeEvent event) {
    }
    void afterEpisode(EpisodeEvent event);
}
