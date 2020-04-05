package com.evolve.eventbus.loop;

import com.evolve.eventbus.model.Message;

public interface EventGroup {
  void dispatch(Message message);
}
