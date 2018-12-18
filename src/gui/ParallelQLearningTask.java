package gui;

import java.util.concurrent.ExecutorService;

import rl.Util;
import rl.listener.EpisodeListener;
import rl.listener.TickListener;
import rl.parallel.ParallelEngine;

public class ParallelQLearningTask implements Runnable {
    private ParallelEngine engine;
    
    public ParallelQLearningTask(ExecutorService executorService) {
        engine = new ParallelEngine(executorService);
    }
    
    public void addTickListenerForAgent1(TickListener... listeners) {
        engine.addTickListenersForAgent1(listeners);
    }
    public void addTickListenerForAgent2(TickListener... listeners) {
        engine.addTickListenersForAgent2(listeners);
    }
    public void addTickListenerForBothAgents(TickListener... listeners) {
        engine.addTickListenersForBothAgents(listeners);
    }

    public void addEpisodeListenerForAgent1(EpisodeListener... listeners) {
        engine.addEpisodeListenersForAgent1(listeners);
    }
    public void addEpisodeListenerForAgent2(EpisodeListener... listeners) {
        engine.addEpisodeListenersForAgent2(listeners);
    }
    public void addEpisodeListenerForBothAgents(EpisodeListener... listeners) {
        engine.addEpisodeListenersForBothAgents(listeners);
    }

    @Override
    public void run() {
        engine.learn(Util.numEpisodes);
    }
}