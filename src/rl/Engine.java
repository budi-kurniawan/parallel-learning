package rl;

import static rl.Action.DOWN;
import static rl.Action.LEFT;
import static rl.Action.RIGHT;
import static rl.Action.UP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import rl.event.EpisodeEvent;
import rl.event.TickEvent;
import rl.event.TrialEvent;
import rl.listener.EpisodeListener;
import rl.listener.TickListener;
import rl.listener.TrialListener;

public class Engine implements Runnable {
    
    public static int[] actions = { UP, DOWN, LEFT, RIGHT };
    protected List<TickListener> tickListeners = new ArrayList<>();
    protected List<EpisodeListener> episodeListeners = new ArrayList<>();
    protected List<TrialListener> trialListeners = new ArrayList<>();
    protected int[][] stateActions;
    protected QEntry[][] q;
    
    public Engine() {
        stateActions = Util.getStateActions(Util.numRows, Util.numCols);
        q = Util.createInitialQ(Util.numRows,  Util.numCols);
    }
    
    @Override
    public void run() {
        Environment environment = new Environment();
        long startTime = System.currentTimeMillis();
        for (int episode = 1; episode <= Util.numEpisodes; episode++) {
            Agent agent = createAgent(environment, episode, Util.numEpisodes);//new Agent(environment, stateActions, q, episode, numEpisodes);
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
            fireAfterEpisodeEvent(agent, episode);
        }
        long endTime = System.currentTimeMillis();
        fireAfterTrialEvent(new TrialEvent(this, startTime, endTime, q));
        //saveQ(q);
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
        engine.run();
    }
}
