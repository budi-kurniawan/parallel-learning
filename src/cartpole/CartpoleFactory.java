package cartpole;

import common.Factory;

public abstract class CartpoleFactory implements Factory {
    
    @Override
    public int[] getActions() {
        return CartpoleUtil.actions;
    }
    
    @Override
    public int[][] getStateActions() {
        return CartpoleUtil.getStateActions();
    }
}
