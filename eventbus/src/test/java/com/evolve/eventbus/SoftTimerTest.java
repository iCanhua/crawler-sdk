package com.evolve.eventbus;

import com.evolve.eventbus.event.Event;
import com.evolve.eventbus.handler.EventHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import static org.mockito.Mockito.when;

@Slf4j
public class SoftTimerTest {

  EventBus eventBus;
  @Mock
  Event event;
  EventHub eventHub;

  @Before
  public void before(){
    MockitoAnnotations.initMocks(this);
    eventHub = EventHub.builder().initLoopCount(3).initMessageCacheSize(20).build();
    eventBus =eventHub.getEventBus();
    when(event.getId()).thenReturn("mockEventId_1");
    when(event.getDescription()).thenReturn("mockEventDesc_1");
  }

  CountDownLatch latch = new CountDownLatch(3);
  ArrayList<String> timeArrival = new ArrayList<>();
  @Test
  public void testSchedule() throws InterruptedException {
    EventHandler eventHandler=(e, p)->{
      log.info("测试的eventHandler,收到结果event [{}],payload[{}],当前线程[{}]",e.getDescription(),p,Thread.currentThread().getId());
      log.info("接收时间{}",System.currentTimeMillis());
      timeArrival.add(p.toString());
      latch.countDown();
    };
    eventHub.getSoftTimer().futureMessage(event,"2000",eventHandler).emitAfter(2000);
    log.info("发送时间{}",System.currentTimeMillis());
    eventHub.getSoftTimer().futureMessage(event,"3000",eventHandler).emitAfter(3000);
    log.info("发送时间{}",System.currentTimeMillis());
    eventHub.getSoftTimer().futureMessage(event,"1000",eventHandler).emitAfter(1000);
    log.info("发送时间{}",System.currentTimeMillis());
    latch.await(8000,TimeUnit.MILLISECONDS);
    Assert.assertEquals("1000", timeArrival.get(0));
    Assert.assertEquals("2000", timeArrival.get(1));
    Assert.assertEquals("3000", timeArrival.get(2));
    eventHub.getSoftTimer().futureMessage(event,"1000",eventHandler).emitAfter(1000);
    Thread.sleep(2000);
    log.info("当前主线程【{}】",Thread.currentThread().getId());
  }
}