package com.evolve.eventbus.model;


import com.evolve.eventbus.handler.EventHandler;
import com.evolve.eventbus.event.Event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {

  private Event event;

  private Object payload;

  private transient EventHandler handler;

  public Message(Event event, Object payload, EventHandler handler) {
    this.event = event;
    this.payload = payload;
    this.handler =handler;
  }

}
