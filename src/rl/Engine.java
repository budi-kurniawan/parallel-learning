package rl;

import static rl.Action.DOWN;
import static rl.Action.LEFT;
import static rl.Action.RIGHT;
import static rl.Action.UP;

import java.util.ArrayList;
import java.util.List;

import rl.event.EpisodeEvent;
import rl.event.TickEvent;
import rl.listener.LearningListener;

public class Engine {
    
    public static int[] actions = { UP, DOWN, LEFT, RIGHT };
    protected static final int MAX_TICKS = 2000;
    private List<LearningListener> learningListeners = new ArrayList<>();
    protected int[][] stateActions;
    
    public Engine() {
        stateActions = Util.getStateActions(Util.numRows, Util.numCols);
    }
    
    public void learn(int numEpisodes)  {
        Environment environment = new Environment();
        QEntry[][] q = Util.createInitialQ(Util.numRows,  Util.numCols);
        for (int episode = 1; episode <= numEpisodes; episode++) {
            Agent agent = new Agent(environment, stateActions, q, episode, numEpisodes);
            fireBeforeEpisodeEvent(new EpisodeEvent(this, episode, agent.getEffectiveEpsilon(), q));
            int count = 0;
            while (true) {
                count++;
                int prevState = agent.getState();
                agent.tick();
                int state = agent.getState();
                fireTickEvent(new TickEvent(this, prevState, state));
                if (agent.terminal || count == MAX_TICKS) {
                    break; // end of episode
                }
            }
            fireAfterEpisodeEvent(new EpisodeEvent(this, episode, agent.getEffectiveEpsilon(), q));
        }
        //saveQ(q);
    }
    
    public int[][] getStateActions() {
        return stateActions;
    }
    
    private void fireBeforeEpisodeEvent(EpisodeEvent event) {
        for (LearningListener learningListener : learningListeners) {
            learningListener.beforeEpisode(event);
        }
    }
    
    private void fireAfterEpisodeEvent(EpisodeEvent event) {
        for (LearningListener learningListener : learningListeners) {
            learningListener.afterEpisode(event);
        }
    }
    
    private void fireTickEvent(TickEvent event) {
        if (event.getPrevState() == Integer.MIN_VALUE) {
            return;
        }
        for (LearningListener learningListener : learningListeners) {
            learningListener.afterTick(event);
        }
    }
    
    public void addLearningListeners(LearningListener... listeners) {
        for (LearningListener listener : listeners) {
            learningListeners.add(listener);
        }
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
    
    public static void main(String[] args) {
        int numRows = 5;
        int numCols = 5;
        Engine engine = new Engine();
        engine.learn(Util.numEpisodes);
    }
    
}
