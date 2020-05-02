package com.evolve.eventbus.model;

import java.util.Objects;

import lombok.Data;

/**
 * 中介者id，注册在事件中心的id
 */
public class HandlerID {
  /**
   * 唯一id
   */
  private String handlerId;

  /**
   * 构造器
   * @param handlerId
   */
  public HandlerID(String handlerId) {
    this.handlerId = handlerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HandlerID handlerID = (HandlerID) o;
    return handlerId.equals(handlerID.handlerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(handlerId);
  }
}
