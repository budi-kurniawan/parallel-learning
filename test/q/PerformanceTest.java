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

public class PerformanceTest {
    
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
                if (event.getEpisode() == Util.numEpisodes) {
                    System.out.println("max episode reached by single agent ");
                    latch.countDown();
                    return;
                }
                if (policyFound) {
                    return;
                }
                QEntry[][] qTable = event.getQ();
                List<StateAction> steps = new ArrayList<>();
                if (Util.policyFound(qTable, steps)) {
                    // policy found
                    policyFound = true;
                    for (Future<?> future : futures) {
                        future.cancel(true);
                    }
                    System.out.println("Policy found by single agent at episode " + event.getEpisode());
                    for (StateAction step : steps) {
                        System.out.print("(" + step.state + ", " + step.action + "), ");
                    }
                    System.out.println("(" + Util.getGoalState() + ")");
                    latch.countDown();
                }
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
        QEntry[][] q1 = Util.createInitialQ(Util.numRows,  Util.numCols);
        //QEntry[][] q2 = q1;//Util.createInitialQ(Util.numRows,  Util.numCols);
        List<QEntry[][]> qTables = new ArrayList<>();
        for (int i = 0; i < numAgents; i++) {
            qTables.add(q1);
        }
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
                if (event.getEpisode() == Util.numEpisodes) {
                    System.out.println("max episode reached by agent " + event.getAgent().getIndex());
                    latch.countDown();
                    return;
                }
                if (policyFound) {
                    return;
                }
                int episode = event.getEpisode();
//                if (episode % 100 != 0) {
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
        PerformanceTest test = new PerformanceTest();

        // warm up
        test.testSingleAgent(executorService);
        test.testSingleAgent(executorService);
        System.out.println("----------------------------------------------------------");

        long t1 = System.currentTimeMillis();
        test.testSingleAgent(executorService);
        long t2 = System.currentTimeMillis();
        System.out.println("Single agent learning took: " + (t2 - t1) + "ms");

        System.out.println("ZXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        
        test.testParallelAgents(executorService, numAgents);
        test.testParallelAgents(executorService, numAgents);
        long t5 = System.currentTimeMillis();
        test.testParallelAgents(executorService, numAgents);
        long t6 = System.currentTimeMillis();
        System.out.println("Parallel agents learning took : " + (t6 - t5) + "ms");

        System.out.println();

        executorService.shutdown();
    
    }
}