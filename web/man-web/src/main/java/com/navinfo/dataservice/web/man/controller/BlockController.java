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
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.block.BlockOperation;
import com.navinfo.dataservice.engine.man.block.BlockService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.Page;

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

	private BlockService service=BlockService.getInstance();

	@RequestMapping(value = "/block/open")
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
			JSONArray blockArray = dataJson.getJSONArray("blocks");
			int blockSize=blockArray.size();
			int updateCount=service.batchOpen(userId, dataJson);
			String message = "批量开启block：" + updateCount + "个成功，" + (blockSize - updateCount) + "个失败。";
			return new ModelAndView("jsonView", success(message));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	/**
	 * 批量修改Block信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/block/update")
	public ModelAndView update(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			JSONArray blockArray = dataJson.getJSONArray("blocks");
			int blockSize=blockArray.size();
			int updateCount=service.batchUpdate(dataJson,userId);
			String message = "批量修改block：" + updateCount + "个成功，" + (blockSize - updateCount) + "个失败。";
			return new ModelAndView("jsonView", success(message));
		} catch (Exception e) {
			log.error("修改失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	/**
	 * 规划管理-block管理-block规划-保存
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value = "/block/save")
	public ModelAndView blockSave(HttpServletRequest request) {
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
			JSONArray blockArray = dataJson.getJSONArray("blocks");
			int blockSize=blockArray.size();
			int insertCount=service.batchOpen(userId, dataJson);
			int updateCount=service.batchUpdate(dataJson,userId);
			String message = "批量保存block：" + updateCount + "个成功，" + (blockSize - (insertCount+updateCount)) + "个失败。";
			return new ModelAndView("jsonView", success(message));
		} catch (Exception e) {
			log.error("保存失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 根据几何范围，查询范围内的可出品的block并返回
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/block/listByProduce")
	public ModelAndView listByProduce(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if (!dataJson.containsKey("wkt") || !dataJson.containsKey("type")||!dataJson.containsKey("stage")){
				throw new IllegalArgumentException("wkt/type/stage不能为空");
			}
			List<HashMap> data = service.listByProduce(dataJson);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取block列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	/**
	 * 根据输入的几何，查询跟几何相关的并且符合规划状态的Block，返回block信息列表。
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/block/listByWkt")
	public ModelAndView listByWkt(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if (!(dataJson.containsKey("wkt")) || !(dataJson.containsKey("planningStatus"))) {
				throw new IllegalArgumentException("wkt、planningStatus参数是必须的。");
			}
			String wkt = dataJson.getString("wkt");
			if (StringUtils.isEmpty(wkt)) {
				throw new IllegalArgumentException("wkt参数值不能为空");
			}
			List<HashMap> data = service.listByWkt(dataJson);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取block列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	/**
	 * 查询Block详细信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/block/query")
	public ModelAndView query(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			HashMap data = service.query(dataJson);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 查询Block详细信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/block/wktByBlockId")
	public ModelAndView queryWktByBlockId(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int blockId = 0;
			int blockManId = 0;
			int type = dataJson.getInt("type");
			if(1==type){
				blockId = dataJson.getInt("blockId");
			}else if(4==type){
				blockManId = dataJson.getInt("blockManId");
			}
			List data = service.queryWktByBlockId(blockId,blockManId,type);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}


	/**
	 * 判断block是否可关闭:该block每个阶段的所有子任务均关闭，则该block可以关闭。
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/block/close")
	public ModelAndView close(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));

			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			if (!(dataJson.containsKey("blockManIds"))) {
				throw new IllegalArgumentException("blockIds参数是必须的。");
			}

			JSONArray blockManIds = dataJson.getJSONArray("blockManIds");
			List<Integer> blockManIdList = (List<Integer>) JSONArray.toCollection(blockManIds, Integer.class);

			List<Integer> unClosedBlock = BlockService.getInstance().close(blockManIdList);

			String message = "批量关闭block：" + (blockManIdList.size() - unClosedBlock.size()) + "个成功，" + unClosedBlock.size() + "个失败。";			
			return new ModelAndView("jsonView", success(message));

		} catch (Exception e) {
			log.error("获取block列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	/*@RequestMapping(value = "/block/listAll")
	public ModelAndView listAll(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (!dataJson.containsKey("enterParam")){
				throw new IllegalArgumentException("enterParam参数是必须的。");
			}
			JSONObject enterParam = dataJson.getJSONObject("enterParam");
			String listType =null;
			if (dataJson.containsKey("listType")){
				listType = dataJson.getString("listType");
			}
			JSONObject condition = dataJson.getJSONObject("condition");
			JSONObject order = dataJson.getJSONObject("order");
			List<HashMap> data = service.listAll(enterParam,listType,condition, order);

			if (!dataJson.containsKey("pageNum")){
				
				return new ModelAndView("jsonView", success(data));
			}else{
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
				Map<String, Object> resultMap=new HashMap<String, Object>();
				BlockOperation blockOperation= new BlockOperation();
				resultMap.put("result", blockOperation.getPagedList(curPageNum, curPageSize, data));
				resultMap.put("totalCount", data.size());
				return new ModelAndView("jsonView", success(resultMap));
			}
			
		} catch (Exception e) {
			log.error("获取列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}*/
	
	/**
	 *  根据采集组或者日编组，查询Block列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/block/list")
	public ModelAndView list(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			JSONObject condition = new JSONObject();	
			if(dataJson.containsKey("condition")){
				condition=dataJson.getJSONObject("condition");
			}
			JSONObject order = new JSONObject();	
			if(dataJson.containsKey("order")){
				order=dataJson.getJSONObject("order");
			}			
			int curPageNum= 1;//默认为第一页
			if (dataJson.containsKey("pageNum")){
				curPageNum = dataJson.getInt("pageNum");
			}
			int curPageSize= 20;//默认为20条记录/页
			if (dataJson.containsKey("pageSize")){
				curPageSize = dataJson.getInt("pageSize");
			}
			int snapshot = 0; 
			if(dataJson.containsKey("snapshot")){
				snapshot=dataJson.getInt("snapshot");
			}
			//0采集，1日编
			int stage = 0;
			if (dataJson.containsKey("stage")){
				stage=dataJson.getInt("stage");
			}
			Map<String, Object> resultMap=new HashMap<String, Object>();
			BlockOperation blockOperation= new BlockOperation();
			Page page=service.list(stage,condition,order,curPageNum,curPageSize,snapshot);
			resultMap.put("result", page.getResult());
			resultMap.put("totalCount", page.getTotalCount());
			return new ModelAndView("jsonView", success(resultMap));
			
		} catch (Exception e) {
			log.error("获取列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	
	/**
	 * 根据情报id分组返回block信息
	 * @param request
	 * @return
	 */
	/*@RequestMapping(value = "/block/listByInfoId")
	public ModelAndView listByInfoId(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if (!(dataJson.containsKey("inforId"))) {
				throw new IllegalArgumentException("inforId参数是必须的。");
			}
			List<HashMap> data = service.listByInfoId(dataJson);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取block列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}*/
	
	/**
	 *  根据采集组或者日编组，查询Block列表
	 * @param request
	 * @return
	 */
	/*@RequestMapping(value = "/block/listByGroupId")
	public ModelAndView listByGroupId(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (!dataJson.containsKey("groupId")){
				throw new IllegalArgumentException("groupId参数是必须的。");
			}
			if (!dataJson.containsKey("stage")){
				throw new IllegalArgumentException("stage参数是必须的。");
			}
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
			Map<String, Object> resultMap=new HashMap<String, Object>();
			BlockOperation blockOperation= new BlockOperation();
			Page page=service.listByGroupId(dataJson,curPageNum,curPageSize);
			resultMap.put("result", page.getResult());
			resultMap.put("totalCount", page.getTotalCount());
			return new ModelAndView("jsonView", success(resultMap));
			
		} catch (Exception e) {
			log.error("获取列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}*/
	
	/**
	 * 发布对象：1.分配的采集作业组组长2.分配的日编作业组组长
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/block/pushMsg")
	public ModelAndView pushMsg(HttpServletRequest request){
		try{
			//AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONArray blockManIds=dataJson.getJSONArray("blockManIds");
			//long userId=tokenObj.getUserId();
			String msg=service.blockPushMsg(blockManIds);
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("发布失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
