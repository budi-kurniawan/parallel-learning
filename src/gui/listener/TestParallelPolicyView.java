package gui.listener;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import rl.Agent;
import rl.Environment;
import rl.QEntry;
import rl.Util;
import rl.event.EpisodeEvent;

public class TestParallelPolicyView extends PolicyView {
    
    private volatile boolean policyFound = false;
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
            System.out.println(event.getAgent().getId() + " interrupted at episode " + event.getEpisode());
            Thread.currentThread().interrupt(); // this interrupts the agent that did NOT find the policy
            return;
        }
        int numStates = Util.numRows * Util.numCols;
        QEntry[][] q = event.getQ();
        QEntry[][] other = event.getOtherQ();
        QEntry[][] combined = new QEntry[numStates][Util.actions.length];
        // combine q and other
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < Util.actions.length; j++) {
                combined[i][j] = new QEntry();
                QEntry q1 = q[i][j];
                QEntry q2 = other[i][j];
                if (q1.value == -Double.MAX_VALUE) {
                    combined[i][j].value = -Double.MAX_VALUE;
                } else {
                    if (q1.counter == 0 && q2.counter == 0) {
                        combined[i][j].value = 0;
                    } else {
                        combined[i][j].value = (q1.value * q1.counter + q2.value * q2.counter) / (q1.counter + q2.counter);
                    }
                }
            }
        }
        
        int[][] stateActions = Util.getStateActions(Util.numRows, Util.numCols);
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
        if (agent.getState() == Util.numCols * Util.numRows - 1 
                && count <= Util.numCols + Util.numRows) {
            System.out.println("TestParallelPolicyView. policyFound by " + event.getAgent().getId() + " at episode:" + event.getEpisode());
            System.out.println(" agent Id:"  + event.getAgent().getId());
            Platform.runLater(() -> {
                drawGrid(gc, leftMargin, topMargin);
                drawTerminalStates(gc, Environment.wells);
                drawPolicy(combined);
                writeCaption("Policy found at episode " + event.getEpisode());
            });
            
            // interrupt the thread running, effectively the agent
            policyFound = true;
            Thread.currentThread().interrupt(); // this will interrupt the agent that found the policy
        }
        
    }
    
    private void drawPolicy(QEntry[][] q) {
        int[][] stateActions = Util.getStateActions(Util.numRows, Util.numCols);
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