package gui;

import java.util.concurrent.ExecutorService;

import rl.Util;
import rl.listener.LearningListener;
import rl.parallel.ParallelEngine;

public class ParallelQLearningTask implements Runnable {
    private ParallelEngine engine;
    
    public ParallelQLearningTask(ExecutorService executorService) {
        engine = new ParallelEngine(executorService);
    }
    
    public void addTickListenerForAgent1(LearningListener... learningListeners) {
        engine.addLearningListenersForAgent1(learningListeners);
    }
    public void addTickListenerForAgent2(LearningListener... learningListeners) {
        engine.addLearningListenersForAgent2(learningListeners);
    }
    public void addTickListenerForBothAgents(LearningListener... learningListeners) {
        engine.addLearningListenersForBothAgents(learningListeners);
    }
    
    @Override
    public void run() {
        engine.learn(Util.numEpisodes);
    }
}
