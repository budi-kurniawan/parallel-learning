package gridworld.gui.listener;

import common.Agent;
import common.Environment;
import common.QEntry;
import common.event.EpisodeEvent;
import gridworld.GridworldUtil;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;

public class TestPolicyView extends LearningView {
    private boolean policyFound = false;
    private int[][] stateActions = GridworldUtil.getStateActions();
    
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
            if (agent.terminal || stepsToGoal == GridworldUtil.MAX_TICKS) {
                break; // end of episode
            }
        }
        if (agent.getState() == GridworldUtil.getGoalState()
                && stepsToGoal <= GridworldUtil.numCols + GridworldUtil.numRows) {
            policyFound = true;
            drawGrid(gc, leftMargin, topMargin);
            drawTerminalStates(gc, Environment.wells);
            drawPolicy(q);
            writeCaption("Policy at episode " + event.getEpisode());
        }
    }

    private void drawPolicy(QEntry[][] q) {
        int[][] stateActions = GridworldUtil.getStateActions();
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
            if (agent.terminal || count == GridworldUtil.MAX_TICKS) {
                break; // end of episode
            }
        }
    }
    private void draw(int prevState, int state) {
        int[] rowCol1 = GridworldUtil.stateToRowColumn(prevState, GridworldUtil.numCols);
        int[] rowCol2 = GridworldUtil.stateToRowColumn(state, GridworldUtil.numCols);
        Platform.runLater(() -> drawLine(gc, 
                rowCol1[0], rowCol1[1], rowCol2[0], rowCol2[1]));
        try {
            Thread.sleep(1);
        } catch (Exception e) {
        }

    }
}