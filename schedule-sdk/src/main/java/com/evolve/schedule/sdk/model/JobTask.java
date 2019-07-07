package com.evolve.schedule.sdk.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public class JobTask {

  ScheduleJob job;

  private String taskFeature;

  private String nameSpace;

  /**
   * 一个task只有一个启动器
   */
  private JSONObject starter;

  public JobTask(ScheduleJob job) {
    this.job = job;
  }

  public void start(Map<String, Object> jobDataMap) throws Exception {
    starter = newStart(jobDataMap, null);
    if (taskFeature != null && !"".equals(taskFeature)) {
      starter.put("taskFeature", taskFeature);
    }
    if (nameSpace != null && !"".equals(nameSpace)) {
      starter.put("nameSpace", nameSpace);
    }
    job.start(this);
  }

  JSONObject toJSON() {
    return starter;
  }

  private JSONObject newStart(Map<String, Object> jobDataMap, String trigger) {
    JSONObject starter = new JSONObject();
    starter.put("jobDetail", newJobDetail(jobDataMap));
    //    start.put("trigger","");
    return starter;
  }

  private JSONObject newJobDetail(Map<String, Object> jobDataMap) {
    JSONObject jobDetail = new JSONObject();
    jobDetail.put("group", job.getGroup());
    jobDetail.put("job", job.getJob());
    jobDetail.put("jobDataMap", jobDataMap);
    return jobDetail;
  }


  public void setTaskFeature(String taskFeature) {
    this.taskFeature = taskFeature;
  }

  public void setNameSpace(String nameSpace) {
    this.nameSpace = nameSpace;
  }
}
