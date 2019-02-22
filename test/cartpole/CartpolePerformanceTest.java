package cartpole;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import cartpole.listener.CartpoleParallelAgentsTickListener;
import cartpole.listener.CartpoleSingleAgentTickListener;
import cartpole.parallel.CartpoleStopWalkFactory;
import common.CommonUtil;
import common.Engine;
import common.Factory;
import common.QEntry;
import common.QLearningAgent;
import common.TestResult;
import common.TestUtil;
import common.listener.TickListener;

public class CartpolePerformanceTest {
    //// SINGLE AGENT
    public void testSingleAgent() {
        QEntry[][] q = CartpoleUtil.createInitialQ();
        Factory factory = new QLearningCartpoleFactory(q);
        CartpoleSingleAgentTickListener listener = new CartpoleSingleAgentTickListener();
        Engine engine = new Engine(factory, listener);
        engine.call();
    }

    public TestResult[] runSingleAgent() {
//        testSingleAgent();
//        testSingleAgent();
        TestResult[] testResults = new TestResult[CommonUtil.numTrials];
        for (int trial = 1; trial <= CommonUtil.numTrials; trial++) {
            QEntry[][] q = CartpoleUtil.createInitialQ();
            Factory factory = new QLearningCartpoleFactory(q);
            Engine engine = new Engine(factory);
            CartpoleSingleAgentTickListener listener = new CartpoleSingleAgentTickListener();
            engine.addTickListeners(listener);
            engine.call();
            long totalProcessingTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
            testResults[trial - 1] = new TestResult(engine.lastEpisode, engine.totalTicks, totalProcessingTime);
        }
        return testResults;
    }
    
    //// NAIVE
    public Engine[] doNaiveParallelAgents(ExecutorService executorService, int numAgents, int trialNumber) {
        QEntry[][] q = CartpoleUtil.createInitialQ();
        Factory factory = new QLearningCartpoleFactory(q);
        TickListener listener = new CartpoleParallelAgentsTickListener(trialNumber);
        Engine[] engines = new Engine[numAgents];
        Arrays.setAll(engines, i -> new Engine(i, factory, listener)); // assign a new Engine() to each element in engines
        try {
            executorService.invokeAny(Arrays.asList(engines));
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }
        return engines;
    }

//    public Engine[] doNaiveParallelAgents(ExecutorService executorService, int numAgents, int trialNumber) {
//        QEntry[][] q = CartpoleUtil.createInitialQ();
//        Factory factory = new QLearningCartpoleFactory(q);
//        Engine[] engines = new Engine[numAgents];
//        TickListener listener = new CartpoleParallelAgentsTickListener(trialNumber);
//        List<Future<Void>> futures = new ArrayList<>(numAgents);
//        for (int i = 0; i < numAgents; i++) {
//            engines[i] = new Engine(i, factory, listener);
//            futures.add(executorService.submit(engines[i]));
//        }
//        CommonUtil.blockUntilAllThreadsAreComplete(futures);
//        return engines;
//    }
    
    public TestResult[] runNaiveParallelAgents(int numAgents, ExecutorService executorService) {
//        testNaiveParallelAgents(executorService, numAgents, 0);
//        testNaiveParallelAgents(executorService, numAgents, 0);
        TestResult[] testResults = new TestResult[CommonUtil.numTrials];
        for (int trial = 1; trial <= CommonUtil.numTrials; trial++) {
            Engine[] engines = doNaiveParallelAgents(executorService, numAgents, trial);
            long minimumProcessTime = Long.MAX_VALUE;
            int minOptimumEpisode = Integer.MAX_VALUE;
            int totalEpisodes = 0;
            int totalTicks = 0;
            long totalProcessTime = 0L;
            for (Engine engine : engines) {
                long processTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
                minimumProcessTime = Math.min(processTime, minimumProcessTime);
                if (engine.optimumEpisode != 0) {
                    minOptimumEpisode = Math.min(engine.optimumEpisode, minOptimumEpisode);
                }
                totalEpisodes += engine.lastEpisode;
                totalTicks += engine.totalTicks;
                totalProcessTime += (engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime());
            }
            testResults[trial - 1] = new TestResult(totalEpisodes, totalTicks, totalProcessTime);
        }
        return testResults;
   }

    //// STOP WALK
    public Engine[] doStopWalkParallelAgents(ExecutorService executorService, int numAgents, Lock[] locks, int trialNumber) {
        QEntry[][] q = CartpoleUtil.createInitialQ();
        Factory factory = new CartpoleStopWalkFactory(q, locks);
        TickListener listener = new CartpoleParallelAgentsTickListener(trialNumber);
        Engine[] engines = IntStream.range(0, numAgents).mapToObj(i -> new Engine(i, factory, listener)).toArray(Engine[]::new);
        try {
            executorService.invokeAny(Arrays.asList(engines));
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }
        return engines;
    }
    
    public TestResult[] runStopWalkParallelAgents(int numAgents, ExecutorService executorService) {
        int numStates = 163;
        Lock[] locks = IntStream.range(0,  numStates).mapToObj(i -> new ReentrantLock()).toArray(ReentrantLock[]::new);
//        testStopWalkParallelAgents(executorService, numAgents, locks, 0);
//        testStopWalkParallelAgents(executorService, numAgents, locks, 0);
        TestResult[] testResults = new TestResult[CommonUtil.numTrials];
        for (int trial = 1; trial <= CommonUtil.numTrials; trial++) {
            Engine[] engines = doStopWalkParallelAgents(executorService, numAgents, locks, trial);
            long minimumProcessTime = Long.MAX_VALUE;
            int minOptimumEpisode = Integer.MAX_VALUE;
            int totalEpisodes = 0;
            int totalTicks = 0;
            long totalProcessTime = 0L;
            for (Engine engine : engines) {
                long processTime = engine.getTotalProcessTime();// - engine.getAfterEpisodeListenerProcessTime();
                minimumProcessTime = Math.min(processTime, minimumProcessTime);
                if (engine.optimumEpisode != 0) {
                    minOptimumEpisode = Math.min(engine.optimumEpisode, minOptimumEpisode);
                }
                totalEpisodes += engine.lastEpisode;
                totalTicks += engine.totalTicks;
                totalProcessTime += (engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime());
            }
            testResults[trial - 1] = new TestResult(totalEpisodes, totalTicks, totalProcessTime);
        }
        return testResults;
   }
    
    public static void main(String[] args) {
        int minNumAgents = 2;
        int maxNumAgents = 60;
        QLearningAgent.ALPHA = 0.1F;
        QLearningAgent.GAMMA = 0.99F;
        QLearningAgent.EPSILON = 0.1F;
        CommonUtil.numEpisodes = 200_000;
        CartpoleUtil.randomizeStartingPositions = true;
        System.out.println("Cartpole performance test with " + maxNumAgents + " cores\n");
        
        ExecutorService executorService = Executors.newCachedThreadPool();
        CartpolePerformanceTest test = new CartpolePerformanceTest();
        
        IntStream.of(200/*, 500, 1000, 1500*/).forEach(maxTicks -> {
            CommonUtil.MAX_TICKS = maxTicks;
            
            //// SINGLE AGENT
            TestResult[] singleAgentTestResults = test.runSingleAgent();

            int iteration = (maxNumAgents - minNumAgents) / 2 + 1;
            //// NAIVE
            TestResult[][] naiveParallelTestResultsTable = new TestResult[iteration][];
            for (int i = minNumAgents, count = 0; i <= maxNumAgents; i+=2, count++) {
                TestResult[] naiveParallelTestResults = test.runNaiveParallelAgents(i, executorService);
                naiveParallelTestResultsTable[count] = naiveParallelTestResults;
            }
            
            //// STOP WALK
            TestResult[][] stopWalkTestResultsTable = new TestResult[iteration][];
            for (int i = minNumAgents, count = 0; i <= maxNumAgents; i+=2, count++) {
                TestResult[] stopWalkTestResults = test.runStopWalkParallelAgents(i, executorService);
                stopWalkTestResultsTable[count] = stopWalkTestResults;
            }
            
//            for (int i = minNumProcessors; i <= maxNumProcessors; i += 2) {
//                CommonUtil.contentionCount.set(0);
//                CommonUtil.tickCount.set(0);
//                System.out.println("====================================== Start of StopWalk (contention). numAgent "
//                        + i + " ===================================================================");
//                test.runStopWalkParallelAgents(i, executorService);
//                System.out.println("lock contention count:" + CommonUtil.contentionCount.get());
//                System.out.println("tick count:" + CommonUtil.tickCount.get());
//            }
            
            // print results
            System.out.println("MaxTicks: " + maxTicks);
            System.out.println("\nParallel Naive");
            System.out.println(TestUtil.formatTestResults(minNumAgents, maxNumAgents, 
            		singleAgentTestResults, naiveParallelTestResultsTable));
            System.out.println("\nStop Walk");
            System.out.println(TestUtil.formatTestResults(minNumAgents, maxNumAgents, 
            		singleAgentTestResults, stopWalkTestResultsTable));
        	System.out.println("\n");
        });
        executorService.shutdown();
    }
}