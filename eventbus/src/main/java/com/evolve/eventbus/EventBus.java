package com.evolve.eventbus;


import com.evolve.eventbus.event.Event;
import com.evolve.eventbus.handler.EventHandler;
import com.evolve.eventbus.model.Message;

import lombok.extern.slf4j.Slf4j;


/**
 * 消息总线
 * @date 2019年1月9日
 */
@Slf4j
public class EventBus {

  private EventBus(){}

  private static final EventBus instance = new EventBus();

  public static EventBus getInstance(){
    return instance;
  }
  /**
   * add  event to queue tail
   *
   * @param event
   * @param toHandler
   */
  public void send(EventHandler toHandler, Event event, Object payload) {
    Message message = EventHub.getInstance().retrieve(event, payload,toHandler);
    schedule(message);
  }

  /**
   * 事件处理的中介者
   *
   * @param handlerId
   * @param event
   * @param payload
   */
  public void send(String handlerId, Event event, Object payload) {
    Message message = EventHub.getInstance().retrieve(event, payload,EventHub.getInstance().getHandler(handlerId));
    schedule(message);
  }

  /**
   * add  event to queue tail
   *
   * @param event
   * @param payload
   */
  public void publish(Event event, Object payload) {
    EventHub.getInstance();
  }

  private void schedule(Message message){
    EventHub.getInstance();
  }

}
