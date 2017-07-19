package com.navinfo.dataservice.web.man.controller;

import com.alibaba.fastjson.JSONArray;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.job.JobService;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.JobType;
import net.sf.json.JSONObject;
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
            long itemId = parameter.getLong("itemId");
            int itemType = parameter.getInt("itemType");
            int jobType = parameter.getInt("jobType");

            JSONArray result = JobService.getInstance().getJobProgress(itemId, ItemType.valueOf(itemType), JobType.valueOf(jobType));
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
            if (!data.containsKey("itemType")||!data.containsKey("itemId")||!data.containsKey("isContinue")){
                throw new IllegalArgumentException("itemId|itemType|isContinue不能为空");
            }
            AccessToken tokenObj = (AccessToken) request.getAttribute("token");
            long operator = tokenObj.getUserId();

            long itemId = data.getLong("itemId");
            ItemType itemType = ItemType.valueOf(data.getInt("itemType"));
            boolean isContinue = data.getBoolean("isContinue");

            long jobId = JobService.getInstance().day2month(itemId,itemType,operator,isContinue,null);
            JSONObject result = new JSONObject();
            result.put("jobId",jobId);
            return new ModelAndView("jsonView", success(result));
        } catch (Exception e) {
            log.error("创建日落月任务失败，原因：" + e.getMessage(), e);
            return new ModelAndView("jsonView", exception(e));
        }
    }
}
