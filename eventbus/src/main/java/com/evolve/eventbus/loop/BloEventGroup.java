package com.evolve.eventbus.loop;

import com.evolve.eventbus.model.Message;

import java.util.ArrayList;

/**
 * 阻塞事件处理
 */
public class BloEventGroup implements EventGroup{

  private ArrayList<EventLoop> eventLoops;

  public BloEventGroup(int loopCount) {
    eventLoops = new ArrayList<>();
    for (int i=0; i<loopCount; i++){
      eventLoops.add(new EventLoop());
    }
  }

  public void dispatch(Message message) {
    EventLoop loop =getEventLoop();
    loop.push(message);
  }

  /**
   * 后期实现为Netty的模式，固定的事务，如session,channel绑定固定的eventLoop
   * @return
   */
  private EventLoop getEventLoop(){
    EventLoop  minLBLoop =eventLoops.get(0);
    int minSize =minLBLoop.getTaskSize();
    for(EventLoop eventLoop:eventLoops){
      if (eventLoop.getTaskSize()==0){
        return eventLoop;
      }else {

        if(eventLoop.getTaskSize()<minSize){
          minSize = eventLoop.getTaskSize();
          minLBLoop =eventLoop;
        }
      }
    }
    return minLBLoop;
  }

}
