package cartpole.gui.listener;

import cartpole.CartpoleEnvironment;
import common.Agent;
import common.CommonUtil;
import common.Environment;
import common.QEntry;
import common.event.EpisodeEvent;
import common.event.TickEvent;
import common.event.TrialEvent;
import common.listener.EpisodeListener;
import common.listener.TickListener;
import common.listener.TrialListener;
import gridworld.GridworldEnvironment;
import gridworld.GridworldUtil;
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
    private QEntry[][] q;

    public CartPoleLearningView(GraphicsContext gc, int leftMargin, int topMargin, QEntry[][] q) {
        this.gc = gc;
        this.leftMargin = leftMargin;
        this.topMargin = topMargin;
        this.q = q;
    }
    
    @Override
    public void afterTick(TickEvent event) {
        Environment environment = event.getEnvironment();
        if (environment instanceof CartpoleEnvironment) {
            CartpoleEnvironment cartPoleEnvironment = (CartpoleEnvironment) environment;
            Platform.runLater(() -> {
                drawCartPole(cartPoleEnvironment.x, cartPoleEnvironment.theta);
            });
            try {
                Thread.sleep(8);
            } catch (Exception e) {
            }
            
            if (event.getTick() == CommonUtil.MAX_TICKS) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    writeCaption("Goal reached on episode " + event.getEpisode());
                });
            } else if (event.getSource().terminal) {
                animateFailure(cartPoleEnvironment.x, cartPoleEnvironment.theta, event.getEpisode(), event.getTick());
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
        double alpha = theta;
        //double r1 = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
        // translate (x2, y1) to (0,0)
        x2 -= x1;
        y2 -= y1;
        
        // rotate and translate back by (x1, y1)
        double x2b = x2 * Math.cos(alpha) - y2 * Math.sin(alpha) + x1;
        double y2b = y2 * Math.cos(alpha) + x2 * Math.sin(alpha) + y1;
        
//        System.out.println("x:" + x + ", theta:" + theta + ", (" + x1 + ", " + y1 + ") " + "  (" + x2 + ", " + y2 + ") -> (" + x2b + ", " + y2b + ")");
//        System.out.println("r1:" + r1 + ", r2:" + Math.sqrt((x1-x2b)*(x1-x2b) + (y1-y2b)*(y1-y2b)));
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
                drawCartPole(x, (float) (theta * (1 + count / 10.0F)));
            });
        }
        try {
            Thread.sleep(120);
        } catch (Exception e) {
        }
    }
    
    private void clear() {
        drawGrid(gc, leftMargin, topMargin);
    }
    
    @Override
    public void beforeEpisode(EpisodeEvent event) {
        Platform.runLater(() -> {
            writeCaption("Episode " + event.getEpisode() + " (EPSILON: " + event.getEpsilon() + ")");
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
//        int episode = event.getEpisode();
//        if (episode % 200 == 0) {
//            QEntry[][] q = event.getQ();
//            for (int i = 0; i < 163; i++) {
//                try {
//                    System.out.println("q[" + i + "].value : (" + q[i][0].value + ", " + q[i][1].value + ")");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    protected void drawGrid(GraphicsContext gc, int leftMargin, int topMargin) {
        gc.setFill(Color.ANTIQUEWHITE);
        gc.fillRect(leftMargin, topMargin, WIDTH, HEIGHT);
    }

    @Override
    public void beforeTrial(TrialEvent event) {
        System.out.println("Before trial");
    }

    @Override
    public void afterTrial(TrialEvent event) {
        System.out.println("After trial");
        System.out.println("Q:" + q.length);
        CartpoleEnvironment environment = new CartpoleEnvironment();
        int[] actions = {0, 1};
        int[][] stateActions = new int[163][2];
        for (int i = 0; i < 163; i++) {
            stateActions[i] = actions;
        }
        Agent agent = new Agent(environment, stateActions, q, 1);
        int tick = 0;
        while (true) {
            tick++;
            int prevState = agent.getState();
            agent.test();
            int state = agent.getState();
            Platform.runLater(() -> {
                drawCartPole(environment.x, environment.theta);
            });
            int count = tick;
            if (agent.terminal) {
                Platform.runLater(() -> writeCaption("Test failed after " + count + " ticks"));
                break;
            }
            try {
                Thread.sleep(8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }    
}