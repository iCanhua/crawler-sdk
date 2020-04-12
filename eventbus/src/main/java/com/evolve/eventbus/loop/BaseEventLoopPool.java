package com.evolve.eventbus.loop;

import com.evolve.eventbus.event.AttachEvent;
import com.evolve.eventbus.event.Event;
import com.evolve.eventbus.model.Message;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件循环处理组
 */
public class BaseEventLoopPool implements EventLoopPool {

  private ArrayList<EventLoop> eventLoops;

  private Map<String,EventLoop> attachMap=new ConcurrentHashMap();

  public BaseEventLoopPool(int loopCount) {
    eventLoops = new ArrayList<>();
    for (int i=0; i<loopCount; i++){
      eventLoops.add(new EventLoop());
    }
  }

  public void dispatch(Message message) {
    EventLoop loop =getEventLoop(message.getEvent());
    loop.push(message);
  }

  /**
   * 后期实现为Netty的模式，固定的事务，如session,channel绑定固定的eventLoop
   * @return
   */
  private EventLoop getEventLoop(Event event){
    if(event instanceof AttachEvent){
      String attachId = ((AttachEvent) event).attachId();
      return attachMap.computeIfAbsent(attachId,k->getEventLoop());
    }else {
      return getEventLoop();
    }
  }

  /**
   * 后期实现为Netty的模式，固定的事务，如session,channel绑定固定的eventLoop
   * @return
   */
  private EventLoop getEventLoop(){
    EventLoop  firstLoop =eventLoops.get(0);
    if(eventLoops.size()==1||firstLoop.getTaskSize()==0){
      return firstLoop;
    }
    return eventLoops.stream().reduce((result,element)->{
      if(element.getTaskSize()<=result.getTaskSize()){
        return element;
      }else {
        return result;
      }
    }).get();
  }

}
