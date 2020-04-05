package com.evolve.eventbus;

import com.evolve.eventbus.event.Event;
import com.evolve.eventbus.handler.EventHandler;
import com.evolve.eventbus.model.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 事件中心
 */
public class EventHub {

  private EventHub(){}

  private static final EventHub instance = new EventHub();

  private ConcurrentHashMap<String,EventHandler> signHandlers = new ConcurrentHashMap();

  private ConcurrentLinkedQueue<Message> messagesPool = new ConcurrentLinkedQueue<>(); // for reuse

  public static EventHub getInstance(){
    return instance;
  }

  public EventHandler getHandler(String handlerId){
    return signHandlers.get(handlerId);
  }

  Message retrieve(Event event, Object payload, EventHandler handler) {
    Message message = messagesPool.poll();
    if(message == null) {
      message = new Message(event, payload, handler);
    } else {
      message.setEvent(event);
      message.setPayload(payload);
      message.setHandler(handler);
    }
    return message;
  }


  public void release(Message message) {
    message.setHandler(null);
    message.setPayload(null);
    message.setEvent(null);
    messagesPool.add(message);
  }

}
