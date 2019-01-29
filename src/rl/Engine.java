package rl;

import static rl.Action.DOWN;
import static rl.Action.LEFT;
import static rl.Action.RIGHT;
import static rl.Action.UP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import rl.event.EpisodeEvent;
import rl.event.TickEvent;
import rl.event.TrialEvent;
import rl.listener.EpisodeListener;
import rl.listener.TickListener;
import rl.listener.TrialListener;

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
        stateActions = Util.getStateActions();
        q = Util.createInitialQ(Util.numRows,  Util.numCols);
    }

    public Engine(QEntry[][] q) {
        stateActions = Util.getStateActions();
        this.q  = q;
    }
    
    @Override
    public Void call() {
        long start = System.nanoTime();
        Environment environment = new Environment();
        for (int episode = 1; episode <= Util.numEpisodes; episode++) {
            if (Thread.interrupted()) {
                break;
            }
            Agent agent = createAgent(environment, episode, Util.numEpisodes);
            fireBeforeEpisodeEvent(new EpisodeEvent(agent, episode, agent.getEffectiveEpsilon(), q));
            int count = 0;
            while (true) {
                count++;
                int prevState = agent.getState();
                agent.tick();
                int state = agent.getState();
                fireTickEvent(new TickEvent(agent, prevState, state));
                if (agent.terminal || count == Util.MAX_TICKS) {
                    break; // end of episode
                }
            }
            long startEp = System.nanoTime();
            fireAfterEpisodeEvent(agent, episode);
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
        int numStates = Util.numRows * Util.numCols;
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
    
    private void fireTickEvent(TickEvent event) {
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
    
    public static void main(String[] args) {
        Util.numRows = 5;
        Util.numCols = 5;
        Util.numEpisodes = 2000;
        Engine engine = new Engine();
        engine.call();
    }
}
