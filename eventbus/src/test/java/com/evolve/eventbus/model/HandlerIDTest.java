package com.evolve.eventbus.model;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class HandlerIDTest {
  @Test
  public void testEqual(){
    HandlerID handler1 = new HandlerID("handler1");
    HandlerID handler2 = new HandlerID("handler1");
    Assert.assertEquals(handler1,handler2);
  }

}