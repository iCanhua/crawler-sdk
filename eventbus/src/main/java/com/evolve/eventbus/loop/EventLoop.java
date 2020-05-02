package com.evolve.eventbus.loop;


import com.evolve.eventbus.model.Message;

import java.util.concurrent.LinkedBlockingQueue;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventLoop implements Runnable {

  Thread thread =new Thread(this,"EventLoop instance in EventBus");

  private LinkedBlockingQueue<Message> taskRegistered;// wait to be handled

  public EventLoop() {
    taskRegistered=new LinkedBlockingQueue<>();
    thread.start();
  }

  public EventLoop(int queueSize) {
    taskRegistered=new LinkedBlockingQueue<>(queueSize);
    thread.start();
  }

  /**
   * consume the message in queue in loop way
   */
  public void loop() throws InterruptedException {
    while (true) {
      Message message = taskRegistered.take();
      try {
        log.debug("ready to parse an event, type is {}. the queue size is {} now",
                  message.getEvent(),
                  taskRegistered.size());
        if(message != null && message.getHandler()!=null) {
          long curTime = System.currentTimeMillis();
          message.getHandler().handle(message.getEvent(), message.getPayload());
          if(System.currentTimeMillis() - curTime > 2000){
            log.warn("======= there is some event exceed than 2s =======:"+message.toString());
          }
        }
      } catch (Exception e) {
        log.error("event loop parse event cause an exception", e);
      }finally {
        message.release();
      }
    }
  }

  /**
   * add  event to queue tail
   *
   */
  public void push(Message task) {
    try {
      taskRegistered.put(task);
    } catch (InterruptedException e) {
      log.error("put item to queue cause an exception", e);
    }
    log.debug("add an event to tail of the queue, task desc is {} ,the queue size is {} now", task.toString(), taskRegistered
        .size());
  }

  @SneakyThrows
  @Override
  public void run() {
    loop();
  }

  public int getTaskSize(){
    return taskRegistered.size();
  }
}
