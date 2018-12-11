package rl.listener;

import rl.event.EpisodeEvent;
import rl.event.TickEvent;

public interface LearningListener {
    void afterTick(TickEvent event);
    void beforeEpisode(EpisodeEvent event);
    void afterEpisode(EpisodeEvent event);
}
