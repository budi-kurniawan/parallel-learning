package q;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import q.listener.SingleAgentEpisodeListener;
import rl.Engine;
import rl.QEntry;
import rl.StateAction;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.listener.EpisodeListener;
import rl.parallel.ParallelEngine;

public class PerformanceTest {
    
    static long serialCheckingTime = 0L;
    static long parallelCheckingTime = 0L;
    
    public void testSingleAgent(ExecutorService executorService) {
        Engine engine = new Engine();
        SingleAgentEpisodeListener listener = new SingleAgentEpisodeListener();
        engine.addEpisodeListeners(listener);
        engine.call();
        System.out.println("listener total process time:" + listener.getTotalProcessTime());
    }
    
    public void testParallelAgents(ExecutorService executorService, int numAgents) {
        QEntry[][] q = Util.createInitialQ(Util.numRows, Util.numCols);
        List<QEntry[][]> qTables = new ArrayList<>();
        for (int i = 0; i < numAgents; i++) {
            qTables.add(q);
        }
        ParallelEngine[] parallelEngines = new ParallelEngine[numAgents];
        EpisodeListener listener = new EpisodeListener() {
            volatile boolean policyFound = false;
            @Override
            public void beforeEpisode(EpisodeEvent event) {
            }
            
            @Override
            public void afterEpisode(EpisodeEvent event) {
                long start = System.nanoTime();
                if (event.getEpisode() == Util.numEpisodes) {
                    return;
                }
                if (policyFound) {
                    return;
                }
                int episode = event.getEpisode();
                int agentIndex = event.getAgent().getIndex();
                List<QEntry[][]> qTables = event.getQTables();
                List<StateAction> steps = new ArrayList<>();
                if (Util.policyFound(qTables.get(0), steps)) {
                    // policy found
                    policyFound = true;
                    
                    StringBuilder sb = new StringBuilder(1000);
                    sb.append("Policy found by agent " + agentIndex + " at episode " + event.getEpisode() + "\n");
//                    for (StateAction step : steps) {
//                        sb.append("(" + step.state + ", " + step.action + "), ");
//                    }
//                    sb.append("(" + Util.getGoalState() + ")\n");
                    System.out.println(sb.toString());
                    Thread.currentThread().interrupt();
                }
                long end = System.nanoTime();
                parallelCheckingTime += (end - start); // updated by the last agent still running
            }
        };
        
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
        
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Util.numRows = Util.numCols = 50;
        Util.numEpisodes = 15000;
        int numAgents = 2;
        PerformanceTest test = new PerformanceTest();

        // warm up
        test.testSingleAgent(executorService);
        test.testSingleAgent(executorService);
        
        System.out.println("----------------------------------------------------------");

        int numTrials = 100;
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
        
        int numStates = Util.numRows * Util.numCols;
        test.testParallelAgents(executorService, numAgents);
        test.testParallelAgents(executorService, numAgents);
        long totalParallel = 0;
        for (int i = 0; i < numTrials; i++) {
            parallelCheckingTime = 0;
            long t5 = System.nanoTime();
            test.testParallelAgents(executorService, numAgents);
            long t6 = System.nanoTime();
            totalParallel += (t6 - t5 - parallelCheckingTime);
        }

        System.out.println("totalSerial:" + totalSerial);
        System.out.println("totalSerialListener:" + totalSerialListener);
        System.out.println("Avg serial:" + (totalSerial - totalSerialListener) / 1000000 / numTrials + "ms");
        System.out.println("Avg parallel:" + totalParallel / 1000000 / numTrials + "ms");
        System.out.println();
        executorService.shutdownNow();
   }
}