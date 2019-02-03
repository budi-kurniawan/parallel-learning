package q;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import q.listener.ParallelAgentsEpisodeListener;
import q.listener.SingleAgentEpisodeListener;
import rl.Engine;
import rl.QEntry;
import rl.Util;
import rl.listener.EpisodeListener;
import rl.parallel.ParallelEngine;
import rl.parallel.stopwalk.StopWalkEngine;

public class PerformanceTest {
    //// SINGLE AGENT
    public void testSingleAgent(ExecutorService executorService) {
        Engine engine = new Engine();
        SingleAgentEpisodeListener listener = new SingleAgentEpisodeListener();
        engine.addEpisodeListeners(listener);
        engine.call();
        Util.printMessage("single engine total process time: " + engine.getTotalProcessTime() / 1_000_000 + "ms");
    }

    public void runSingleAgent(ExecutorService executorService) {
        Util.canPrintMessage = false;
        // warm up
        testSingleAgent(executorService);
        testSingleAgent(executorService);
        
        Util.canPrintMessage = true;
        System.out.println("===== Single agent warm-up done ");

        long totalProcessingTime = 0;
        long totalAfterEpisodeListenerProcessingTime = 0;
        
        for (int i = 0; i < Util.numTrials; i++) {
            Engine engine = new Engine(new SingleAgentEpisodeListener());
            engine.call();
            totalProcessingTime += engine.getTotalProcessTime();
            totalAfterEpisodeListenerProcessingTime += engine.getAfterEpisodeListenerProcessTime();
        }            
        Util.printMessage("total processing time:" + totalProcessingTime);
        Util.printMessage("total after episode listener processing time:" + totalAfterEpisodeListenerProcessingTime);
        Util.printMessage("Avg single engine:" + (totalProcessingTime - totalAfterEpisodeListenerProcessingTime) / 1_000_000 / Util.numTrials + "ms");
    }
    
    //// NAIVE
    public Engine[] testNaiveParallelAgents(ExecutorService executorService, int numAgents, int trialNumber) {
        QEntry[][] q = Util.createInitialQ(Util.numRows, Util.numCols);
        List<QEntry[][]> qTables = new ArrayList<>();
        for (int i = 0; i < numAgents; i++) {
            qTables.add(q);
        }
        Engine[] engines = new ParallelEngine[numAgents];
        EpisodeListener listener = new ParallelAgentsEpisodeListener(trialNumber);
        for (int i = 0; i < numAgents; i++) {
            engines[i] = new ParallelEngine(i, qTables);
            engines[i].addEpisodeListeners(listener);
        }
        try {
            executorService.invokeAny(Arrays.asList(engines));
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }

        for (int i = 0; i < numAgents; i++) {
            Engine engine = engines[i];
            long totalProcessTime = engine.getTotalProcessTime();
            long totalAfterEpisodeTime = engine.getAfterEpisodeListenerProcessTime();
            System.out.println("engine processing time:" + (totalProcessTime - totalAfterEpisodeTime) / 1_000_000 
                    + " (" + totalProcessTime / 1_000_000 + " - " + totalAfterEpisodeTime / 1_000_000 + ")");
        }
        return engines;
    }
    
    
    public void runNaiveParallelAgents(int numAgents, ExecutorService executorService) {
        Util.canPrintMessage = false;
        testNaiveParallelAgents(executorService, numAgents, 0);
        testNaiveParallelAgents(executorService, numAgents, 0);
        Util.canPrintMessage = true;
        Util.printMessage("===== NaiveParallel warm-up done");
        long totalProcessingTime = 0L;
        for (int i = 0; i < Util.numTrials; i++) {
            Engine[] engines = testNaiveParallelAgents(executorService, numAgents, i + 1);
            long minimumProcessTime = Long.MAX_VALUE;
            for (Engine engine : engines) {
                long processTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
                minimumProcessTime = Math.min(processTime, minimumProcessTime);
            }
            totalProcessingTime += minimumProcessTime;
        }
        Util.printMessage("Avg processing time:" + totalProcessingTime / 1_000_000 / Util.numTrials + "ms\n");
        executorService.shutdownNow();
   }

    //// STOP WALK
    public Engine[] testStopWalkParallelAgents(ExecutorService executorService, int numAgents, Lock[] locks, int trialNumber) {
        QEntry[][] q = Util.createInitialQ(Util.numRows, Util.numCols);
        Engine[] engines = new StopWalkEngine[numAgents];
        EpisodeListener listener = new ParallelAgentsEpisodeListener(trialNumber);
        for (int i = 0; i < numAgents; i++) {
            engines[i] = new StopWalkEngine(i, q, locks);
            engines[i].addEpisodeListeners(listener);
        }
        try {
            executorService.invokeAny(Arrays.asList(engines));
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }

        for (int i = 0; i < numAgents; i++) {
            Engine engine = engines[i];
            long totalProcessTime = engine.getTotalProcessTime();
            long totalAfterEpisodeTime = engine.getAfterEpisodeListenerProcessTime();
            System.out.println("engine processing time:" + (totalProcessTime - totalAfterEpisodeTime) / 1_000_000 
                    + " (" + totalProcessTime / 1_000_000 + " - " + totalAfterEpisodeTime / 1_000_000 + ")");
        }
        return engines;
    }
    
    
    public void runStopWalkParallelAgents(int numAgents, ExecutorService executorService) {
        Util.canPrintMessage = false;
        int numStates = Util.numRows * Util.numCols;
        Lock[] locks = new Lock[numStates];
        for (int i = 0; i < numStates; i++) {
            locks[i] = new ReentrantLock();
        }
        testStopWalkParallelAgents(executorService, numAgents, locks, 0);
        testStopWalkParallelAgents(executorService, numAgents, locks, 0);
        Util.canPrintMessage = true;
        Util.printMessage("===== StopWalkParallel warm-up done");
        long totalProcessingTime = 0L;
        for (int i = 0; i < Util.numTrials; i++) {
            Engine[] engines = testStopWalkParallelAgents(executorService, numAgents, locks, i + 1);
            long minimumProcessTime = Long.MAX_VALUE;
            for (Engine engine : engines) {
                long processTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
                minimumProcessTime = Math.min(processTime, minimumProcessTime);
            }
            totalProcessingTime += minimumProcessTime;
        }
        Util.printMessage("Avg processing time:" + totalProcessingTime / 1_000_000 / Util.numTrials + "ms\n");
        executorService.shutdownNow();
   }
    
    public static void main(String[] args) {
        int numProcessors = 20;
        System.out.println("Performance test for " + numProcessors + " cores");
        ExecutorService executorService = Executors.newFixedThreadPool(500);
        Util.numRows = Util.numCols = 100;
        Util.numEpisodes = 35000;

        PerformanceTest test = new PerformanceTest();
        test.runSingleAgent(executorService);


        //// NAIVE
        for (int i = 2; i <= numProcessors; i+=2) {
            executorService = Executors.newFixedThreadPool(500);
            System.out.println("====================================== Start of Naive. numAgent " + i + " ===================================================================");
            test.runNaiveParallelAgents(i, executorService);
            System.out.println("====================================== End of numAgent " + i + " ======================================================================");
        }

        //// STOP WALK
        for (int i = 2; i <= numProcessors; i+=2) {
            executorService = Executors.newFixedThreadPool(500);
            System.out.println("====================================== Start of StopWalk. numAgent " + i + " ===================================================================");
            test.runStopWalkParallelAgents(i, executorService);
            System.out.println("====================================== End of numAgent " + i + " ======================================================================");
        }
        Util.canPrintMessage = false;
        Util.countContention = true;
        for (int i = 2; i <= numProcessors; i+=2) {
            Util.contentionCount.set(0);
            Util.tickCount.set(0);
            executorService = Executors.newFixedThreadPool(500);
            System.out.println("====================================== Start of StopWalk (contention). numAgent " + i + " ===================================================================");
            test.runStopWalkParallelAgents(i, executorService);
            System.out.println("lock contention count:" + Util.contentionCount.get());
            System.out.println("tick count:" + Util.tickCount.get());
            System.out.println("====================================== End of numAgent " + i + " ======================================================================");
        }
    }
}