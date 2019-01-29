package q;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import q.listener.ParallelAgentsEpisodeListener;
import q.listener.SingleAgentEpisodeListener;
import rl.Engine;
import rl.QEntry;
import rl.Util;
import rl.listener.EpisodeListener;
import rl.parallel.ParallelEngine;

public class PerformanceTestOLD {
    
    static long serialCheckingTime = 0L;
    static long parallelCheckingTime = 0L;
    
    public void testSingleAgent(ExecutorService executorService) {
        Engine engine = new Engine();
        SingleAgentEpisodeListener listener = new SingleAgentEpisodeListener();
        engine.addEpisodeListeners(listener);
        engine.call();
        Util.printMessage("single engine total process time: " + engine.getTotalProcessTime() / 1_000_000 + "ms");
    }
    
    public void testParallelAgents(ExecutorService executorService, int numAgents, int trialNumber) {
        QEntry[][] q = Util.createInitialQ(Util.numRows, Util.numCols);
        List<QEntry[][]> qTables = new ArrayList<>();
        for (int i = 0; i < numAgents; i++) {
            qTables.add(q);
        }
        ParallelEngine[] parallelEngines = new ParallelEngine[numAgents];
        EpisodeListener listener = new ParallelAgentsEpisodeListener(trialNumber);
        for (int i = 0; i < numAgents; i++) {
            parallelEngines[i] = new ParallelEngine(i, qTables);
            parallelEngines[i].addEpisodeListeners(listener);
        }
        try {
            executorService.invokeAny(Arrays.asList(parallelEngines));
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        Util.canPrintMessage = false;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Util.numRows = Util.numCols = 50;
        Util.numEpisodes = 15000;
        int numAgents = 2;
        PerformanceTestOLD test = new PerformanceTestOLD();

        // warm up
        test.testSingleAgent(executorService);
        test.testSingleAgent(executorService);
        Util.canPrintMessage = true;
        Util.printMessage("===== Single agent warm-up done");

        int numTrials = 10;
        long totalSerial = 0;
        long totalSerialListener = 0;
        
        for (int i = 0; i < numTrials; i++) {
            long t1 = System.nanoTime();
            Engine engine = new Engine();
            SingleAgentEpisodeListener listener = new SingleAgentEpisodeListener();
            engine.addEpisodeListeners(listener);
            engine.call();
            long t2 = System.nanoTime();
            totalSerialListener += listener.getTotalProcessTime();
            totalSerial += (t2 - t1);

            //            System.out.println("Single agent learning took: " + (t2 - t1)/1000000 + "ms");
//            System.out.println("Single agent checking took: " + serialCheckingTime / 1000000 + "ms");
//            System.out.println("Total serial: " + (t2-t1-serialCheckingTime)/1000000 + "ms");
//
//            System.out.println("----------------------------------------------------------");
        }            

//        if (totalSerial > 0) {
//            System.out.println("Avg serial:" + totalSerial / 1000000 / numTrials + "ms");
//            executorService.shutdownNow();
//            return;
//        }
        
        Util.canPrintMessage = false;
        test.testParallelAgents(executorService, numAgents, 0);
        test.testParallelAgents(executorService, numAgents, 0);
        Util.canPrintMessage = true;
        Util.printMessage("===== Parallel agent warm-up done");
        long totalParallel = 0;
        for (int i = 0; i < numTrials; i++) {
            parallelCheckingTime = 0;
            long t5 = System.nanoTime();
            test.testParallelAgents(executorService, numAgents, i + 1);
            long t6 = System.nanoTime();
            totalParallel += (t6 - t5 - parallelCheckingTime);
        }

        Util.printMessage("totalSerial:" + totalSerial);
        Util.printMessage("totalSerialListener:" + totalSerialListener);
        Util.printMessage("Avg serial:" + (totalSerial - totalSerialListener) / 1000000 / numTrials + "ms");
        Util.printMessage("Avg parallel:" + totalParallel / 1000000 / numTrials + "ms");
        executorService.shutdownNow();
   }
}