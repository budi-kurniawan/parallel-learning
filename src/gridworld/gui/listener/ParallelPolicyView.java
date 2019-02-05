package gridworld.gui.listener;

import common.QEntry;
import common.event.EpisodeEvent;
import common.event.TrialEvent;
import gridworld.GridworldUtil;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;

public class ParallelPolicyView extends PolicyView {
    protected String caption = "";

    public ParallelPolicyView(int leftMargin, int topMargin, GraphicsContext gc) {
        super(leftMargin, topMargin, gc);
    }

    @Override
    public void afterEpisode(EpisodeEvent event) {
        QEntry[][] combined = GridworldUtil.combineQTables(event.getQTables());
        Platform.runLater(() -> {
            clear();
            drawGrid(gc, leftMargin, topMargin);
            drawQ(combined);
        });
    }
    
    @Override
    public void afterTrial(TrialEvent event) {
        caption += "(" + (event.getEndTime() - event.getStartTime()) + "ms) ";
        QEntry[][] combined = GridworldUtil.combineQTables(event.getQTables());
        Platform.runLater(() -> {
            clear();
            drawGrid(gc, leftMargin, topMargin);
            drawQ(combined);
            writeCaption(caption);
        });
    }
}