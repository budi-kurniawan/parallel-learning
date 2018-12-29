package gui.listener;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import rl.Agent;
import rl.Environment;
import rl.QEntry;
import rl.Util;
import rl.event.EpisodeEvent;

public class TestPolicyView extends LearningView {
    private boolean policyFound = false;
    private int[][] stateActions = Util.getStateActions();
    
    public TestPolicyView(int leftMargin, int topMargin, GraphicsContext gc) {
        super(leftMargin, topMargin, gc);
    }
    
    @Override
    public void beforeEpisode(EpisodeEvent event) {
    }
    
    @Override
    public void afterEpisode(EpisodeEvent event) {
        if (policyFound) {
            return;
        }
        QEntry[][] q = event.getQ();//event.getQTables().get(event.getAgent().getIndex());
        Environment environment = new Environment();
        Agent agent = new Agent(environment, stateActions, q, 1, 1);
        int stepsToGoal = 0;
        while (true) {
            stepsToGoal++;
            int prevState = agent.getState();
            agent.test();
            int state = agent.getState();
//            if (prevState != Integer.MIN_VALUE) {
//                draw(prevState, state);
//            }
            if (agent.terminal || stepsToGoal == Util.MAX_TICKS) {
                break; // end of episode
            }
        }
        if (agent.getState() == Util.getGoalState()
                && stepsToGoal <= Util.numCols + Util.numRows) {
            policyFound = true;
            drawGrid(gc, leftMargin, topMargin);
            drawTerminalStates(gc, Environment.wells);
            drawPolicy(q);
            writeCaption("Policy at episode " + event.getEpisode());
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
                draw(prevState, state);
            }
            if (agent.terminal || count == Util.MAX_TICKS) {
                break; // end of episode
            }
        }
    }
    private void draw(int prevState, int state) {
        int[] rowCol1 = Util.stateToRowColumn(prevState, Util.numCols);
        int[] rowCol2 = Util.stateToRowColumn(state, Util.numCols);
        Platform.runLater(() -> drawLine(gc, 
                rowCol1[0], rowCol1[1], rowCol2[0], rowCol2[1]));
        try {
            Thread.sleep(1);
        } catch (Exception e) {
        }

    }
}