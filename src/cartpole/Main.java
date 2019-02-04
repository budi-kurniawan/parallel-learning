package cartpole;

import q.listener.SingleAgentEpisodeListener;
import rl.Util;
import cartpole.Engine;

public class Main {
    /**
     * http://incompleteideas.net/sutton/book/code/pole.c
     * Paper: http://www.derongliu.org/adp/adp-cdrom/Barto1983.pdf
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("cartpole ...");
        Util.numEpisodes = 2;
        Util.MAX_TICKS = 200;
        Engine engine = new Engine();
        //SingleAgentEpisodeListener listener = new SingleAgentEpisodeListener();
        //engine.addEpisodeListeners(listener);
        engine.call();
        
        
//        for (int x = 0; x < 3; x++) {
//            for (int xDot = 0; xDot < 3; xDot++) {
//                for (int theta = 0; theta < 6; theta++) {
//                    for (int thetaDot = 0; thetaDot < 3; thetaDot++) {
//                        int state = x * 54 + xDot *18 + theta * 3 + thetaDot;
//                        System.out.println(x + "-" + xDot + "-" + theta + "-" + thetaDot + " : " + state);
//                    }
//                }
//            }
//        }

    }
}
