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
import gridworld.GridworldUtil;

public abstract class AbstractEngine implements Callable<Void> {
    
    protected List<TickListener> tickListeners = new ArrayList<>();
    protected List<EpisodeListener> episodeListeners = new ArrayList<>();
    protected List<TrialListener> trialListeners = new ArrayList<>();
    private long totalProcessTime;
    private long afterEpisodeListenerProcessTime;
    
    public AbstractEngine() {
    }
    
    public AbstractEngine(EpisodeListener episodeListener) {
        this.episodeListeners.add(episodeListener);
    }

    public abstract QEntry[][] getQ();
    public abstract int[] getActions();
    public abstract Environment createEnvironment();
    public abstract int[][] getStateActions();
    public abstract Agent createAgent(Environment environment, int episode);
    
    @Override
    public Void call() {
        long start = System.nanoTime();
        Environment environment = createEnvironment();
        QEntry[][] q = getQ();
        for (int episode = 1; episode <= CommonUtil.numEpisodes; episode++) {
            if (Thread.interrupted()) {
                break;
            }
            Agent agent = createAgent(environment, episode);
            fireBeforeEpisodeEvent(new EpisodeEvent(agent, episode, agent.getEffectiveEpsilon(), q));
            for (int tick = 1; tick <= CommonUtil.MAX_TICKS; tick++) {
                if (Thread.interrupted()) {
                    break;
                }
                int prevState = agent.getState();
                agent.tick();
                int state = agent.getState();
                fireTickEvent(new TickEvent(agent, environment, tick, episode, prevState, state));
                if (agent.terminal) {
                    break; // end of episode
                }
            }
            long startEp = System.nanoTime();
            fireAfterEpisodeEvent(agent, episode);
            environment.reset();
            long endEp = System.nanoTime();
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
        int numStates = GridworldUtil.numRows * GridworldUtil.numCols;
        int numActions = getActions().length;
        for (int i = 0; i < numStates; i++) {
            System.out.print("S" + i + ": ");
            for (int j = 0; j < numActions; j++) {
                System.out.print(q[i][j].value + ", ");
            }
            System.out.println();
        }
    }
    
    protected void fireAfterEpisodeEvent(Agent agent, int episode) {
        EpisodeEvent event = new EpisodeEvent(agent, episode, agent.getEffectiveEpsilon(), getQ());
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