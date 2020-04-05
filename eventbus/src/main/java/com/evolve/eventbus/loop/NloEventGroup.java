package com.evolve.eventbus.loop;


import com.evolve.eventbus.model.Message;


public class NloEventGroup implements EventGroup{

  EventLoop loop;

  private NloEventGroup() {
    loop= new EventLoop();
  }

  private static NloEventGroup nloEventGroup = new NloEventGroup();

  public static NloEventGroup getInstance(){
    return nloEventGroup;
  }

  public void dispatch(Message task) {
    loop.push(task);
  }

}
