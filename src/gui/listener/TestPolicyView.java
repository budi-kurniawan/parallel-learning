package gui.listener;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import rl.Agent;
import rl.Environment;
import rl.QEntry;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.event.TickEvent;
import rl.event.TrialEvent;

public class TestPolicyView extends LearningView {
    public TestPolicyView(int leftMargin, int topMargin, GraphicsContext gc) {
        super(leftMargin, topMargin, gc);
    }
    
    @Override
    public void afterTrial(TrialEvent event) {
        drawGrid(gc, leftMargin, topMargin);
        drawTerminalStates(gc, Environment.wells);
        QEntry[][] q = event.getQ();
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
