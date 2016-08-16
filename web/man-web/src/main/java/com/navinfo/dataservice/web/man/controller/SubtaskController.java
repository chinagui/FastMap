package com.navinfo.dataservice.web.man.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.subtask.SubtaskList;
import com.navinfo.dataservice.api.man.model.subtask.SubtaskListByUser;
import com.navinfo.dataservice.api.man.model.subtask.SubtaskListByWkt;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.web.man.page.SubtaskListByUserPage;
import com.navinfo.dataservice.web.man.page.SubtaskListPage;
import com.navinfo.dataservice.web.man.response.NullResponse;
import com.navinfo.dataservice.web.man.response.SubtaskListByUserResponse;
import com.navinfo.dataservice.web.man.response.SubtaskListByWktResponse;
import com.navinfo.dataservice.web.man.response.SubtaskListResponse;
import com.navinfo.dataservice.web.man.response.SubtaskQueryResponse;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.ApiResponse;


/** 
 * @ClassName: SubtaskController
 * @author songdongyan
 * @date 2016年6月6日
 * @Description: SubtaskController.java
 */
@RestController
@Api(value = "subtask-api", description = "subtask操作")  
@RequestMapping("/subtask") 
@Controller
public class SubtaskController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
//	@Autowired

	/*
	 * 创建一个子任务。
	 */
	@ApiOperation(value = "创建subtask", notes = "创建subtask")  
    @ResponseBody 
	@RequestMapping(value = { "/create" }, method = {RequestMethod.POST,RequestMethod.GET})
	public NullResponse create(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
			,@ApiParam(required =true, name = "parameter", value="{<br/>\"blockId\":\"\",blockId，与taskId只能传一个<br/>\"taskId\":\"\",taskId，与blockId只能传一个<br/>\"type\":\"\",作业要素（0POI，1Road，2一体化）<br/>\"stage\":\"\",作业阶段（0采集，1日编，2月编）<br/>\"descp\":\"\",任务描述<br/>\"planStartDate\":\"\",计划开始时间<br/>\"planEndDate\":\"\",计划结束时间<br/>\"exeUserId\":\"\",作业人员<br/>\"gridIds\":\"\"grid数组<br/>}")@RequestParam( value = "parameter") String parameter
//			,@ApiParam(required =true, name = "file", value="文件")@RequestParam( value = "file") MultipartFile file
			,HttpServletRequest request){
		try{	

			AccessToken tokenObj=(AccessToken) request.getAttribute("token");

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			long userId = tokenObj.getUserId();
			JSONArray gridIds = new JSONArray();
			
			//创建区域专项子任务
			if(dataJson.containsKey("taskId")){
				List<Integer> gridIdList = GridService.getInstance().getGridListByTaskId(dataJson.getInt("taskId"));
				gridIds.addAll(gridIdList);
			}else{
				gridIds = dataJson.getJSONArray("gridIds");
			}
			//根据gridIds获取wkt
			String wkt = GridUtils.grids2Wkt(gridIds);
			if(wkt.contains("MULTIPOLYGON")){
				throw new IllegalArgumentException("请输入符合条件的grids");
			}
			
			Object[] gridIdList = gridIds.toArray();
			dataJson.put("gridIds",gridIdList);
			
			Subtask bean = (Subtask) JsonOperation.jsonToBean(dataJson,Subtask.class);
			bean.setCreateUserId((int)userId);
			bean.setGeometry(wkt);
				
			SubtaskService.getInstance().create(bean);	

			NullResponse result = new NullResponse(0,"创建成功",null);
			return result;
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			NullResponse result = new NullResponse(-1,e.getMessage(),null);
			return result;
		}
	}
	


	/*
	 * 根据几何范围查询任务列表
	 */
	@ApiOperation(value = "根据wkt获取subtask", notes = "根据wkt获取subtask")  
//	@ApiResponses(value = {
//		    @ApiResponse(code = 500, message = "Internal server error")}
//		)
	@RequestMapping(value = { "/listByWkt" }, method = RequestMethod.GET)
//	@RequestMapping(value = "/subtask/listByWkt")
	public SubtaskListByWktResponse listByWkt(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
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

//			//根据需要的返回字段拼装结果
//			List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
//
//			for(int i=0;i<subtaskList.size();i++){
//				HashMap<String, Object> subtask = new HashMap<String, Object>();
//				subtask.put("subtaskId", subtaskList.get(i).getSubtaskId());
//				subtask.put("geometry", subtaskList.get(i).getGeometry());
//				subtask.put("descp", subtaskList.get(i).getDescp());
//				subtask.put("name", subtaskList.get(i).getName());
//				subtask.put("stage", subtaskList.get(i).getStage());
//				subtask.put("status", subtaskList.get(i).getStatus());
//				subtask.put("type", subtaskList.get(i).getType());
//				subtask.put("gridIds", subtaskList.get(i).getGridIds());
//				list.add(subtask);
//			}
//			return new ModelAndView("jsonView", success(list));

		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			SubtaskListByWktResponse responseListByWkt = new SubtaskListByWktResponse(-1,e.getMessage(),null);
			return responseListByWkt;
//			return new ModelAndView("jsonView",exception(e));
		}
	}
	

	@ApiOperation(value = "获取subtask列表", notes = "获取subtask列表")  
	@RequestMapping(value = { "/list" }, method = RequestMethod.GET)
//	@RequestMapping(value = "/subtask/list")
	public SubtaskListResponse list(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
			,@ApiParam(required =true, name = "parameter", value="{<br/>\"stage\":1\\\\作业阶段,<br/>\"condition\":1\\\\搜索条件（JSON），均可选，不支持组合。<br/>\t{\"subtaskId\"子任务Id<br/>\"subtaskName\"子任务名称<br/>\"ExeUserId\"作业员<br/>\"blockId\"所属blockId<br/>\"blockName\"所属block名称<br/>\"taskId\"所属任务id<br/>\"taskName\"所属任务名称},<br/>\"order\":\\\\排序条件（JSON），可选，按照时间查询,json中只有一个有效排序条件，不支持组合排序。<br/>\t{<br/>\"subtaskId\":\"desc\",<br/>\"status\":\"desc\",<br/>\"planStartDate\":\"desc\"//降序，asc升序,<br/>\"planEndDate\":\"desc\",<br/>\"blockId\":\"desc\"},<br/>\"pageNum\":1\\\\页码,<br/>\"pageSize\":1\\\\每页条数<br/>}")@RequestParam( value = "parameter") String postData			,HttpServletRequest request){
		try{		
			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int curPageNum= 1;//默认为第一页
			if(dataJson.containsKey("pageNum")){
				curPageNum = dataJson.getInt("pageNum");
			}
			
			int pageSize = 20;//默认页容量为10
			if(dataJson.containsKey("pageSize")){
				pageSize = dataJson.getInt("pageSize");
			}
			
			JSONObject order =null; 
			if(dataJson.containsKey("order")){
				order=dataJson.getJSONObject("order");
			}
			
			JSONObject condition =null; 
			if(dataJson.containsKey("condition")){
				condition=dataJson.getJSONObject("condition");
			}
			
			//作业阶段
			int stage = dataJson.getInt("stage");
			
			Page page = SubtaskService.getInstance().list(stage,condition,order,pageSize,curPageNum);
			
			
			Map map = new HashMap();
			map.put("pageSize", 1);
			map.put("pageNum", 1);
			map.put("start", 1);
			map.put("totalCount", 1);
			map.put("result", page.getResult());
			
			SubtaskListPage pageList = new SubtaskListPage();
			pageList = (SubtaskListPage)map;

//			SubtaskListPage pageList = new SubtaskListPage(page.getPageSize(),page.thePageNum(),page.getStart(),page.getTotalCount(),(List<SubtaskList>)page.getResult());
			SubtaskListResponse responseList = new SubtaskListResponse(0,"success",pageList);
			return responseList;
//            return new ModelAndView("jsonView", success(page));
		
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			SubtaskListResponse responseList = new SubtaskListResponse(-1,e.getMessage(),null);
			return responseList;
//			return new ModelAndView("jsonView",exception(e));
		}
	}
	
//	@RequestMapping(value = "/subtask/listByUser")
//	public ModelAndView listByUser(HttpServletRequest request){
	@ApiOperation(value = "根据作业员获取子任务列表", notes = "根据作业员获取subtask列表")  
	@RequestMapping(value = { "/listByUser" }, method = RequestMethod.GET)
	public SubtaskListByUserResponse listByUser(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
			,@ApiParam(required =true, name = "parameter", value="{<br/>\"exeUserId\":1\\\\作业员ID,<br/>\"stage\":1\\\\0采集、1日编、2月编,<br/>\"type\":1\\\\0 POI、1道路、2一体化、3专项,<br/>\"status\":1\\\\2进行中、3已完成,<br/>\"snapshot\":1\\\\0 返回全部字段/1 不返回geometry和gridIds,<br/>\"pageNum\":1\\\\页码（默认1，返回首页）,<br/>\"pageSize\":1\\\\每页条数，默认20<br/>}")@RequestParam( value = "parameter") String postData			,HttpServletRequest request){
		try{	

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

            Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
            
            Page page = SubtaskService.getInstance().listByUserPage(bean,snapshot,pageSize,curPageNum);
            
            SubtaskListByUserPage SubtaskListByUserPage = new SubtaskListByUserPage(page.getPageSize(),page.thePageNum(),page.getStart(),page.getTotalCount(),(List<SubtaskListByUser>)page.getResult());
			SubtaskListByUserResponse responseList = new SubtaskListByUserResponse(0,"success",SubtaskListByUserPage);
			return responseList;
            
//            return new ModelAndView("jsonView", success(page));
            
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			SubtaskListByUserResponse responseList = new SubtaskListByUserResponse(-1,e.getMessage(),null);
			return responseList;
//			return new ModelAndView("jsonView",exception(e));
		}
	}
	

	/*
	 * 根据subtaskId查询一个任务的详细信息。
	 */
//	@RequestMapping(value = "/subtask/query")
//	public ModelAndView query(HttpServletRequest request){
	@ApiOperation(value = "查询subtask信息", notes = "查询subtask信息")  
	@RequestMapping(value = { "/query" }, method = RequestMethod.GET)
	public SubtaskQueryResponse query(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
			,@ApiParam(required =true, name = "parameter", value="{\"subtaskId\":1\\子任务ID}")@RequestParam( value = "parameter") String postData
			,HttpServletRequest request){
		try{
			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
			
			Subtask subtask = SubtaskService.getInstance().query(bean);	
			if(subtask!=null&&subtask.getSubtaskId()!=null){
				SubtaskListByUser subtaskListByUser = new SubtaskListByUser(subtask.getSubtaskId()
						,subtask.getName()
						,subtask.getStage()
						,subtask.getType()
						,subtask.getStatus()
						,subtask.getDescp()
						,subtask.getDbId()
						,subtask.getGridIds()
						,subtask.getGeometry()
						, subtask.getPlanStartDate()
						, subtask.getPlanEndDate());
				SubtaskQueryResponse response = new SubtaskQueryResponse(0,"success",subtaskListByUser);
				return response;
			}else{
				throw new Exception("该任务不存在");
			}

//			//根据需要的返回字段拼装结果
//			HashMap<String, Object> data = new HashMap<String, Object>();
//			if(subtask!=null&&subtask.getSubtaskId()!=null){
//				data.put("subtaskId", subtask.getSubtaskId());
//				data.put("stage", subtask.getStage());
//				data.put("type", subtask.getType());
//				data.put("planStartDate", subtask.getPlanStartDate());
//				data.put("planEndDate", subtask.getPlanEndDate());
//				data.put("descp", subtask.getDescp());
//				data.put("name", subtask.getName());
//				data.put("gridIds", subtask.getGridIds());
//				data.put("dbId", subtask.getDbId());
//				data.put("geometry", subtask.getGeometry());
//			}
//			else{
//				throw new Exception("该任务不存在");
//			}
//			
//			JSONObject result = JsonOperation.beanToJson(data);
//			
//			return new ModelAndView("jsonView", success(data));
			
		}catch(Exception e){
			log.error("获取明细失败，原因："+e.getMessage(), e);
			SubtaskQueryResponse response = new SubtaskQueryResponse(0,"success",null);
			return response;
//			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/*
	 * 批量修改子任务详细信息。
	 */
//	@RequestMapping(value = "/subtask/update")
//	public ModelAndView update(HttpServletRequest request){
	@ApiOperation(value = "批量修改子任务详细信息", notes = "批量修改子任务详细信息")  
	@RequestMapping(value = { "/update" }, method = RequestMethod.GET)
	public NullResponse update(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
			,@ApiParam(required =true, name = "parameter", value="{<br/>\"subtasks\":<br/>[<br/>{<br/>\"subtaskId\":32,<br/>\"descp\":\"testtest\",<br/>\"planStartDate\":\"20160430\",<br/>\"planEndDate\":\"20160630\",<br/>\"exeUserId\":21<br/>}<br/>]<br/>} ")@RequestParam( value = "parameter") String postData
			,HttpServletRequest request){
		try{

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			if(!dataJson.containsKey("subtasks")){
//				return new ModelAndView("jsonView", success("修改成功"));
				NullResponse result = new NullResponse(-1,"请输入subtasks",null);
				return result;
			}
			
			JSONArray subtaskArray=dataJson.getJSONArray("subtasks");
			List<Subtask> subtaskList = new ArrayList<Subtask>();
			for(int i = 0;i<subtaskArray.size();i++){
				Subtask subtask = (Subtask)JsonOperation.jsonToBean(subtaskArray.getJSONObject(i),Subtask.class);
				subtaskList.add(subtask);
			}
			
			List<Integer> updatedSubtaskIdList = SubtaskService.getInstance().update(subtaskList);
			
			String message = "批量修改子任务：" + updatedSubtaskIdList.size() + "个成功，" + (subtaskList.size() - updatedSubtaskIdList.size()) + "个失败。";
			
//			return new ModelAndView("jsonView", success(message));
			NullResponse result = new NullResponse(0,"success",null);
			return result;
			
		}catch(Exception e){
			log.error("更新失败，原因："+e.getMessage(), e);
//			return new ModelAndView("jsonView",exception(e));
			NullResponse result = new NullResponse(-1,e.getMessage(),null);
			return result;
		}
	}
	
	/*
	 * 关闭多个子任务。
	 */
//	@RequestMapping(value = "/subtask/close")
//	public ModelAndView close(HttpServletRequest request){
	@ApiOperation(value = "关闭subtask", notes = "关闭subtask")  
	@RequestMapping(value = { "/close" }, method = RequestMethod.GET)
	public NullResponse close(@ApiParam(required =true, name = "access_token", value="接口调用凭证")@RequestParam( value = "access_token") String access_token
			,@ApiParam(required =true, name = "parameter", value="{<br/>\"subtaskIds\":[12]#子任务列表<br/>	}")@RequestParam( value = "parameter") String parameter
			,HttpServletRequest request){
		try{		
			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			if(!dataJson.containsKey("subtaskIds")){
				throw new IllegalArgumentException("subtaskIds不能为空。");
//				return new ModelAndView("jsonView", exception("请传subtaskId"));
			}
			
			JSONArray subtaskIds = dataJson.getJSONArray("subtaskIds");
			
			List<Integer> subtaskIdList = (List<Integer>)JSONArray.toCollection(subtaskIds,Integer.class);
			List<Integer> unClosedSubtaskList = SubtaskService.getInstance().close(subtaskIdList);
			
			String message = "批量关闭子任务：" + (subtaskIdList.size() - unClosedSubtaskList.size()) + "个成功，" + unClosedSubtaskList.size() + "个失败。";
			
//			return new ModelAndView("jsonView", success(message));
			NullResponse result = new NullResponse(0,message,null);
			return result;
		
		}catch(Exception e){
			log.error("批量关闭失败，原因："+e.getMessage(), e);
//			return new ModelAndView("jsonView",exception(e));
			NullResponse result = new NullResponse(-1,e.getMessage(),null);
			return result;
		}
	}
	

}
