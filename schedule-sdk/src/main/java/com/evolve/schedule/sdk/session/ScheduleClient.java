package com.evolve.schedule.sdk.session;

import com.evolve.schedule.sdk.model.ScheduleJob;

import java.util.HashMap;
import java.util.Map;

public class ScheduleClient {

  String serverUrl;

  Map<String, ScheduleJob> jobCache = new HashMap<>();

  public String getServerUrl() {
    return serverUrl;
  }

  public ScheduleClient(String url){
    this.serverUrl = url;
  }

  public ScheduleJob getJob(String group, String job){
    String jobCacheKey =  genJobCacheKey(group,job);
    if(jobCache.get(jobCacheKey)==null){

      jobCache.put(jobCacheKey,new ScheduleJob(this,group,job));
    }
    return jobCache.get(jobCacheKey);
  }

  private String genJobCacheKey(String group, String job){
    return group+job;
  }


}
