package cartpole;

import rl.Environment;
import rl.Result;

public class CartPoleEnvironment extends Environment {
    private static final float GRAVITY = 9.8F;
    private static final float MASSCART = 1.0F;
    private static final float MASSPOLE = 0.1F;
    private static final float TOTAL_MASS = MASSPOLE + MASSCART;
    private static final float LENGTH = 0.5F; /* actually half the pole's length */
    private static final float POLEMASS_LENGTH = MASSPOLE * LENGTH;
    private static final float FORCE_MAG = 10.0F;
    private static final float TAU = 0.02F; /* seconds between state updates */
    private static final float FOURTHIRDS = 1.3333333333333F;
    private static final float ONE_DEGREE = 0.0174532F; /* 2pi/360 */
    private static final float SIX_DEGREES = 0.1047192F;
    private static final float TWELVE_DEGREES = 0.2094384F;
    private static final float FIFTY_DEGREES = 0.87266F;
    private static final int TERMINAL_STATE = 162;
    public float x;
    public float xDot;
    public float theta;
    public float thetaDot;

    @Override
    public Result submit(int state, int action) {
        //System.out.println("action:" + action);
        float force = action == 1 ? FORCE_MAG : -FORCE_MAG;
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);
        double temp = (force + POLEMASS_LENGTH * thetaDot * thetaDot * sinTheta) / TOTAL_MASS;
        double thetaAcc = (GRAVITY * sinTheta - cosTheta * temp)
                / (LENGTH * (FOURTHIRDS - MASSPOLE * cosTheta * cosTheta / TOTAL_MASS));
        double xAcc = temp - POLEMASS_LENGTH * thetaAcc * cosTheta / TOTAL_MASS;

        // Update the four state variables, using Euler's method.
        x += TAU * xDot;
        xDot += TAU * xAcc;
        theta += TAU * thetaDot;
        thetaDot += TAU * thetaAcc;

        int nextState = getBox();
        boolean terminal = nextState == TERMINAL_STATE;
        int reward = terminal ? -1 : 0;
        return new Result(reward, nextState, terminal);
    }

    @Override
    public int getStartState() {
        int startState = getBox();
        return startState;
    }
    
    @Override
    public void reset() {
        System.out.println("CartPoleEnv .reset()");
        x = xDot = theta = thetaDot = 0.0F;
    }
    
    // returns 0..161, or 162 if failing
    private int getBox() {
        //System.out.println("x:" + x + ", theta:" + theta + ", x_dot:" + xDot + ", theta_dot:" + thetaDot);
        if (x < -2.4 || x > 2.4 || theta < -TWELVE_DEGREES || theta > TWELVE_DEGREES) {
            //System.out.println("Terminal. x:" + x + ", theta:" + theta);
            return TERMINAL_STATE; /* signal failure */
        }

        int box = 0;
        if (x < -0.8)
            box = 0;
        else if (x < 0.8)
            box = 1;
        else
            box = 2;

        if (xDot < -0.5)
            ;
        else if (xDot < 0.5)
            box += 3;
        else
            box += 6;

        if (theta < -SIX_DEGREES)
            ;
        else if (theta < -ONE_DEGREE)
            box += 9;
        else if (theta < 0)
            box += 18;
        else if (theta < ONE_DEGREE)
            box += 27;
        else if (theta < SIX_DEGREES)
            box += 36;
        else
            box += 45;

        if (thetaDot < -FIFTY_DEGREES)
            ;
        else if (thetaDot < FIFTY_DEGREES)
            box += 54;
        else
            box += 108;
        return (box);
    }
}