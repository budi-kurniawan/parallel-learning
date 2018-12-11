package rl.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import rl.Agent;
import rl.Engine;
import rl.Environment;
import rl.QEntry;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.event.TickEvent;
import rl.listener.LearningListener;

public class ParallelEngine extends Engine {
    private ExecutorService executorService;
    protected List<LearningListener> agent1LearningListeners = new ArrayList<>();
    protected List<LearningListener> agent2LearningListeners = new ArrayList<>();
    protected List<LearningListener> sharedLearningListeners = new ArrayList<>();
    
    public ParallelEngine(ExecutorService executorService) {
        super();
        this.executorService = executorService;
    }
    
    private class MyCallable implements Callable<Void> {
        private Environment environment;
        private int[][] stateActions;
        private QEntry[][] q1;
        private QEntry[][] q2;
        private int numEpisodes;
        private List<LearningListener> learningListeners;
        private List<LearningListener> sharedLearningListeners;
        
        public MyCallable(Environment environment, int[][] stateActions, QEntry[][] q1, QEntry[][] q2, int numEpisodes,
                List<LearningListener> learningListeners, List<LearningListener> sharedLearningListeners) {
            this.environment = environment;
            this.stateActions = stateActions;
            this.q1 = q1;
            this.q2 = q2;
            this.numEpisodes = numEpisodes;
            this.learningListeners = learningListeners;
            this.sharedLearningListeners = sharedLearningListeners;
        }
        
        @Override
        public Void call() throws Exception {
            for (int episode = 1; episode <= numEpisodes; episode++) {
                ParallelAgent agent = new ParallelAgent(environment, stateActions, q1, q2, episode, numEpisodes);
                doFireBeforeEpisodeEvent(new EpisodeEvent(this, episode, agent.getEffectiveEpsilon(), q1), learningListeners);
                int count = 0;
                while (true) {
                    count++;
                    int prevState1 = agent.getState();
                    agent.tick();
                    int state1 = agent.getState();
                    doFireTickEvent(new TickEvent(this, prevState1, state1), learningListeners);
                    if (count == MAX_TICKS || agent.terminal) {
                        break;
                    }
                }
                doFireAfterEpisodeEvent(new EpisodeEvent(this, episode, agent.getEffectiveEpsilon(), q1), learningListeners);
                doFireAfterEpisodeEvent(new EpisodeEvent(this, episode, agent.getEffectiveEpsilon(), q1, q2), sharedLearningListeners);
            }
            return null;
        }
    }
        
    @Override
    public void learn(int numEpisodes)  {
        Environment environment = new Environment();
        QEntry[][] q1 = Util.createInitialQ(Util.numRows,  Util.numCols);
        QEntry[][] q2 = Util.createInitialQ(Util.numRows,  Util.numCols);
        MyCallable callable1 = new MyCallable(environment, stateActions, q1, q2, numEpisodes, agent1LearningListeners, sharedLearningListeners);
        MyCallable callable2 = new MyCallable(environment, stateActions, q2, q1, numEpisodes, agent2LearningListeners, sharedLearningListeners);
        Future<Void> future1 = executorService.submit(callable1);
        Future<Void> future2 = executorService.submit(callable2);
        try {
            future1.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        try {
            future2.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        //saveQ(q);
    }
    
    private void doFireBeforeEpisodeEvent(EpisodeEvent event, List<LearningListener> listeners) {
        for (LearningListener learningListener : listeners) {
            learningListener.beforeEpisode(event);
        }
    }
    
    private void doFireAfterEpisodeEvent(EpisodeEvent event, List<LearningListener> listeners) {
        for (LearningListener learningListener : listeners) {
            learningListener.afterEpisode(event);
        }
    }
    
    private void doFireTickEvent(TickEvent event, List<LearningListener> listeners) {
        if (event.getPrevState() == Integer.MIN_VALUE) {
            return;
        }
        for (LearningListener learningListener : listeners) {
            learningListener.afterTick(event);
        }
    }
    
    public void addLearningListenersForAgent1(LearningListener... listeners) {
        for (LearningListener listener : listeners) {
            agent1LearningListeners.add(listener);
        }
    }

    public void addLearningListenersForAgent2(LearningListener... listeners) {
        for (LearningListener listener : listeners) {
            agent2LearningListeners.add(listener);
        }
    }
    
    public void addLearningListenersForBothAgents(LearningListener... listeners) {
        for (LearningListener listener : listeners) {
            sharedLearningListeners.add(listener);
        }
    }

//    protected void saveQ(double[][] q) {
//        // TODO save q to a file
//    }
}
