package common;

/*
 * Injected to an Engine, an instance of Factory is responsible for creating objects needed by the Engine
 * 
 */
public interface Factory {
    int[] getActions();
    Environment createEnvironment();
    int[][] getStateActions();
    Agent createAgent(int index, Environment environment, int episode);
}
