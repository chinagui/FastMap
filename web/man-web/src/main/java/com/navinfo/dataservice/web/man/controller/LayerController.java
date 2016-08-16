package com.navinfo.dataservice.web.man.controller;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.layer.LayerService;
import com.navinfo.dataservice.engine.man.task.TaskService;

/**
 * @ClassName: CustomisedLayerController
 * @author code generator
 * @date 2016年4月6日 下午6:25:24
 * @Description: TODO
 */
@Controller
public class LayerController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	/**
	 * 创建一个重点区块。
		原则：
			(1)随便画闭合多边形
		(2)用户画多边形时，控制重点区块不能交叉
		(3)新增的直接输入名称。名称为空不允许保存(web)，弹出提示框
		使用场景：
		规划管理页面--重点区块图层 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/layer/create")
	public ModelAndView create(HttpServletRequest request) {
		try {
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");

			StringBuffer sb = new StringBuffer();
			String line = null;
			try {
				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null)
					sb.append(line);
			} catch (Exception e) {
				throw new IllegalArgumentException("读取POST参数失败。");
			}

			String data = sb.toString();
			
			JSONObject dataJson = JSONObject.fromObject(data);
			
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空");
			}
			long userId = tokenObj.getUserId();
			//long userId=2;
			//log.info(tokenObj);
			
			LayerService.getInstance().create(userId, dataJson.getString("layerName"),dataJson.getString("wkt"));
			return new ModelAndView("jsonView", success("创建成功"));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	/**
	 * 修改重点区块。
		原则：
		通过layerId修改重点区块的坐标wkt,layerName
		使用场景：
		规划管理页面--重点区块图层--重点区块图层编辑
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/layer/update")
	public ModelAndView update(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request
					.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空");
			}
			String wkt=null;
			if(dataJson.containsKey("wkt")){wkt=dataJson.getString("wkt");}
			String layerName=null;
			if(dataJson.containsKey("layerName")){wkt=dataJson.getString("layerName");}
			LayerService.getInstance().update(dataJson.getString("layerId"),wkt,layerName);
			return new ModelAndView("jsonView", success("修改成功"));
		} catch (Exception e) {
			log.error("修改失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	/**
	 * 删除一个重点区块。
		原则：
			通过layerId直接删除重点区块信息
		使用场景：
			规划管理页面--重点区块图层--重点区块图层编辑
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/layer/delete")
	public ModelAndView delete(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request
					.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空");
			}
			LayerService.getInstance().delete(dataJson.getString("layerId"));
			return new ModelAndView("jsonView", success("删除成功"));
		} catch (Exception e) {
			log.error("删除失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	/**
	 * 根据输入的几何，查询跟几何相关的并且符合规划状态的重点区块，返回重点区块列表。
		原则：
			根据输入的polygon（一般是长方形），将在坐标区域内的所有重点区块返回
		使用场景：
		规划管理页面--重点区块图层 图层加载的时候需要返回在图层框内的重点区块列表，用于显示
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/layer/listByWkt")
	public ModelAndView query(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request
					.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空");
			}
			List data = LayerService.getInstance().listByWkt(dataJson.getString("wkt"));
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("根据geo获取重点区块列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/*
	 * 规划管理页面--重点区块图层--搜索
	 */
	@RequestMapping(value = "/layer/listAll")
	public ModelAndView listAll(HttpServletRequest request){
		try{	
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			JSONObject condition = dataJson.getJSONObject("condition");			
			JSONObject order = dataJson.getJSONObject("order");
			
			List<HashMap> data = LayerService.getInstance().listAll(condition,order);			
			return new ModelAndView("jsonView", success(JsonOperation.beanToJsonList(data)));
			//return new ModelAndView("jsonView", success(data.getResult()));
		}catch(Exception e){
			log.error("获取全部列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
