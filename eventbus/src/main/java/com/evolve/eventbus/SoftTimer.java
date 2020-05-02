package com.evolve.eventbus;

import com.evolve.eventbus.event.Event;
import com.evolve.eventbus.handler.EventHandler;
import com.evolve.eventbus.model.Message;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SoftTimer {

  SoftTimer(EventHub eventHub) {
    this.eventHub = eventHub;
  }

  private EventHub eventHub;

  private Lock timerLock = new ReentrantLock();

  private Condition awakenCondition = timerLock.newCondition();

  private PriorityBlockingQueue<FutureMessage> futureMessageQueue =
      new PriorityBlockingQueue<>(128, (o1, o2) -> {
        {
          long inter = o1.getEmitTime() - o2.getEmitTime();
          if (inter == 0) {
            return 0;
          }
          return inter>0?1:-1;
        }
      });

  private Thread awakenThread = new Thread(()->{
    log.info("\n定时器启动");
    while (true) {
      try {
        ensureQueueNotNull();
        timerLock.lock();
        FutureMessage message = futureMessageQueue.peek();
        Long interval = message.getEmitTime() - System.currentTimeMillis();
        if (interval > 0) {
          boolean await = awakenCondition.await(interval, TimeUnit.MILLISECONDS);
          if(await){
            log.info("\n定时器被打断重新要求检测");
            continue;
          }else {
            message = futureMessageQueue.take();
            eventHub.getEventBus().send(message.getHandler(),message.getEvent(),message.getPayload());
          }
        }else {
          message=futureMessageQueue.take();
          eventHub.getEventBus().send(message.getHandler(),message.getEvent(),message.getPayload());
        }
        timerLock.unlock();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  });

  private void ensureQueueNotNull() throws InterruptedException {
    FutureMessage message = futureMessageQueue.peek();
    if(message==null){
      log.info("定时器等待定时任务进入！------");
      message = futureMessageQueue.take();
      futureMessageQueue.offer(message);
    }
  }

  void start(){
    awakenThread.start();
  }


  void schedule(FutureMessage message) {
    futureMessageQueue.offer(message);
    timerLock.lock();
    awakenCondition.signal();
    timerLock.unlock();
  }

  public FutureMessage futureMessage(Event event, Object payload, EventHandler handler){
    return new FutureMessage(event,payload,handler);
  }

  public class FutureMessage extends Message {

    Long emitTime;

    public FutureMessage(Event event, Object payload, EventHandler handler) {
      super(event, payload, handler);
    }


    public Long getEmitTime() {
      return emitTime;
    }

    public void emitAfter(long millisecond) {
      this.emitTime = genEmitTime(millisecond);
      schedule(this);
    }

    private Long genEmitTime(Long afterMillis) {
      return System.currentTimeMillis() + afterMillis;
    }
  }

}
