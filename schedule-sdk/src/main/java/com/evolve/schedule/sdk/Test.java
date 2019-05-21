package com.evolve.schedule.sdk;


import com.evolve.schedule.sdk.session.ScheduleClient;
import com.evolve.schedule.sdk.session.ScheduleJob;

import java.util.HashMap;

public class Test {

  private static ScheduleClient scheduleClient = new ScheduleClient("http://47.107.134.104:8080");

  public static void main(String[] args) {
    testPush();
  }

  private static void testPush() {
    ScheduleJob job = scheduleClient.getJob("amazon", "asinStock");
    for (int i = 0; i < 100000; i++) {
      HashMap dataMap = new HashMap();
      dataMap.put("testHachi", i);
      try {
        job.start(dataMap);
        System.out.println("成功放进去：" + i);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static void testPoll() {
    ScheduleJob job= scheduleClient.getJob("amazon", "asinStock");
    try {
      System.out.println(job.getTask(1));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
