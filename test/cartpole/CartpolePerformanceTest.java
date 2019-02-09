package cartpole;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cartpole.listener.CartpoleParallelAgentsTickListener;
import cartpole.listener.CartpoleSingleAgentTickListener;
import common.CommonUtil;
import common.Engine;
import common.Factory;
import common.QEntry;
import common.QLearningAgent;
import common.listener.TickListener;
import common.parallel.stopwalk.StopWalkEngine;

public class CartpolePerformanceTest {
    //// SINGLE AGENT
    public void testSingleAgent() {
        QEntry[][] q = CartpoleUtil.createInitialQ();
        Factory factory = new QLearningCartpoleFactory(q);
        CartpoleSingleAgentTickListener listener = new CartpoleSingleAgentTickListener();
        Engine engine = new Engine(factory, listener);
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
            QEntry[][] q = CartpoleUtil.createInitialQ();
            Factory factory = new QLearningCartpoleFactory(q);
            Engine engine = new Engine(factory);
            CartpoleSingleAgentTickListener listener = new CartpoleSingleAgentTickListener();
            engine.addTickListeners(listener);
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
        QEntry[][] q = CartpoleUtil.createInitialQ();
        Factory factory = new QLearningCartpoleFactory(q);
        Engine[] engines = new Engine[numAgents];
        TickListener listener = new CartpoleParallelAgentsTickListener(trialNumber);
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
            long totalAfterEpisodeTime = 0;//engine.getAfterEpisodeListenerProcessTime();
            System.out.println("engine processing time:" + (totalProcessTime - totalAfterEpisodeTime) / 1_000_000 
                    + " (" + totalProcessTime / 1_000_000 + " - " + totalAfterEpisodeTime / 1_000_000 + ")");
        }
        return engines;
    }
    
    
    public void runNaiveParallelAgents(int numAgents, ExecutorService executorService) {
        CommonUtil.canPrintMessage = false;
//        testNaiveParallelAgents(executorService, numAgents, 0);
//        testNaiveParallelAgents(executorService, numAgents, 0);
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
        QEntry[][] q = CartpoleUtil.createInitialQ();
        Factory factory = new QLearningCartpoleFactory(q);
        Engine[] engines = new StopWalkEngine[numAgents];
        TickListener listener = new CartpoleParallelAgentsTickListener(trialNumber);
        for (int i = 0; i < numAgents; i++) {
            engines[i] = new StopWalkEngine(i, factory, locks);
            engines[i].addTickListeners(listener);
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
        int numStates = 163;
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
        int numProcessors = 20;
        QLearningAgent.ALPHA = 0.1F;
        QLearningAgent.GAMMA = 0.99F;
        QLearningAgent.EPSILON = 0.1F;
        CommonUtil.MAX_TICKS = 100_000;
        CommonUtil.numEpisodes = 200_000;
        System.out.println("Performance test for " + numProcessors + " cores");

        CartpolePerformanceTest test = new CartpolePerformanceTest();
        test.runSingleAgent();

        ExecutorService executorService = null;
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