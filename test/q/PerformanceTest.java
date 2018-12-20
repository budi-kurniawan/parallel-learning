package q;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import rl.Engine;
import rl.QEntry;
import rl.Util;
import rl.parallel.ParallelEngine;

public class PerformanceTest {
    
    public void testSingleAgent() {
        Engine engine = new Engine();
        engine.run();
    }
    
    public void testParallelAgents(ExecutorService executorService) {
        QEntry[][] q1 = Util.createInitialQ(Util.numRows,  Util.numCols);
        QEntry[][] q2 = Util.createInitialQ(Util.numRows,  Util.numCols);
        ParallelEngine parallelEngine1 = new ParallelEngine(q1, q2);
        ParallelEngine parallelEngine2 = new ParallelEngine(q2, q1);
        Future<?> future1 = executorService.submit(parallelEngine1);
        Future<?> future2 = executorService.submit(parallelEngine2);
        try {
            future1.get();
            future2.get();
        } catch (Exception e) {
            
        }
    }
    
    public static void main(String[] args) {
        
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Util.numRows = 30;
        Util.numCols = 30;
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