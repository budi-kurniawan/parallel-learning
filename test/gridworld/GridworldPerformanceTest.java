package gridworld;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import common.CommonUtil;
import common.Engine;
import common.Factory;
import common.QEntry;
import common.TestResult;
import common.TestUtil;
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
    }

    public TestResult[] runSingleAgent() {
//        testSingleAgent();
//        testSingleAgent();
        TestResult[] testResults = new TestResult[CommonUtil.numTrials];
        for (int trial = 1; trial <= CommonUtil.numTrials; trial++) {
            // need to create a new Q table for each trial
            QEntry[][] q = GridworldUtil.createInitialQ();
            Factory factory = new GridworldFactory(q);
            Engine engine = new Engine(factory);
            GridworldSingleAgentEpisodeListener listener = new GridworldSingleAgentEpisodeListener();
            engine.addEpisodeListeners(listener);
            engine.call();
            long processingTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
            testResults[trial - 1] = new TestResult(engine.lastEpisode, engine.totalTicks, processingTime);
        }
        return testResults;
    }
    
    //// NAIVE
    public Engine[] doNaiveParallelAgents(ExecutorService executorService, int numAgents, int trialNumber) {
        QEntry[][] q = GridworldUtil.createInitialQ();
        Factory factory = new GridworldFactory(q);
        EpisodeListener listener = new GridworldParallelAgentsEpisodeListener(trialNumber);
        Engine[] engines = new Engine[numAgents];
        Arrays.setAll(engines, i -> new Engine(i, factory, listener)); // assign a new Engine() to each element in engines
        try {
            executorService.invokeAny(Arrays.asList(engines));
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }
        return engines;
    }
    
    
    public TestResult[] runNaiveParallelAgents(int numAgents, ExecutorService executorService) {
//        doNaiveParallelAgents(executorService, numAgents, 0);
//        doNaiveParallelAgents(executorService, numAgents, 0);
        TestResult[] testResults = new TestResult[CommonUtil.numTrials];
        for (int trial = 1; trial <= CommonUtil.numTrials; trial++) {
            Engine[] engines = doNaiveParallelAgents(executorService, numAgents, trial);
            long minimumProcessTime = Long.MAX_VALUE;
            int totalTicks = 0;
            int totalEpisodes = 0;
            long totalProcessTime = 0L;
            for (Engine engine : engines) {
                long processTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
                minimumProcessTime = Math.min(processTime, minimumProcessTime);
                totalTicks += engine.totalTicks;
                totalEpisodes += engine.lastEpisode;
                totalProcessTime += (engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime());
            }
            testResults[trial - 1] = new TestResult(totalEpisodes, totalTicks, totalProcessTime);
        }
        return testResults;
   }

    //// STOP WALK
    public Engine[] doStopWalkParallelAgents(ExecutorService executorService, int numAgents, Lock[] locks, int trialNumber) {
        QEntry[][] q = GridworldUtil.createInitialQ();
        Factory factory = new GridworldStopWalkFactory(q, locks);
        EpisodeListener listener = new GridworldParallelAgentsEpisodeListener(trialNumber);
        Engine[] engines = IntStream.range(0, numAgents).mapToObj(i -> new Engine(i, factory, listener)).toArray(Engine[]::new);
        try {
            executorService.invokeAny(Arrays.asList(engines));
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }
        return engines;
    }
    
    public TestResult[] runStopWalkParallelAgents(int numAgents, ExecutorService executorService) {
        CommonUtil.canPrintMessage = false;
        int numStates = GridworldUtil.numRows * GridworldUtil.numCols;
        Lock[] locks = IntStream.range(0,  numStates).mapToObj(i -> new ReentrantLock()).toArray(ReentrantLock[]::new);
//        testStopWalkParallelAgents(executorService, numAgents, locks, 0);
//        testStopWalkParallelAgents(executorService, numAgents, locks, 0);
        TestResult[] testResults = new TestResult[CommonUtil.numTrials];
        for (int trial = 1; trial <= CommonUtil.numTrials; trial++) {
            Engine[] engines = doStopWalkParallelAgents(executorService, numAgents, locks, trial);
            long minimumProcessTime = Long.MAX_VALUE;
            int totalTicks = 0;
            int totalEpisodes = 0;
            long totalProcessTime = 0L;
            for (Engine engine : engines) {
                long processTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
                minimumProcessTime = Math.min(processTime, minimumProcessTime);
                totalTicks += engine.totalTicks;
                totalEpisodes += engine.lastEpisode;
                totalProcessTime += (engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime());
            }
            testResults[trial - 1] = new TestResult(totalEpisodes, totalTicks, totalProcessTime);
        }
        return testResults;
   }
    
    public static void main(String[] args) {
        int minNumAgents = 2;
        int maxNumAgents = 60;
        System.out.println("Gridworld performance test with " + maxNumAgents + " cores");
        GridworldUtil.numRows = GridworldUtil.numCols = 100;
        CommonUtil.numEpisodes = 35000;
        ExecutorService executorService = Executors.newCachedThreadPool();
        GridworldPerformanceTest test = new GridworldPerformanceTest();
        
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

        System.out.println("\nParallel Naive");
        System.out.println(TestUtil.formatTestResults(minNumAgents, maxNumAgents, 
                singleAgentTestResults, naiveParallelTestResultsTable));
        System.out.println("\nStop Walk");
        System.out.println(TestUtil.formatTestResults(minNumAgents, maxNumAgents, 
                singleAgentTestResults, stopWalkTestResultsTable));
        System.out.println("\n");
        executorService.shutdown();
    }
}