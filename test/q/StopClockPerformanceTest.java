package q;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import q.listener.SingleAgentEpisodeListener;
import rl.Engine;
import rl.QEntry;
import rl.StateAction;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.listener.EpisodeListener;
import rl.parallel.ParallelEngine;
import rl.parallel.stopflow.StopClockEngine;

public class StopClockPerformanceTest {
    
    static long serialCheckingTime = 0L;
    static long parallelCheckingTime = 0L;
    
    public void testSingleAgent(ExecutorService executorService) {
        Engine engine = new Engine();
        SingleAgentEpisodeListener listener = new SingleAgentEpisodeListener();
        engine.addEpisodeListeners(listener);
        engine.call();
        System.out.println("listener total process time:" + listener.getTotalProcessTime());
//        Future<?> future1 = executorService.submit(engine);
//        try {
//            future1.get();
//            System.out.println("listener total process time:" + listener.getTotalProcessTime());
//        } catch (InterruptedException | ExecutionException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        futures.add(future1);
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
    
    public void testParallelAgents(ExecutorService executorService, int numAgents) {
        int numStates = Util.numRows * Util.numCols;
        Lock[] locks = new Lock[numStates];
        for (int i = 0; i < numStates; i++) {
            locks[i] = new ReentrantLock();
        }
        
        QEntry[][] q = Util.createInitialQ(Util.numRows, Util.numCols);
        StopClockEngine[] stopClockEngines = new StopClockEngine[numAgents];
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
                QEntry[][] qTable = event.getQ();
                List<StateAction> steps = new ArrayList<>();
                if (Util.policyFound(qTable, steps)) {
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
                if (agentIndex == 0) {
                    parallelCheckingTime += (end - start);
                }
            }
        };
        for (int i = 0; i < numAgents; i++) {
            stopClockEngines[i] = new StopClockEngine(i, q, locks);
            stopClockEngines[i].addEpisodeListeners(listener);
        }
        try {
            executorService.invokeAny(Arrays.asList(stopClockEngines));
        } catch (InterruptedException | ExecutionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
//        for (int i = 0; i < numAgents; i++) {
//            Future<?> future = executorService.submit(stopClockEngines[i]);
//            futures.add(future);
//        }
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
    
    public static void main(String[] args) {
        
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Util.numRows = Util.numCols = 50;
        Util.numEpisodes = 15000;
        int numAgents = 2;
        StopClockPerformanceTest test = new StopClockPerformanceTest();

        // warm up
        test.testSingleAgent(executorService);
        test.testSingleAgent(executorService);
        
        System.out.println("----------------------------------------------------------");

        int numTrials = 1;
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
        
        System.out.println("lock contention count:" + Util.contentionCount.get());
        System.out.println("tick count:" + Util.tickCount.get());
    
   }
}