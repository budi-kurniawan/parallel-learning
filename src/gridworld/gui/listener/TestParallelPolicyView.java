package gridworld.gui.listener;

import common.CommonUtil;
import common.QEntry;
import common.agent.QLearningAgent;
import common.event.EpisodeEvent;
import gridworld.GridworldEnvironment;
import gridworld.GridworldUtil;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;

public class TestParallelPolicyView extends PolicyView {
    
    private volatile boolean policyFound = false;
    private int[][] stateActions = GridworldUtil.getStateActions();
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
            System.out.println(event.getSource().getIndex() + " interrupted at episode " + event.getEpisode());
            Thread.currentThread().interrupt(); // this interrupts the agent that did NOT find the policy
            return;
        }
        GridworldEnvironment environment = new GridworldEnvironment();
        QEntry[][] q = ((QLearningAgent) event.getSource()).getQ();
        QLearningAgent agent = new QLearningAgent(environment, stateActions, q, 1);
        int count = 0;
        while (true) {
            count++;
            int prevState = agent.getState();
            agent.test();
            int state = agent.getState();
            if (agent.isTerminal() || count == CommonUtil.MAX_TICKS) {
                break; // end of episode
            }
        }
        if (agent.getState() == GridworldUtil.getGoalState()
                && count <= GridworldUtil.numCols + GridworldUtil.numRows) {
            System.out.println("TestParallelPolicyView. policyFound by " + event.getSource().getIndex() + " at episode:" + event.getEpisode());
            System.out.println(" agent Id:"  + event.getSource().getIndex());
            caption += " policy at episode " + event.getEpisode();
            Platform.runLater(() -> {
                drawGrid(gc, leftMargin, topMargin);
                drawTerminalStates(gc, GridworldEnvironment.wells);
                drawPolicy(q);
                writeCaption(caption);
            });
            
            // interrupt the thread running, effectively the agent
            policyFound = true;
            Thread.currentThread().interrupt(); // this will interrupt the agent that found the policy
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
                int[] rowCol1 = GridworldUtil.stateToRowColumn(prevState, GridworldUtil.numCols);
                int[] rowCol2 = GridworldUtil.stateToRowColumn(state, GridworldUtil.numCols);
                Platform.runLater(() -> drawLine(gc, 
                        rowCol1[0], rowCol1[1], rowCol2[0], rowCol2[1]));
            }
            if (agent.isTerminal() || count == CommonUtil.MAX_TICKS) {
                break; // end of episode
            }
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }
    }
}