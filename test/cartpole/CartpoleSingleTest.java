package cartpole;

import cartpole.listener.CartpoleLoggingTickListener;
import common.CommonUtil;
import common.Engine;
import common.Factory;
import common.QEntry;
import common.QLearningAgent;

public class CartpoleSingleTest {
    //// SINGLE AGENT
    public void testSingleAgent() {
        QEntry[][] q = CartpoleUtil.createInitialQ();
        Factory factory = new QLearningCartpoleFactory(q);
        CartpoleLoggingTickListener listener = new CartpoleLoggingTickListener();
        Engine engine = new Engine(factory, listener);
        engine.call();
        listener.cleanUp();
        CommonUtil.printMessage("single engine total process time: " + engine.getTotalProcessTime() / 1_000_000 + "ms");
    }

    public static void main(String[] args) {
        QLearningAgent.ALPHA = 0.1F;
        QLearningAgent.GAMMA = 0.99F;
        QLearningAgent.EPSILON = 0.1F;
        CommonUtil.MAX_TICKS = 100_000;
        CommonUtil.numEpisodes = 200_000;
        CartpoleUtil.randomizeStartingPositions = false;
        CartpoleSingleTest test = new CartpoleSingleTest();
        test.testSingleAgent();
        

    }
}