package cartpole;

import common.QEntry;
import gridworld.GridworldUtil;

public class Main {
    /**
     * http://incompleteideas.net/sutton/book/code/pole.c
     * Paper: http://www.derongliu.org/adp/adp-cdrom/Barto1983.pdf
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("cartpole ...");
        GridworldUtil.numEpisodes = 200;
        GridworldUtil.MAX_TICKS = 200;
        QEntry[][] q = CartpoleUtil.createInitialQ();
        CartpoleEngine engine = new CartpoleEngine(q, CartpoleUtil.getStateActions());
        engine.call();
    }
}
