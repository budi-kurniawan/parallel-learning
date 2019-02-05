package gridworld;

import common.Environment;
import gridworld.GridworldUtil;

public class AgentTest {
    
    public void testGetExploreExploitAction() {
        GridworldUtil.numRows = 5;
        GridworldUtil.numCols = 5;
        int[] actions = {0, 1, 2, 3};
        int[][] stateActions = new int[1][4];
        stateActions[0] = actions;
        Environment environment = new Environment();
        double[][] q = new double[GridworldUtil.numRows * GridworldUtil.numCols][actions.length];
        q[0][0] = 1.0;
        q[0][1] = 0.0;
        q[0][2] = 0.0;
        q[0][3] = 1.0;
//        Agent agent = new Agent(environment, stateActions, q, 1, 1);
//        
//        for (int i = 0; i < 10; i++) {
//            int result = agent.getExploitAction(0);
//            System.out.println(result); // must return 0 or 3
//        }
    }

    public static void main(String[] args) {
        AgentTest test = new AgentTest();
        test.testGetExploreExploitAction();
    }

}
