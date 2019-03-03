package cartpole;

import common.Agent;
import common.Environment;
import common.agent.ActorCriticAgent;

public class ActorCriticCartpoleFactory extends CartpoleFactory {
    private Agent agent = null;
    public ActorCriticCartpoleFactory() {
        super();
    }

    @Override
    public Environment createEnvironment() {
        return new ActorCriticCartpoleEnvironment();
    }
    
    @Override
    public Agent createAgent(int index, Environment environment, int episode) {
        if (agent == null) {
            agent = new ActorCriticAgent(environment, episode);
        }
        return agent;
    }
}
