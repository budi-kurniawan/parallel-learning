package gridworld.gui.listener;

import common.QEntry;
import common.agent.QLearningAgent;
import common.event.EpisodeEvent;
import common.event.TrialEvent;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;

public class ParallelPolicyView extends PolicyView {
    protected String caption = "";

    public ParallelPolicyView(int leftMargin, int topMargin, GraphicsContext gc) {
        super(leftMargin, topMargin, gc);
    }

    @Override
    public void afterEpisode(EpisodeEvent event) {
        QLearningAgent agent = (QLearningAgent) event.getSource();
        QEntry[][] q = agent.getQ();
        Platform.runLater(() -> {
            clear();
            drawGrid(gc, leftMargin, topMargin);
            drawQ(q);
        });
    }
    
    @Override
    public void afterTrial(TrialEvent event) {
        caption += "(" + (event.getEndTime() - event.getStartTime()) + "ms) ";
        QLearningAgent agent = (QLearningAgent) event.getSource();
        Platform.runLater(() -> {
            clear();
            drawGrid(gc, leftMargin, topMargin);
            drawQ(agent.getQ());
            writeCaption(caption);
        });
    }
}