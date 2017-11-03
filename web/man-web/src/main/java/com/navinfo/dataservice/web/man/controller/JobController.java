package com.navinfo.dataservice.web.man.controller;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.job.JobService;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.JobType;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by wangshishuai3966 on 2017/7/10.
 */
@Controller
public class JobController extends BaseController {

    private Logger log = LoggerRepos.getLogger(this.getClass());

    @RequestMapping(value = "/job/get")
    public ModelAndView getProgress(HttpServletRequest request) {
        try {
            JSONObject parameter = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
            if (parameter == null) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }
            if (!parameter.containsKey("itemId") || !parameter.containsKey("itemType") || !parameter.containsKey("jobType")){
                throw new IllegalArgumentException("itemId|itemType|jobType不能为空");
            }
            ItemType itemType = ItemType.valueOf(parameter.getInt("itemType"));
            JobType jobType = JobType.valueOf(parameter.getInt("jobType"));
            long itemId = parameter.getLong("itemId");

            JSONArray result = JobService.getInstance().getJobProgress(itemId, itemType, jobType);
            return new ModelAndView("jsonView", success(result));
        } catch (Exception e) {
            log.error("查询进度失败，原因：" + e.getMessage(), e);
            return new ModelAndView("jsonView", exception(e));
        }
    }

    @RequestMapping(value = "/job/get/day2monthBylot")
    public ModelAndView getDay2MonthProgress(HttpServletRequest request) {
        try {
            JSONObject result = JobService.getInstance().getDay2MonthLotJobProgress();
            return new ModelAndView("jsonView", success(result));
        } catch (Exception e) {
            log.error("查询进度失败，原因：" + e.getMessage(), e);
            return new ModelAndView("jsonView", exception(e));
        }
    }

    @RequestMapping(value = "/job/run/tips2mark")
    public ModelAndView tips2Mark(HttpServletRequest request) {
        try {
            JSONObject data = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
            if (data == null) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }
            if (!data.containsKey("itemType")||!data.containsKey("itemId")||!data.containsKey("isContinue")){
                throw new IllegalArgumentException("itemId|itemType|isContinue不能为空");
            }
            AccessToken tokenObj = (AccessToken) request.getAttribute("token");
            long operator = tokenObj.getUserId();

            long itemId = data.getLong("itemId");
            ItemType itemType = ItemType.valueOf(data.getInt("itemType"));
            boolean isContinue = data.getBoolean("isContinue");

            long jobId = JobService.getInstance().tips2Mark(itemId,itemType,operator,isContinue,null);
            JSONObject result = new JSONObject();
            result.put("jobId",jobId);
            return new ModelAndView("jsonView", success(result));
        } catch (Exception e) {
            log.error("创建tips转mark任务失败，原因：" + e.getMessage(), e);
            return new ModelAndView("jsonView", exception(e));
        }
    }

    @RequestMapping(value = "/job/run/day2month")
    public ModelAndView day2month(HttpServletRequest request) {
        try {
            JSONObject data = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
            if (data == null) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }
            if (!data.containsKey("type")||!data.containsKey("isContinue")){
                throw new IllegalArgumentException("type|isContinue不能为空");
            }

            boolean isContinue = data.getBoolean("isContinue");
            ItemType itemType = ItemType.valueOf(data.getInt("type"));
            if(itemType==ItemType.PROJECT){
                if(!data.containsKey("programId")){
                    throw new IllegalArgumentException("programId不能为空");
                }
                if(!isContinue){
                    if(!data.containsKey("lot")){
                        throw new IllegalArgumentException("lot不能为空");
                    }
                }
            }else if(itemType==ItemType.LOT){
                if(!data.containsKey("lot")){
                    throw new IllegalArgumentException("lot不能为空");
                }
            }

            AccessToken tokenObj = (AccessToken) request.getAttribute("token");
            long operator = tokenObj.getUserId();

            String parameter=null;
            long itemId=0;
            if(data.containsKey("lot")){
                if(itemType == ItemType.LOT){
                    itemId = data.getInt("lot");
                }else {
                    int lot = data.getInt("lot");
                    JSONObject msg = new JSONObject();
                    msg.put("lot", lot);
                    parameter = msg.toString();
                }
            }
            if(data.containsKey("programId")){
                itemId = data.getLong("programId");
            }

            long jobId = JobService.getInstance().day2month(itemId,itemType,operator,isContinue,parameter);
            JSONObject result = new JSONObject();
            result.put("jobId",jobId);
            return new ModelAndView("jsonView", success(result));
        } catch (Exception e) {
            log.error("创建日落月任务失败，原因：" + e.getMessage(), e);
            return new ModelAndView("jsonView", exception(e));
        }
    }
    
    @RequestMapping(value = "/job/run/commonJob")
    public ModelAndView runCommonJob(HttpServletRequest request) {
        try {
            JSONObject data = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
            if (data == null) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }
            if (!data.containsKey("isContinue")){
                throw new IllegalArgumentException("isContinue不能为空");
            }

            boolean isContinue = data.getBoolean("isContinue");
            ItemType itemType = ItemType.DEFAULT;
            if(data.containsKey("itemType")){
            	itemType=ItemType.valueOf(data.getInt("itemType"));
            }

            AccessToken tokenObj = (AccessToken) request.getAttribute("token");
            long operator = tokenObj.getUserId();
            
            JobType jobType = JobType.valueOf(data.getInt("jobType"));

            String parameter=null;
            long itemId=0;
            if(data.containsKey("itemId")){
                itemId = data.getInt("itemId");
            }
            
            if(data.containsKey("condition")){
            	parameter = data.getJSONObject("condition").toString();
            }

            long jobId = JobService.getInstance().runCommonJob(jobType,itemId,itemType,operator,isContinue,parameter);
            JSONObject result = new JSONObject();
            result.put("jobId",jobId);
            return new ModelAndView("jsonView", success(result));
        } catch (Exception e) {
            log.error("runCommonJob，原因：" + e.getMessage(), e);
            return new ModelAndView("jsonView", exception(e));
        }
    }
}
