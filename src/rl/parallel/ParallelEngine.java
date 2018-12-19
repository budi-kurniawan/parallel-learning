package rl.parallel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import rl.Engine;
import rl.Environment;
import rl.QEntry;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.event.TickEvent;
import rl.listener.EpisodeListener;
import rl.listener.TickListener;

public class ParallelEngine extends Engine {
    private ExecutorService executorService;
    protected List<TickListener> agent1TickListeners = new ArrayList<>();
    protected List<TickListener> agent2TickListeners = new ArrayList<>();
    protected List<TickListener> sharedTickListeners = new ArrayList<>();
    protected List<EpisodeListener> agent1EpisodeListeners = new ArrayList<>();
    protected List<EpisodeListener> agent2EpisodeListeners = new ArrayList<>();
    protected List<EpisodeListener> sharedEpisodeListeners = new ArrayList<>();
    
    public ParallelEngine(ExecutorService executorService) {
        super();
        this.executorService = executorService;
    }
    
    private class ParallelLearningTask implements Callable<Void> {
        private Environment environment;
        private int[][] stateActions;
        private QEntry[][] q1;
        private QEntry[][] q2;
        private int numEpisodes;
        private List<EpisodeListener> episodeListeners;
        private List<EpisodeListener> sharedEpisodeListeners;
        private String agentId;
        
        public ParallelLearningTask(String agentId, Environment environment, int[][] stateActions, QEntry[][] q1, QEntry[][] q2, int numEpisodes,
                List<EpisodeListener> episodeListeners, List<EpisodeListener> sharedEpisodeListeners) {
            this.agentId = agentId;
            this.environment = environment;
            this.stateActions = stateActions;
            this.q1 = q1;
            this.q2 = q2;
            this.numEpisodes = numEpisodes;
            this.episodeListeners = episodeListeners;
            this.sharedEpisodeListeners = sharedEpisodeListeners;
        }
        
        @Override
        public Void call() throws Exception {
            for (int episode = 1; episode <= numEpisodes; episode++) {
                ParallelAgent agent = new ParallelAgent(agentId, environment, stateActions, q1, q2, episode, numEpisodes);
                doFireBeforeEpisodeEvent(new EpisodeEvent(this, episode, agent.getEffectiveEpsilon(), q1), episodeListeners);
                int count = 0;
                while (true) {
                    if (Thread.interrupted()) {
                        return null;
                    }
                    count++;
                    int prevState1 = agent.getState();
                    agent.tick();
                    int state1 = agent.getState();
                    //doFireTickEvent(new TickEvent(this, prevState1, state1), learningListeners);
                    if (count == Util.MAX_TICKS || agent.terminal || Thread.interrupted()) {
                        break;
                    }
                }
                doFireAfterEpisodeEvent(new EpisodeEvent(this, agentId, episode, agent.getEffectiveEpsilon(), q1), episodeListeners);
                doFireAfterEpisodeEvent(new EpisodeEvent(this, agentId, episode, agent.getEffectiveEpsilon(), q1, q2), sharedEpisodeListeners);
            }
            return null;
        }
    }
        
    @Override
    public void learn(int numEpisodes)  {
        System.out.println("ParallelEngine.learn()");
        Environment environment = new Environment();
        QEntry[][] q1 = Util.createInitialQ(Util.numRows,  Util.numCols);
        QEntry[][] q2 = Util.createInitialQ(Util.numRows,  Util.numCols);
        ParallelLearningTask task1 = new ParallelLearningTask("agent-1", environment, stateActions, q1, q2, numEpisodes, agent1EpisodeListeners, sharedEpisodeListeners);
        ParallelLearningTask task2 = new ParallelLearningTask("agent-2", environment, stateActions, q2, q1, numEpisodes, agent2EpisodeListeners, sharedEpisodeListeners);
        Future<Void> future1 = executorService.submit(task1);
        Future<Void> future2 = executorService.submit(task2);
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
    
    private void doFireBeforeEpisodeEvent(EpisodeEvent event, List<EpisodeListener> listeners) {
        listeners.forEach(listener -> listener.beforeEpisode(event));
    }
    
    private void doFireAfterEpisodeEvent(EpisodeEvent event, List<EpisodeListener> listeners) {
        listeners.forEach(listener -> listener.afterEpisode(event));
    }
    
    private void doFireTickEvent(TickEvent event, List<TickListener> listeners) {
        if (event.getPrevState() != Integer.MIN_VALUE) {
            listeners.forEach(listener -> listener.afterTick(event));
        }
    }
    
    public void addTickListenersForAgent1(TickListener... listeners) {
        agent1TickListeners.addAll(Arrays.stream(listeners).collect(Collectors.toList()));
    }

    public void addTickListenersForAgent2(TickListener... listeners) {
        agent2TickListeners.addAll(Arrays.stream(listeners).collect(Collectors.toList()));
    }
    
    public void addTickListenersForBothAgents(TickListener... listeners) {
        sharedTickListeners.addAll(Arrays.stream(listeners).collect(Collectors.toList()));
    }

    public void addEpisodeListenersForAgent1(EpisodeListener... listeners) {
        agent1EpisodeListeners.addAll(Arrays.stream(listeners).collect(Collectors.toList()));
    }

    public void addEpisodeListenersForAgent2(EpisodeListener... listeners) {
        agent2EpisodeListeners.addAll(Arrays.stream(listeners).collect(Collectors.toList()));
    }
    
    public void addEpisodeListenersForBothAgents(EpisodeListener... listeners) {
        sharedEpisodeListeners.addAll(Arrays.stream(listeners).collect(Collectors.toList()));
    }

//    protected void saveQ(double[][] q) {
//        // TODO save q to a file
//    }
}
