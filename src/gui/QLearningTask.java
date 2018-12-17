package gui;

import rl.Engine;
import rl.Util;
import rl.listener.EpisodeListener;
import rl.listener.TickListener;
import rl.listener.TrialListener;

public class QLearningTask implements Runnable {
    private Engine engine;
    
    public QLearningTask() {
        engine = new Engine();
    }
    
    public void addTickListener(TickListener... learningListeners) {
        engine.addTickListeners(learningListeners);
    }

    public void addEpisodeListener(EpisodeListener... learningListeners) {
        engine.addEpisodeListeners(learningListeners);
    }
    
    public void addTrialListener(TrialListener... trialListeners) {
        engine.addTrialListeners(trialListeners);
    }
    
    @Override
    public void run() {
        engine.learn(Util.numEpisodes);
    }
}
