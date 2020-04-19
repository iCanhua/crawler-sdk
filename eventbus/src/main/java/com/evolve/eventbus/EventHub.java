package com.evolve.eventbus;

import com.evolve.eventbus.event.Event;
import com.evolve.eventbus.handler.EventHandler;
import com.evolve.eventbus.loop.BaseEventLoopGroup;
import com.evolve.eventbus.loop.EventLoopGroup;
import com.evolve.eventbus.model.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.Getter;
import lombok.Setter;

/**
 * 事件中心
 */
public class EventHub {

  private EventHub() {
    eventBus = new EventBus(this);
  }

  private ConcurrentHashMap<String, EventHandler> signHandlers = new ConcurrentHashMap();

  private ConcurrentLinkedQueue<Message> messagesPool = new ConcurrentLinkedQueue<>(); // for reuse
  @Setter
  @Getter
  private EventLoopGroup eventLoopGroup;
  @Getter
  private EventBus eventBus;
  @Setter
  @Getter
  private int messageCacheSize=0;

  public EventHandler getHandler(String handlerId) {
    return signHandlers.get(handlerId);
  }

  Message retrieve(Event event, Object payload, EventHandler handler) {
    Message message = messagesPool.poll();
    if (message == null) {
      message = new Message(event, payload, handler);
      message.setEventHub(this);
    } else {
      message.setEvent(event);
      message.setPayload(payload);
      message.setHandler(handler);
    }
    return message;
  }

  void schedule(Message message) {
    eventLoopGroup.dispatch(message);
  }

  /**
   * 释放消息，如果pool过大，则不放回池子；
   * @param message
   */
  public void release(Message message) {
    message.setHandler(null);
    message.setPayload(null);
    message.setEvent(null);
    if(messagesPool.size()<=messageCacheSize){
      messagesPool.add(message);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * 唯一建造方法
   */
  public static class Builder {

    private int loopCount = 1;

    private int messageCacheSize =10;

    public Builder initLoopCount(int count) {
      loopCount = count;
      return this;
    }

    public Builder initMessageCacheSize(int size) {
      messageCacheSize = size;
      return this;
    }

    public EventHub build() {
      EventHub eventHub = new EventHub();
      eventHub.setEventLoopGroup(new BaseEventLoopGroup(loopCount));
      eventHub.setMessageCacheSize(messageCacheSize);
      return eventHub;
    }
  }

  public static void main(String[] args) {
    EventHub eventHub = EventHub.builder().build();
    Message retrieve = eventHub.retrieve(null, null, null);
    retrieve.setPayload("dd");
    System.out.println("dgfg");

  }

}
