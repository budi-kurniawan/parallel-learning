package common.listener;

import common.event.TickEvent;

public interface TickListener {
    void afterTick(TickEvent event);
}
