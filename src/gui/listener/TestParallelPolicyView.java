package gui.listener;

import java.util.List;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import rl.Agent;
import rl.Environment;
import rl.QEntry;
import rl.Util;
import rl.event.EpisodeEvent;

public class TestParallelPolicyView extends PolicyView {
    
    private volatile boolean policyFound = false;
    private int[][] stateActions = Util.getStateActions();
    private String caption = "";

    public TestParallelPolicyView(int leftMargin, int topMargin, GraphicsContext gc) {
        super(leftMargin, topMargin, gc);
    }

    @Override
    public void beforeEpisode(EpisodeEvent event) {
    }
    
    @Override
    public void afterEpisode(EpisodeEvent event) {
        if (policyFound) {
            // second agent
            System.out.println(event.getAgent().getIndex() + " interrupted at episode " + event.getEpisode());
            Thread.currentThread().interrupt(); // this interrupts the agent that did NOT find the policy
            return;
        }
        List<QEntry[][]> qTables = event.getQTables();
        QEntry[][] combined = null;
        if (qTables.get(0) == qTables.get(1)) {
            combined = qTables.get(0);
        } else {
            combined = Util.combineQTables(qTables);
        }
        
        Environment environment = new Environment();
        Agent agent = new Agent(environment, stateActions, combined, 1, 1);
        int count = 0;
        while (true) {
            count++;
            int prevState = agent.getState();
            agent.test();
            int state = agent.getState();
//            if (prevState != Integer.MIN_VALUE) {
//                draw(prevState, state);
//            }
            if (agent.terminal || count == Util.MAX_TICKS) {
                break; // end of episode
            }
        }
        if (agent.getState() == Util.getGoalState()
                && count <= Util.numCols + Util.numRows) {
            System.out.println("TestParallelPolicyView. policyFound by " + event.getAgent().getIndex() + " at episode:" + event.getEpisode());
            System.out.println(" agent Id:"  + event.getAgent().getIndex());
            QEntry[][] combinedFinal = combined;
            caption += " policy at episode " + event.getEpisode();
            Platform.runLater(() -> {
                drawGrid(gc, leftMargin, topMargin);
                drawTerminalStates(gc, Environment.wells);
                drawPolicy(combinedFinal);
                writeCaption(caption);
            });
            
            // interrupt the thread running, effectively the agent
            policyFound = true;
            Thread.currentThread().interrupt(); // this will interrupt the agent that found the policy
        }
        
    }
    
    private void drawPolicy(QEntry[][] q) {
        int[][] stateActions = Util.getStateActions();
        Environment environment = new Environment();
        Agent agent = new Agent(environment, stateActions, q, 1, 1);
        int count = 0;
        while (true) {
            count++;
            int prevState = agent.getState();
            agent.test();
            int state = agent.getState();
            if (prevState != Integer.MIN_VALUE) {
                int[] rowCol1 = Util.stateToRowColumn(prevState, Util.numCols);
                int[] rowCol2 = Util.stateToRowColumn(state, Util.numCols);
                Platform.runLater(() -> drawLine(gc, 
                        rowCol1[0], rowCol1[1], rowCol2[0], rowCol2[1]));
            }
            if (agent.terminal || count == Util.MAX_TICKS) {
                break; // end of episode
            }
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }
    }
}