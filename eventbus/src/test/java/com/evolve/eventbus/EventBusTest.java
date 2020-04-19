package com.evolve.eventbus;

import com.evolve.eventbus.event.Event;
import com.evolve.eventbus.handler.EventHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;

import static org.mockito.Mockito.when;

@Slf4j
public class EventBusTest {

  EventBus eventBus;
  @Mock
  Event event;

  @Before
  public void before(){
    MockitoAnnotations.initMocks(this);
    EventHub eventHub = EventHub.builder().initLoopCount(3).initMessageCacheSize(20).build();
    eventBus =eventHub.getEventBus();
    when(event.getId()).thenReturn("mockEventId_1");
    when(event.getDescription()).thenReturn("mockEventDesc_1");
  }



  Lock lock = new ReentrantLock();
  Event testSend2HandlerEvent;
  Object testSend2HandlerPayload;
  Condition testSend2HandlerCondition = lock.newCondition();
  @Test
  public void testSend2Handler() throws InterruptedException {
    EventHandler eventHandler=(e,p)->{
      log.info("测试的eventHandler,收到结果event [{}],payload[{}],当前线程[{}]",e.getDescription(),p,Thread.currentThread().getId());
      testSend2HandlerEvent=e;
      testSend2HandlerPayload=p;
      lock.lock();
      testSend2HandlerCondition.signal();
      lock.unlock();
    };
    eventBus.send(eventHandler,event,"2333");
    lock.lock();
    testSend2HandlerCondition.await(3000L, TimeUnit.MILLISECONDS);
    lock.unlock();
    Assert.assertEquals(testSend2HandlerEvent.getId(),"mockEventId_1");
    Assert.assertEquals(testSend2HandlerPayload,"2333");
    log.info("当前主线程【{}】",Thread.currentThread().getId());
  }

  @Test
  public void testSend2HandlerId() {
  }

  @Test
  public void publish() {
  }
}