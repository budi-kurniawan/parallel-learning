package cartpole;

import common.Environment;

public abstract class AbstractCartpoleEnvironment implements Environment {
    protected static final int N_BOXES = 162; /* Number of disjoint boxes of state space. */
    private static final float GRAVITY = 9.8F;
    private static final float MASSCART = 1.0F;
    private static final float MASSPOLE = 0.1F;
    private static final float TOTAL_MASS = (MASSPOLE + MASSCART);
    private static final float LENGTH = 0.5F; /* actually half the pole's length */
    private static final float POLEMASS_LENGTH = (MASSPOLE * LENGTH);
    private static final float FORCE_MAG = 10.0F;
    private static final float TAU = 0.02F; /* seconds between state updates */
    private static final float FOURTHIRDS = 1.3333333333333F;
    private static final float one_degree = 0.0174532F; /* 2pi/360 */
    private static final float six_degrees = 0.1047192F;
    private static final float twelve_degrees = 0.2094384F;
    private static final float fifty_degrees = 0.87266F;
    public float x;
    public float xDot;
    public float theta;
    public float thetaDot;
    
    public void updateInternalVariables(int action) {
        float force = (action > 0) ? FORCE_MAG : -FORCE_MAG;
        float cosTheta = (float) Math.cos(theta);
        float sinTheta = (float) Math.sin(theta);
        float temp = (force + POLEMASS_LENGTH * thetaDot * thetaDot * sinTheta) / TOTAL_MASS;
        float thetaacc = (GRAVITY * sinTheta - cosTheta * temp)
                / (LENGTH * (FOURTHIRDS - MASSPOLE * cosTheta * cosTheta / TOTAL_MASS));
        float xacc = temp - POLEMASS_LENGTH * thetaacc * cosTheta / TOTAL_MASS;

        /*** Update the four state variables, using Euler's method. ***/
        x += TAU * xDot;
        xDot += TAU * xacc;
        theta += TAU * thetaDot;
        thetaDot += TAU * thetaacc;
    }
    
    public int getBox(float x, float x_dot, float theta, float theta_dot) {
        int box = 0;
        if (x < -2.4 || x > 2.4 || theta < -twelve_degrees || theta > twelve_degrees)
            return (-1); /* to signal failure */
        if (x < -0.8)
            box = 0;
        else if (x < 0.8)
            box = 1;
        else
            box = 2;

        if (x_dot < -0.5)
            ;
        else if (x_dot < 0.5)
            box += 3;
        else
            box += 6;

        if (theta < -six_degrees)
            ;
        else if (theta < -one_degree)
            box += 9;
        else if (theta < 0)
            box += 18;
        else if (theta < one_degree)
            box += 27;
        else if (theta < six_degrees)
            box += 36;
        else
            box += 45;

        if (theta_dot < -fifty_degrees)
            ;
        else if (theta_dot < fifty_degrees)
            box += 54;
        else
            box += 108;
        return (box);
    }   
}