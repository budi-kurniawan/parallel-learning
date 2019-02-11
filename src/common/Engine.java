package common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import common.event.EpisodeEvent;
import common.event.TickEvent;
import common.event.TrialEvent;
import common.listener.EpisodeListener;
import common.listener.TickListener;
import common.listener.TrialListener;

public class Engine implements Callable<Void> {
    
    public int optimumEpisode;
    protected Factory factory;
    protected int index;
    protected List<TickListener> tickListeners = new ArrayList<>();
    protected List<EpisodeListener> episodeListeners = new ArrayList<>();
    protected List<TrialListener> trialListeners = new ArrayList<>();
    private long totalProcessTime;
    private long afterEpisodeListenerProcessTime;
    
    public Engine(Factory factory) {
        this.factory = factory;
    }
    
    public Engine(Factory factory, EpisodeListener episodeListener) {
        this.factory = factory;
        this.episodeListeners.add(episodeListener);
    }

    public Engine(Factory factory, TickListener tickListener) {
        this.factory = factory;
        this.tickListeners.add(tickListener);
    }

    public Engine(int index, Factory factory) {
        this.index = index;
        this.factory = factory;
    }

    public Engine(int index, Factory factory, EpisodeListener episodeListener) {
        this(factory, episodeListener);
        this.index = index;
    }

    public Engine(int index, Factory factory, TickListener tickListener) {
        this(factory, tickListener);
        this.index = index;
    }
    
    @Override
    public Void call() {
        long start = System.nanoTime();
        Environment environment = factory.createEnvironment();
        QEntry[][] q = factory.getQ();
        for (int episode = 1; episode <= CommonUtil.numEpisodes; episode++) {
            if (Thread.interrupted()) {
                break;
            }
            QLearningAgent agent = factory.createAgent(index, environment, episode);
            fireBeforeEpisodeEvent(new EpisodeEvent(agent, episode, agent.getEffectiveEpsilon(), q));
            for (int tick = 1; tick <= CommonUtil.MAX_TICKS; tick++) {
                if (Thread.interrupted()) {
                    // needs to call interrupt because Thread.interrupted() clears the interrups flag, so the outer Thread.interrupted() will be alerted
                    Thread.currentThread().interrupt();
                    break;
                }
                int prevState = agent.getState();
                agent.tick();
                int state = agent.getState();
                fireTickEvent(new TickEvent(agent, environment, q, tick, episode, prevState, state));
                if (agent.reachedGoal) {
                    optimumEpisode = episode;
                }
                if (agent.terminal) {
                    break; // end of episode
                }
            }
            long startEp = System.nanoTime();
            fireAfterEpisodeEvent(agent, episode);
            long endEp = System.nanoTime();
            environment.reset();
            afterEpisodeListenerProcessTime += (endEp - startEp);
            totalProcessTime = System.nanoTime() - start;// just in case this thread is interrupted, we will still have a processing time
        }
        long end = System.nanoTime();
        totalProcessTime = end - start;
        fireAfterTrialEvent(new TrialEvent(this, start, end, q));
        return null;
        //saveQ(q);
    }

    public long getTotalProcessTime() {
        return totalProcessTime;
    }
    
    public long getAfterEpisodeListenerProcessTime() {
        return afterEpisodeListenerProcessTime;
    }
    
    public void addTickListeners(TickListener... listeners) {
        tickListeners.addAll(Arrays.stream(listeners).collect(Collectors.toList()));
    }

    public void addEpisodeListeners(EpisodeListener... listeners) {
        episodeListeners.addAll(Arrays.stream(listeners).collect(Collectors.toList()));
    }
    
    public void addTrialListeners(TrialListener... listeners) {
        trialListeners.addAll(Arrays.stream(listeners).collect(Collectors.toList()));
    }
    
    protected void saveQ(QEntry[][] q) {
        int numStates = q.length;
        int numActions = factory.getActions().length;
        for (int i = 0; i < numStates; i++) {
            System.out.print("S" + i + ": ");
            for (int j = 0; j < numActions; j++) {
                System.out.print(q[i][j].value + ", ");
            }
            System.out.println();
        }
    }
    
    protected void fireAfterEpisodeEvent(QLearningAgent agent, int episode) {
        EpisodeEvent event = new EpisodeEvent(agent, episode, agent.getEffectiveEpsilon(), factory.getQ());
        episodeListeners.forEach(listener -> listener.afterEpisode(event));
    }
    
    protected void fireTickEvent(TickEvent event) {
        if (event.getPrevState() != Integer.MIN_VALUE) {
            tickListeners.forEach(listener -> listener.afterTick(event));
        }
    }
    
    private void fireBeforeEpisodeEvent(EpisodeEvent event) {
        episodeListeners.forEach(listener -> listener.beforeEpisode(event));
    }
    
    private void fireAfterTrialEvent(TrialEvent event) {
        trialListeners.forEach(listener -> listener.afterTrial(event));
    }
}