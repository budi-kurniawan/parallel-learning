package common;

public interface Environment {
    Result submit(int state, int action);
    int getStartState();
    void reset();
}
