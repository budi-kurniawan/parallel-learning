package q;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import rl.Agent;
import rl.Engine;
import rl.Environment;
import rl.QEntry;
import rl.StateAction;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.listener.EpisodeListener;
import rl.parallel.ParallelEngine;

public class PerformanceTest {
    
    public void testSingleAgent() {
        Engine engine = new Engine();
        engine.run();
    }
    
    public void testParallelAgents(ExecutorService executorService) {
        QEntry[][] q1 = Util.createInitialQ(Util.numRows,  Util.numCols);
        QEntry[][] q2 = Util.createInitialQ(Util.numRows,  Util.numCols);
        List<QEntry[][]> qTables = new ArrayList<>();
        qTables.add(q1);
        qTables.add(q2);
        ParallelEngine parallelEngine1 = new ParallelEngine(0, qTables);
        ParallelEngine parallelEngine2 = new ParallelEngine(1, qTables);

        CountDownLatch latch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        EpisodeListener listener = new EpisodeListener() {
            volatile boolean policyFound = false;
            @Override
            public void beforeEpisode(EpisodeEvent event) {
            }
            
            @Override
            public void afterEpisode(EpisodeEvent event) {
                if (policyFound) {
                    return;
                }
                int agentIndex = event.getAgent().getIndex();
                QEntry[][] qTable = event.getQTables().get(agentIndex);
                Environment environment = new Environment();
                int[][] stateActions = Util.getStateActions(Util.numRows, Util.numCols);
                Agent agent = new Agent(environment, stateActions, qTable, 1, 1);
                int maxStepsAllowed = Util.numCols + Util.numRows - 1;
                List<StateAction> steps = new ArrayList<>();
                
                int stepsToGoal = 0;
                while (stepsToGoal < maxStepsAllowed) {
                    stepsToGoal++;
                    int prevState = agent.getState();
                    agent.test();
                    int action = agent.getAction();
                    if (prevState != Integer.MIN_VALUE) {
                        steps.add(new StateAction(prevState, action));
                    }
                    //int state = agent.getState();
                }
                //System.out.println("steps to Goal of agent " + agentIndex + ": " + stepsToGoal + ", agent in state:" + agent.getState());
                if (agent.getState() == Util.getGoalState()) {
                    // policy found
                    policyFound = true;
                    for (Future<?> future : futures) {
                        future.cancel(true);
                    }
                    latch.countDown();
                    System.out.println("Policy found by agent " + agent.getIndex() + " at episode " + event.getEpisode());
                    for (StateAction step : steps) {
                        System.out.print("(" + step.state + ", " + step.action + "), ");
                    }
                    System.out.println("(" + agent.getState() + ")");
                }
            }
        };
        parallelEngine1.addEpisodeListeners(listener);
        parallelEngine2.addEpisodeListeners(listener);
        Future<?> future1 = executorService.submit(parallelEngine1);
        Future<?> future2 = executorService.submit(parallelEngine2);
        futures.add(future1);
        futures.add(future2);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        try {
//            future1.get();
//            future2.get();
//        } catch (Exception e) {
//            
//        }
    }
    
    public static void main(String[] args) {
        
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Util.numRows = 8;
        Util.numCols = 8;
        Util.numEpisodes = 5000;
        PerformanceTest test = new PerformanceTest();
        
        long t1 = System.currentTimeMillis();
        test.testSingleAgent();
        long t2 = System.currentTimeMillis();
        System.out.println("Single agent learning took: " + (t2 - t1));

        t1 = System.currentTimeMillis();
        test.testParallelAgents(executorService);
        t2 = System.currentTimeMillis();
        System.out.println("Parallel agents learning took : " + (t2 - t1));
        
        executorService.shutdown();
    
    }
}