package cartpole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cartpole.listener.CartpoleParallelAgentsTickListener;
import cartpole.listener.CartpoleSingleAgentTickListener;
import cartpole.parallel.CartpoleStopWalkFactory;
import common.CommonUtil;
import common.Engine;
import common.Factory;
import common.QEntry;
import common.QLearningAgent;
import common.listener.TickListener;

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
//        testSingleAgent();
//        testSingleAgent();
        
        CommonUtil.canPrintMessage = true;
//        System.out.println("===== Single agent warm-up done ");

        long totalProcessingTime = 0;
        long totalAfterEpisodeListenerProcessingTime = 0;
        int totalEpisodes = 0;
        
        for (int i = 0; i < CommonUtil.numTrials; i++) {
            QEntry[][] q = CartpoleUtil.createInitialQ();
            Factory factory = new QLearningCartpoleFactory(q);
            Engine engine = new Engine(factory);
            CartpoleSingleAgentTickListener listener = new CartpoleSingleAgentTickListener();
            engine.addTickListeners(listener);
            engine.call();
            totalProcessingTime += engine.getTotalProcessTime();
            totalAfterEpisodeListenerProcessingTime += engine.getAfterEpisodeListenerProcessTime();
            totalEpisodes += engine.optimumEpisode;
        }
//        CommonUtil.printMessage("total processing time:" + totalProcessingTime);
//        CommonUtil.printMessage("total after episode listener processing time:" + totalAfterEpisodeListenerProcessingTime);
        CommonUtil.printMessage("Avg single engine:" + (totalProcessingTime - totalAfterEpisodeListenerProcessingTime) / 1_000_000 / CommonUtil.numTrials + "ms\n");
        CommonUtil.printMessage("Avg episodes:" + (float) totalEpisodes / CommonUtil.numTrials + "\n");
    }
    
    //// NAIVE
//    public Engine[] doNaiveParallelAgents(ExecutorService executorService, int numAgents, int trialNumber) {
//        QEntry[][] q = CartpoleUtil.createInitialQ();
//        Factory factory = new QLearningCartpoleFactory(q);
//        Engine[] engines = new Engine[numAgents];
//        TickListener listener = new CartpoleParallelAgentsTickListener(trialNumber);
//        for (int i = 0; i < numAgents; i++) {
//            engines[i] = new Engine(i, factory, listener);
//        }
//        try {
//            executorService.invokeAny(Arrays.asList(engines));
//        } catch (InterruptedException | ExecutionException e1) {
//            e1.printStackTrace();
//        }
//        return engines;
//    }

    private void blockUntilAllThreadsFinish(List<Future<Void>> futures) {
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e1) {
                e1.printStackTrace();
            }
        }
    }
    public Engine[] doNaiveParallelAgents(ExecutorService executorService, int numAgents, int trialNumber) {
        QEntry[][] q = CartpoleUtil.createInitialQ();
        Factory factory = new QLearningCartpoleFactory(q);
        Engine[] engines = new Engine[numAgents];
        TickListener listener = new CartpoleParallelAgentsTickListener(trialNumber);
        List<Future<Void>> futures = new ArrayList<>(numAgents);
        for (int i = 0; i < numAgents; i++) {
            engines[i] = new Engine(i, factory, listener);
            futures.add(executorService.submit(engines[i]));
        }
        blockUntilAllThreadsFinish(futures);
        return engines;
    }
    
    public void runNaiveParallelAgents(int numAgents, ExecutorService executorService) {
        CommonUtil.canPrintMessage = false;
//        testNaiveParallelAgents(executorService, numAgents, 0);
//        testNaiveParallelAgents(executorService, numAgents, 0);
        CommonUtil.canPrintMessage = true;
//        CommonUtil.printMessage("===== NaiveParallel warm-up done");
        long totalProcessingTime = 0L;
        int totalEpisodes = 0;
        for (int trial = 1; trial <= CommonUtil.numTrials; trial++) {
            Engine[] engines = doNaiveParallelAgents(executorService, numAgents, trial);
            long minimumProcessTime = Long.MAX_VALUE;
            int minOptimumEpisode = Integer.MAX_VALUE;
            for (Engine engine : engines) {
                long processTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
                minimumProcessTime = Math.min(processTime, minimumProcessTime);
                if (engine.optimumEpisode != 0) {
                    minOptimumEpisode = Math.min(engine.optimumEpisode, minOptimumEpisode);
                }
            }
            totalEpisodes += minOptimumEpisode;
            if (trial == 1) {
                CommonUtil.printMessage("Min optimum episode: ");
            }
            CommonUtil.printMessage(minOptimumEpisode + (trial < CommonUtil.numTrials ? ", " : "\n"));
            totalProcessingTime += minimumProcessTime;
        }
        CommonUtil.printMessage("Avg processing time: " + totalProcessingTime / 1_000_000 / CommonUtil.numTrials + "ms\n");
        CommonUtil.printMessage("Avg episodes:" + (float) totalEpisodes / CommonUtil.numTrials + "\n");
   }

    //// STOP WALK
    public Engine[] testStopWalkParallelAgents(ExecutorService executorService, int numAgents, Lock[] locks, int trialNumber) {
        QEntry[][] q = CartpoleUtil.createInitialQ();
        Factory factory = new CartpoleStopWalkFactory(q, locks);
        Engine[] engines = new Engine[numAgents];
        TickListener listener = new CartpoleParallelAgentsTickListener(trialNumber);
        for (int i = 0; i < numAgents; i++) {
            engines[i] = new Engine(i, factory);
            engines[i].addTickListeners(listener);
        }
        List<Future<Void>> futures = new ArrayList<>(numAgents);
        for (int i = 0; i < numAgents; i++) {
            engines[i] = new Engine(i, factory, listener);
            futures.add(executorService.submit(engines[i]));
        }
        blockUntilAllThreadsFinish(futures);
        return engines;
    }
    
    public void runStopWalkParallelAgents(int numAgents, ExecutorService executorService) {
        CommonUtil.canPrintMessage = false;
        int numStates = 163;
        Lock[] locks = new Lock[numStates];
        for (int i = 0; i < numStates; i++) {
            locks[i] = new ReentrantLock();
        }
//        testStopWalkParallelAgents(executorService, numAgents, locks, 0);
//        testStopWalkParallelAgents(executorService, numAgents, locks, 0);
        CommonUtil.canPrintMessage = true;
//        CommonUtil.printMessage("===== StopWalkParallel warm-up done");
        long totalProcessingTime = 0L;
        int totalEpisodes = 0;
        for (int trial = 1; trial <= CommonUtil.numTrials; trial++) {
            Engine[] engines = testStopWalkParallelAgents(executorService, numAgents, locks, trial);
            long minimumProcessTime = Long.MAX_VALUE;
            int minOptimumEpisode = Integer.MAX_VALUE;
            for (Engine engine : engines) {
                long processTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
                minimumProcessTime = Math.min(processTime, minimumProcessTime);
                if (engine.optimumEpisode != 0) {
                    minOptimumEpisode = Math.min(engine.optimumEpisode, minOptimumEpisode);
                }
                
            }
            totalEpisodes += minOptimumEpisode;
            if (trial == 1) {
                CommonUtil.printMessage("Min optimum episode: ");
            }
            CommonUtil.printMessage(minOptimumEpisode + (trial < CommonUtil.numTrials ? ", " : "\n"));
            totalProcessingTime += minimumProcessTime;
        }
        CommonUtil.printMessage("Avg processing time:" + totalProcessingTime / 1_000_000 / CommonUtil.numTrials + "ms\n");
        CommonUtil.printMessage("Avg episodes:" + (float) totalEpisodes / CommonUtil.numTrials + "\n");
   }
    
    public static void main(String[] args) {
        int numProcessors = 4;
        QLearningAgent.ALPHA = 0.1F;
        QLearningAgent.GAMMA = 0.99F;
        QLearningAgent.EPSILON = 0.1F;
        CommonUtil.MAX_TICKS = 1500;
        CommonUtil.numEpisodes = 200_000;
        CartpoleUtil.randomizeStartingPositions = true;
        System.out.println("Cartpole performance test with " + numProcessors + " cores");

        CartpolePerformanceTest test = new CartpolePerformanceTest();
        test.runSingleAgent();
        
        //// NAIVE
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 2; i <= numProcessors; i+=2) {
            System.out.println("====================================== Start of Naive. numAgent " + i + " ===================================================================");
            test.runNaiveParallelAgents(i, executorService);
            System.out.println();
        }

        //// STOP WALK
        for (int i = 2; i <= numProcessors; i+=2) {
            System.out.println("====================================== Start of StopWalk. numAgent " + i + " ===================================================================");
            test.runStopWalkParallelAgents(i, executorService);
            System.out.println();
        }
//        CommonUtil.canPrintMessage = false;
//        CommonUtil.countContention = true;
//        for (int i = 2; i <= numProcessors; i+=2) {
//            CommonUtil.contentionCount.set(0);
//            CommonUtil.tickCount.set(0);
//            System.out.println("====================================== Start of StopWalk (contention). numAgent " + i + " ===================================================================");
//            test.runStopWalkParallelAgents(i, executorService);
//            System.out.println("lock contention count:" + CommonUtil.contentionCount.get());
//            System.out.println("tick count:" + CommonUtil.tickCount.get());
//            System.out.println("====================================== End of numAgent " + i + " ======================================================================");
//        }
        executorService.shutdown();
        System.out.println("Done");
    }
}