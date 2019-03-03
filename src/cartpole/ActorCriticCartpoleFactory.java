package cartpole;

import common.Agent;
import common.Environment;

public class ActorCriticCartpoleFactory extends CartpoleFactory {
    public ActorCriticCartpoleFactory() {
        super();
    }

    @Override
    public Environment createEnvironment() {
        return new ActorCriticCartpoleEnvironment();
    }
    
    @Override
    public Agent createAgent(int index, Environment environment, int episode) {
        return null;
    }


}
