package rl.listener;

import rl.event.EpisodeEvent;
import rl.event.TickEvent;

public interface EpisodeListener {
    void beforeEpisode(EpisodeEvent event);
    void afterEpisode(EpisodeEvent event);
}
