package com.evolve.eventbus.loop;


import com.evolve.eventbus.model.Message;


public class SingleEventLoopPool implements EventLoopPool {

  BaseEventLoopPool pool;

  public SingleEventLoopPool() {
    pool=new BaseEventLoopPool(1);
  }


  public void dispatch(Message message) {
    pool.dispatch(message);
  }

}
