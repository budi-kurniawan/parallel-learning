package q;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import rl.Engine;
import rl.QEntry;
import rl.StateAction;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.listener.EpisodeListener;
import rl.parallel.ParallelEngine;

public class PerformanceTestOLD {
    
    static long serialCheckingTime = 0L;
    static long parallelCheckingTime = 0L;
    
    public void testSingleAgent(ExecutorService executorService) {
        Engine engine = new Engine();
        List<Future<?>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        EpisodeListener listener = new EpisodeListener() {
            volatile boolean policyFound = false;
            @Override
            public void beforeEpisode(EpisodeEvent event) {
            }
            
            @Override
            public void afterEpisode(EpisodeEvent event) {
                long start = System.nanoTime();
                int episode = event.getEpisode();
                if (episode == Util.numEpisodes) {
                    //System.out.println("max episode reached by single agent ");
                    latch.countDown();
                    return;
                }
                if (policyFound) {
                    return;
                }
//                if (episode != Util.numEpisodes - 1) {
//                    return;
//                }
                QEntry[][] qTable = event.getQ();
                List<StateAction> steps = new ArrayList<>();
                if (Util.policyFound(qTable, steps)) {
                    // policy found
                    policyFound = true;
                    for (Future<?> future : futures) {
                        future.cancel(true);
                    }
                    StringBuilder sb = new StringBuilder(1000);
                    sb.append("Policy found by single agent at episode " + event.getEpisode() + "\n");
//                    for (StateAction step : steps) {
//                        sb.append("(" + step.state + ", " + step.action + "), ");
//                    }
//                    sb.append("(" + Util.getGoalState() + ")\n");
                    System.out.println(sb.toString());
                    latch.countDown();
                }
                long end = System.nanoTime();
                serialCheckingTime += (end - start);
            }
        };
        engine.addEpisodeListeners(listener);
        Future<?> future1 = executorService.submit(engine);
        futures.add(future1);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void testParallelAgents(ExecutorService executorService, int numAgents) {
        List<QEntry[][]> qTables = new ArrayList<>();
        QEntry[][] q1 = Util.createInitialQ(Util.numRows,  Util.numCols);
        for (int i = 0; i < numAgents; i++) {
            qTables.add(q1);
        }
//        for (int i = 0; i < numAgents; i++) {
//            qTables.add(Util.createInitialQ(Util.numRows,  Util.numCols));
//        }
        
        ParallelEngine[] parallelEngines = new ParallelEngine[numAgents];
        CountDownLatch latch = new CountDownLatch(numAgents);
        List<Future<?>> futures = new ArrayList<>();
        EpisodeListener listener = new EpisodeListener() {
            volatile boolean policyFound = false;
            @Override
            public void beforeEpisode(EpisodeEvent event) {
            }
            
            @Override
            public void afterEpisode(EpisodeEvent event) {
                long start = System.nanoTime();
                if (event.getEpisode() == Util.numEpisodes) {
                    //System.out.println("max episode reached by agent " + event.getAgent().getIndex());
                    latch.countDown();
                    return;
                }
                if (policyFound) {
                    return;
                }
                int episode = event.getEpisode();
//                if (episode != Util.numEpisodes - 1) {
//                    return;
//                }
                int agentIndex = event.getAgent().getIndex();
                QEntry[][] qTable = event.getQTables().get(agentIndex);
                List<StateAction> steps = new ArrayList<>();
                if (Util.policyFound(qTable, steps)) {
                    // policy found
                    policyFound = true;
                    for (Future<?> future : futures) {
                        future.cancel(true);
                    }
                    StringBuilder sb = new StringBuilder(1000);
                    sb.append("Policy found by agent " + agentIndex + " at episode " + event.getEpisode() + "\n");
//                    for (StateAction step : steps) {
//                        sb.append("(" + step.state + ", " + step.action + "), ");
//                    }
//                    sb.append("(" + Util.getGoalState() + ")\n");
                    System.out.println(sb.toString());
                    for (int i = 0; i < numAgents; i++) {
                        latch.countDown();
                    }
                }
                long end = System.nanoTime();
//                if (agentIndex == 0) {
                parallelCheckingTime += (end - start);
//                }
            }
        };
        for (int i = 0; i < numAgents; i++) {
            parallelEngines[i] = new ParallelEngine(i, qTables);
            parallelEngines[i].addEpisodeListeners(listener);
        }
        for (int i = 0; i < numAgents; i++) {
            Future<?> future = executorService.submit(parallelEngines[i]);
            futures.add(future);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Util.numRows = Util.numCols = 50;
        Util.numEpisodes = 15000;
        int numAgents = 2;
        PerformanceTestOLD test = new PerformanceTestOLD();

        // warm up
        test.testSingleAgent(executorService);
        test.testSingleAgent(executorService);
        test.testParallelAgents(executorService, numAgents);
        test.testParallelAgents(executorService, numAgents);
        
        System.out.println("----------------------------------------------------------");

        serialCheckingTime = 0;
        long t1 = System.nanoTime();
        test.testSingleAgent(executorService);
        long t2 = System.nanoTime();
        System.out.println("Single agent learning took: " + (t2 - t1)/1000000 + "ms");
        System.out.println("Single agent checking took: " + serialCheckingTime / 1000000 + "ms");
        System.out.println("Total serial: " + (t2-t1-serialCheckingTime)/1000000 + "ms");

        System.out.println("----------------------------------------------------------");
        
        parallelCheckingTime = 0;
        long t5 = System.nanoTime();
        test.testParallelAgents(executorService, numAgents);
        long t6 = System.nanoTime();
        System.out.println("Parallel agents learning took : " + (t6 - t5)/1000000 + "ms");
        System.out.println("Parallel agent checking took: " + parallelCheckingTime / 1000000 + "ms");
        System.out.println("Total parallel: " + (t6-t5-parallelCheckingTime)/1000000 + "ms");

        System.out.println();

        executorService.shutdown();
    
    }
}