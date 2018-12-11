package gui;

import rl.Engine;
import rl.Util;
import rl.listener.LearningListener;

public class QLearningTask implements Runnable {
    private Engine engine;
    
    public QLearningTask() {
        engine = new Engine();
    }
    
    public void addTickListener(LearningListener... learningListeners) {
        engine.addLearningListeners(learningListeners);
    }
    
    @Override
    public void run() {
        engine.learn(Util.numEpisodes);
    }
}
