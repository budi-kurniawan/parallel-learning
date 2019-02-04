package cartpole;

import rl.Agent;
import rl.Engine;
import rl.Environment;
import rl.QEntry;
import rl.listener.EpisodeListener;

public class CartPoleEngine extends Engine {
    
    public static int[] actions = { 0, 1 }; // 0 force to left, 1 force to right
    private static final int NUM_STATES = 163;
    protected QEntry[][] q;
    
    private static final int[][] stateActions = new int[NUM_STATES][actions.length];
    static {
        for (int i = 0; i < NUM_STATES; i++) {
            stateActions[i] = actions;
        }
    }
//    public CartPoleEngine() {
//        
//        //stateActions = Util.getStateActions();
//        q = new QEntry[NUM_STATES][actions.length];//Util.createInitialQ(Util.numRows,  Util.numCols);
//        for (int i = 0; i < NUM_STATES; i++) {
//            for (int j = 0; j < 2; j++) {
//                q[i][j] = new QEntry();
//            }
//        }
//    }
//
//    public CartPoleEngine(EpisodeListener episodeListener) {
//        this();
//        this.episodeListeners.add(episodeListener);
//    }
//
//    public CartPoleEngine(QEntry[][] q) {
//        //stateActions = Util.getStateActions();
//        this.q  = q;
//    }
    public CartPoleEngine(QEntry[][] q, int[][] stateActions) {
        super(q, stateActions);
        this.q = q;
    }
    
    @Override
    protected Agent createAgent(Environment environment, int episode, int numEpisodes) {
        return new Agent(environment, stateActions, q, episode, numEpisodes);
    }
    
    @Override
    protected Environment createEnvironment() {
        return new CartPoleEnvironment();
    }

    @Override
    public int[][] getStateActions() {
        return stateActions;
    }  
}