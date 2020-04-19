package com.evolve.eventbus.loop;

import com.evolve.eventbus.model.Message;

public interface EventLoopGroup {

  void dispatch(Message message);

}
