
package com.navinfo.dataservice.web.man.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.Page;
import com.wordnik.swagger.annotations.ApiParam;


/** 
 * @ClassName: SubtaskController
 * @author songdongyan
 * @date 2016年6月6日
 * @Description: SubtaskController.java
 */
@Controller
public class SubtaskController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
//	@Autowired

	/*
	 * 创建一个子任务。
	 */
	/**
	 * @Title: create
	 * @Description: (修改)创建子任务(第七迭代)
	 * @param access_token
	 * @param parameter (增加参数:qualityExeUserId, qualityPlanStartDate, qualityPlanEndDate)
	 * @param request
	 * @return  NullResponse
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月3日 下午5:06:43 
	 */
	@RequestMapping(value = { "/subtask/create" })
	public ModelAndView create(HttpServletRequest request){
		try{	

			AccessToken tokenObj=(AccessToken) request.getAttribute("token");

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			long userId = tokenObj.getUserId();
			//创建subtask	
			SubtaskService.getInstance().create(userId,dataJson);

			return new ModelAndView("jsonView", success());
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	


	/*
	 * 根据几何范围查询任务列表
	 * 20161130 by zhangxiaoyi 经确认，管理平台未使用这个接口
	 */
	//@RequestMapping(value = { "/listByWkt" }, method = RequestMethod.GET)
	/*@RequestMapping(value = "/subtask/listByWkt")
	public ModelAndView listByWkt(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
			,@ApiParam(required =true, name = "parameter", value="{\"wkt\":\"\"}")@RequestParam( value = "parameter") String postData
			,HttpServletRequest request){
		try{	

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			//获取几何范围
			String wkt = dataJson.getString("wkt");
			
			List<Subtask> subtaskList = SubtaskService.getInstance().listByWkt(wkt);

			List<SubtaskListByWkt> subtaskListByWktList = new ArrayList<SubtaskListByWkt>();
			for(int i=0;i<subtaskList.size();i++){
				SubtaskListByWkt subtaskListByWkt = new SubtaskListByWkt();
				subtaskListByWkt.setSubtaskId(subtaskList.get(i).getSubtaskId());
				subtaskListByWkt.setGeometry(subtaskList.get(i).getGeometry());
				subtaskListByWkt.setReferGeometry(subtaskList.get(i).getReferGeometry());
				subtaskListByWkt.setDescp(subtaskList.get(i).getDescp());
				subtaskListByWkt.setName(subtaskList.get(i).getName());
				subtaskListByWkt.setStage(subtaskList.get(i).getStage());
				subtaskListByWkt.setStatus(subtaskList.get(i).getStatus());
				subtaskListByWkt.setType(subtaskList.get(i).getType());
				subtaskListByWkt.setGridIds(subtaskList.get(i).getGridIds());
				subtaskListByWktList.add(subtaskListByWkt);
			}
			SubtaskListByWktResponse responseListByWkt = new SubtaskListByWktResponse(0,"success",subtaskListByWktList);
			return responseListByWkt;
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			SubtaskListByWktResponse responseListByWkt = new SubtaskListByWktResponse(-1,e.getMessage(),null);
			return responseListByWkt;
		}
	}*/

	

	//@ApiOperation(value = "根据作业员获取子任务列表", notes = "根据作业员获取subtask列表")  
	@RequestMapping(value = { "/subtask/listByUser" })
	public ModelAndView listByUser(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
			,@ApiParam(required =true, name = "parameter", value="{<br/>\"exeUserId\":1\\\\作业员ID,<br/>\"stage\":1\\\\0采集、1日编、2月编,<br/>\"type\":1\\\\0 POI、1道路、2一体化、3专项,<br/>\"status\":1\\\\2进行中、3已完成,<br/>\"snapshot\":1\\\\0 返回全部字段/1 不返回geometry和gridIds,<br/>\"pageNum\":1\\\\页码（默认1，返回首页）,<br/>\"pageSize\":1\\\\每页条数，默认20<br/>}")@RequestParam( value = "parameter") String postData			,HttpServletRequest request){
		try{	
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
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
			
			int snapshot = dataJson.getInt("snapshot");
			dataJson.remove("snapshot");
			
			//增加平台参数。0：采集端，1：编辑平台2管理平台
			int platForm = 0;//默认采集端
			if(dataJson.containsKey("platForm")){
				platForm = dataJson.getInt("platForm");
				dataJson.remove("platForm");
			}
			if(!dataJson.containsKey("exeUserId")||dataJson.getInt("exeUserId")==0){
				dataJson.put("exeUserId", (int)tokenObj.getUserId());
			}
            Page page = SubtaskService.getInstance().listByUserPage(dataJson,snapshot,platForm,pageSize,curPageNum);
            
			return new ModelAndView("jsonView",success(page));
            
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			//SubtaskListByUserResponse responseList = new SubtaskListByUserResponse(-1,e.getMessage(),null);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	

	/**
	 * @Title: query
	 * @Description: 根据subtaskId查询一个任务的详细信息。(修改)(第七迭代)
	 * @param access_token
	 * @param postData
	 * @param request
	 * @return  SubtaskQueryResponse(增加返回值qualitySubtaskId,qualityExeUserId, qualityPlanStartDate, qualityPlanEndDate)
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月4日 下午4:06:13 
	 */
	//@ApiOperation(value = "查询subtask信息", notes = "查询subtask信息")  
	@RequestMapping(value = { "/subtask/query" })
	public ModelAndView query(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
			,@ApiParam(required =true, name = "parameter", value="{\"subtaskId\":1\\子任务ID}")@RequestParam( value = "parameter") String postData
			,HttpServletRequest request){
		try{
			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int subtaskId = dataJson.getInt("subtaskId");
			int platform=0;//增加平台参数。0：采集端，1：编辑平台2管理平台（grids返回值不一样）
			if(dataJson.containsKey("platform")){
				platform=dataJson.getInt("platform");
			}
			Map<String,Object> subtask = SubtaskService.getInstance().query(subtaskId,platform);
			return new ModelAndView("jsonView", success(subtask));
		}catch(Exception e){
			log.error("获取明细失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	

	/**
	 * @Title: update
	 * @Description: (修改)批量修改子任务详细信息。(第七迭代)
	 * @param access_token
	 * @param postData
	 * @param request(增加参数:qualitySubtaskId,qualityExeUserId, qualityPlanStartDate, qualityPlanEndDate)
	 * @return  NullResponse
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月4日 上午10:23:20 
	 */
	//@ApiOperation(value = "批量修改子任务详细信息", notes = "批量修改子任务详细信息")  
	@RequestMapping(value = { "/subtask/update" })
	public ModelAndView update(HttpServletRequest request){
		try{
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			SubtaskService.getInstance().update(dataJson,userId);
			return new ModelAndView("jsonView", success());
			
		}catch(Exception e){
			log.error("更新失败，原因："+e.getMessage(), e);
			//NullResponse result = new NullResponse(-1,e.getMessage(),null);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
//	/*
//	 * 关闭多个子任务。
//	 */
//	//@ApiOperation(value = "关闭subtask", notes = "关闭subtask")  
//	@RequestMapping(value = { "/subtask/close" })
//	public ModelAndView close(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
//			,@ApiParam(required =true, name = "parameter", value="{<br/>\"subtaskIds\":[12]#子任务列表<br/>	}")@RequestParam( value = "parameter") String parameter
//			,HttpServletRequest request){
//		try{		
//			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
//			long userId=tokenObj.getUserId();
//			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
//			if(dataJson==null){
//				throw new IllegalArgumentException("parameter参数不能为空。");
//			}
//			
//			if(!dataJson.containsKey("subtaskIds")){
//				throw new IllegalArgumentException("subtaskIds不能为空。");
//			}
//			
//			JSONArray subtaskIds = dataJson.getJSONArray("subtaskIds");
//			
//			List<Integer> subtaskIdList = (List<Integer>)JSONArray.toCollection(subtaskIds,Integer.class);
//			List<Integer> unClosedSubtaskList = SubtaskService.getInstance().close(subtaskIdList,userId);
//			
//			String message = "批量关闭子任务：" + (subtaskIdList.size() - unClosedSubtaskList.size()) + "个成功，" + unClosedSubtaskList.size() + "个失败。";
//			return new ModelAndView("jsonView", success(message));
//		
//		}catch(Exception e){
//			log.error("批量关闭失败，原因："+e.getMessage(), e);
//			//NullResponse result = new NullResponse(-1,e.getMessage(),null);
//			return new ModelAndView("jsonView", exception(e));
//		}
//	}
	
	/*
	 * 关闭单个子任务。
	 */
	//@ApiOperation(value = "关闭subtask", notes = "关闭subtask")  
	@RequestMapping(value = { "/subtask/close" })
	public ModelAndView close(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
			,@ApiParam(required =true, name = "parameter", value="{<br/>\"subtaskId\":12#子任务<br/>	}")@RequestParam( value = "parameter") String parameter
			,HttpServletRequest request){
		try{		
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			if(!dataJson.containsKey("subtaskId")){
				throw new IllegalArgumentException("subtaskId不能为空。");
			}
			
			int subtaskId = dataJson.getInt("subtaskId");
			
			String message = SubtaskService.getInstance().close(subtaskId,userId);
			if((message!=null)&&(!message.isEmpty())){
				return new ModelAndView("jsonView", exception(message));
			}else{
				return new ModelAndView("jsonView", success(message));
			}
		
		}catch(Exception e){
			log.error("关闭失败，原因："+e.getMessage(), e);
			//NullResponse result = new NullResponse(-1,e.getMessage(),null);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/*
	 * 删除子任务，前端只有草稿状态的子任务有删除按钮
	 */
	//@ApiOperation(value = "删除subtask", notes = "删除subtask")  
	@RequestMapping(value = { "/subtask/delete" })
	public ModelAndView delete(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
			,@ApiParam(required =true, name = "parameter", value="{<br/>\"subtaskIds\":[12]#子任务列表<br/>	}")@RequestParam( value = "parameter") String parameter
			,HttpServletRequest request){
		try{		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			if(!dataJson.containsKey("subtaskId")){
				throw new IllegalArgumentException("subtaskId不能为空。");
			}
			
			int subtaskId = dataJson.getInt("subtaskId");
			
			SubtaskService.getInstance().delete(subtaskId);
			
			return new ModelAndView("jsonView", success());
		
		}catch(Exception e){
			log.error("批量关闭失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	
	/*
	 * 发布功能：选中一条/多条开启状态记录后，点击“发布”按钮，将这些记录进行消息推送。未选中记录时，提示“请选择子任务”；发布后提示“发布成功”/“发布失败”
	 * 草稿状态记录，点击发布后，子任务状态变更为“开启”，同时进行消息推送
	 * 开启状态记录，点击发布后，子任务状态不变更，只进行消息推送
	 * ps:开启状态时，“保存”包含消息发布功能，点击“发布”仍可单独发布消息
	 */
	@RequestMapping(value = "/subtask/pushMsg")
	public ModelAndView pushMsg(HttpServletRequest request){
		try{
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			long userId=tokenObj.getUserId();
			
			JSONArray subtaskIds=dataJson.getJSONArray("subtaskIds");
			
			String msg= SubtaskService.getInstance().pushMsg(userId,subtaskIds);

			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	

	
	
//	@ApiOperation(value = "获取subtask列表", notes = "获取subtask列表")  
//	@RequestMapping(value = { "/list" }, method = RequestMethod.GET)
//	public SubtaskListResponse list(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
//			,@ApiParam(required =true, name = "parameter", value="{<br/>\"stage\":1\\\\作业阶段,<br/>\"condition\":1\\\\搜索条件（JSON），均可选，不支持组合。<br/>\t{\"subtaskId\"子任务Id<br/>\"subtaskName\"子任务名称<br/>\"ExeUserId\"作业员<br/>\"blockId\"所属blockId<br/>\"blockName\"所属block名称<br/>\"taskId\"所属任务id<br/>\"taskName\"所属任务名称},<br/>\"order\":\\\\排序条件（JSON），可选，按照时间查询,json中只有一个有效排序条件，不支持组合排序。<br/>\t{<br/>\"subtaskId\":\"desc\",<br/>\"status\":\"desc\",<br/>\"planStartDate\":\"desc\"//降序，asc升序,<br/>\"planEndDate\":\"desc\",<br/>\"blockId\":\"desc\"},<br/>\"pageNum\":1\\\\页码,<br/>\"pageSize\":1\\\\每页条数<br/>}")@RequestParam( value = "parameter") String postData			,HttpServletRequest request){
//		try{		
//			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
//			long userId = tokenObj.getUserId();
//			
//			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
//			if(dataJson==null){
//				throw new IllegalArgumentException("parameter参数不能为空。");
//			}
//			
//			int curPageNum= 1;//默认为第一页
//			if(dataJson.containsKey("pageNum")){
//				curPageNum = dataJson.getInt("pageNum");
//			}
//			
//			int pageSize = 30;//默认页容量为20
//			if(dataJson.containsKey("pageSize")){
//				pageSize = dataJson.getInt("pageSize");
//			}
//			
//			int snapshot = 0; 
//			if(dataJson.containsKey("snapshot")){
//				snapshot=dataJson.getInt("snapshot");
//			}
//			
//			JSONObject order =null; 
//			if(dataJson.containsKey("order")){
//				order=dataJson.getJSONObject("order");
//			}
//			
//			JSONObject condition =null; 
//			if(dataJson.containsKey("condition")){
//				condition=dataJson.getJSONObject("condition");
//			}
//			
//			//作业阶段
//			int stage = dataJson.getInt("stage");
//			
//			Page page = SubtaskService.getInstance().list(userId,stage,condition,order,pageSize,curPageNum,snapshot);
//
//			SubtaskListPage pageList = new SubtaskListPage(page.getPageSize(),page.thePageNum(),page.getStart(),page.getTotalCount(),(List<SubtaskList>)page.getResult());
//			SubtaskListResponse responseList = new SubtaskListResponse(0,"success",pageList);
//			return responseList;
//		
//		}catch(Exception e){
//			log.error("查询失败，原因："+e.getMessage(), e);
//			SubtaskListResponse responseList = new SubtaskListResponse(-1,e.getMessage(),null);
//			return responseList;
//		}
//	}
	
	
	/**
	 * @Title: list
	 * @Description: 获取subtask列表,只返回作业子任务(修改)(第七迭代)
	 * @param request
	 * @return  SubtaskListResponse
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月4日 下午3:59:29 
	 */
	//@ApiOperation(value = "获取subtask列表", notes = "获取subtask列表")  
	@RequestMapping(value = { "/subtask/list" })
	public ModelAndView list(HttpServletRequest request){
		try{		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int curPageNum= 1;//默认为第一页
			if(dataJson.containsKey("pageNum")){
				curPageNum = dataJson.getInt("pageNum");
			}
			
			int pageSize = 30;//默认页容量为20
			if(dataJson.containsKey("pageSize")){
				pageSize = dataJson.getInt("pageSize");
			}
			//查询条件
			JSONObject condition = dataJson.getJSONObject("condition");
//			//block/task规划状态。2:"已发布",3:"已完成" 。状态不同，排序方式不同。
//			int planStatus = dataJson.getInt("planStatus");			
//			Page page = SubtaskService.getInstance().list(planStatus,condition,pageSize,curPageNum);
			Page page = SubtaskService.getInstance().list(condition,pageSize,curPageNum);
			return new ModelAndView("jsonView",success(page));
		
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			//SubtaskListResponse responseList = new SubtaskListResponse(-1,e.getMessage(),null);
			return new ModelAndView("jsonView",exception(e));
		}
	}

	

//	/**
//	 * @Title: listByGroup
//	 * @Description: 根据作业组获取子任务列表（修改）(第七迭代)
//	 * @param access_token
//	 * @param postData
//	 * @param request 
//	 * @return  SubtaskListResponse (增加返回值:qualitySubtaskId,qualityExeUserId, qualityPlanStartDate, qualityPlanEndDate)
//	 * @throws 
//	 * @author zl zhangli5174@navinfo.com
//	 * @date 2016年11月4日 上午11:30:18 
//	 */
//	//@ApiOperation(value = "获取subtask列表", notes = "获取subtask列表")  
//	@RequestMapping(value = { "/subtask/listByGroup" })
//	public ModelAndView listByGroup(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
//			,@ApiParam(required =true, name = "parameter", value="{<br/>\"stage\":1\\\\作业阶段,<br/>\"condition\":1\\\\搜索条件（JSON），均可选，不支持组合。<br/>\t{\"subtaskId\"子任务Id<br/>\"subtaskName\"子任务名称<br/>\"ExeUserId\"作业员<br/>\"blockId\"所属blockId<br/>\"blockName\"所属block名称<br/>\"taskId\"所属任务id<br/>\"taskName\"所属任务名称},<br/>\"order\":\\\\排序条件（JSON），可选，按照时间查询,json中只有一个有效排序条件，不支持组合排序。<br/>\t{<br/>\"subtaskId\":\"desc\",<br/>\"status\":\"desc\",<br/>\"planStartDate\":\"desc\"//降序，asc升序,<br/>\"planEndDate\":\"desc\",<br/>\"blockId\":\"desc\"},<br/>\"pageNum\":1\\\\页码,<br/>\"pageSize\":1\\\\每页条数<br/>}")@RequestParam( value = "parameter") String postData			,HttpServletRequest request){
//		try{		
//			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
//			if(dataJson==null){
//				throw new IllegalArgumentException("parameter参数不能为空。");
//			}
//			
//			int curPageNum= 1;//默认为第一页
//			if(dataJson.containsKey("pageNum")){
//				curPageNum = dataJson.getInt("pageNum");
//			}
//			
//			int pageSize = 30;//默认页容量为20
//			if(dataJson.containsKey("pageSize")){
//				pageSize = dataJson.getInt("pageSize");
//			}
//			
//			int snapshot = 0; 
//			if(dataJson.containsKey("snapshot")){
//				snapshot=dataJson.getInt("snapshot");
//			}
//			
//			JSONObject order =null; 
//			if(dataJson.containsKey("order")){
//				order=dataJson.getJSONObject("order");
//			}
//			
//			JSONObject condition =null; 
//			if(dataJson.containsKey("condition")){
//				condition=dataJson.getJSONObject("condition");
//			}
//			
//			//作业阶段
//			int stage = dataJson.getInt("stage");
//			//作业组
//			int groupId = dataJson.getInt("groupId");
//			
//			Page page = SubtaskService.getInstance().listByGroup(groupId,stage,condition,order,pageSize,curPageNum,snapshot);
//			return new ModelAndView("jsonView", success(page));
//		
//		}catch(Exception e){
//			log.error("查询失败，原因："+e.getMessage(), e);
//			//SubtaskListResponse responseList = new SubtaskListResponse(-1,e.getMessage(),null);
//			return new ModelAndView("jsonView", exception(e));
//		}
//	}
	/**
	 * 根据wkt获取不规则子任务圈subtask_refer
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/subtask/listReferByWkt")
	public ModelAndView queryListReferByWkt(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			List<HashMap<String,Object>> data = SubtaskService.getInstance().queryListReferByWkt(dataJson);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取城市列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 按照token用户子任务类型统计子任务量
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/subtask/staticWithType")
	public ModelAndView staticWithType(HttpServletRequest request) {
		try {
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			Map<String, Object> data = SubtaskService.getInstance().staticWithType(userId);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取城市列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 1.根据参数cityName与infor表中的admin_name模糊匹配，获取匹配成功的情报的所有采集子任务列表
	 * 应用场景：独立工具：采集成果中/无转快时，获取快线子任务列表
	 * @author songhe
	 * @param  cityName
	 * @return List
	 * 
	 */
	@RequestMapping(value = "/subtask/listAllInforByCity")
	public ModelAndView listAllInforByCity(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
//			JSONObject dataJson = JSONObject.fromObject(new String(request.getParameter("parameter").getBytes("iso8859-1"),"utf-8"));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			String cityName = dataJson.getString("cityName");
			JSONObject condition = new JSONObject();
			
			if (dataJson.containsKey("condition")) {
				condition = JSONObject.fromObject(dataJson.get("condition"));
			}
			List<Map<String, Object>> data = SubtaskService.getInstance().listAllInforByCity(cityName, condition);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("子任务查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 编辑子任务圈接口
	 * 原则：如果S圈对应的采集子任务已经开启，则不能进行任何操作；草稿状态子任务的S圈如果修改，则删除与采集子任务的关联
	 * 应用场景：独立工具--外业规划--绘制子任务圈—合并/切分等操作
	 * @author songhe
	 * @param  cityName
	 * @return List
	 * 
	 */
	@RequestMapping(value = "/subtask/paintRefer")
	public ModelAndView paintRefer(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int taskId = dataJson.getInt("taskId");
			JSONObject condition = new JSONObject();			
			if (dataJson.containsKey("condition")) {
				condition = JSONObject.fromObject(dataJson.get("condition"));
			}
			SubtaskService.getInstance().paintRefer(taskId, condition);
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			log.error("子任务查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 获取所有质检子任务列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/subtask/unPlanQualitylist")
	public ModelAndView unPlanQualitylist(HttpServletRequest request) {
		try{	
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));		
			Integer taskId = dataJson.getInt("taskId");
			JSONObject data = SubtaskService.getInstance().unPlanQualitylist(taskId);
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 删除质检圈
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/subtask/qualityDelete")
	public ModelAndView qualityDelete(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson == null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int qualityId = dataJson.getInt("qualityId");

			SubtaskService.getInstance().qualityDelete(qualityId);
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			log.error("删除质检圈失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 获取质检圈列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/subtask/qualitylist")
	public ModelAndView qualitylist(HttpServletRequest request){
		try{	
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson == null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int subtaskId = dataJson.getInt("subtaskId");
			
			JSONObject data = SubtaskService.getInstance().qualitylist(subtaskId);
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取质检圈列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 创建质检圈
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/subtask/qualityCreate")
	public ModelAndView qualityCreate(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson == null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			SubtaskService.getInstance().qualityCreate(dataJson);
			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			log.error("创建质检圈失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 修改质检圈
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/subtask/qualityUpdate")
	public ModelAndView qualityUpdate(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson == null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			SubtaskService.getInstance().qualityUpdate(dataJson);
			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			log.error("修改质检圈失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 日编子任务未规划grid接口
	 * grid及tips完成情况统计
	 * 筛选出未规划的grid
	 * 按照tips个数从大到小排序，gridid从大到小排序
	 * 
	 * */
	@RequestMapping(value = "/subtask/unPlanGridList")
	public ModelAndView unPlanGridList(HttpServletRequest request){
		try{
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int pageNum = 1;
			if(dataJson.containsKey("pageNum")){
				pageNum = dataJson.getInt("pageNum");
			}
			int pageSize = 20;
			if(dataJson.containsKey("pageSize")){
				pageSize = dataJson.getInt("pageSize");
			}
			if(!dataJson.containsKey("taskId")){
				throw new Exception("缺少taskId");
			}
			int taskId = dataJson.getInt("taskId");
			
			Map<String, Object> result = SubtaskService.getInstance().unPlanGridList(taskId,pageNum,pageSize);
			
			return new ModelAndView("jsonView", success(result));
		}catch(Exception e){
			log.error("日编子任务未规划grid接口异常，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
		
	}
	
	/**
	 * 提交质检圈
	 * 修改subtask表quality_plan_status=1
	 * 应用场景：独立工具--外业规划--绘制质检圈—完成
	 * 
	 * */
	@RequestMapping(value = "/subtask/qualityCommit")
	public ModelAndView qualityCommit(HttpServletRequest request){
		try{
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!dataJson.containsKey("subtaskId")){
				throw new Exception("缺少subtaskId");
			}
			int subtaskId = dataJson.getInt("subtaskId");
			
			SubtaskService.getInstance().qualityCommit(subtaskId);
			
			return new ModelAndView("jsonView", success());
		}catch(Exception e){
			log.error("日编子任务未规划grid接口异常，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
		
	}

	/**
	 * 日编子任务自动规划
	 * 根据taskId获取未规划的gridId和tips统计
	 * 将未规划的grid自动分配到几个子任务中，尽量保证每个子任务tips数量相近
	 * 应用场景：管理平台—子任务—日编规划—自动规划按钮
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/subtask/autoPlan")
	public ModelAndView autoPlan(HttpServletRequest request){
		try{
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			if(!dataJson.containsKey("subtaskNum")){
				throw new Exception("缺少subtaskNum");
			}
			if(!dataJson.containsKey("taskId")){
				throw new Exception("缺少taskId");
			}
			int taskId = dataJson.getInt("taskId");
			int subtaskNum = dataJson.getInt("subtaskNum");

			SubtaskService.getInstance().autoPlan(taskId, subtaskNum);

			return new ModelAndView("jsonView", success());
		}catch(Exception e){
			log.error("日编子任务自动规划接口异常，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}

	}
}
