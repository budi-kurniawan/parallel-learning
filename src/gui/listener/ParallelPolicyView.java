package gui.listener;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import rl.QEntry;
import rl.Util;
import rl.event.EpisodeEvent;
import rl.event.TrialEvent;

public class ParallelPolicyView extends PolicyView {
    protected String caption = "";

    public ParallelPolicyView(int leftMargin, int topMargin, GraphicsContext gc) {
        super(leftMargin, topMargin, gc);
    }

    @Override
    public void afterEpisode(EpisodeEvent event) {
        int numStates = Util.numRows * Util.numCols;
        QEntry[][] q = event.getQ();
        QEntry[][] other = event.getOtherQ();
        QEntry[][] combined = new QEntry[numStates][Util.actions.length];
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
        Platform.runLater(() -> {
            clear();
            drawGrid(gc, leftMargin, topMargin);
            drawQ(combined);
        });
    }
    
    @Override
    public void afterTrial(TrialEvent event) {
        caption += "(" + (event.getEndTime() - event.getStartTime()) + "ms) ";
        Platform.runLater(() -> {
            clear();
            drawGrid(gc, leftMargin, topMargin);
            drawQ(event.getQ());
            writeCaption(caption);
        });
    }
}