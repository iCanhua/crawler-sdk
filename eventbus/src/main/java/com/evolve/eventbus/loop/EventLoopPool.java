package com.evolve.eventbus.loop;

import com.evolve.eventbus.model.Message;

public interface EventLoopPool {

  void dispatch(Message message);

}
