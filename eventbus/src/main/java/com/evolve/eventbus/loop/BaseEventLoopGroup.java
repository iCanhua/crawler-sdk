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
public class BaseEventLoopGroup implements EventLoopGroup {

  private ArrayList<EventLoop> eventLoops;

  private Map<String,EventLoop> attachMap=new ConcurrentHashMap();

  public BaseEventLoopGroup(int loopCount) {
    eventLoops = new ArrayList<>();
    for (int i=0; i<loopCount; i++){
      eventLoops.add(new EventLoop());
    }
  }

  public void dispatch(Message message) {
    EventLoop loop = getEventLoop(message.getEvent());
    loop.push(message);
  }

  /**
   * 后期实现为Netty的模式，固定的事务，如session,channel绑定固定的eventLoop
   * @return
   */
  private EventLoop getEventLoop(Event event){
    if(event instanceof AttachEvent){
      String attachId = ((AttachEvent) event).attachId();
      return attachMap.computeIfAbsent(attachId,k-> getLowLoadEventLoop());
    }else {
      return getLowLoadEventLoop();
    }
  }

  /**
   * 获取负载最低的loop
   * @return
   */
  private EventLoop getLowLoadEventLoop(){
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
