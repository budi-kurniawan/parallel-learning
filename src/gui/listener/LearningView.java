package gui.listener;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import rl.Environment;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.event.TickEvent;
import rl.listener.EpisodeListener;
import rl.listener.TickListener;

public class LearningView implements TickListener, EpisodeListener {
    public static int cellWidth = 30;
    public static int cellHeight = 30;
    
    protected GraphicsContext gc;
    protected int leftMargin;
    protected int topMargin;
    protected int viewWidth;
    protected int viewHeight;
    
    public LearningView(int leftMargin, int topMargin, GraphicsContext gc) {
        this.leftMargin = leftMargin;
        this.topMargin = topMargin;
        this.gc = gc;
        this.viewWidth = Util.numCols * cellWidth;
        if (Util.numCols > 15) {
            cellWidth = cellHeight = 20;
        }
        if (Util.numCols > 25) {
            cellWidth = cellHeight = 16;
        }
    }
    
    public void setMargins(int leftMargin, int topMargin) {
        this.leftMargin = leftMargin;
        this.topMargin = topMargin;
    }
    
    @Override
    public void afterTick(TickEvent event) {
        int[] rowCol1 = Util.stateToRowColumn(event.getPrevState(), Util.numCols);
        int[] rowCol2 = Util.stateToRowColumn(event.getState(), Util.numCols);
        Platform.runLater(() -> drawLine(gc, 
                rowCol1[0], rowCol1[1], rowCol2[0], rowCol2[1]));
        try {
            Thread.sleep(1);
        } catch (Exception e) {
        }
    }
    
    @Override
    public void beforeEpisode(EpisodeEvent event) {
        Platform.runLater(() -> {
            clear();
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(2);
            drawGrid(gc, leftMargin, topMargin);
            drawTerminalStates(gc, Environment.wells);
            writeCaption("Episode " + event.getEpisode() + " (EPSILON: " + event.getEpsilon() + ")");
        });
    }
    
    @Override
    public void afterEpisode(EpisodeEvent event) {
        try {
            Thread.sleep(85);
        } catch (Exception e) {
        }
    }
    
    protected void clear() {
        gc.clearRect(leftMargin, topMargin, Util.numCols * cellWidth, Util.numRows * cellHeight + Util.CAPTION_HEIGHT);
    }
    
    protected void drawLine(GraphicsContext gc, int row1, int col1, int row2, int col2) {
        gc.setLineWidth(5);
        if (row1 > row2) {
            gc.setStroke(Color.HOTPINK);
        } else if (row1 < row2) {
            gc.setStroke(Color.ORANGE);
        } else if (col1 < col2) {
            gc.setStroke(Color.BLACK);
        } else {
            gc.setStroke(Color.BLUE);
        }
        gc.strokeLine((col1 + 0.5) * cellWidth + leftMargin, (Util.numRows - row1 - 0.5) * cellHeight + topMargin,
                (col2 + 0.5) * cellWidth + leftMargin, (Util.numRows - row2 - 0.5) * cellHeight + topMargin);
    }

    protected void drawGrid(GraphicsContext gc, int leftMargin, int topMargin) {
        // draw horizontal lines
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);
        for (int i = 0; i <= Util.numRows; i++) {
            gc.strokeLine(leftMargin, i * cellHeight + topMargin, Util.numCols * cellWidth + leftMargin,
                    i * cellHeight + topMargin);
        }
        // draw vertical lines
        for (int i = 0; i <= Util.numCols; i++) {
            gc.strokeLine(i * cellWidth + leftMargin, topMargin, i * cellWidth + leftMargin,
                    Util.numRows * cellHeight + topMargin);
        }
        gc.setFill(Color.GREEN);
        gc.fillRect((Util.numCols - 1) * cellWidth + leftMargin, topMargin, cellWidth, cellHeight);
    }
    
    protected void drawTerminalStates(GraphicsContext gc, int[] states) {
        gc.setFill(Color.RED);
        for (int state : states) {
            if (state + 1 > Util.numRows * Util.numCols) {
                break;
            }
            int[] rowCol = Util.stateToRowColumn(state, Util.numCols);
            gc.fillRect(rowCol[1] * cellWidth + leftMargin, (Util.numRows - rowCol[0] - 1) * cellHeight + topMargin, cellWidth, cellHeight);
        }
    }
    
    protected void writeCaption(String text) {
        gc.setLineWidth(1);
        gc.strokeText(text, leftMargin, (Util.numRows + 0.5) * cellHeight + topMargin);
    }
}