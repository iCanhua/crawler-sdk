package com.evolve.eventbus.handler;

import com.evolve.eventbus.event.Event;
import com.evolve.eventbus.model.HandlerID;


/**
 * 带有签名的事件处理器
 * 作用：利用id注册，使得事件框架成为中介者
 */
public interface SignEventHandler extends EventHandler {
  /**
   * 事件处理者在事件框架中断的id
   * @return
   */
  HandlerID getId();

  static SignEventHandler getMissingHandler(){
    return new SignEventHandler() {
      @Override
      public HandlerID getId() {
        return new HandlerID(null);
      }

      @Override
      public void handle(Event event, Object payload) {

      }
    };
  }

}
