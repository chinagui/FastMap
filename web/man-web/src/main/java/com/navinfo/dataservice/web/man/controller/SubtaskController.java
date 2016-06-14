package com.navinfo.dataservice.web.man.controller;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import com.navinfo.dataservice.commons.util.StringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.token.AccessTokenFactory;
import com.navinfo.dataservice.commons.util.DateUtils;

//import com.navinfo.dataservice.commons.util.StringUtils;
import org.apache.commons.lang.StringUtils;
import javax.servlet.http.HttpServletRequest;

import com.navinfo.dataservice.engine.man.subtask.Subtask;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.GridUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: SubtaskController
 * @author songdongyan
 * @date 2016年6月6日
 * @Description: SubtaskController.java
 */
@Controller
public class SubtaskController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	@Autowired
	private SubtaskService service;

	@RequestMapping(value = "/subtask/create")
	public ModelAndView create(HttpServletRequest request){
		try{	

			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));

			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			long userId = tokenObj.getUserId();
			
			JSONArray gridIds = dataJson.getJSONArray("gridIds");

			dataJson.remove("gridIds");

			String wkt = GridUtils.grids2Wkt(gridIds);
			
			Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
			
			service.create(bean,userId,gridIds,wkt);	
			
			return new ModelAndView("jsonView", success("创建成功"));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/subtask/listByWkt")
	public ModelAndView listByWkt(HttpServletRequest request){
		try{	

			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}		
		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));

			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			List<HashMap> data = service.listByWkt(dataJson);
			
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/subtask/listByBlock")
	public ModelAndView listByBlock(HttpServletRequest request){
		try{		
			
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));

			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			int curPageNum= 1;//默认为第一页
			if(dataJson.containsKey("pageNum")){
				curPageNum = dataJson.getInt("pageNum");
				dataJson.remove("pageNum");
			}
			
			int pageSize = 20;//默认页容量为10
			if(dataJson.containsKey("pageSize")){
				pageSize = dataJson.getInt("pageSize");
				dataJson.remove("pageSize");
			}
			
			Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
			
			List<Subtask> subtaskList = service.listByBlock(bean);
			
			List<HashMap<String, Object>> list = new ArrayList();
			
			Page page = new Page(curPageNum);
            page.setPageSize(pageSize);
			
			for(int i=0;i<subtaskList.size();i++){
				HashMap<String, Object> subtask = new HashMap<String, Object>();
				page.setTotalCount(subtaskList.size());
				subtask.put("subtaskId", subtaskList.get(i).getSubtaskId());
				subtask.put("geometry", subtaskList.get(i).getGeometry());
				subtask.put("stage", subtaskList.get(i).getStage());
				subtask.put("type", subtaskList.get(i).getType());
				subtask.put("planStartDate", subtaskList.get(i).getPlanStartDate());
				subtask.put("planEndDate", subtaskList.get(i).getPlanEndDate());
				subtask.put("descp", subtaskList.get(i).getDescp());
				list.add(subtask);
			}
	

            page.setResult(list);
            
            return new ModelAndView("jsonView", success(page));
			
//			return new ModelAndView("jsonView", success(data));
			
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/subtask/listByUser")
	public ModelAndView listByUser(HttpServletRequest request){
		try{	

			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));

			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			int curPageNum= 1;//默认为第一页
			if(dataJson.containsKey("pageNum")){
				curPageNum = dataJson.getInt("pageNum");
			}
			
			int pageSize = 20;//默认页容量为10
			if(dataJson.containsKey("pageSize")){
				pageSize = dataJson.getInt("pageSize");
			}
			
			
			Page data = service.listByUser(dataJson,curPageNum,pageSize);	
			
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	

	
	@RequestMapping(value = "/subtask/query")
	public ModelAndView query(HttpServletRequest request){
		try{
			
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));

			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
			
			Subtask subtask = service.query(bean);	
			
			HashMap<String, Object> data = new HashMap<String, Object>();

			data.put("subtaskId", subtask.getSubtaskId());
			data.put("geometry", subtask.getGeometry());
			data.put("stage", subtask.getStage());
			data.put("type", subtask.getType());
			data.put("planStartDate", subtask.getPlanStartDate());
			data.put("planEndDate", subtask.getPlanEndDate());
			data.put("descp", subtask.getDescp());

			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取明细失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	
	@RequestMapping(value = "/subtask/update")
	public ModelAndView update(HttpServletRequest request){
		try{

			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			service.update(dataJson);			
			return new ModelAndView("jsonView", success("修改成功"));
			
		}catch(Exception e){
			log.error("更新失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	
	@RequestMapping(value = "/subtask/close")
	public ModelAndView close(HttpServletRequest request){
		try{		
			
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));

			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			if(!dataJson.containsKey("subtaskIds")){
				return new ModelAndView("jsonView", success("关闭成功"));
			}
			
			JSONArray subtaskArray = dataJson.getJSONArray("subtaskIds");
			
			service.close(subtaskArray);
			
			return new ModelAndView("jsonView", success("关闭成功"));
		}catch(Exception e){
			log.error("批量关闭失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
