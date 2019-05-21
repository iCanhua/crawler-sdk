package com.evolve.schedule.sdk.session;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.evolve.schedule.sdk.util.HttpUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleJob {

  ScheduleClient scheduleClient;

  private static final String SCHEDULE_API = "/v2/engine/schedule";

  private static final String READY_UNIT_API = "/v2/engine/amazon/asinStock/ready_unit";

  private String SCHEDULE_URL;

  private String GET_TASK_URL;

  private String group;

  private String job;

  public ScheduleJob(ScheduleClient client,String group,String job){
    scheduleClient = client;
    SCHEDULE_URL = client.getServerUrl() + SCHEDULE_API;
    GET_TASK_URL = client.getServerUrl() + READY_UNIT_API;
    this.group = group;
    this.job = job;
  }

  public void start(Map<String,Object> jobDataMap) throws Exception {
    JSONArray start = new JSONArray();
    start.add(newStart(jobDataMap,null));
    JSONObject request = new JSONObject();
    request.put("start",start);
    postJson(SCHEDULE_URL,request);
  }

  public void starts(List<Map<String,Object>> jobDataMaps) throws Exception {
    JSONArray start = new JSONArray();
    for(Map<String,Object> dataMap:jobDataMaps){
      start.add(newStart(dataMap,null));
    }
    JSONObject request = new JSONObject();
    request.put("start",start);
    postJson(SCHEDULE_URL,request);
  }

  public void schedule(Map<String,Object> jobDataMap , String cron){

  }

  public JSONObject getTask(int size) throws Exception {
    Map params = new HashMap();
    params.put("size",size);
    String response = HttpUtils.get(GET_TASK_URL,params,null);
    return JSON.parseObject(response);
  }

  private JSONObject newStart(Map<String,Object> jobDataMap,String trigger){
    JSONObject start = new JSONObject();
    start.put("jobDetail",newJobDetail(jobDataMap));
//    start.put("trigger","");
    return start;
  }

  private JSONObject newJobDetail(Map<String,Object> jobDataMap){
    JSONObject jobDetail = new JSONObject();
    jobDetail.put("group",group);
    jobDetail.put("job",job);
    jobDetail.put("jobDataMap",jobDataMap);
    return jobDetail;
  }

  private String postJson(String url,JSONObject body) throws Exception {
    Map<String,String> headers = new HashMap<>();
    headers.put("Content-Type","application/json");
    String response = HttpUtils.post(url, JSON.toJSONString(body, SerializerFeature.DisableCircularReferenceDetect), headers);
    return response;
  }

}
