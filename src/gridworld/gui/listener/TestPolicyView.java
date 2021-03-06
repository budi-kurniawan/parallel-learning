package gridworld.gui.listener;

import common.CommonUtil;
import common.QEntry;
import common.agent.QLearningAgent;
import common.event.EpisodeEvent;
import gridworld.GridworldEnvironment;
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
        QEntry[][] q = ((QLearningAgent) event.getSource()).getQ();//event.getQTables().get(event.getAgent().getIndex());
        GridworldEnvironment environment = new GridworldEnvironment();
        QLearningAgent agent = new QLearningAgent(environment, stateActions, q, 1);
        int stepsToGoal = 0;
        while (true) {
            stepsToGoal++;
            int prevState = agent.getState();
            agent.test();
            int state = agent.getState();
            if (agent.isTerminal() || stepsToGoal == CommonUtil.MAX_TICKS) {
                break; // end of episode
            }
        }
        if (agent.getState() == GridworldUtil.getGoalState()
                && stepsToGoal <= GridworldUtil.numCols + GridworldUtil.numRows) {
            policyFound = true;
            drawGrid(gc, leftMargin, topMargin);
            drawTerminalStates(gc, GridworldEnvironment.wells);
            drawPolicy(q);
            writeCaption("Policy at episode " + event.getEpisode());
        }
    }

    private void drawPolicy(QEntry[][] q) {
        int[][] stateActions = GridworldUtil.getStateActions();
        GridworldEnvironment environment = new GridworldEnvironment();
        QLearningAgent agent = new QLearningAgent(environment, stateActions, q, 1);
        int count = 0;
        while (true) {
            count++;
            int prevState = agent.getState();
            agent.test();
            int state = agent.getState();
            if (prevState != Integer.MIN_VALUE) {
                draw(prevState, state);
            }
            if (agent.isTerminal() || count == CommonUtil.MAX_TICKS) {
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