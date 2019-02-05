package cartpole;

import common.CommonUtil;
import common.QEntry;

public class Main {
    /**
     * http://incompleteideas.net/sutton/book/code/pole.c
     * Paper: http://www.derongliu.org/adp/adp-cdrom/Barto1983.pdf
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("cartpole ...");
        CommonUtil.numEpisodes = 200;
        CommonUtil.MAX_TICKS = 200;
        QEntry[][] q = CartpoleUtil.createInitialQ();
        CartpoleEngine engine = new CartpoleEngine(q);
        engine.call();
    }
}
