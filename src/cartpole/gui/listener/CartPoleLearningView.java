package cartpole.gui.listener;

import cartpole.CartPoleEnvironment;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import rl.Environment;
import rl.QEntry;
import rl.event.EpisodeEvent;
import rl.event.TickEvent;
import rl.listener.EpisodeListener;
import rl.listener.TickListener;

public class CartPoleLearningView implements TickListener, EpisodeListener {
    protected GraphicsContext gc;
    private int leftMargin;
    private int topMargin;
    private int WIDTH = 400;
    private int HEIGHT = 400;
    private int CART_WIDTH = 40;
    private int CART_HEIGHT = 40;
    private int POLE_WIDTH = 10;
    private int POLE_HEIGHT = 200;

    public CartPoleLearningView(GraphicsContext gc, int leftMargin, int topMargin) {
        this.gc = gc;
        this.leftMargin = leftMargin;
        this.topMargin = topMargin;
    }
    
//    private float prevX;
//    private float prevTheta;
    
    @Override
    public void afterTick(TickEvent event) {
        Environment environment = event.getEnvironment();
        if (environment instanceof CartPoleEnvironment) {
            CartPoleEnvironment cartPoleEnvironment = (CartPoleEnvironment) environment;
            Platform.runLater(() -> {
                drawCartPole(cartPoleEnvironment.x, cartPoleEnvironment.theta);
            });
            try {
                Thread.sleep(1);
            } catch (Exception e) {
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
    
    private void clear() {
        drawGrid(gc, leftMargin, topMargin);
    }
    
    @Override
    public void beforeEpisode(EpisodeEvent event) {
        Platform.runLater(() -> {
//            clear();
//            drawGrid(gc, leftMargin, topMargin);
            writeCaption("Episode " + event.getEpisode() + " (EPSILON: " + event.getEpsilon() + ")");
        });
    }

    protected void writeCaption(String text) {
        gc.setFill(Color.ANTIQUEWHITE);
        gc.fillRect(leftMargin, topMargin + HEIGHT - 10, WIDTH, 100);
        gc.setLineWidth(1);
        gc.strokeText(text, leftMargin, topMargin + HEIGHT + 30);
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
    
}