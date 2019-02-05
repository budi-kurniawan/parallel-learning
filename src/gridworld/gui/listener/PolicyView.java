package gridworld.gui.listener;

import common.Action;
import common.AbstractEngine;
import common.QEntry;
import common.event.EpisodeEvent;
import common.event.TickEvent;
import common.event.TrialEvent;
import common.listener.TrialListener;
import gridworld.GridworldEnvironment;
import gridworld.GridworldUtil;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PolicyView extends LearningView implements TrialListener {
    public PolicyView(int leftMargin, int topMargin, GraphicsContext gc) {
        super(leftMargin, topMargin, gc);
    }

    @Override
    public void afterTick(TickEvent event) {
        // do nothing
    }

    @Override
    public void beforeEpisode(EpisodeEvent event) {
        // do nothing
    }
    
    @Override
    public void afterEpisode(EpisodeEvent event) {
        Platform.runLater(() -> {
            clear();
            drawGrid(gc, leftMargin, topMargin);
            drawQ(event.getQ());
        });
    }

    @Override
    public void beforeTrial(TrialEvent event) {
    }
    
    @Override
    public void afterTrial(TrialEvent event) {
        Platform.runLater(() -> {
            clear();
            drawGrid(gc, leftMargin, topMargin);
            drawQ(event.getQ());
            writeCaption("Learning took " + (event.getEndTime() - event.getStartTime()) + "ms");
        });
    }

    protected void drawQ(QEntry[][] q) {
        gc.setLineWidth(2);
        gc.setStroke(Color.ORANGE);
        int numStates = GridworldUtil.numRows * GridworldUtil.numCols;
        for (int i = 0; i < numStates - 1; i++) {
            double maxValue = -Double.MAX_VALUE;
            for (int action = 0; action < GridworldUtil.actions.length; action++) {
                if (q[i][action].value > maxValue) {
                    maxValue = q[i][action].value;
                }
            }
            int[] rowCol = GridworldUtil.stateToRowColumn(i, GridworldUtil.numCols);
            for (int action = 0; action < GridworldUtil.actions.length; action++) {
                if (q[i][action].value == maxValue) {
                    switch(action) {
                    case Action.UP:
                        drawUpArrow(rowCol[0], rowCol[1]);
                        break;
                    case Action.DOWN:
                        drawDownArrow(rowCol[0], rowCol[1]);
                        break;
                    case Action.LEFT:
                        drawLeftArrow(rowCol[0], rowCol[1]);
                        break;
                    case Action.RIGHT:
                        drawRightArrow(rowCol[0], rowCol[1]);
                        break;
                    }
                }
            }
        }
        drawTerminalStates(gc, GridworldEnvironment.wells);
        try {
            Thread.sleep(40);
        } catch (Exception e) {
        }
    }
    
    private void drawUpArrow(int row, int col) {
        double x1 = leftMargin + (col + 0.5) * cellWidth;
        double y1 = topMargin + (GridworldUtil.numRows - row - 0.5) * cellHeight;
        double x2 = x1;
        double y2 = y1 - 0.5 * cellHeight + 2;
        gc.strokeLine(x1, y1, x2, y2);
        gc.strokeLine(x2 - 4, y2 + 5, x2, y2);
        gc.strokeLine(x2 + 4, y2 + 5, x2, y2);
    }
    
    private void drawDownArrow(int row, int col) {
        double x1 = leftMargin + (col + 0.5) * cellWidth;
        double y1 = topMargin + (GridworldUtil.numRows - row - 0.5) * cellHeight;
        double x2 = x1;
        double y2 = y1 + 0.5 * cellHeight - 2;
        gc.strokeLine(x1, y1, x2, y2);
        gc.strokeLine(x2 - 4, y2 - 5, x2, y2);
        gc.strokeLine(x2 + 4, y2 - 5, x2, y2);
    }

    private void drawLeftArrow(int row, int col) {
        double x1 = leftMargin + (col + 0.5) * cellWidth;
        double y1 = topMargin + (GridworldUtil.numRows - row - 0.5) * cellHeight;
        double x2 = x1 - 0.5 * cellWidth + 2;
        double y2 = y1;
        gc.strokeLine(x1, y1, x2, y2);
        gc.strokeLine(x2 + 5, y1 - 5, x2, y2);
        gc.strokeLine(x2 + 5, y1 + 5, x2, y2);
    }
    
    private void drawRightArrow(int row, int col) {
        double x1 = leftMargin + (col + 0.5) * cellWidth;
        double y1 = topMargin + (GridworldUtil.numRows - row - 0.5) * cellHeight;
        double x2 = x1 + 0.5 * cellWidth -2;
        double y2 = y1;
        gc.strokeLine(x1, y1, x2, y2);
        gc.strokeLine(x2 - 5, y1 - 5, x2, y2);
        gc.strokeLine(x2 - 5, y1 + 5, x2, y2);
    }
}