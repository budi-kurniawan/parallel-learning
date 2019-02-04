package cartpole;

import q.listener.SingleAgentEpisodeListener;
import rl.Util;
import cartpole.CartPoleEngine;

public class Main {
    /**
     * http://incompleteideas.net/sutton/book/code/pole.c
     * Paper: http://www.derongliu.org/adp/adp-cdrom/Barto1983.pdf
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("cartpole ...");
        Util.numEpisodes = 200;
        Util.MAX_TICKS = 200;
        CartPoleEngine engine = new CartPoleEngine();
        //SingleAgentEpisodeListener listener = new SingleAgentEpisodeListener();
        //engine.addEpisodeListeners(listener);
        engine.call();
    }
}
