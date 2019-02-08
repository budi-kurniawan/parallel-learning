package cartpole;

import common.Environment;
import common.QEntry;

public class QLearningCartpoleFactory extends CartpoleFactory {
    public QLearningCartpoleFactory(QEntry[][] q) {
        super(q);
    }

    @Override
    public Environment createEnvironment() {
        return new QLearningCartpoleEnvironment();
    }

}
