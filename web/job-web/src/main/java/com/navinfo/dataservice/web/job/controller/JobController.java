package com.navinfo.dataservice.web.job.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.job.model.JobStep;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.jobframework.service.JobService;

import net.sf.json.JSONObject;

/** 
* @ClassName: JobController 
* @author Xiao Xiaowen 
* @date 2016年4月6日 下午6:25:24 
* @Description: TODO
*/
@Controller
public class JobController extends BaseController {
	protected Logger log = LoggerRepos.getLogger(this.getClass());

	@RequestMapping(value = "/hello")
	public ModelAndView hello(HttpServletRequest request){
		try{
			return new ModelAndView("jsonView", success(JobService.getInstance().hello()));
		}catch(Exception e){
			log.error("内部错误，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/create")
	public ModelAndView create(HttpServletRequest request){
		try{
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (paraJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(paraJson.get("jobType")==null){
				throw new IllegalArgumentException("jobType参数不能为空。");
			}
			if(paraJson.get("request")==null){
				throw new IllegalArgumentException("request参数不能为空。");
			}
			String jobType = paraJson.getString("jobType");
			JSONObject jobRequest = paraJson.getJSONObject("request");
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			String descp = null;
			if(paraJson.containsKey("descp")){
				descp = paraJson.getString("descp");
			}
			long taskId=0L;
			if(paraJson.containsKey("taskId")){
				taskId=paraJson.getLong("taskId");
			}
			long jobId = JobService.getInstance().create(jobType, jobRequest, userId,taskId, descp);
			Map<String,Object> data = new HashMap<String,Object>();
			data.put("jobId", jobId);
			return new ModelAndView("jsonView", success("job已创建。",data));
		}catch(Exception e){
			log.error("job创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/get")
	public ModelAndView get(HttpServletRequest request){
		try{
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (paraJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			JobInfo jobInfo = null;
			if(paraJson.get("jobId")!=null){
				int jobId = paraJson.getInt("jobId");
				jobInfo = JobService.getInstance().getJobById(Integer.valueOf(jobId));
			}else if(paraJson.get("jobGuid")!=null){
				String jobGuid = paraJson.getString("jobGuid");
				jobInfo = JobService.getInstance().getJobByGuid(jobGuid);
			}else{
				throw new IllegalArgumentException("jobId或者jobGuid参数不能都为空。");
			}
			if(jobInfo==null){
				return new ModelAndView("jsonView", fail("未找到该job信息"));
			}
			Map<String,Object> data = new HashMap<String,Object>();
			data.put("jobId", jobInfo.getId());
			data.put("createTime", DateUtils.dateToString(jobInfo.getCreateTime()));
			data.put("beginTime", DateUtils.dateToString(jobInfo.getBeginTime()));
			data.put("endTime", DateUtils.dateToString(jobInfo.getEndTime()));
			data.put("status", jobInfo.getStatus());
			data.put("resultMsg", jobInfo.getResultMsg());
			data.put("stepCount", jobInfo.getStepCount());
			if(jobInfo.getStepListSize()>0){
				JobStep step = jobInfo.getSteps().get(jobInfo.getStepListSize()-1);
				data.put("latestStepSeq", step.getStepSeq());
				data.put("latestStepMsg", step.getStepMsg());
			}else{
				data.put("latestStepSeq", null);
				data.put("latestStepMsg", null);
				
			}
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("job创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
