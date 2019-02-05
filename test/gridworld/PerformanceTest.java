package gridworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import common.AbstractEngine;
import common.CommonUtil;
import common.QEntry;
import common.listener.EpisodeListener;
import common.parallel.ParallelEngine;
import common.parallel.stopwalk.StopWalkEngine;
import gridworld.GridworldUtil;
import gridworld.listener.ParallelAgentsEpisodeListener;
import gridworld.listener.SingleAgentEpisodeListener;

public class PerformanceTest {
    //// SINGLE AGENT
    public void testSingleAgent(ExecutorService executorService) {
        QEntry[][] q = GridworldUtil.createInitialQ();
        AbstractEngine engine = new GridworldEngine(q);
        SingleAgentEpisodeListener listener = new SingleAgentEpisodeListener();
        engine.addEpisodeListeners(listener);
        engine.call();
        CommonUtil.printMessage("single engine total process time: " + engine.getTotalProcessTime() / 1_000_000 + "ms");
    }

    public void runSingleAgent(ExecutorService executorService) {
        CommonUtil.canPrintMessage = false;
        // warm up
        testSingleAgent(executorService);
        testSingleAgent(executorService);
        
        CommonUtil.canPrintMessage = true;
        System.out.println("===== Single agent warm-up done ");

        long totalProcessingTime = 0;
        long totalAfterEpisodeListenerProcessingTime = 0;
        
        QEntry[][] q = GridworldUtil.createInitialQ();
        for (int i = 0; i < CommonUtil.numTrials; i++) {
            AbstractEngine engine = new GridworldEngine(q);
            SingleAgentEpisodeListener listener = new SingleAgentEpisodeListener();
            engine.addEpisodeListeners(listener);
            engine.call();
            totalProcessingTime += engine.getTotalProcessTime();
            totalAfterEpisodeListenerProcessingTime += engine.getAfterEpisodeListenerProcessTime();
        }            
        CommonUtil.printMessage("total processing time:" + totalProcessingTime);
        CommonUtil.printMessage("total after episode listener processing time:" + totalAfterEpisodeListenerProcessingTime);
        CommonUtil.printMessage("Avg single engine:" + (totalProcessingTime - totalAfterEpisodeListenerProcessingTime) / 1_000_000 / CommonUtil.numTrials + "ms");
    }
    
    //// NAIVE
    public AbstractEngine[] testNaiveParallelAgents(ExecutorService executorService, int numAgents, int trialNumber) {
        QEntry[][] q = GridworldUtil.createInitialQ();
        List<QEntry[][]> qTables = new ArrayList<>();
        for (int i = 0; i < numAgents; i++) {
            qTables.add(q);
        }
        AbstractEngine[] engines = new ParallelEngine[numAgents];
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
            AbstractEngine engine = engines[i];
            long totalProcessTime = engine.getTotalProcessTime();
            long totalAfterEpisodeTime = engine.getAfterEpisodeListenerProcessTime();
            System.out.println("engine processing time:" + (totalProcessTime - totalAfterEpisodeTime) / 1_000_000 
                    + " (" + totalProcessTime / 1_000_000 + " - " + totalAfterEpisodeTime / 1_000_000 + ")");
        }
        return engines;
    }
    
    
    public void runNaiveParallelAgents(int numAgents, ExecutorService executorService) {
        CommonUtil.canPrintMessage = false;
        testNaiveParallelAgents(executorService, numAgents, 0);
        testNaiveParallelAgents(executorService, numAgents, 0);
        CommonUtil.canPrintMessage = true;
        CommonUtil.printMessage("===== NaiveParallel warm-up done");
        long totalProcessingTime = 0L;
        for (int i = 0; i < CommonUtil.numTrials; i++) {
            AbstractEngine[] engines = testNaiveParallelAgents(executorService, numAgents, i + 1);
            long minimumProcessTime = Long.MAX_VALUE;
            for (AbstractEngine engine : engines) {
                long processTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
                minimumProcessTime = Math.min(processTime, minimumProcessTime);
            }
            totalProcessingTime += minimumProcessTime;
        }
        CommonUtil.printMessage("Avg processing time:" + totalProcessingTime / 1_000_000 / CommonUtil.numTrials + "ms\n");
        executorService.shutdownNow();
   }

    //// STOP WALK
    public AbstractEngine[] testStopWalkParallelAgents(ExecutorService executorService, int numAgents, Lock[] locks, int trialNumber) {
        QEntry[][] q = GridworldUtil.createInitialQ();
        AbstractEngine[] engines = new StopWalkEngine[numAgents];
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
            AbstractEngine engine = engines[i];
            long totalProcessTime = engine.getTotalProcessTime();
            long totalAfterEpisodeTime = engine.getAfterEpisodeListenerProcessTime();
            System.out.println("engine processing time:" + (totalProcessTime - totalAfterEpisodeTime) / 1_000_000 
                    + " (" + totalProcessTime / 1_000_000 + " - " + totalAfterEpisodeTime / 1_000_000 + ")");
        }
        return engines;
    }
    
    
    public void runStopWalkParallelAgents(int numAgents, ExecutorService executorService) {
        CommonUtil.canPrintMessage = false;
        int numStates = GridworldUtil.numRows * GridworldUtil.numCols;
        Lock[] locks = new Lock[numStates];
        for (int i = 0; i < numStates; i++) {
            locks[i] = new ReentrantLock();
        }
        testStopWalkParallelAgents(executorService, numAgents, locks, 0);
        testStopWalkParallelAgents(executorService, numAgents, locks, 0);
        CommonUtil.canPrintMessage = true;
        CommonUtil.printMessage("===== StopWalkParallel warm-up done");
        long totalProcessingTime = 0L;
        for (int i = 0; i < CommonUtil.numTrials; i++) {
            AbstractEngine[] engines = testStopWalkParallelAgents(executorService, numAgents, locks, i + 1);
            long minimumProcessTime = Long.MAX_VALUE;
            for (AbstractEngine engine : engines) {
                long processTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
                minimumProcessTime = Math.min(processTime, minimumProcessTime);
            }
            totalProcessingTime += minimumProcessTime;
        }
        CommonUtil.printMessage("Avg processing time:" + totalProcessingTime / 1_000_000 / CommonUtil.numTrials + "ms\n");
        executorService.shutdownNow();
   }
    
    public static void main(String[] args) {
        int numProcessors = 20;
        System.out.println("Performance test for " + numProcessors + " cores");
        ExecutorService executorService = Executors.newFixedThreadPool(500);
        GridworldUtil.numRows = GridworldUtil.numCols = 100;
        CommonUtil.numEpisodes = 35000;

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
        CommonUtil.canPrintMessage = false;
        CommonUtil.countContention = true;
        for (int i = 2; i <= numProcessors; i+=2) {
            CommonUtil.contentionCount.set(0);
            CommonUtil.tickCount.set(0);
            executorService = Executors.newFixedThreadPool(500);
            System.out.println("====================================== Start of StopWalk (contention). numAgent " + i + " ===================================================================");
            test.runStopWalkParallelAgents(i, executorService);
            System.out.println("lock contention count:" + CommonUtil.contentionCount.get());
            System.out.println("tick count:" + CommonUtil.tickCount.get());
            System.out.println("====================================== End of numAgent " + i + " ======================================================================");
        }
    }
}