package common;

import static common.Action.DOWN;
import static common.Action.LEFT;
import static common.Action.RIGHT;
import static common.Action.UP;

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

public class Engine implements Callable<Void> {
    
    public static int[] actions = { UP, DOWN, LEFT, RIGHT };
    protected List<TickListener> tickListeners = new ArrayList<>();
    protected List<EpisodeListener> episodeListeners = new ArrayList<>();
    protected List<TrialListener> trialListeners = new ArrayList<>();
    protected int[][] stateActions;
    protected QEntry[][] q;
    private long totalProcessTime;
    private long afterEpisodeListenerProcessTime;
    
    public Engine() {
        stateActions = GridworldUtil.getStateActions();
        q = GridworldUtil.createInitialQ(GridworldUtil.numRows,  GridworldUtil.numCols);
    }

    public Engine(EpisodeListener episodeListener) {
        this();
        this.episodeListeners.add(episodeListener);
    }

    public Engine(QEntry[][] q) {
        stateActions = GridworldUtil.getStateActions();
        this.q  = q;
    }

    public Engine(QEntry[][] q, int[][] stateActions) {
        this.q  = q;
        this.stateActions = stateActions;
    }
    
    @Override
    public Void call() {
        long start = System.nanoTime();
        Environment environment = createEnvironment();
        for (int episode = 1; episode <= GridworldUtil.numEpisodes; episode++) {
            if (Thread.interrupted()) {
                break;
            }
            Agent agent = createAgent(environment, episode, GridworldUtil.numEpisodes);
            fireBeforeEpisodeEvent(new EpisodeEvent(agent, episode, agent.getEffectiveEpsilon(), q));
            for (int tick = 1; tick <= GridworldUtil.MAX_TICKS; tick++) {
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
    
    protected Agent createAgent(Environment environment, int episode, int numEpisodes) {
        return new Agent(environment, stateActions, q, episode, numEpisodes);
    }
    
    protected Environment createEnvironment() {
        return new Environment();
    }

    public int[][] getStateActions() {
        return stateActions;
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
        int numActions = actions.length;
        for (int i = 0; i < numStates; i++) {
            System.out.print("S" + i + ": ");
            for (int j = 0; j < numActions; j++) {
                System.out.print(q[i][j].value + ", ");
            }
            System.out.println();
        }
    }
    
    protected void fireAfterEpisodeEvent(Agent agent, int episode) {
        EpisodeEvent event = new EpisodeEvent(agent, episode, agent.getEffectiveEpsilon(), q);
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