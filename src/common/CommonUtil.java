package common;

import java.util.concurrent.atomic.AtomicInteger;

public class CommonUtil {
    public static int MAX_TICKS = 20000;
    public static int numEpisodes = 100;
    public static int numTrials = 10;
    public static AtomicInteger contentionCount = new AtomicInteger(0);
    public static AtomicInteger tickCount = new AtomicInteger(0);
    public static boolean countContention = false;
    public static boolean canPrintMessage = false;
    
    public static void printMessage(String message) {
        if (canPrintMessage) {
            System.out.println(message);
        }
    }

    public static float getEffectiveEpsilon(int episode, int numEpisode, float epsilon) {
        if (numEpisodes == 1) {
            return epsilon;
        } else {
            return (numEpisodes - episode) * epsilon / (numEpisodes - 1);
        }
    }
}
