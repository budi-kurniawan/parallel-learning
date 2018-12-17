package gui;

import rl.Engine;
import rl.Util;
import rl.listener.LearningListener;
import rl.listener.TrialListener;

public class QLearningTask implements Runnable {
    private Engine engine;
    
    public QLearningTask() {
        engine = new Engine();
    }
    
    public void addTickListener(LearningListener... learningListeners) {
        engine.addLearningListeners(learningListeners);
    }

    public void addTrialListener(TrialListener... trialListeners) {
        engine.addTrialListeners(trialListeners);
    }
    
    @Override
    public void run() {
        engine.learn(Util.numEpisodes);
    }
}
