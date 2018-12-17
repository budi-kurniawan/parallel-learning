package gui;

import java.util.concurrent.ExecutorService;

import rl.Util;
import rl.listener.TickListener;
import rl.parallel.ParallelEngine;

public class ParallelQLearningTask implements Runnable {
    private ParallelEngine engine;
    
    public ParallelQLearningTask(ExecutorService executorService) {
        engine = new ParallelEngine(executorService);
    }
    
    public void addTickListenerForAgent1(TickListener... learningListeners) {
        engine.addLearningListenersForAgent1(learningListeners);
    }
    public void addTickListenerForAgent2(TickListener... learningListeners) {
        engine.addLearningListenersForAgent2(learningListeners);
    }
    public void addTickListenerForBothAgents(TickListener... learningListeners) {
        engine.addLearningListenersForBothAgents(learningListeners);
    }
    
    @Override
    public void run() {
        engine.learn(Util.numEpisodes);
    }
}
