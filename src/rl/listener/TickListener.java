package rl.listener;

import rl.event.TickEvent;

public interface TickListener {
    void afterTick(TickEvent event);
}
