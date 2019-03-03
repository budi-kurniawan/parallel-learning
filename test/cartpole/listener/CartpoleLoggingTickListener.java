package cartpole.listener;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import common.CommonUtil;
import common.QEntry;
import common.event.TickEvent;
import common.listener.TickListener;

public class CartpoleLoggingTickListener implements TickListener {
    volatile boolean policyFound = false;
    private long totalProcessTime = 0L;
    BufferedWriter bw = null;
    
    public CartpoleLoggingTickListener() {
        try {
            bw = Files.newBufferedWriter(Paths.get("./cartpole-q.log"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void afterTick(TickEvent event) {
        long start = System.nanoTime();
        if (event.getTick() == CommonUtil.MAX_TICKS) {
            // policy found
//            printQ(event.getQ());
            policyFound = true;
            StringBuilder sb = new StringBuilder(1000);
            sb.append("Goal reached by single agent at episode " + event.getEpisode() + "\n");
            System.out.println(sb.toString());
            Thread.currentThread().interrupt();
        }
        long end = System.nanoTime();
        totalProcessTime += end - start;
    }

    public long getTotalProcessTime() {
        return totalProcessTime;
    }
    
    private void printQ(QEntry[][] q) {
        try {
            for (int i = 0; i < q.length; i++) {
                if (q[i][0].value != 0 || q[i][1].value != 0) {
                    bw.write(i + ":(" + q[i][0].value + ", " + q[i][1].value + "),\n");
                }
            }
            bw.write("\n");
        } catch (IOException e) {
            
        }
    }
    public void cleanUp() {
        try {
            bw.close();
        } catch (IOException e) {
            
        }
    }
}