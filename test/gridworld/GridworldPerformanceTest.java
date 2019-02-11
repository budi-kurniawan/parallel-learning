package gridworld;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import common.CommonUtil;
import common.Engine;
import common.Factory;
import common.QEntry;
import common.listener.EpisodeListener;
import gridworld.listener.GridworldParallelAgentsEpisodeListener;
import gridworld.listener.GridworldSingleAgentEpisodeListener;
import gridworld.parallel.GridworldStopWalkFactory;

public class GridworldPerformanceTest {
    //// SINGLE AGENT
    public void testSingleAgent() {
        QEntry[][] q = GridworldUtil.createInitialQ();
        Factory factory = new GridworldFactory(q);
        Engine engine = new Engine(factory);
        GridworldSingleAgentEpisodeListener listener = new GridworldSingleAgentEpisodeListener();
        engine.addEpisodeListeners(listener);
        engine.call();
        CommonUtil.printMessage("single engine total process time: " + engine.getTotalProcessTime() / 1_000_000 + "ms");
    }

    public void runSingleAgent() {
        CommonUtil.canPrintMessage = false;
        // warm up
        testSingleAgent();
        testSingleAgent();
        
        CommonUtil.canPrintMessage = true;
        System.out.println("===== Single agent warm-up done ");

        long totalProcessingTime = 0;
        long totalAfterEpisodeListenerProcessingTime = 0;
        
        for (int i = 0; i < CommonUtil.numTrials; i++) {
            // need to create a new Q table for each trial
            QEntry[][] q = GridworldUtil.createInitialQ();
            Factory factory = new GridworldFactory(q);
            Engine engine = new Engine(factory);
            GridworldSingleAgentEpisodeListener listener = new GridworldSingleAgentEpisodeListener();
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
    public Engine[] testNaiveParallelAgents(ExecutorService executorService, int numAgents, int trialNumber) {
        QEntry[][] q = GridworldUtil.createInitialQ();
        Factory factory = new GridworldFactory(q);
        Engine[] engines = new Engine[numAgents];
        EpisodeListener listener = new GridworldParallelAgentsEpisodeListener(trialNumber);
        for (int i = 0; i < numAgents; i++) {
            engines[i] = new Engine(i, factory, listener);
        }
        try {
            executorService.invokeAny(Arrays.asList(engines));
            System.out.println(executorService.getClass());
            ThreadPoolExecutor s = null;
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
        CommonUtil.canPrintMessage = false;
        testNaiveParallelAgents(executorService, numAgents, 0);
        testNaiveParallelAgents(executorService, numAgents, 0);
        CommonUtil.canPrintMessage = true;
        CommonUtil.printMessage("===== NaiveParallel warm-up done");
        long totalProcessingTime = 0L;
        for (int i = 0; i < CommonUtil.numTrials; i++) {
            Engine[] engines = testNaiveParallelAgents(executorService, numAgents, i + 1);
            long minimumProcessTime = Long.MAX_VALUE;
            for (Engine engine : engines) {
                long processTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
                minimumProcessTime = Math.min(processTime, minimumProcessTime);
            }
            totalProcessingTime += minimumProcessTime;
        }
        CommonUtil.printMessage("Avg processing time:" + totalProcessingTime / 1_000_000 / CommonUtil.numTrials + "ms\n");
        executorService.shutdownNow();
   }

    //// STOP WALK
    public Engine[] testStopWalkParallelAgents(ExecutorService executorService, int numAgents, Lock[] locks, int trialNumber) {
        QEntry[][] q = GridworldUtil.createInitialQ();
        Factory factory = new GridworldStopWalkFactory(q, locks);
        Engine[] engines = new Engine[numAgents];
        EpisodeListener listener = new GridworldParallelAgentsEpisodeListener(trialNumber);
        for (int i = 0; i < numAgents; i++) {
            engines[i] = new Engine(i, factory, listener);
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
            Engine[] engines = testStopWalkParallelAgents(executorService, numAgents, locks, i + 1);
            long minimumProcessTime = Long.MAX_VALUE;
            for (Engine engine : engines) {
                long processTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
                minimumProcessTime = Math.min(processTime, minimumProcessTime);
            }
            totalProcessingTime += minimumProcessTime;
        }
        CommonUtil.printMessage("Avg processing time:" + totalProcessingTime / 1_000_000 / CommonUtil.numTrials + "ms\n");
        executorService.shutdownNow();
   }
    
    public static void main(String[] args) {
        int numProcessors = 2;
        System.out.println("Gridworld performance test with " + numProcessors + " cores");
        ExecutorService executorService = null;
        GridworldUtil.numRows = GridworldUtil.numCols = 100;
        CommonUtil.numEpisodes = 35000;

        GridworldPerformanceTest test = new GridworldPerformanceTest();
        test.runSingleAgent();


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