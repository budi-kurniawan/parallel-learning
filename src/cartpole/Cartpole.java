package cartpole;

public class Cartpole {
    private static final float GRAVITY = 9.8F;
    private static final float MASSCART = 1.0F;
    private static final float MASSPOLE = 0.1F;
    private static final float LENGTH = 0.5F; /* actually half the pole's length */
    private static final float TOTAL_MASS = (MASSPOLE + MASSCART);
    private static final float POLEMASS_LENGTH = (MASSPOLE * LENGTH);
    private static final float FORCE_MAG = 10.0F;
    private static final float FORCE_HALF = 5.0F;
    private static final float TAU = 0.02F; /* seconds between state updates */
    private static final float FOURTHIRDS = 1.3333333333333F;
    private static final float ONE_DEGREE = 0.0174532F; /* 2pi/360 */
    private static final float SIX_DEGREES = 0.1047192F;
    private static final float TWELVE_DEGREES = 0.2094384F;
    private static final float FIFTY_DEGREES = 0.87266F;

    public float x;         // cart position in meters
    public float xDot;     // cart velocity
    public float theta;     // pole angle in radians
    public float thetaDot; // pole angular velocity
    
    public void reset() {
        x = xDot = theta = thetaDot = 0;
    }
    
    public void applyAction(int action) {
        float force = 0F;// = action > 0 ? FORCE_MAG : -FORCE_MAG;
        switch(action) {
        case 0:
            force = -FORCE_MAG;
            break;
        case 1:
            force = FORCE_MAG;
            break;
        case 2:
            force = -FORCE_HALF;
            break;
        case 3:
            force = FORCE_HALF;
            break;
        }
        float cosTheta = (float) Math.cos(theta);
        float sinTheta = (float) Math.sin(theta);
        float temp = (force + POLEMASS_LENGTH * thetaDot * thetaDot * sinTheta) / TOTAL_MASS;
        float thetaacc = (GRAVITY * sinTheta - cosTheta * temp)
                / (LENGTH * (FOURTHIRDS - MASSPOLE * cosTheta * cosTheta / TOTAL_MASS));
        float xacc = temp - POLEMASS_LENGTH * thetaacc * cosTheta / TOTAL_MASS;

        x += TAU * xDot;
        xDot += TAU * xacc;
        theta += TAU * thetaDot;
        thetaDot += TAU * thetaacc;
    }

    /*----------------------------------------------------------------------
    get_box:  Given the current state, returns a number from 0 to 161 or -1
      designating the region of the state space encompassing the current state.
      Returns a value of -1 if a failure state is encountered.
    ----------------------------------------------------------------------*/
    public int getBox() {
        if (x < -2.4 || x > 2.4 || theta < -TWELVE_DEGREES || theta > TWELVE_DEGREES) {
            return -1; // signal failure
        }
        int discretizedX = x < -0.8 ? 0 : (x < 0.8 ? 1 : 2);
        int discretizedXDot = xDot < -0.5 ? 0 : (xDot < 0.5 ? 1 : 2);
        int discretizedThetaDot = thetaDot < -FIFTY_DEGREES ? 0 : (thetaDot < FIFTY_DEGREES ? 1 : 2);
        int discretizedTheta;
        if (theta < -SIX_DEGREES) {
            discretizedTheta = 0;
        } else if (theta < -ONE_DEGREE) {
            discretizedTheta = 1;
        } else if (theta < 0) {
            discretizedTheta = 2;
        } else if (theta < ONE_DEGREE) {
            discretizedTheta = 3;
        } else if (theta < SIX_DEGREES) {
            discretizedTheta = 4;
        } else {
            discretizedTheta = 5;
        }
        return (discretizedTheta * 3 * 3 * 3 + discretizedThetaDot * 3 * 3 + discretizedXDot * 3 + discretizedX);
    }
}