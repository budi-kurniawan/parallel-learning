package gui.listener;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import rl.Action;
import rl.Engine;
import rl.Environment;
import rl.QEntry;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.event.TickEvent;

public class PolicyView extends LearningView {
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
    
    protected void drawQ(QEntry[][] q) {
        gc.setLineWidth(2);
        gc.setStroke(Color.ORANGE);
        int numStates = Util.numRows * Util.numCols;
        for (int i = 0; i < numStates - 1; i++) {
            double maxValue = -Double.MAX_VALUE;
            for (int action = 0; action < Engine.actions.length; action++) {
                if (q[i][action].value > maxValue) {
                    maxValue = q[i][action].value;
                }
            }
            int[] rowCol = Util.stateToRowColumn(i, Util.numCols);
            for (int action = 0; action < Engine.actions.length; action++) {
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
        drawTerminalStates(gc, Environment.wells);
        try {
            Thread.sleep(40);
        } catch (Exception e) {
        }
    }
    
    private void drawUpArrow(int row, int col) {
        double x1 = leftMargin + (col + 0.5) * BOX_WIDTH;
        double y1 = topMargin + (Util.numRows - row - 0.5) * BOX_HEIGHT;
        double x2 = x1;
        double y2 = y1 - 0.5 * BOX_HEIGHT + 2;
        gc.strokeLine(x1, y1, x2, y2);
        gc.strokeLine(x2 - 4, y2 + 5, x2, y2);
        gc.strokeLine(x2 + 4, y2 + 5, x2, y2);
    }
    
    private void drawDownArrow(int row, int col) {
        double x1 = leftMargin + (col + 0.5) * BOX_WIDTH;
        double y1 = topMargin + (Util.numRows - row - 0.5) * BOX_HEIGHT;
        double x2 = x1;
        double y2 = y1 + 0.5 * BOX_HEIGHT - 2;
        gc.strokeLine(x1, y1, x2, y2);
        gc.strokeLine(x2 - 4, y2 - 5, x2, y2);
        gc.strokeLine(x2 + 4, y2 - 5, x2, y2);
    }

    private void drawLeftArrow(int row, int col) {
        double x1 = leftMargin + (col + 0.5) * BOX_WIDTH;
        double y1 = topMargin + (Util.numRows - row - 0.5) * BOX_HEIGHT;
        double x2 = x1 - 0.5 * BOX_WIDTH + 2;
        double y2 = y1;
        gc.strokeLine(x1, y1, x2, y2);
        gc.strokeLine(x2 + 5, y1 - 5, x2, y2);
        gc.strokeLine(x2 + 5, y1 + 5, x2, y2);
    }
    
    private void drawRightArrow(int row, int col) {
        double x1 = leftMargin + (col + 0.5) * BOX_WIDTH;
        double y1 = topMargin + (Util.numRows - row - 0.5) * BOX_HEIGHT;
        double x2 = x1 + 0.5 * BOX_WIDTH -2;
        double y2 = y1;
        gc.strokeLine(x1, y1, x2, y2);
        gc.strokeLine(x2 - 5, y1 - 5, x2, y2);
        gc.strokeLine(x2 - 5, y1 + 5, x2, y2);
    }
}