package com.navinfo.dataservice.web.man.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.man.model.Infor;
import com.navinfo.dataservice.api.man.model.InforMan;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.inforMan.InforManService;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: InforManController
 * @author code generator
 * @date 2016年4月6日 下午6:25:24
 * @Description: TODO
 */
@Controller
public class InforManController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private InforManService service=InforManService.getInstance();

	/**
	 * 规划管理-情报管理-情报规划-创建情报
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/inforMan/create")
	public ModelAndView create(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			service.create(dataJson, userId);
			return new ModelAndView("jsonView", success("创建成功"));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	/**
	 * 规划管理-情报管理-查看及编辑情报信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/inforMan/update")
	public ModelAndView update(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			service.update(dataJson);
			return new ModelAndView("jsonView", success("修改成功"));
		} catch (Exception e) {
			log.error("修改失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	/**
	 * 规划管理-情报管理-情报规划-保存情报
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/inforMan/save")
	public ModelAndView inforSave(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			if (service.query(dataJson.getString("inforId"))==null){
				service.create(dataJson, userId);
			}else{
				service.update(dataJson);
			}
			return new ModelAndView("jsonView", success("创建成功"));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	/**
	 * 情报管理--查看及编辑情报信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/inforMan/close")
	public ModelAndView close(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONArray inforManIds = dataJson.getJSONArray("inforIds");
			service.close(JSONArray.toList(inforManIds));
			String msg="批量关闭"+inforManIds.size()+"个成功，0个失败";
			return new ModelAndView("jsonView", success(msg));
		} catch (Exception e) {
			log.error("情报规划批量关闭失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	/**
	 * 情报管理--查看及编辑情报信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/inforMan/query")
	public ModelAndView query(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			HashMap<String,Object> data = service.query(dataJson.getString("inforId"));
			
			if(data!=null){
				return new ModelAndView("jsonView", success(data));
			}
			else{
				return new ModelAndView("jsonView", success(null));
			}
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 情报管理--查看及编辑情报信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/inforMan/queryByTaskId")
	public ModelAndView queryByTaskId(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			HashMap<String,Object> data = service.queryByTaskId(dataJson.getInt("taskId"));
			
			if(data!=null){
				return new ModelAndView("jsonView", success(data));
			}
			else{
				return new ModelAndView("jsonView", success(null));
			}
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	/**
	 * 情报管理--查看及编辑情报信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/inforMan/listAll")
	public ModelAndView listAll(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			JSONObject condition = dataJson.getJSONObject("condition");
			JSONObject order = dataJson.getJSONObject("order");

			int curPageNum = 1;// 默认为第一页
			String curPage = dataJson.getString("pageNum");
			if (StringUtils.isNotEmpty(curPage)) {
				curPageNum = Integer.parseInt(curPage);
			}
			int curPageSize = 20;// 默认为20条记录/页
			String curSize = dataJson.getString("pageSize");
			if (StringUtils.isNotEmpty(curSize)) {
				curPageSize = Integer.parseInt(curSize);
			}
			Page data = service.listAll(condition, order, curPageNum, curPageSize);
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("result", data.getResult());
			resultMap.put("totalCount", data.getTotalCount());
			return new ModelAndView("jsonView", success(resultMap));
		} catch (Exception e) {
			log.error("获取列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
}
