package cartpole.gui.listener;

import cartpole.AbstractCartpoleEnvironment;
import common.CommonUtil;
import common.Environment;
import common.event.EpisodeEvent;
import common.event.TickEvent;
import common.event.TrialEvent;
import common.listener.EpisodeListener;
import common.listener.TickListener;
import common.listener.TrialListener;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CartPoleLearningView implements TickListener, EpisodeListener, TrialListener {
    protected GraphicsContext gc;
    private int leftMargin;
    private int topMargin;
    private int WIDTH = 660;
    private int HEIGHT = 320;
    private int CART_WIDTH = 40;
    private int CART_HEIGHT = 40;
    private int POLE_WIDTH = 10;
    private int POLE_HEIGHT = 200;
    private int maxTicks = 0;

    public CartPoleLearningView(GraphicsContext gc, int leftMargin, int topMargin) {
        this.gc = gc;
        this.leftMargin = leftMargin;
        this.topMargin = topMargin;
    }
    
    @Override
    public void afterTick(TickEvent event) {
        int tick = event.getTick();
        if (tick > maxTicks) {
            maxTicks = tick;
        }
        Environment environment = event.getEnvironment();
        if (environment instanceof AbstractCartpoleEnvironment) {
            AbstractCartpoleEnvironment cartPoleEnvironment = (AbstractCartpoleEnvironment) environment;
            Platform.runLater(() -> {
                drawCartPole(cartPoleEnvironment.cartpole.x, cartPoleEnvironment.cartpole.theta);
            });
            try {
                Thread.sleep(8);
            } catch (Exception e) {
            }
            if (event.getTick() == CommonUtil.MAX_TICKS) {
                System.out.println("Goal reached at episode " + event.getEpisode());
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    writeCaption("Goal reached at episode " + event.getEpisode());
                });
            } else if (event.getSource().isTerminal()) {
                System.out.println("Failed at episode " + event.getEpisode() + " after " + tick + " ticks (max: " 
                        + maxTicks + ")");
                animateFailure(cartPoleEnvironment.cartpole.x, cartPoleEnvironment.cartpole.theta, event.getEpisode(), tick);
            }
        }
    }

    private void drawCartPole(float x, float theta) {
        clear();
        float left = leftMargin + (WIDTH - CART_WIDTH) / 2 + x * 50;
        float top = topMargin + HEIGHT - CART_HEIGHT;
        // draw cart
        gc.setFill(Color.GREEN);
        gc.fillRect(left, top, CART_WIDTH, CART_HEIGHT);
        
        // draw pole
        float x1 = (left + CART_WIDTH + left) / 2;
        float y1 = top;
        // x2, y2 without rotation
        float x2 = x1;
        float y2 = top - POLE_HEIGHT;
        // translate (x2, y1) to (0,0)
        x2 -= x1;
        y2 -= y1;
        // rotate and translate back by (x1, y1)
        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);
        double x2b = x2 * cosTheta - y2 * sinTheta + x1;
        double y2b = y2 * cosTheta + x2 * sinTheta + y1;
        gc.setLineWidth(POLE_WIDTH);
        gc.setStroke(Color.RED);
        gc.strokeLine(x1, y1, x2b, y2b);
    }

    private void animateFailure(float x, float theta, int episode, int tick) {
        Platform.runLater(() -> writeCaption("Failed at episode " + episode + " after " + tick + " ticks"));
        for (int i = 1; i < 50; i++) {
            final int count = i;
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
            Platform.runLater(() -> {
                drawCartPole(x, (float) (theta * (1 + count / 8.0F)));
            });
        }
        try {
            Thread.sleep(90);
        } catch (Exception e) {
        }
    }
    
    private void clear() {
        drawGrid(gc, leftMargin, topMargin);
    }
    
    @Override
    public void beforeEpisode(EpisodeEvent event) {
        Platform.runLater(() -> {
            writeCaption("Episode " + event.getEpisode());
        });
    }

    protected void writeCaption(String text) {
        gc.setFill(Color.ANTIQUEWHITE);
        gc.fillRect(leftMargin, topMargin + HEIGHT - 10, WIDTH, 100);
        gc.setLineWidth(1);
        gc.strokeText(text, leftMargin + 5, topMargin + HEIGHT + 20);
    }

    @Override
    public void afterEpisode(EpisodeEvent event) {
    }

    protected void drawGrid(GraphicsContext gc, int leftMargin, int topMargin) {
        gc.setFill(Color.ANTIQUEWHITE);
        gc.fillRect(leftMargin, topMargin, WIDTH, HEIGHT);
    }

    @Override
    public void beforeTrial(TrialEvent event) {
    }

    @Override
    public void afterTrial(TrialEvent event) {
    }    
}