package com.navinfo.dataservice.web.man.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
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
			HashMap<String,Object> data = service.query(dataJson.getInt("inforId"));			
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	/**
	 * 一级情报监控
	 * 原则： 
	 * 根据wkt（可选）筛选全国一级情报数据。
	 * 1.	将wkt转成gridList
	 * 2.	与infor_grid_mapping关联可获取与wkt交叉的情报数据列表
	 * 使用场景：管理平台-一级情报监控
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/inforMan/monitor")
	public ModelAndView monitor(HttpServletRequest request){
		try{
			String parStr=URLDecode(request.getParameter("parameter"));
			List<Map<String, Object>> res=service.monitor(JSONObject.fromObject(parStr));
			return new ModelAndView("jsonView",success(res));
		}catch(Exception e){
			log.error("", e);
			return new ModelAndView("jsonView",exception(e));
		}
	}

}
