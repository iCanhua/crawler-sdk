package com.evolve.eventbus.event;

/**
 * 可以被绑定的事件，同一个被绑的事件，会进入到一个eventLoop中去
 * 例如，同一个channel一般都需要在同一个循环中处理。
 */
public interface AttachEvent extends Event{
  /**
   * 获取绑定id
   * @return
   */
  String attachId();
}
