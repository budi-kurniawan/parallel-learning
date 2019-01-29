package rl.parallel.stopwalk;

import java.util.concurrent.locks.Lock;

import rl.Agent;
import rl.Environment;
import rl.QEntry;
import rl.Result;
import rl.Util;

public class StopWalkAgent extends Agent {
    private Lock[] locks;
    
    public StopWalkAgent(int agentIndex, Environment environment, int[][] stateActions, QEntry[][] q, int episode, int numEpisodes, Lock[] locks) {
        super(environment, stateActions, q, episode, numEpisodes);
        this.index = agentIndex;
        this.locks = locks;
    }

    @Override
    protected void learn() {
//        Util.tickCount.incrementAndGet();
        if (state == Integer.MIN_VALUE) {
            state = 0;
        } else {
            int prevState = state;
            action = getExploreExploitAction(prevState); 
            Result result = environment.submit(prevState, action);
            this.state = result.nextState;
            this.reward = result.reward;
            this.terminal = result.terminal;
            Lock lock1 = null;
            Lock lock2 = null;
            
            if (prevState < state) {
                lock1 = locks[prevState];
                lock2 = locks[state];
            } else {
                lock1 = locks[state];
                lock2 = locks[prevState];
            }
            
//            if (!lock1.tryLock()) {
//                Util.contentionCount.incrementAndGet();
//            } else {
//                lock1.unlock();
//            }
            
            lock1.lock();
//            if (!lock2.tryLock()) {
//                Util.contentionCount.incrementAndGet();
//            } else {
//                lock2.unlock();
//            }
            
            lock2.lock();
            try {
                double oldValue = getQValue(prevState, action);
                double newValue = (1 - ALPHA) * oldValue + ALPHA * (reward + GAMMA * getMaxQ(state));
                try {
                    updateQValue(prevState, action, newValue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                lock2.unlock();
                lock1.unlock();
            }
        }
    }
}