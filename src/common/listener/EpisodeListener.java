package common.listener;

import common.event.EpisodeEvent;

public interface EpisodeListener {
    public default void beforeEpisode(EpisodeEvent event) {
    }
    void afterEpisode(EpisodeEvent event);
}
