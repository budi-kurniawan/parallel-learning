package cartpole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import rl.Agent;
import rl.QEntry;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.event.TickEvent;
import rl.event.TrialEvent;
import rl.listener.EpisodeListener;
import rl.listener.TickListener;
import rl.listener.TrialListener;

public class Engine implements Callable<Void> {
    
    public static int[] actions = { 0, 1 }; // 0 force to left, 1 force to right
    private static final int NUM_STATES = 163;
    protected List<TickListener> tickListeners = new ArrayList<>();
    protected List<EpisodeListener> episodeListeners = new ArrayList<>();
    protected List<TrialListener> trialListeners = new ArrayList<>();
    //protected int[][] stateActions;
    protected QEntry[][] q;
    private long totalProcessTime;
    private long afterEpisodeListenerProcessTime;
    
    private static final int[][] stateActions = new int[NUM_STATES][actions.length];
    static {
        for (int i = 0; i < NUM_STATES; i++) {
            stateActions[i] = actions;
        }
    }
    public Engine() {
        //stateActions = Util.getStateActions();
        q = new QEntry[NUM_STATES][actions.length];//Util.createInitialQ(Util.numRows,  Util.numCols);
        for (int i = 0; i < NUM_STATES; i++) {
            for (int j = 0; j < 2; j++) {
                q[i][j] = new QEntry();
            }
        }
    }

    public Engine(EpisodeListener episodeListener) {
        this();
        this.episodeListeners.add(episodeListener);
    }

    public Engine(QEntry[][] q) {
        //stateActions = Util.getStateActions();
        this.q  = q;
    }
    
    @Override
    public Void call() {
        long start = System.nanoTime();
        CartPoleEnvironment environment = new CartPoleEnvironment();
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
    
    protected Agent createAgent(CartPoleEnvironment environment, int episode, int numEpisodes) {
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
