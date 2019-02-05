package common;

public class Result {
    public float reward;
    public int nextState;
    public boolean terminal;
    
    public Result(float reward, int nextState, boolean terminal) {
        this.reward = reward;
        this.nextState = nextState;
        this.terminal = terminal;
    }
}
