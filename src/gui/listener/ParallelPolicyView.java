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
        QEntry[][] combined = Util.combineQTables(event.getQTables());
        Platform.runLater(() -> {
            clear();
            drawGrid(gc, leftMargin, topMargin);
            drawQ(combined);
        });
    }
    
    @Override
    public void afterTrial(TrialEvent event) {
        caption += "(" + (event.getEndTime() - event.getStartTime()) + "ms) ";
        QEntry[][] combined = Util.combineQTables(event.getQTables());
        Platform.runLater(() -> {
            clear();
            drawGrid(gc, leftMargin, topMargin);
            drawQ(combined);
            writeCaption(caption);
        });
    }
}