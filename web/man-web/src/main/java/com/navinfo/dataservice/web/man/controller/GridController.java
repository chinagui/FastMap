package com.navinfo.dataservice.web.man.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;


import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;

import net.sf.json.JSONObject;

/**
 * @ClassName: GridController
 * @author code generator
 * @date 2016年4月6日 下午6:25:24
 * @Description: TODO
 */
@Controller
public class GridController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	/**
	 * 作业管理--采集管理--采集子任务范围选择，可对待分配、已分配的所有grid进行操作
	 * 根据输入的几何，查询几何范围内的grid，获取grid的分配状态，并返回grid列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/grid/listByAlloc")
	public ModelAndView queryListByWkt(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if (!dataJson.containsKey("wkt") || !dataJson.containsKey("stage")){
				throw new IllegalArgumentException("wkt/stage不能为空");
			}
			List<HashMap<String, Object>> data = GridService.getInstance().queryListByAlloc(dataJson);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取grid列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 出品管理--日出品管理
	 * 根据输入的几何，查询跟几何范围内的grid，返回已出品的快速更新grid列表。
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/grid/listByProduce")
	public ModelAndView queryListByProduce(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if (!dataJson.containsKey("wkt")){
				throw new IllegalArgumentException("wkt不能为空");
			}
			List<String> data = GridService.getInstance().queryListProduce(dataJson);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取grid列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 融合管理--融合月编辑库
	 * 根据输入的几何，查询跟几何范围内的grid，获取可融合的grid，并返回grid列表。
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/grid/listByMerge")
	public ModelAndView queryMergeGrid(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if (!dataJson.containsKey("wkt")){
				throw new IllegalArgumentException("wkt");
			}
			List<String> data = GridService.getInstance().queryMergeGrid(dataJson);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取grid列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 作业管理
	 * 根据输入gridId,stage，获取grid所在的subtask列表。
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/grid/subtaskList")
	public ModelAndView subtaskList(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int gridId = 0;
			if (!dataJson.containsKey("gridId")){
				throw new IllegalArgumentException("gridId");
			}
			gridId = dataJson.getInt("gridId");
			int stage = 0;
			if (!dataJson.containsKey("stage")){
				throw new IllegalArgumentException("stage");
			}
			stage = dataJson.getInt("stage");
			
			int curPageNum= 1;//默认为第一页
			if(dataJson.containsKey("pageNum")){
				curPageNum = dataJson.getInt("pageNum");
			}
			
			int pageSize = 20;//默认页容量为20
			if(dataJson.containsKey("pageSize")){
				pageSize = dataJson.getInt("pageSize");
			}
			
			Page page = GridService.getInstance().subtaskList(gridId,stage,curPageNum,pageSize);
			return new ModelAndView("jsonView", success(page));
		} catch (Exception e) {
			log.error("获取grid列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 作业管理--采集管理--采集子任务范围选择
	 * 后台自动计算并在地图上显示该task关联的grid
	 * 图层仅展示
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/grid/listTaskGrid")
	public ModelAndView listTaskGrid(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int blockManId = dataJson.getInt("taskId");
			/*int neighbor = 0;
			if(dataJson.containsKey("neighbor")){
				neighbor = dataJson.getInt("neighbor");
			}*/
			
			List<Integer> data = GridService.getInstance().listTaskGrid(blockManId);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取grid列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

}
