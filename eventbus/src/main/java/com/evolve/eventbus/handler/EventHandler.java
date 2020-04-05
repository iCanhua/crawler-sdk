package com.evolve.eventbus.handler;

import com.evolve.eventbus.event.Event;

/**
 * 事件处理者
 */
public interface EventHandler <T extends Event>{

    void handle(T event, Object payload);

}
