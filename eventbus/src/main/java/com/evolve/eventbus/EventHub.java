package com.evolve.eventbus;

import com.evolve.eventbus.event.Event;
import com.evolve.eventbus.handler.EventHandler;
import com.evolve.eventbus.loop.BaseEventLoopPool;
import com.evolve.eventbus.loop.EventLoopPool;
import com.evolve.eventbus.loop.SingleEventLoopPool;
import com.evolve.eventbus.model.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.Getter;
import lombok.Setter;

/**
 * 事件中心
 */
public class EventHub {

  private EventHub(){
    eventBus = new EventBus(this);
  }

  private ConcurrentHashMap<String,EventHandler> signHandlers = new ConcurrentHashMap();

  private ConcurrentLinkedQueue<Message> messagesPool = new ConcurrentLinkedQueue<>(); // for reuse
  @Setter
  @Getter
  private EventLoopPool pool;
  @Getter
  private EventBus eventBus;

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

  void schedule(Message message){
    pool.dispatch(message);
  }


  public void release(Message message) {
    message.setHandler(null);
    message.setPayload(null);
    message.setEvent(null);
    messagesPool.add(message);
  }

  public static Builder builder(){
    return new Builder();
  }

  /**
   * 唯一建造方法
   */
  private static class Builder{

    private EventLoopPool pool;

    private int loopCount=1;

    public Builder loopCount(int count){
      loopCount = count;
      return this;
    }

    public Builder eventPool(EventLoopPool pool){
      this.pool = pool;
      return this;
    }

    public EventHub build(){
      EventHub eventHub = new EventHub();
      if(pool!=null){
        eventHub.setPool(pool);
        return eventHub;
      }else if(loopCount==1){
        eventHub.setPool(new SingleEventLoopPool());
        return eventHub;
      }else {
        eventHub.setPool(new BaseEventLoopPool(loopCount));
        return eventHub;
      }
    }
  }

}
