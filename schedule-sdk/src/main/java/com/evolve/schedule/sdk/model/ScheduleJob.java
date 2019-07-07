package com.evolve.schedule.sdk.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.evolve.schedule.sdk.session.ScheduleClient;
import com.evolve.schedule.sdk.util.HttpUtils;

import java.util.HashMap;
import java.util.Map;

public class ScheduleJob {

  ScheduleClient scheduleClient;

  private static final String SCHEDULE_API = "/v2/engine/schedule";

  private static final String READY_UNIT_API = "/v2/engine/{group}/{job}/ready_unit";

  private String SCHEDULE_URL;

  private String GET_TASK_URL;

  private String group;

  private String job;

  public ScheduleJob(ScheduleClient client, String group, String job) {
    scheduleClient = client;
    SCHEDULE_URL = client.getServerUrl() + SCHEDULE_API;
    GET_TASK_URL = client.getServerUrl() + READY_UNIT_API;
    GET_TASK_URL = GET_TASK_URL.replace("{group}",group);
    GET_TASK_URL = GET_TASK_URL.replace("{job}",job);
    this.group = group;
    this.job = job;
  }

  void start(JobTask task) throws Exception {
    JSONArray startArray = new JSONArray();
    startArray.add(task.toJSON());
    JSONObject request = new JSONObject();
    request.put("start", startArray);
    postJson(SCHEDULE_URL, request);
  }

  public JobTask newTask(){
    return new JobTask(this);
  }


  public JSONArray getTask(int size) throws Exception {
    Map params = new HashMap();
    params.put("size", String.valueOf(size));
    String response = HttpUtils.get(GET_TASK_URL, params, null);
    return (JSONArray) JSONArray.parse(response);
  }


  private String postJson(String url, JSONObject body) throws Exception {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    String response = HttpUtils.post(url,
                                     JSON.toJSONString(body,
                                                       SerializerFeature.DisableCircularReferenceDetect),
                                     headers);
    return response;
  }

  public String getGroup() {
    return group;
  }

  public String getJob() {
    return job;
  }
}
