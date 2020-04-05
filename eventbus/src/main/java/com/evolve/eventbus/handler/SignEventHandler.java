package com.evolve.eventbus.handler;

/**
 * 带有签名的事件处理器
 * 作用：利用id注册，使得事件框架成为中介者
 */
public interface SignEventHandler extends EventHandler {
  /**
   * 事件处理者在事件框架中断的id
   * @return
   */
  String getId();

}
