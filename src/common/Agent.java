package common;

public interface Agent {
    void tick();
    boolean isTerminal();
    int getState();
    int getIndex();
}
