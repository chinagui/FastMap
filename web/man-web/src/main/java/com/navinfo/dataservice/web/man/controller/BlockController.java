package com.navinfo.dataservice.web.man.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.block.BlockService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName: BlockController 
* @author code generator 
* @date 2016年4月6日 下午6:25:24 
* @Description: TODO
*/
@Controller
public class BlockController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	@Autowired 
	private BlockService service;

	
	@RequestMapping(value = "/block/open/")
	public ModelAndView create(HttpServletRequest request){
		try{	
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			service.batchOpen(userId,dataJson);			
			return new ModelAndView("jsonView", success("创建成功"));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/block/update")
	public ModelAndView update(HttpServletRequest request){
		try{			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("param")));			
			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			service.update(dataJson);			
			return new ModelAndView("jsonView", success("修改成功"));
		}catch(Exception e){
			log.error("修改失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 根据几何范围，查询范围内的可出品的block并返回
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/block/listByProduce/")
	public ModelAndView listByProduce(HttpServletRequest request){
		try{			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!(dataJson.containsKey("wkt"))){
				throw new IllegalArgumentException("wkt参数是必须的。");
			}
			String wkt= dataJson.getString("wkt");
			if(StringUtils.isEmpty(wkt)){
				throw new IllegalArgumentException("wkt参数值不能为空");
			}
			List<HashMap> data = service.listByProduce(wkt);			
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取block列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	/**
	 * 根据输入的几何，查询跟几何相关的并且符合规划状态的Block，返回block信息列表。
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/block/listByWkt")
	public ModelAndView listByWkt(HttpServletRequest request){
		try{			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!(dataJson.containsKey("wkt")) || !(dataJson.containsKey("planningStatus"))){
				throw new IllegalArgumentException("wkt、planningStatus参数是必须的。");
			}
			String wkt= dataJson.getString("wkt");
			String  planningStatus = dataJson.getString("planningStatus");
			if(StringUtils.isEmpty(wkt) || StringUtils.isEmpty(planningStatus)){
				throw new IllegalArgumentException("wkt、planningStatus参数值不能为空");
			}
			List<HashMap> data = service.listByWkt(dataJson);			
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取block列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 查询Block详细信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/block/query")
	public ModelAndView query(HttpServletRequest request){
		try{
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			HashMap data = service.query(dataJson);			
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取明细失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 根据用户组和作业阶段，返回属于该用户组的，有该作业阶段的子任务的Block。
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/block/listByGroup")
	public ModelAndView listByGroup(HttpServletRequest request){
		try{			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!(dataJson.containsKey("groupIds")) || !(dataJson.containsKey("stage"))){
				throw new IllegalArgumentException("groupIds、stage参数是必须的。");
			}
			List<HashMap> data = service.listByGroup(dataJson);			
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取block列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
