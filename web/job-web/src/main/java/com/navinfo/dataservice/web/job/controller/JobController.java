package com.navinfo.dataservice.web.job.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.job.model.JobType;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.StringUtils;
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
	
	@Resource(name="jobService")
	private JobService service;
	@RequestMapping(value = "/job/create/")
	public ModelAndView create(HttpServletRequest request){
		try{
			String jobType = URLDecode(request.getParameter("jobType"));
			JSONObject jobRequest = JSONObject.fromObject(URLDecode(request.getParameter("request")));
			String projectId = URLDecode(request.getParameter("projectId"));
			String userId = URLDecode(request.getParameter("userId"));
			String descp = URLDecode(request.getParameter("descp"));
			if(StringUtils.isEmpty(jobType)){
				throw new IllegalArgumentException("jobType参数不能为空。");
			}
			if(jobRequest==null){
				throw new IllegalArgumentException("request参数不能为空。");
			}
			long jobId = service.create(JobType.getJobType(jobType), jobRequest, Long.valueOf(projectId), Long.valueOf(userId), descp);
			return new ModelAndView("jsonView", success(String.valueOf(jobId)));
		}catch(Exception e){
			log.error("获取db失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
