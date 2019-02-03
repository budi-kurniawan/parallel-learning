package cartpole;

import rl.Result;

public class Environment {
    private float x;
    private float xDot;
    private float theta;
    private float thetaDot;
    // state: 0..161
    

    public Environment() {
    }

    public Result submit(int state, int action) {
        int nextState = 0;
        int reward = 1;
        boolean terminal = reward == 1 || reward == -1;
        return new Result(reward, nextState, terminal);
    }
    
    // returns 0..161, or -1 if failing
    private int getState()  {
        // x, xDot, theta, thetaDot: 3 x 3 x 6 x 3 = 162
        // x * 54 + xDot * 18 + theta * 3 + thetaDot
        // 0-0-0-0 : 0
        // 0-0-1-0 : 3
        // 0-0-2-0 : 6
        // 0-0-8-0 : 24
        return discretizeX(x) * 54 + discretizeXDot(xDot) * 18 + discretizeTheta(theta) * 3 + discretizeThetaDot(thetaDot);
    }
    
    private int discretizeX(float x) {
        if (x >= -2.4F && x < -0.8) {
            return 0;
        } else if (x >= -.8F && x <= .8F) {
            return 1;
        } else if (x > .8F && x <= 2.4F) {
            return 2;
        }
        System.err.println("Invalid x:" + x);
        return Integer.MIN_VALUE;
    }
    private int discretizeXDot(float xDot) {
        if (xDot < -.5F) {
            return 0;
        } else if (xDot >= -.5F && xDot <= .5F) {
            return 1;
        } else {
            return 2;
        }
    }
    private int discretizeTheta(float theta) {
        //0, 1, 6, 12
        if (theta >= -12 && theta < -6) {
            return 0;
        } else if (theta >= -6 && theta < -1) {
            return 1;
        } else if (theta >= -1 && theta < 0) {
            return 2;
        } else if (theta >= 0 && theta < 1) {
            return 3;
        } else if (theta >= 1 && theta < 6) {
            return 4;
        } else if (theta >= 6 && theta < 12) {
            return 5;
        } else {
            System.err.println("Invalid theta: + theta);");
            return Integer.MIN_VALUE;
        }
    }
    private int discretizeThetaDot(float thetaDot) {
        if (thetaDot < -50) {
            return 0;
        } else if (thetaDot >= -50 && thetaDot <= 50) {
            return 1;
        } else {
            return 2; // thetaDot > 50
        }
    }
}