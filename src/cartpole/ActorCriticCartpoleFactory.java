package cartpole;

import common.Environment;
import common.QEntry;

public class ActorCriticCartpoleFactory extends CartpoleFactory {
    public ActorCriticCartpoleFactory(QEntry[][] q) {
        super(q);
    }

    @Override
    public Environment createEnvironment() {
        return new ActorCriticCartpoleEnvironment();
    }

}
