package q;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import q.listener.SingleAgentEpisodeListener;
import rl.Engine;
import rl.QEntry;
import rl.StateAction;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.listener.EpisodeListener;
import rl.parallel.stopwalk.StopWalkEngine;

public class StopClockPerformanceTest {
    
    public void testSingleAgent(ExecutorService executorService) {
        Engine engine = new Engine();
        SingleAgentEpisodeListener listener = new SingleAgentEpisodeListener();
        engine.addEpisodeListeners(listener);
        engine.call();
        System.out.println("single engine total process time:" + engine.getTotalProcessTime());
        System.out.println("listener total process time:" + listener.getTotalProcessTime());
    }
    
    public StopWalkEngine[] testParallelAgents(ExecutorService executorService, int numAgents, Lock[] locks, int trialNumber) {
        QEntry[][] q = Util.createInitialQ(Util.numRows, Util.numCols);
        StopWalkEngine[] stopWalkEngines = new StopWalkEngine[numAgents];
        EpisodeListener listener = new EpisodeListener() {
            volatile boolean policyFound = false;
            @Override
            public void beforeEpisode(EpisodeEvent event) {
            }
            
            @Override
            public void afterEpisode(EpisodeEvent event) {
                if (event.getEpisode() == Util.numEpisodes) {
                    return;
                }
                if (policyFound) {
                    return;
                }
                int agentIndex = event.getAgent().getIndex();
                QEntry[][] qTable = event.getQ();
                List<StateAction> steps = new ArrayList<>();
                if (Util.policyFound(qTable, steps)) {
                    // policy found
                    policyFound = true;
                    
                    StringBuilder sb = new StringBuilder(1000);
                    sb.append("Policy found by agent " + agentIndex + " at episode " + event.getEpisode() + " (trial #" + trialNumber + ")\n");
//                    for (StateAction step : steps) {
//                        sb.append("(" + step.state + ", " + step.action + "), ");
//                    }
//                    sb.append("(" + Util.getGoalState() + ")\n");
                    System.out.println(sb.toString());
                    Thread.currentThread().interrupt();
                }
            }
        };
        
        for (int i = 0; i < numAgents; i++) {
            stopWalkEngines[i] = new StopWalkEngine(i, q, locks);
            stopWalkEngines[i].addEpisodeListeners(listener);
        }
        try {
            executorService.invokeAny(Arrays.asList(stopWalkEngines));
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }

        for (int i = 0; i < numAgents; i++) {
            StopWalkEngine engine = stopWalkEngines[i];
            long totalProcessTime = engine.getTotalProcessTime();
            long totalAfterEpisodeTime = engine.getAfterEpisodeListenerProcessTime();
            System.out.println("engine processing time:" + (totalProcessTime - totalAfterEpisodeTime) / 1_000_000 
                    + " (" + totalProcessTime / 1_000_000 + " - " + totalAfterEpisodeTime / 1_000_000 + ")");
        }
        
        return stopWalkEngines;
        
    }
    
    public static void runSingleAgent(ExecutorService executorService) {
        Util.canPrintMessage = false;
        StopClockPerformanceTest test = new StopClockPerformanceTest();

        // warm up
        test.testSingleAgent(executorService);
        test.testSingleAgent(executorService);
        
        Util.canPrintMessage = true;
        System.out.println("===== Single agent warm-up done ");

        long totalProcessingTime = 0;
        long totalAfterEpisodeListenerProcessingTime = 0;
        
        for (int i = 0; i < Util.numTrials; i++) {
            Engine engine = new Engine();
            SingleAgentEpisodeListener listener = new SingleAgentEpisodeListener();
            engine.addEpisodeListeners(listener);
            engine.call();
            totalProcessingTime += engine.getTotalProcessTime();
            totalAfterEpisodeListenerProcessingTime += engine.getAfterEpisodeListenerProcessTime();
        }            
        System.out.println("total processing time:" + totalProcessingTime);
        System.out.println("total after episode listener processing time:" + totalAfterEpisodeListenerProcessingTime);
        System.out.println("Avg single engine:" + (totalProcessingTime - totalAfterEpisodeListenerProcessingTime) / 1_000_000 / Util.numTrials + "ms");
        
    }
    
    public static void runParallelAgents(int numAgents, ExecutorService executorService) {
        Util.countContention = false;
        Util.canPrintMessage = false;
        StopClockPerformanceTest test = new StopClockPerformanceTest();

        int numStates = Util.numRows * Util.numCols;
        Lock[] locks = new Lock[numStates];
        for (int i = 0; i < numStates; i++) {
            locks[i] = new ReentrantLock();
        }
        Util.canPrintMessage = false;
        test.testParallelAgents(executorService, numAgents, locks, 0);
        test.testParallelAgents(executorService, numAgents, locks, 0);
        Util.canPrintMessage = true;
        Util.printMessage("===== Parallel warm-up done");
        long totalProcessingTime = 0L;
        for (int i = 0; i < Util.numTrials; i++) {
            Engine[] engines = test.testParallelAgents(executorService, numAgents, locks, i + 1);
            long minimumProcessTime = Long.MAX_VALUE;
            for (Engine engine : engines) {
                long processTime = engine.getTotalProcessTime() - engine.getAfterEpisodeListenerProcessTime();
                if (processTime < minimumProcessTime) {
                    minimumProcessTime = processTime;
                }
            }
            totalProcessingTime += minimumProcessTime;
        }
        
        System.out.println("Avg processing time:" + totalProcessingTime / 1_000_000 / Util.numTrials + "ms");

        System.out.println();

        executorService.shutdownNow();
        
        System.out.println("lock contention count:" + Util.contentionCount.get());
        System.out.println("tick count:" + Util.tickCount.get());
    
   }
    
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(500);
        Util.numRows = Util.numCols = 100;
        Util.numEpisodes = 35000;

        runSingleAgent(executorService);
        System.out.println("====================================== End of Single Agent ======================================================================");

        for (int i = 2; i <= 8; i+=2) {
            executorService = Executors.newFixedThreadPool(500);
            System.out.println("====================================== Start of numAgent " + i + " ===================================================================");
            runParallelAgents(i, executorService);
            System.out.println("====================================== End of numAgent " + i + " ======================================================================");
        }
    }
}