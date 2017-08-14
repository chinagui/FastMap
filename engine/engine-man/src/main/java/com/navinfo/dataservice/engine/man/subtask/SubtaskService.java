package com.navinfo.dataservice.engine.man.subtask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.commons.kmeans.KPoint;
import com.navinfo.dataservice.commons.kmeans.Kmeans;
import com.navinfo.dataservice.engine.man.job.bean.JobType;
import com.navinfo.dataservice.engine.man.log.ManLogOperation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.model.Block;
import com.navinfo.dataservice.api.man.model.Infor;
import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Program;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.token.AccessTokenFactory;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.PageModelUtils;
import com.navinfo.dataservice.commons.util.QuikSortListUtils;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.dao.mq.email.EmailPublisher;
import com.navinfo.dataservice.engine.man.block.BlockService;
import com.navinfo.dataservice.engine.man.infor.InforService;
import com.navinfo.dataservice.engine.man.message.MessageService;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.statics.StaticsService;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.engine.man.timeline.TimelineService;
import com.navinfo.dataservice.engine.man.userGroup.UserGroupService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @ClassName: SubtaskService
 * @author code generator
 * @date 2016-06-06 07:40:14
 * @Description: TODO
 */

public class SubtaskService {
	private static final Logger log = LoggerRepos.getLogger(SubtaskService.class);

	private SubtaskService() {
	}

	private static class SingletonHolder {
		private static final SubtaskService INSTANCE = new SubtaskService();
	}

	public static SubtaskService getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * @Title: create
	 * @Description: 创建子任务。
	 * @param userId
	 * @param dataJson
	 * @throws ServiceException 
	 * @throws ParseException 
	 */
	public void create(long userId,JSONObject dataJson) throws ServiceException, ParseException{
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			int referId =0;
			SubtaskRefer myRefer=null;
			if(dataJson.containsKey("referId")){
				referId = dataJson.getInt("referId");
				if(referId!=0){
					JSONObject condition=new JSONObject();
					JSONArray ids=new JSONArray();
					ids.add(referId);
					condition.put("ids", ids);
					List<SubtaskRefer> referObj = queryReferByTaskId(conn,condition,true);
					if(referObj!=null&&referObj.size()>0){
						myRefer=referObj.get(0);
					}
				}
			}
			//处理grid：1list转map;2根据grid计算几何
			if(dataJson.containsKey("gridIds")){
				JSONArray gridIds = dataJson.getJSONArray("gridIds");
				if(!gridIds.isEmpty() || gridIds.size()>0){
					Map<String,Integer> gridIdMap = new HashMap<String,Integer>();
					for(Object gridId:gridIds.toArray()){
						gridIdMap.put(gridId.toString(), 1);
					}
					dataJson.put("gridIds",gridIdMap);
					String wkt = GridUtils.grids2Wkt(gridIds);
					dataJson.put("geometry",wkt);	
				}
			}else if(myRefer!=null){
				Set<String> grids = CompGeometryUtil.geo2GridsWithoutBreak(myRefer.getGeometry());
				Map<String,Integer> gridIdMap = new HashMap<String,Integer>();
				for(String gridId:grids){
					gridIdMap.put(gridId, 1);
				}
				dataJson.put("gridIds",gridIdMap);
				String wkt = GridUtils.grids2Wkt(JSONArray.fromObject(grids));
				dataJson.put("geometry",wkt);
			}else{
				int taskId = dataJson.getInt("taskId");
				Map<Integer,Integer> gridIdMap = TaskService.getInstance().getGridMapByTaskId(taskId);
				Map<String,Integer> gridIdMap2 = new HashMap<String,Integer>();
				for(Map.Entry<Integer, Integer> entry:gridIdMap.entrySet()){
					gridIdMap2.put(entry.getKey().toString(), entry.getValue());
				}
				dataJson.put("gridIds",gridIdMap2);
				String wkt = GridUtils.grids2Wkt(JSONArray.fromObject(gridIdMap.keySet()));
				dataJson.put("geometry",wkt);
			}
			
			if(dataJson.containsKey("qualityMethod") && dataJson.getJSONArray("qualityMethod").size() == 0){
				dataJson.remove("qualityMethod");
			}
			int qualityMethod=0;//质检方式仅作用于质检子任务
			if(dataJson.containsKey("qualityMethod")){
				JSONArray qualityMethodArr = dataJson.getJSONArray("qualityMethod");
				dataJson.discard("qualityMethod");
				if(qualityMethodArr.contains(1)&&qualityMethodArr.contains(2)){
					qualityMethod=3;
				}
				if(qualityMethodArr.contains(1)&&!qualityMethodArr.contains(2)){
					qualityMethod=1;
				}
				if(!qualityMethodArr.contains(1)&&qualityMethodArr.contains(2)){
					qualityMethod=2;
				}
			}
			
			//创建质检子任务
			//这里变量的创建都放在判断里，减小不必要的内存占用
			int qualitySubtaskId = 0;
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			if(dataJson.containsKey("hasQuality") && dataJson.getInt("hasQuality") == 1){
				int qualityExeUserId = 0;
				int qualityExeGroupId = 0;
				if(dataJson.containsKey("qualityExeUserId")){
					qualityExeUserId=dataJson.getInt("qualityExeUserId");
					dataJson.discard("qualityExeUserId");//删除 是否新建质检子任务标识 ,因为Subtask实体类里灭幼这个字段
				}
				if(dataJson.containsKey("qualityExeGroupId")){
					qualityExeGroupId=dataJson.getInt("qualityExeGroupId");
					dataJson.discard("qualityExeGroupId");
				}
				String qualityPlanStartDate = dataJson.getString("qualityPlanStartDate");
				String qualityPlanEndDate = dataJson.getString("qualityPlanEndDate");
				//删除传入参数的对应键值对,因为bean中没有这些字段
				dataJson.discard("qualityPlanStartDate");
				dataJson.discard("qualityPlanEndDate");
				dataJson.discard("hasQuality");
				
				Subtask qualityBean = createSubtaskBean(userId,dataJson);
				if(!StringUtils.isEmpty(qualityBean.getName())){
					qualityBean.setName(qualityBean.getName()+"_质检");}
				qualityBean.setIsQuality(1);
				qualityBean.setStatus(2);
				qualityBean.setQualityMethod(qualityMethod);
				qualityBean.setExeUserId(qualityExeUserId);
				//这里添加了操作组的赋值，创建月编质检子任务的时候，作业组ID前端单独传这个字段
				qualityBean.setExeGroupId(qualityExeGroupId);
				qualityBean.setPlanStartDate(new Timestamp(df.parse(qualityPlanStartDate).getTime()));
				qualityBean.setPlanEndDate(new Timestamp(df.parse(qualityPlanEndDate).getTime()));
				//创建质检子任务 subtask	
				qualitySubtaskId = createSubtaskWithSubtaskId(conn,qualityBean);
			}
			
			//根据参数生成subtask bean
			Subtask bean = createSubtaskBean(userId,dataJson);
			bean.setIsQuality(0);
			if(qualitySubtaskId!=0){
				bean.setQualitySubtaskId(qualitySubtaskId);
			}
			//创建subtask	
			createSubtaskWithSubtaskId(conn,bean);
			
			//质检子任务继承子任务名称
			if(qualitySubtaskId!=0){
				updateQualityName(conn,qualitySubtaskId);
			}
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	

	/**
	 * 修改质检子任务名称，按照常规子任务_质检的原则
	 * @param conn
	 * @param qualityTaskId
	 * @throws ServiceException 
	 */
	public void updateQualityName(Connection conn,int qualityTaskId) throws ServiceException{
		try {
			String updateSql="UPDATE SUBTASK"
					+ "   SET NAME ="
					+ "       (SELECT T.NAME || '_质检'"
					+ "          FROM SUBTASK T"
					+ "         WHERE T.QUALITY_SUBTASK_ID = "+qualityTaskId+")"
					+ " WHERE SUBTASK_ID = "+qualityTaskId;
			QueryRunner run = new QueryRunner();
			log.info("修改质检子任务名称:"+updateSql);
			run.update(conn, updateSql);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改质检子任务名称，原因为:" + e.getMessage(), e);
		}
	}
	
	/**
	 * 子任务创建的时候，若没有名称，会默认为情报子任务，并自动按照情报原则进行赋值
	 * @Title: createSubtask
	 * @Description: 创建一个子任务。
	 * @param Subtask对象
	 * @return
	 * @throws ServiceException  Subtask
	 * @throws 
	 */
	public int createSubtask(Subtask bean)throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			return createSubtaskWithSubtaskId(conn, bean);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * @param conn
	 * @param subtask
	 * @throws ServiceException 
	 */
	public int createSubtaskWithSubtaskId(Connection conn, Subtask bean) throws ServiceException {
		try {			
			//默认subtask状态为草稿2
			if(bean.getStatus()== null){
				bean.setStatus(2);
			}
			// 获取subtaskId，名称赋值的时候需要用到子任务id，所以必须放在前面
			int subtaskId = SubtaskOperation.getSubtaskId(conn);
			bean.setSubtaskId(subtaskId);
			//情报项目为空时，需要后台自动创建名称
			bean=autoInforName(conn,bean);
			
			// 插入subtask
			SubtaskOperation.insertSubtask(conn, bean);
			
			// 插入SUBTASK_GRID_MAPPING
			if(bean.getGridIds() != null){
				SubtaskOperation.insertSubtaskGridMapping(conn, bean);
				/*
				 * 2017.07.06 zl
				 * //web端对于通过不规则任务圈创建的常规子任务，可能会出现grid计算超出block范围的情况（web无法解决），在此处进行二次处理
				List<Integer> deleteGrids = SubtaskOperation.checkSubtaskGridMapping(conn, bean);
				if(deleteGrids!=null&&deleteGrids.size()>0){
					updateSubtaskGeo(conn,bean.getSubtaskId());
				}*/
			}
			log.debug("子任务创建成功!");
			return subtaskId;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		}
	}
	
	private void updateSubtaskGeo(Connection conn,int subtaskId)throws Exception{
		try{
			Map<Integer, Integer> gridMap = SubtaskOperation.getGridIdsBySubtaskIdWithConn(conn, subtaskId);
			JSONArray newGrids=new JSONArray();
			newGrids.addAll(gridMap.keySet());
			SubtaskOperation.updateSubtaskGeo(conn, GridUtils.grids2Wkt(newGrids), subtaskId);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		}
	}
	
	/*
	 * 修改子任务详细信息。 
	 */
	public void update(JSONObject dataJson, long userId) throws ServiceException, ParseException, Exception {
		Connection conn = null;
		try {
			// 持久化
			conn = DBConnector.getInstance().getManConnection();
			List<Subtask> subtaskList = new ArrayList<Subtask>();
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			
			int referId =0;
			SubtaskRefer myRefer=null;
			if(dataJson.containsKey("referId")){
				referId = dataJson.getInt("referId");
				if(referId!=0){
					JSONObject condition=new JSONObject();
					JSONArray ids=new JSONArray();
					ids.add(referId);
					condition.put("ids", ids);
					List<SubtaskRefer> referObj = queryReferByTaskId(conn,condition,true);
					if(referObj!=null&&referObj.size()>0){
						myRefer=referObj.get(0);
					}
				}
			}
			
			//处理grid：1list转map;2根据grid计算几何
			if(dataJson.containsKey("gridIds")){
				JSONArray gridIds = dataJson.getJSONArray("gridIds");
				if(!gridIds.isEmpty() || gridIds.size()>0){
					Map<String,Integer> gridIdMap = new HashMap<String,Integer>();
					for(Object gridId:gridIds.toArray()){
						gridIdMap.put(gridId.toString(), 1);
					}
					dataJson.put("gridIds",gridIdMap);
					String wkt = GridUtils.grids2Wkt(gridIds);
					dataJson.put("geometry",wkt);	
				}
			}else if(myRefer!=null){
				Set<String> grids = CompGeometryUtil.geo2GridsWithoutBreak(myRefer.getGeometry());
				Map<String,Integer> gridIdMap = new HashMap<String,Integer>();
				for(String gridId:grids){
					gridIdMap.put(gridId, 1);
				}
				dataJson.put("gridIds",gridIdMap);
				String wkt = GridUtils.grids2Wkt(JSONArray.fromObject(grids));
				dataJson.put("geometry",wkt);
			}else{
				int taskId = dataJson.getInt("taskId");
				Map<Integer,Integer> gridIdMap = TaskService.getInstance().getGridMapByTaskId(taskId);
				Map<String,Integer> gridIdMap2 = new HashMap<String,Integer>();
				for(Map.Entry<Integer, Integer> entry:gridIdMap.entrySet()){
					gridIdMap2.put(entry.getKey().toString(), entry.getValue());
				}
				dataJson.put("gridIds",gridIdMap2);
				String wkt = GridUtils.grids2Wkt(JSONArray.fromObject(gridIdMap.keySet()));
				dataJson.put("geometry",wkt);
			}
			
			//modify by songhe
			//这里添加了一个质检方式上传的为空的判断，上传了这个字段，但是内容为空的时候直接不处理
			if(dataJson.containsKey("qualityMethod") && dataJson.getJSONArray("qualityMethod").size() == 0){
				dataJson.remove("qualityMethod");
			}
			int qualityMethod=0;//质检方式仅作用于质检子任务
			if(dataJson.containsKey("qualityMethod")){
				JSONArray qualityMethodArr = dataJson.getJSONArray("qualityMethod");
				dataJson.discard("qualityMethod");
				if(qualityMethodArr.contains(1)&&qualityMethodArr.contains(2)){
					qualityMethod=3;
				}
				if(qualityMethodArr.contains(1)&&!qualityMethodArr.contains(2)){
					qualityMethod=1;
				}
				if(!qualityMethodArr.contains(1)&&qualityMethodArr.contains(2)){
					qualityMethod=2;
				}
			}
			
			int qualitySubtaskId = 0;//质检子任务id
			int qualityExeUserId = 0;//是否新建质检子任务标识
			String qualityPlanStartDate = "";
			String qualityPlanEndDate ="";
			int qualityExeGroupId = 0;
			int hasQuality = 0;
				
			if(dataJson.containsKey("qualitySubtaskId")){
				qualitySubtaskId = dataJson.getInt("qualitySubtaskId");
				//删除 质检子任务id ,因为质检子任务Subtask实体类里不应该有这个字段
				dataJson.discard("qualitySubtaskId");
			}
			//是否创建质检子任务，这里更改了创建的标识字段为isQuailty
			if(dataJson.containsKey("hasQuality")){
				hasQuality = dataJson.getInt("hasQuality");	
				dataJson.discard("hasQuality");
				
				if(dataJson.containsKey("qualityExeUserId")){
					qualityExeUserId=dataJson.getInt("qualityExeUserId");
					dataJson.discard("qualityExeUserId");//删除 是否新建质检子任务标识 ,因为Subtask实体类里灭幼这个字段
				}
				if(dataJson.containsKey("qualityExeGroupId")){
					qualityExeGroupId=dataJson.getInt("qualityExeGroupId");
					dataJson.discard("qualityExeGroupId");
				}
				if(dataJson.containsKey("qualityPlanStartDate")){
					qualityPlanStartDate = dataJson.getString("qualityPlanStartDate");
					qualityPlanEndDate = dataJson.getString("qualityPlanEndDate");								
					dataJson.discard("qualityPlanStartDate");//删除 质检子任务计划开始时间 ,因为Subtask实体类里灭幼这个字段
					dataJson.discard("qualityPlanEndDate");//删除 质检子任务计划结束时间 ,因为Subtask实体类里灭幼这个字段
				}
			}				
			//正常修改子任务
			Subtask subtask = createSubtaskBean(userId,dataJson);
			Integer newQualitySubtaskId=qualitySubtaskId;
			//创建或者修改常规任务时，均要调用修改质检任务的代码
			if(qualitySubtaskId != 0){//非0的时候，表示要修改质检子任务
				Subtask qualitySubtask = new Subtask();//生成质检子任务的bean
				qualitySubtask.setCreateUserId(Integer.valueOf(String.valueOf(userId)));
				qualitySubtask.setSubtaskId(qualitySubtaskId);
				qualitySubtask.setExeUserId(qualityExeUserId);
				qualitySubtask.setIsQuality(1);//表示此bean是质检子任务
				qualitySubtask.setQualityMethod(qualityMethod);
				//qualitySubtask.setName(qualitySubtask.getName()+"_质检");
				qualitySubtask.setPlanStartDate(new Timestamp(df.parse(qualityPlanStartDate).getTime()));
				qualitySubtask.setPlanEndDate(new Timestamp(df.parse(qualityPlanEndDate).getTime()));
				qualitySubtask.setExeGroupId(qualityExeGroupId);
				subtaskList.add(qualitySubtask);//将质检子任务也加入修改列表
			}else{
				if(hasQuality == 1){//qualitySubtaskId=0，且isQuailty为1的时候，表示要创建质检子任务
					Subtask qualitySubtask = SubtaskService.getInstance().queryBySubtaskIdS(conn,subtask.getSubtaskId());
					if(!StringUtils.isEmpty(subtask.getName())){
						qualitySubtask.setName(subtask.getName()+"_质检");}
					qualitySubtask.setCreateUserId(Integer.valueOf(String.valueOf(userId)));
					qualitySubtask.setSubtaskId(null);
					qualitySubtask.setExeGroupId(qualityExeGroupId);
					qualitySubtask.setPlanStartDate(new Timestamp(df.parse(qualityPlanStartDate).getTime()));
					qualitySubtask.setPlanEndDate(new Timestamp(df.parse(qualityPlanEndDate).getTime()));
					qualitySubtask.setIsQuality(1);//表示此bean是质检子任务
					qualitySubtask.setExeUserId(qualityExeUserId);
					qualitySubtask.setQualityMethod(qualityMethod);	
					//创建质检子任务 subtask	
					newQualitySubtaskId = createSubtaskWithSubtaskId(conn,qualitySubtask);	
					subtask.setIsQuality(0);
					subtask.setQualitySubtaskId(newQualitySubtaskId);
				}
			}
			subtaskList.add(subtask);
	
			updateSubtask(conn,subtaskList,userId);
			updateQualityName(conn, newQualitySubtaskId);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

//	/*
//	 * 批量修改子任务详细信息。 参数：Subtask对象列表
//	 */
//	public List<Integer> updateSubtask(List<Subtask> subtaskList, long userId) throws ServiceException {
//		Connection conn = null;
//		try {
//			// 持久化
//			conn = DBConnector.getInstance().getManConnection();
//			
//			return updateSubtask(conn,subtaskList,userId);
//
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("修改失败，原因为:" + e.getMessage(), e);
//		} finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}
	
	/*
	 * 批量修改子任务详细信息。 参数：Subtask对象列表
	 */
	public List<Integer> updateSubtask(Connection conn,List<Subtask> subtaskList, long userId) throws ServiceException {
		try {
			List<Integer> updatedSubtaskIdList = new ArrayList<Integer>();
			for (int i = 0; i < subtaskList.size(); i++) {
				Subtask subtask = subtaskList.get(i);
				//情报子任务修改时，若填入执行人，则需修改子任务名称
				subtask=autoInforName(conn,subtask);
				SubtaskOperation.updateSubtask(conn, subtask);
				updatedSubtaskIdList.add(subtask.getSubtaskId());
				
			}
			//发送消息
			try {
				List<Integer> subtaskIdList = new ArrayList<Integer>();
				for (Subtask subtask : subtaskList) {
					//消息发布
					if(subtask!=null&&subtask.getStatus()!=null &&subtask.getStatus()==1){
						subtaskIdList.add(subtask.getSubtaskId());
					}
				}
				if(subtaskIdList.size()==0){return updatedSubtaskIdList;}
				//查询子任务
				if(!subtaskIdList.isEmpty()){
					List<Subtask> list = SubtaskOperation.getSubtaskListBySubtaskIdList(conn,subtaskIdList);
					if(list != null && list.size()>0){
						for (Subtask subtask : list) {
							SubtaskOperation.pushMessage(conn,subtask,userId);
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				log.error("发送失败,原因:"+e.getMessage(), e);
			}
			
			return updatedSubtaskIdList;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:" + e.getMessage(), e);
		} 
	}
		
	/**
	 * 情报子任务自动维护名称，命名原则：情报名称_发布时间_作业员/作业组_子任务ID
	 * 1.质检子任务名称也同样维护
	 * 1.判断是否情报子任务，不是则返回
	 * 2.判断是否新建子任务，若是，名称若为空，自动赋值
	 * 3.修改子任务，若作业员或组是修改时加的，则自动维护名称
	 *  
	 * @param conn
	 * @param newSubtask
	 * @return
	 * @throws Exception
	 */
	public Subtask autoInforName(Connection conn,Subtask newSubtask) throws Exception{
		if(newSubtask.getIsQuality()!=null&&newSubtask.getIsQuality()==1){return newSubtask;};//表示此bean是质检子任务,不做处理
		//if(newSubtask.getExeUserId()==0||newSubtask.getExeGroupId()==0){return newSubtask;}
		Subtask oldSubtask=null;
		if(newSubtask.getSubtaskId()!=0){
			oldSubtask = queryBySubtaskIdS(conn,newSubtask.getSubtaskId());
		}
		int taskId=newSubtask.getTaskId();
		if(taskId==0&&oldSubtask!=null){taskId=oldSubtask.getTaskId();}
		
		Task task = TaskService.getInstance().queryByTaskId(conn, taskId);
		Infor infor = InforService.getInstance().getInforByProgramId(conn, task.getProgramId());
		if(infor==null){
//			//中线子任务，若作业员或组是修改时加的，则自动维护名称：任务名称_作业员
//			if(oldSubtask!=null){
//				if(newSubtask.getExeUserId()!=0&&oldSubtask.getExeUserId()==0){
//					UserInfo userInfo = UserInfoService.getInstance().queryUserInfoByUserId(newSubtask.getExeUserId());
//					newSubtask.setName(newSubtask.getName()+"_"+userInfo.getUserRealName());
//					if(newSubtask.getIsQuality()!=null&&newSubtask.getIsQuality()==1){newSubtask.setName(newSubtask.getName()+"_质检");}
//				}
//				if(newSubtask.getExeGroupId()!=0&&oldSubtask.getExeGroupId()==0){
//					String groupName = UserGroupService.getInstance().getGroupNameByGroupId(newSubtask.getExeGroupId());
//					newSubtask.setName(newSubtask.getName()+"_"+groupName);
//					if(newSubtask.getIsQuality()!=null&&newSubtask.getIsQuality()==1){newSubtask.setName(newSubtask.getName()+"_质检");}
//				}
//			}
			return newSubtask;
		}		
		//快线子任务名称自动赋值
		if(oldSubtask==null){//新建子任务
			if(!StringUtils.isEmpty(newSubtask.getName())){return newSubtask;}
			newSubtask.setName(infor.getInforName()+"_"+DateUtils.dateToString(infor.getPublishDate(), "yyyyMMdd"));
			if(newSubtask.getExeUserId()!=0){
				UserInfo userInfo = UserInfoService.getInstance().queryUserInfoByUserId(newSubtask.getExeUserId());
				newSubtask.setName(newSubtask.getName()+"_"+userInfo.getUserRealName()+"_"+newSubtask.getSubtaskId());
				if(newSubtask.getIsQuality()!=null&&newSubtask.getIsQuality()==1){newSubtask.setName(newSubtask.getName()+"_质检");}
			}
			if(newSubtask.getExeGroupId()!=0){
				String groupName = UserGroupService.getInstance().getGroupNameByGroupId(newSubtask.getExeGroupId());
				newSubtask.setName(newSubtask.getName()+"_"+groupName+"_"+newSubtask.getSubtaskId());
				if(newSubtask.getIsQuality()!=null&&newSubtask.getIsQuality()==1){newSubtask.setName(newSubtask.getName()+"_质检");}
			}	
		}else{
			newSubtask.setName(infor.getInforName()+"_"+DateUtils.dateToString(infor.getPublishDate(), "yyyyMMdd"));
			if(newSubtask.getExeUserId()!=0&&newSubtask.getExeUserId()!=oldSubtask.getExeUserId()){
				UserInfo userInfo = UserInfoService.getInstance().queryUserInfoByUserId(newSubtask.getExeUserId());
				newSubtask.setName(newSubtask.getName()+"_"+userInfo.getUserRealName()+"_"+newSubtask.getSubtaskId());
				if(newSubtask.getIsQuality()!=null&&newSubtask.getIsQuality().intValue()==1){newSubtask.setName(newSubtask.getName()+"_质检");}
			}
			if(newSubtask.getExeGroupId()!=0&&newSubtask.getExeGroupId()!=oldSubtask.getExeGroupId()){
				String groupName = UserGroupService.getInstance().getGroupNameByGroupId(newSubtask.getExeGroupId());
				newSubtask.setName(newSubtask.getName()+"_"+groupName+"_"+newSubtask.getSubtaskId());
				if(newSubtask.getIsQuality()!=null&&newSubtask.getIsQuality()==1){newSubtask.setName(newSubtask.getName()+"_质检");}
			}
		}
		return newSubtask;
	}

	/*
	 * 根据subtaskId查询一个任务的详细信息。 参数为Subtask对象
	 */
	
	public Subtask queryBySubtaskIdS(Integer subtaskId) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			return queryBySubtaskIdS(conn,subtaskId);		
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/*
	 * 根据subtaskId查询一个任务的详细信息。 参数为Subtask对象
	 */
	
	public Subtask queryBySubtaskIdS(Connection conn,Integer subtaskId) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT ST.SUBTASK_ID,ST.NAME,ST.STATUS,ST.STAGE,ST.DESCP,ST.PLAN_START_DATE,ST.PLAN_END_DATE,ST.TYPE,ST.GEOMETRY,ST.REFER_ID");
			sb.append(",ST.EXE_USER_ID,ST.EXE_GROUP_ID");
			sb.append(",T.TASK_ID,T.TYPE TASK_TYPE,R.DAILY_DB_ID,R.MONTHLY_DB_ID,st.is_quality,st.QUALITY_METHOD,ST.WORK_KIND ");
			sb.append(" FROM SUBTASK ST,TASK T,REGION R");
			sb.append(" WHERE ST.TASK_ID = T.TASK_ID");
			sb.append(" AND T.REGION_ID = R.REGION_ID");
			sb.append(" AND ST.SUBTASK_ID = " + subtaskId);
	
			String selectSql = sb.toString();
			log.info("请求子任务详情SQL："+sb.toString());
			

			ResultSetHandler<Subtask> rsHandler = new ResultSetHandler<Subtask>() {
				public Subtask handle(ResultSet rs) throws SQLException {
					//StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
					if (rs.next()) {
						Subtask subtask = new Subtask();						
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setName(rs.getString("NAME"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						subtask.setDescp(rs.getString("DESCP"));
						subtask.setStatus(rs.getInt("STATUS"));
						subtask.setReferId(rs.getInt("REFER_ID"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setExeUserId(rs.getInt("EXE_USER_ID"));
						subtask.setExeGroupId(rs.getInt("EXE_GROUP_ID"));
						subtask.setIsQuality(rs.getInt("IS_QUALITY"));
						subtask.setQualityMethod(rs.getInt("QUALITY_METHOD"));
						// 增加workKind
						subtask.setWorkKind(rs.getInt("WORK_KIND"));
						
						//GEOMETRY
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.setGeometry(GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						subtask.setTaskId(rs.getInt("TASK_ID"));
						if (2 == rs.getInt("STAGE")) {
							//月编子任务
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
						} else {
							subtask.setDbId(rs.getInt("DAILY_DB_ID"));
						}				
						
						return subtask;
					}
					return null;
				}	
			};
			log.info("queryAdminIdBySubtaskS sql:" + sb.toString());
			Subtask subtask = run.query(conn, selectSql,rsHandler);
			if(subtask!=null&&subtask.getSubtaskId()!=null){
				Map<Integer,Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskIdWithConn(conn,subtask.getSubtaskId());
				subtask.setGridIds(gridIds);
			}
			return subtask;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} 
	}
	
	/**
	 * 获取subtask详情
	 * @param subtaskId
	 * @return
	 * @throws ServiceException
	 */
	
	public Map<String,Object> query(int subtaskId,int platform) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			Map<String,Object> subtaskMap= queryBySubtaskId(conn,subtaskId); 
			//增加平台参数。0：采集端，1：编辑平台2管理平台（grids返回值不一样）
			if(platform==2){//管理平台有不规则圈的子任务，则不返回其自身的坐标和grid
				if(subtaskMap.containsKey("referGeometry")){
					subtaskMap.remove("geometry");
					subtaskMap.remove("geometryJSON");
					subtaskMap.remove("gridIds");
				}
			}
			if(platform==0||platform==1){
				if(subtaskMap!=null&&subtaskMap.containsKey("gridIds")){
					Map<Integer,Integer> gridIds=(Map<Integer, Integer>) subtaskMap.get("gridIds");
					subtaskMap.put("gridIds",gridIds.keySet());
				}
				int isQuality=(int) subtaskMap.get("isQuality");
				if(isQuality==1){
					Set<Integer> subtaskIds=new HashSet<Integer>();
					subtaskIds.add(subtaskId);
					Map<Integer, List<SubtaskQuality>> qualitys = SubtaskQualityOperation.queryBySubtaskIds(conn, subtaskIds);
					if(qualitys.containsKey(subtaskId)){
						List<String> qualityGeos=new ArrayList<String>();
						List<JSONObject> qualityGeosJSON=new ArrayList<JSONObject>();
						for(SubtaskQuality qtmp:qualitys.get(subtaskId)){
							qualityGeos.add(GeoTranslator.jts2Wkt(qtmp.getGeometry()));
							qualityGeosJSON.add(GeoTranslator.jts2Geojson(qtmp.getGeometry()));
						}
						subtaskMap.put("qualityGeos", qualityGeos);
						subtaskMap.put("qualityGeosJSON", qualityGeosJSON);
					}
				}
			}
			// 计算子任务对应大区库所包含图幅号，用于判定接边作业
			List<Integer> meshes = listDbMeshesBySubtask(conn, subtaskId);
			subtaskMap.put("meshes", meshes);

			return subtaskMap;	
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}		
	}
	
	/**
	 * 获取subtask详情
	 * @param subtaskId
	 * @return
	 * @throws ServiceException
	 */
	
	public Map<String,Object> queryBySubtaskId(Connection conn,Integer subtaskId) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(" SELECT ST.SUBTASK_ID,                           ");
			sb.append("        ST.NAME,                                 ");
			sb.append("        ST.QUALITY_METHOD,                       ");
			sb.append("        ST.STATUS,                               ");
			sb.append("        ST.STAGE,                                ");
			sb.append("        ST.DESCP,                                ");
			sb.append("        ST.PLAN_START_DATE,                      ");
			sb.append("        ST.PLAN_END_DATE,                        ");
			sb.append("        ST.TYPE,                                 ");
			sb.append("        ST.GEOMETRY,                             ");
			sb.append("        ST.REFER_ID,                             ");
			sb.append("        ST.EXE_USER_ID,                          ");
			sb.append("        ST.work_kind,                          ");
			sb.append("        ST.EXE_GROUP_ID,                         ");
			sb.append("        ST.QUALITY_SUBTASK_ID,                   ");
			sb.append("        ST.IS_QUALITY,                           ");
			sb.append("        sr.geometry refer_GEOMETRY,                             ");
			sb.append("        T.TASK_ID,                               ");
			sb.append("        T.TYPE                TASK_TYPE,         ");
			sb.append("        R.DAILY_DB_ID,                           ");
			sb.append("        R.MONTHLY_DB_ID,                         ");
			sb.append("        T.NAME                TASK_NAME,         ");
			sb.append("        T.TASK_ID,                               ");
			sb.append("        P.TYPE PROGRAM_TYPE                      ");
			sb.append("   FROM SUBTASK ST, TASK T, REGION R,PROGRAM P,subtask_refer sr ");
			sb.append("  WHERE ST.TASK_ID = T.TASK_ID                   ");
			sb.append("    AND T.REGION_ID = R.REGION_ID                ");
			sb.append("    AND st.refer_id = sr.id(+)                ");
			sb.append("    AND T.PROGRAM_ID = P.PROGRAM_ID              ");
			sb.append("    AND ST.SUBTASK_ID = " + subtaskId);

			String selectSql = sb.toString();

			ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
					    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
						Map<String,Object> subtask = new HashMap<String,Object>();						
						subtask.put("subtaskId",rs.getInt("SUBTASK_ID"));
						subtask.put("name",rs.getString("NAME"));
						subtask.put("taskName",rs.getString("TASK_NAME"));
						subtask.put("type",rs.getInt("TYPE"));
						subtask.put("planStartDate",df.format(rs.getTimestamp("PLAN_START_DATE")));
						subtask.put("planEndDate",df.format(rs.getTimestamp("PLAN_END_DATE")));
						subtask.put("descp",rs.getString("DESCP"));
						subtask.put("status",rs.getInt("STATUS"));
						subtask.put("stage",rs.getInt("STAGE"));
						int referId=rs.getInt("REFER_ID");
						subtask.put("referId",referId);
						subtask.put("taskId",rs.getInt("TASK_ID"));
						subtask.put("workKind",rs.getInt("WORK_KIND"));
						subtask.put("programType",rs.getString("PROGRAM_TYPE"));
						subtask.put("isQuality", rs.getInt("IS_QUALITY"));
						
						
						//作业员/作业组信息
						int exeUserId = rs.getInt("EXE_USER_ID");
						int exeGroupId = rs.getInt("EXE_GROUP_ID");
						subtask.put("exeUserId",exeUserId);
						subtask.put("executerId",exeUserId);
						subtask.put("exeGroupId",exeGroupId);
						
						subtask.put("qualitySubtaskId",rs.getInt("QUALITY_SUBTASK_ID"));
						subtask.put("hasQuality",0);
						//获取质检任务信息
						if(!subtask.get("qualitySubtaskId").toString().equals("0")){
							subtask.put("hasQuality",1);
						}
						
						//GEOMETRY
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.put("geometry",GeoTranslator.struct2Wkt(struct));
							String clobStr = GeoTranslator.struct2Wkt(struct);
							subtask.put("geometryJSON",Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							log.warn("子任务geometry查询错误,"+e1.getMessage());
						}
						//refer_GEOMETRY
						STRUCT referStruct = (STRUCT) rs.getObject("refer_GEOMETRY");
						try {
							subtask.put("referGeometry",GeoTranslator.struct2Wkt(referStruct));
							String referClobStr = GeoTranslator.struct2Wkt(referStruct);
							subtask.put("referGeometryJSON",Geojson.wkt2Geojson(referClobStr));
						} catch (Exception e1) {
							log.warn("子任务refer geometry查询错误,"+e1.getMessage());
						}						
						
						subtask.put("taskId",rs.getInt("TASK_ID"));
						if (2 == rs.getInt("STAGE")) {
							//月编子任务
							subtask.put("dbId",rs.getInt("MONTHLY_DB_ID"));
						} else {
							subtask.put("dbId",rs.getInt("DAILY_DB_ID"));
						}	
						
						subtask.put("version",SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
						return subtask;
					}
					return null;
				}	
			};
			log.info("queryBySubtaskId sql:" + selectSql);
			Map<String, Object> result = run.query(conn, selectSql,rsHandler);
			//补充子任务的用户名/组名/gridIds
			if(result.containsKey("exeUserId")){
				Long exeUserId=Long.valueOf(String.valueOf(result.get("exeUserId")));
				if(!exeUserId.equals(Long.valueOf(0))){
					UserInfo userInfo = UserInfoOperation.getUserInfoByUserId(conn,exeUserId);
					result.put("executer",userInfo.getUserRealName());
					result.put("risk",userInfo.getRisk());
				}
			}				
		
			if(result.containsKey("exeGroupId")){
				Long exeGroupId=Long.valueOf(String.valueOf(result.get("exeGroupId")));
				if(!exeGroupId.equals(Long.valueOf(0))){
					UserGroup group = UserGroupService.getInstance().getGroupNameByGroupId(conn,exeGroupId);
					result.put("executer",group.getGroupName());
				}
			}
		
			Map<Integer,Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskIdWithConn(conn,subtaskId);
			result.put("gridIds",gridIds);
		
		
			if(result.containsKey("hasQuality")){
				int hasQuality=(int) result.get("hasQuality");
				if(hasQuality==1){
					Subtask subtaskQuality = queryBySubtaskIdS(conn,(int)result.get("qualitySubtaskId"));
					result.put("qualityExeUserId",subtaskQuality.getExeUserId());
					result.put("qualityPlanStartDate",DateUtils.format(subtaskQuality.getPlanStartDate(), DateUtils.DATE_YMD));
					result.put("qualityPlanEndDate",DateUtils.format(subtaskQuality.getPlanEndDate(), DateUtils.DATE_YMD));
					result.put("qualityTaskStatus",subtaskQuality.getStatus());
					
					int qualityMethod=subtaskQuality.getQualityMethod();
					JSONArray qualityMethodArray=new JSONArray();
					if(qualityMethod==3){
						qualityMethodArray.add(1);
						qualityMethodArray.add(2);
					}else if(qualityMethod!=0){qualityMethodArray.add(qualityMethod);}
					result.put("qualityMethod", qualityMethodArray);
					
					UserInfo userInfo = UserInfoOperation.getUserInfoByUserId(conn,subtaskQuality.getExeUserId());
					if(userInfo!=null){
						result.put("qualityExeUserName",userInfo.getUserRealName());
						result.put("qualityRisk",userInfo.getRisk());
					}
					UserGroup group = UserGroupService.getInstance().getGroupNameByGroupId(conn,subtaskQuality.getExeGroupId());
					result.put("qualityExeGroupId",subtaskQuality.getExeGroupId());
					if(group!=null){result.put("qualityExeGroupName",group.getGroupName());}
				}
			}				
			
			return result;			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		}
	}
	
	/**
	 * 获取subtask详情
	 * @param subtaskId
	 * @return
	 * @throws ServiceException
	 */
	
	public Map<String,Object> queryBySubtaskId(Integer subtaskId) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			return queryBySubtaskId(conn, subtaskId);		
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
//	/**
//	 * 获取subtask详情
//	 * @param subtaskId
//	 * @return
//	 * @throws ServiceException
//	 */
//	
//	public Subtask queryBySubtaskId(Integer subtaskId) throws ServiceException {
//		Connection conn = null;
//		try {
//			conn = DBConnector.getInstance().getManConnection();
//			QueryRunner run = new QueryRunner();
//			
//			StringBuilder sb = new StringBuilder();
//			
//			sb.append("SELECT ST.SUBTASK_ID,ST.NAME,ST.STATUS,ST.DESCP,ST.PLAN_START_DATE,ST.PLAN_END_DATE,ST.TYPE,ST.GEOMETRY,ST.REFER_ID");
//			sb.append(",ST.EXE_USER_ID,ST.EXE_GROUP_ID,ST.QUALITY_SUBTASK_ID,ST.IS_QUALITY");
//			sb.append(",T.TASK_ID,T.TYPE TASK_TYPE,R.DAILY_DB_ID,R.MONTHLY_DB_ID");
//			sb.append(" FROM SUBTASK ST,TASK T,REGION R");
//			sb.append(" WHERE ST.TASK_ID = T.TASK_ID");
//			sb.append(" AND T.REGION_ID = R.REGION_ID");
//			sb.append(" AND ST.SUBTASK_ID = " + subtaskId);
//	
//			String selectSql = sb.toString();
//			
//
//			ResultSetHandler<Subtask> rsHandler = new ResultSetHandler<Subtask>() {
//				public Subtask handle(ResultSet rs) throws SQLException {
//					if (rs.next()) {
//						Subtask subtask = new Subtask();						
//						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
//						subtask.setName(rs.getString("NAME"));
//						subtask.setType(rs.getInt("TYPE"));
//						subtask.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
//						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
//						subtask.setDescp(rs.getString("DESCP"));
//						subtask.setStatus(rs.getInt("STATUS"));
//						subtask.setReferId(rs.getInt("REFER_ID"));
//						
//						//作业员/作业组信息
//						int exeUserId = rs.getInt("EXE_USER_ID");
//						int exeGroupId = rs.getInt("EXE_GROUP_ID");
//						if(exeUserId!=0){
//							//获取作业员名称
//							try {
//								UserInfo userInfo = UserInfoService.getInstance().getUserInfoByUserId(exeUserId);
//								subtask.setExeUserId(exeUserId);
//								subtask.setExecuterId(exeUserId);
//								subtask.setExecuter(userInfo.getUserRealName());
//							} catch (ServiceException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}else{
//							//获取作业组名称
//							try {
//								String userGroupName = UserGroupService.getInstance().getGroupNameByGroupId(exeGroupId);
//								subtask.setExeGroupId(exeUserId);
//								subtask.setExecuterId(exeUserId);
//								subtask.setExecuter(userGroupName);
//							} catch (ServiceException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//						subtask.setQualitySubtaskId(rs.getInt("QUALITY_SUBTASK_ID"));
//						//获取质检任务信息
//						if(subtask.getQualitySubtaskId()!=0){
//							try {
//								Subtask subtaskQuality = queryBySubtaskIdS(subtask.getQualitySubtaskId());
//								subtask.setQualityExeUserId(subtaskQuality.getExecuterId());
//								subtask.setQualityPlanStartDate(subtaskQuality.getPlanStartDate());
//								subtask.setQualityPlanEndDate(subtaskQuality.getPlanEndDate());
//								subtask.setQualityTaskStatus(subtaskQuality.getStatus());
//								UserInfo userInfo = UserInfoService.getInstance().getUserInfoByUserId(exeUserId);
//								subtask.setQualityExeUserName(userInfo.getUserRealName());
//							} catch (ServiceException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//						
//						//GEOMETRY
//						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
//						try {
//							subtask.setGeometry(GeoTranslator.struct2Wkt(struct));
//							String clobStr = GeoTranslator.struct2Wkt(struct);
//							subtask.setGeometryJSON(Geojson.wkt2Geojson(clobStr));
//						} catch (Exception e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//						
//						try {
//							Map<Integer,Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
//							subtask.setGridIds(gridIds);
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						
//						subtask.setTaskId(rs.getInt("TASK_ID"));
//						if (2 == rs.getInt("TASK_TYPE")) {
//							//月编子任务
//							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
//						} else {
//							subtask.setDbId(rs.getInt("DAILY_DB_ID"));
//						}	
//						
//						if(1 == rs.getInt("STATUS")){
//							subtask.setPercent(100);
////							SubtaskStatInfo stat = new SubtaskStatInfo();
////							try{	
////								StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
////								stat = staticApi.getStatBySubtask(rs.getInt("SUBTASK_ID"));
////							} catch (Exception e) {
////								log.warn("subtask query error",e);
////							}
////							subtask.setPercent(stat.getPercent());
//						}
//						subtask.setVersion(SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
//						return subtask;
//					}
//					return null;
//				}	
//			};
//			return run.query(conn, selectSql,rsHandler);			
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
//		} finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}

//	/**
//	 * @Title: queryBySubtaskId
//	 * @Description: 根据subtaskId查询一个任务的详细信息。 参数为SubtaskId
//	 * @param subtaskId
//	 * @param platForm 
//	 * @return
//	 * @throws ServiceException  Subtask
//	 * @throws 
//	 * @author zl zhangli5174@navinfo.com
//	 * @date 2016年11月4日 下午4:08:09 
//	 */
//	public Map<String, Object> queryBySubtaskId(Integer subtaskId) throws ServiceException {
//		Connection conn = null;
//		try {
//			conn = DBConnector.getInstance().getManConnection();
//			QueryRunner run = new QueryRunner();
//			
//			String selectSql = "select st.SUBTASK_ID "
//					+ ",st.NAME"
//					+ ",st.DESCP"
//					+ ",st.PLAN_START_DATE"
//					+ ",st.PLAN_END_DATE"
//					+ ",st.STAGE"
//					+ ",st.TYPE"
//					+ ",st.STATUS"
//					+ ",r.DAILY_DB_ID"
//					+ ",r.MONTHLY_DB_ID"
//					+ ",st.GEOMETRY"
//					//+ ",st.REFER_GEOMETRY"
//					//新增返回字段
//					+ "	,st.quality_Subtask_Id qualitySubtaskId,Q.qualityPlanStartDate ,Q.qualityPlanEndDate ,Q.qualityExeUserId ,Q.qualityTaskStatus";
//			String userSql = ",u.user_id as executer_id,u.user_real_name as executer";
//			String groupSql = ",ug.group_id as executer_id,ug.group_name as executer";
//			String taskSql = ",T.CITY_ID AS BLOCK_ID,T.TASK_ID AS BLOCK_MAN_ID,T.NAME AS BLOCK_MAN_NAME,T.TASK_TYPE AS TASK_TYPE";
//			String blockSql = ",B.BLOCK_ID,BM.BLOCK_MAN_ID, BM.BLOCK_MAN_NAME,T.TASK_TYPE AS TASK_TYPE";
//
//			String fromSql_task = " from subtask st "
//					//左外关联质检子任务表
//					+ " left join (select s.SUBTASK_ID ,s.EXE_USER_ID qualityExeUserId,s.PLAN_START_DATE as qualityPlanStartDate,s.PLAN_END_DATE as qualityPlanEndDate,s.STATUS qualityTaskStatus from subtask s where s.is_quality = 1 ) Q  on st.quality_subtask_id = Q.subtask_id "
//					+ ",task t"
//					+ ",city c"
//					+ ",region r";
//
//			String fromSql_block = " from subtask st"
//					//左外关联质检子任务表
//					+ " left join (select s.SUBTASK_ID ,s.EXE_USER_ID qualityExeUserId,s.PLAN_START_DATE as qualityPlanStartDate,s.PLAN_END_DATE as qualityPlanEndDate,s.STATUS qualityTaskStatus from subtask s where s.is_quality = 1 ) Q  on st.quality_subtask_id = Q.subtask_id"
//					+ ",block b,block_man bm"
//					+ ",region r,task t";
//			
//			String fromSql_user = "  ,user_info u";
//
//			String fromSql_group = " , user_group ug";
//
//			String conditionSql_task = " where st.task_id = t.task_id "
//					+ "and t.city_id = c.city_id "
//					+ "and c.region_id = r.region_id "
//					+ " and st.SUBTASK_ID=" + subtaskId;
//
//			String conditionSql_block = " where st.block_man_id = bm.block_man_id "
//					
//					+ "and b.region_id = r.region_id "
//					+ "and bm.block_id = b.block_id "
//					+ "and bm.task_id = t.task_id "
//					+ " and st.SUBTASK_ID=" + subtaskId;
//			
//			String conditionSql_user = " and st.exe_user_id = u.user_id";
//			String conditionSql_group = " and st.exe_group_id = ug.group_id";
//
//			
//			selectSql = selectSql + userSql + taskSql + fromSql_task + fromSql_user + conditionSql_task + conditionSql_user
//					+ " union all " + selectSql + userSql + blockSql + fromSql_block + fromSql_user + conditionSql_block + conditionSql_user
//					+ " union all " + selectSql + groupSql + taskSql + fromSql_task + fromSql_group + conditionSql_task + conditionSql_group
//					+ " union all " + selectSql + groupSql + blockSql + fromSql_block + fromSql_group + conditionSql_block + conditionSql_group;
//			
//
//			ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
//				public Map<String, Object> handle(ResultSet rs) throws SQLException {
//					if (rs.next()) {
//						Map<String, Object> subtask = new HashMap<String, Object>();
//						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
//						subtask.put("name", rs.getString("NAME"));
//						subtask.put("stage", rs.getInt("STAGE"));
//						subtask.put("type", rs.getInt("TYPE"));
//						subtask.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE"), DateUtils.DATE_YMD));
//						subtask.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE"), DateUtils.DATE_YMD));
//						subtask.put("descp", rs.getString("DESCP"));
//						subtask.put("status", rs.getInt("STATUS"));
//						//**************zl 2016.11.04 ******************
//						subtask.put("qualitySubtaskId", rs.getInt("qualitySubtaskId"));
//						subtask.put("qualityExeUserId", rs.getInt("qualityExeUserId"));
//						subtask.put("qualityPlanStartDate", DateUtils.dateToString(rs.getTimestamp("qualityPlanStartDate"), DateUtils.DATE_YMD));
//						subtask.put("qualityPlanEndDate", DateUtils.dateToString(rs.getTimestamp("qualityPlanEndDate"), DateUtils.DATE_YMD));				
//						subtask.put("qualityTaskStatus", rs.getInt("qualityTaskStatus"));
//						
//						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
//						try {
//							subtask.put("geometry",GeoTranslator.struct2Wkt(struct));
//							String clobStr = GeoTranslator.struct2Wkt(struct);
//							subtask.put("geometryJSON",Geojson.wkt2Geojson(clobStr));
//						} catch (Exception e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//						
//						try {
//							Map<Integer,Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
//							subtask.put("gridIds",gridIds);
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						
//						
//						if(0==rs.getInt("STAGE")){
//							//采集子任务
//							subtask.put("dbId",rs.getInt("DAILY_DB_ID"));
//							subtask.put("blockId",rs.getInt("BLOCK_ID"));
//							subtask.put("blockManId",rs.getInt("BLOCK_MAN_ID"));
//							subtask.put("blockManName",rs.getString("BLOCK_MAN_NAME"));
//						}else if (1 == rs.getInt("STAGE")) {
//							subtask.put("dbId",rs.getInt("DAILY_DB_ID"));
//							subtask.put("blockId",rs.getInt("BLOCK_ID"));
//							subtask.put("blockManId",rs.getInt("BLOCK_MAN_ID"));
//							subtask.put("blockManName",rs.getString("BLOCK_MAN_NAME"));
//						} else if (2 == rs.getInt("STAGE")) {
//							subtask.put("dbId",rs.getInt("MONTHLY_DB_ID"));
//							subtask.put("cityId",rs.getInt("BLOCK_ID"));
//							subtask.put("taskId",rs.getInt("BLOCK_MAN_ID"));
//							subtask.put("taskName",rs.getString("BLOCK_MAN_NAME"));
//						} else {
//							subtask.put("dbId",rs.getInt("MONTHLY_DB_ID"));
//							subtask.put("blockId",rs.getInt("BLOCK_ID"));
//							subtask.put("blockManId",rs.getInt("BLOCK_MAN_ID"));
//							subtask.put("blockManName",rs.getString("BLOCK_MAN_NAME"));
//						}
//						
//						subtask.put("executer",rs.getString("EXECUTER"));
//						subtask.put("executerId",rs.getInt("EXECUTER_ID"));
//						
//						if(1 == rs.getInt("STATUS")){
//							SubtaskStatInfo stat = new SubtaskStatInfo();
//							try{	
//								StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
//								stat = staticApi.getStatBySubtask(rs.getInt("SUBTASK_ID"));
//							} catch (Exception e) {
//								log.warn("subtask query error",e);
//							}
//							subtask.put("percent",stat.getPercent());
//						}
//						subtask.put("version",SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
//						return subtask;
//					}
//					return null;
//				}
//			};
//			log.debug("queryBySubtaskId: "+selectSql);
//			return run.query(conn, selectSql,rsHandler);
//			
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
//		} finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}

	/*
	 * 关闭多个子任务。 参数：Subtask对象列表，List<Subtask>
	 */
	public List<Integer> close(List<Integer> subtaskIdList, long userId)
			throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			List<Integer> closedSubtaskList = subtaskIdList;
			List<Integer> unClosedSubtaskList = new ArrayList<Integer>();
			//判断是否有未完成任务,新需求没有了
//			List<Integer> unClosedSubtaskList = new ArrayList<Integer>();
//			List<Integer> closedSubtaskList = new ArrayList<Integer>();
//			
//			StaticsApi staticsApi = (StaticsApi) ApplicationContextUtil.getBean("staticsApi");
//			
//			for(int i=0;i<subtaskIdList.size();i++){
//				SubtaskStatInfo subtaskStatic = staticsApi.getStatBySubtask(subtaskIdList.get(i));
//				if(subtaskStatic.getPercent()<100){
//					unClosedSubtaskList.add(subtaskIdList.get(i));
//				}else{
//					closedSubtaskList.add(subtaskIdList.get(i));
//				}
//			}
//			// 根据subtaskId列表关闭subtask
//			if (!closedSubtaskList.isEmpty()) {
//				SubtaskOperation.closeBySubtaskList(conn, closedSubtaskList);
//			}
			
			//关闭subtask
			SubtaskOperation.closeBySubtaskList(conn, subtaskIdList);
			//发送消息
			try {
				//查询子任务
				List<Subtask> subtaskList = SubtaskOperation.getSubtaskListBySubtaskIdList(conn, closedSubtaskList);
				Iterator<Subtask> iter = subtaskList.iterator();
				while(iter.hasNext()){
					Subtask subtask = (Subtask) iter.next();
					//查询分配的作业组组长
					List<Long> groupIdList = new ArrayList<Long>();
					if(subtask.getExeUserId()!=0){
						UserGroup userGroup = UserInfoOperation.getUserGroupByUserId(conn, subtask.getExeUserId());
						groupIdList.add(Long.valueOf(userGroup.getGroupId()));
					}else{groupIdList.add((long)subtask.getExeGroupId());}
					Map<Long, UserInfo> leaderIdByGroupId = UserInfoOperation.getLeaderIdByGroupId(conn, groupIdList);
					/*采集/日编/月编子任务关闭
					 * 分配的作业员
					 * 采集/日编/月编子任务关闭：XXX(子任务名称)已关闭，请关注*/
					String msgTitle = "";
					String msgContent = "";
					//2web,1手持端消息
					int pushtype=2;
					if(subtask.getStage()== 0){
						pushtype=1;
						msgTitle = "采集子任务关闭";
						msgContent = "采集子任务关闭:" + subtask.getName() + "已关闭,请关注";
					}else if(subtask.getStage()== 1){
						msgTitle = "日编子任务关闭";
						msgContent = "日编子任务关闭:" + subtask.getName() + "已关闭,请关注";
					}else{
						msgTitle = "月编子任务关闭";
						msgContent = "月编子任务关闭:" + subtask.getName() + "已关闭,请关注";
					}
					//关联要素
					JSONObject msgParam = new JSONObject();
					msgParam.put("relateObject", "SUBTASK");
					msgParam.put("relateObjectId", subtask.getSubtaskId());
					//查询用户名称
					
					if(leaderIdByGroupId !=null && leaderIdByGroupId.size()>0){
						for(Long groupId:leaderIdByGroupId.keySet()){
							//发消息
							UserInfo userInfo = leaderIdByGroupId.get(groupId);
							Message message = new Message();
							message.setMsgTitle(msgTitle);
							message.setMsgContent(msgContent);
							message.setPushUserId((int)userId);
							message.setReceiverId(userInfo.getUserId());
							message.setMsgParam(msgParam.toString());
							message.setPushUser(userInfo.getUserRealName());
							
							MessageService.getInstance().push(message, pushtype);
							//发邮件
							//判断邮箱格式
							String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			                Pattern regex = Pattern.compile(check);
			                Matcher matcher = regex.matcher((CharSequence) userInfo.getUserEmail());
			                if(matcher.matches()){
			            		String toMail = userInfo.getUserEmail();
			            		String mailTitle = msgTitle;
			            		String mailContent = msgContent;
			                	EmailPublisher.publishMsg(toMail, mailTitle, mailContent);
			                }
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				log.error("发送失败,原因:"+e.getMessage(), e);
			}

			return unClosedSubtaskList;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("关闭失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public int queryAdminIdBySubtask(int subtaskId) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT C.ADMIN_ID FROM SUBTASK S,TASK T,BLOCK B,CITY C");
			sb.append(" WHERE S.TASK_ID = T.TASK_ID");
			sb.append(" AND T.BLOCK_ID = B.BLOCK_ID");
			sb.append(" AND B.CITY_ID = C.CITY_ID");
			sb.append(" AND S.SUBTASK_ID =" + subtaskId);

//			String selectSql = "select c.admin_id from block_man bm,block b, city c, subtask s where s.block_man_id=bm.block_man_id and b.block_id = bm.block_id and b.city_id=c.city_id and s.subtask_id=:1";
//
//			selectSql += " union all";
//
//			selectSql += " select c.admin_id from city c, subtask s, task t where c.city_id=t.city_id and s.task_id=t.task_id and s.subtask_id=:2";

			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {

				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {

						int adminId = rs.getInt("admin_id");

						return adminId;

					}

					return 0;
				}
			};
			log.info("queryAdminIdBySubtask sql:" + sb.toString());
			return run.query(conn, sb.toString(), rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}

	/**
	 * @param dataJson
	 * @param snapshot
	 * @param platForm
	 * @param pageSize
	 * @param curPageNum 
	 * @return
	 * @throws ServiceException 
	 */
	public Page listByUserPage(JSONObject dataJson, int snapshot, int platForm, int pageSize, int curPageNum) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			int exeUserId=dataJson.getInt("exeUserId");
			
			//获取用户所在组
			List<Integer> userGroup = UserInfoOperation.getUserGroup(conn, exeUserId);
			if(userGroup!=null&&!userGroup.isEmpty()){
				dataJson.put("exeGroupId", userGroup);
			}
			
			Page page = new Page();
			//snapshot=1不返回geometry和gridIds
			if(snapshot==1){
				page = SubtaskOperation.getListByUserSnapshotPage(conn, dataJson,curPageNum,pageSize,platForm);
				if(platForm==1){//编辑端子任务列表需要返回常规子任务状态，以控制抽取按钮
					List<HashMap<Object,Object>> list=(List<HashMap<Object, Object>>) page.getResult();
					if(list==null||list.size()==0){return page;}
					Set<Integer> subtaskIds=new HashSet<Integer>();
					for(HashMap<Object,Object> tmp:list){
						int subtaskId=(int)tmp.get("subtaskId");
						int isQuality=(int)tmp.get("isQuality");
						if(isQuality==1){subtaskIds.add(subtaskId);}
					}
					if(subtaskIds==null||subtaskIds.size()==0){return page;}
					Map<Integer, Map<String, String>> commonMap = SubtaskOperation.getCommonByQuality(conn, subtaskIds);
					if(commonMap==null||commonMap.size()==0){return page;}
					
					for(HashMap<Object,Object> tmp:list){
						int subtaskId=(int)tmp.get("subtaskId");
						int isQuality=(int)tmp.get("isQuality");
						if(isQuality==1&&commonMap.containsKey(subtaskId)){
							String commonStatus = commonMap.get(subtaskId).get("status");
							tmp.put("commonStatus", commonStatus);
						}
					}
				}
			}else{
				page = SubtaskOperation.getListByUserPage(conn, dataJson,curPageNum,pageSize,platForm);		
				//返回质检圈
				List<HashMap<Object,Object>> list=(List<HashMap<Object, Object>>) page.getResult();
				if(list==null||list.size()==0){return page;}
				Set<Integer> subtaskIds=new HashSet<Integer>();
				for(HashMap<Object,Object> tmp:list){
					int subtaskId=(int)tmp.get("subtaskId");
					int isQuality=(int)tmp.get("isQuality");
					if(isQuality==1){subtaskIds.add(subtaskId);}
				}
				if(subtaskIds==null||subtaskIds.size()==0){return page;}
				Map<Integer, List<SubtaskQuality>> qualityMap = SubtaskQualityOperation.queryBySubtaskIds(conn, subtaskIds);
				for(HashMap<Object,Object> tmp:list){
					int subtaskId=(int)tmp.get("subtaskId");
					int isQuality=(int)tmp.get("isQuality");
					if(isQuality==1&&qualityMap.containsKey(subtaskId)){
						List<SubtaskQuality> qualitys = qualityMap.get(subtaskId);
						List<String> qualityGeos=new ArrayList<String>();
						for(SubtaskQuality qtmp:qualitys){
							qualityGeos.add(GeoTranslator.jts2Wkt(qtmp.getGeometry()));
						}
						tmp.put("qualityGeos", qualityGeos);
					}
				}
			}
			
			return page;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


//	/**
//	 * @Title: createSubtaskBean
//	 * @Description: (修改)根据参数生成subtask bean(第七迭代)
//	 * @param userId
//	 * @param dataJson
//	 * @return
//	 * @throws ServiceException  Subtask
//	 * @throws 
//	 * @author zl zhangli5174@navinfo.com
//	 * @date 2016年11月3日 下午5:07:59 
//	 */
//	public Subtask createSubtaskBean(long userId, JSONObject dataJson) throws ServiceException {
//		try{
//			String wkt = null;
//			JSONArray gridIds;
//			if(!dataJson.containsKey("gridIds")){
//				int taskId = dataJson.getInt("taskId");
//				gridIds = TaskService.getInstance().getGridListByTaskId(taskId);
//			}else{
//				gridIds = dataJson.getJSONArray("gridIds");
//			}
//			if(!gridIds.isEmpty() || gridIds.size()>0){
//				Map<String,Integer> gridIdMap = new HashMap<String,Integer>();
//				for(Object gridId:gridIds.toArray()){
//					gridIdMap.put(gridId.toString(), 1);
//				}
//				dataJson.put("gridIds",gridIdMap);
//				//根据gridIds获取wkt
//				wkt = GridUtils.grids2Wkt(gridIds);
//				dataJson.put("geometry",wkt);	
//			}
//			Subtask bean = (Subtask) JsonOperation.jsonToBean(dataJson,Subtask.class);
//			bean.setCreateUserId((int)userId);
//			bean.setGeometry(wkt);
//			return bean;
//			
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//			throw new ServiceException("子任务创建失败，原因为:" + e.getMessage(), e);
//		}
//	}
	
	/**
	 * @Title: createSubtaskBean
	 * @Description: (修改)根据参数生成subtask bean(第七迭代)
	 * @param userId
	 * @param dataJson
	 * @return
	 * @throws ServiceException  Subtask
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月3日 下午5:07:59 
	 */
	public Subtask createSubtaskBean(long userId, JSONObject dataJson) throws ServiceException {
		try{
			if(!dataJson.containsKey("gridIds")&&dataJson.containsKey("taskId")){
				int taskId = dataJson.getInt("taskId");
				JSONArray gridIds = TaskService.getInstance().getGridListByTaskId(taskId);
				if(!gridIds.isEmpty() || gridIds.size()>0){
					JSONObject gridIdMap = new JSONObject();
//					Map<String,Integer> gridIdMap = new HashMap<String,Integer>();
					for(Object gridId:gridIds.toArray()){
						gridIdMap.put(gridId.toString(), 1);
					}
					dataJson.put("gridIds",gridIdMap);
					//根据gridIds获取wkt
					String wkt = GridUtils.grids2Wkt(gridIds);
					dataJson.put("geometry",wkt);	
				}
			}
			
			Subtask bean = (Subtask) JsonOperation.jsonToBean(dataJson,Subtask.class);
			if(dataJson.containsKey("gridIds")){
				bean.setGridIds(dataJson.getJSONObject("gridIds"));
			}
			bean.setCreateUserId((int)userId);
			return bean;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("子任务创建失败，原因为:" + e.getMessage(), e);
		}
	}
	
	/**
	 * @param userId
	 * @param subTaskIds
	 * @return
	 * @throws Exception 
	 */
	public String pushMsg(Connection conn, long userId, JSONArray subtaskIds) throws Exception {
		try{
			//查询子任务
			List<Subtask> subtaskList = SubtaskOperation.getSubtaskListBySubtaskIdList(conn, subtaskIds);
			
			Iterator<Subtask> iter = subtaskList.iterator();
			int success=0;
			while(iter.hasNext()){
				Subtask subtask = (Subtask) iter.next();
//				//20170708 by zhangxiaoyi 快线采集子任务需判断是否有对应的不规则圈，并锁子任务表，没有则不发布
//				if(subtask.getStage()==0&&(int)subtask.getStatus()== 2){
//					//是否中线子任务
//					Task task = TaskService.getInstance().queryByTaskId(conn, subtask.getTaskId());
//					if(task.getBlockId()!=0&&subtask.getReferId()==0){
//						throw new Exception("发布失败：请选择中线采集子任务对应的不规则圈。");
//					}
//					int referId = subtask.getReferId();
//					lockSubtaskRefer(conn,referId);
//				}
				//修改子任务状态
				if( (int)subtask.getStatus()== 2){
					SubtaskOperation.updateStatus(conn,subtask.getSubtaskId());
				}
				
				//采集子任务需要反向维护任务workKind
				if(subtask.getStage()==0&&subtask.getWorkKind()!=0){
					TaskOperation.updateWorkKind(conn, subtask.getTaskId(), subtask.getWorkKind());
				}
				pushMsg2Crowd(conn,userId,subtask);
				//发送消息
				SubtaskOperation.pushMessage(conn, subtask, userId);
				success ++;
			}
			return "子任务发布成功"+success+"个，失败"+(subtaskList.size()-success)+"个";
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * @param userId
	 * @param subTaskIds
	 * @return
	 * @throws Exception 
	 */
	public String pushMsg(long userId, JSONArray subtaskIds) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			
			return pushMsg(conn, userId, subtaskIds);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 推送众包信息给mapspotter
	 * 
	 * @param conn
	 * @param subtask
	 * @throws Exception 
	 */
	public void pushMsg2Crowd(Connection conn,Long userId,Subtask subtask) throws Exception{
		if(subtask.getWorkKind()!=2){return;}
		log.info("众包子任务发布，调用mapspoter请求："+subtask.getSubtaskId());
		Program program=ProgramService.getInstance().queryProgramByTaskId(conn, subtask.getTaskId());
		if(program==null){throw new Exception("众包子任务发布，通知mapsppotor失败：数据错误，未找到子任务对应项目");}
		JSONObject par=new JSONObject();
		par.put("subTaskId", subtask.getSubtaskId());
		par.put("subTaskName", subtask.getName());
		par.put("userId", subtask.getCreateUserId());
		if(program.getType()==1){
			par.put("priority", 2);
			par.put("geometryJSON", subtask.getGeometryJSON());			
		}else{
			Infor info = InforService.getInstance().getInforByProgramId(conn, program.getProgramId());
			par.put("priority", 1);
			par.put("infocode", info.getInforCode());
		}
		String mapspotterUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.mapspotterCrowdUrl);		
		
		Map<String,String> parMap = new HashMap<String,String>();
		parMap.put("parameter", par.toString());
		parMap.put("access_token", AccessTokenFactory.generate(userId).getTokenString());
		
		log.info(parMap);
		//mapspotterUrl=mapspotterUrl+"access_token="+AccessTokenFactory.generate(userId).getTokenString()+"&parameter="+par.toString().replace("\\", "");
		//mapspotterUrl=mapspotterUrl+"access_token="+AccessTokenFactory.generate(userId).getTokenString()+"&parameter={\"subTaskId\":66,\"priority\":2,\"geometryJSON\":{\"type\":\"Polygon\",\"coordinates\":[[[116.40625,39.9375],[116.40625,39.95833],[116.4375,39.95833],[116.46875,39.95833],[116.46875,39.9375],[116.4375,39.9375],[116.40625,39.9375]]]}}";
		log.info(mapspotterUrl);
		String result = ServiceInvokeUtil.invokeByGet(mapspotterUrl,parMap);
		JSONObject res=new JSONObject();
		res=JSONObject.fromObject(result);
		log.info("众包子任务发布，调用mapspoter请求返回值："+subtask.getSubtaskId()+","+result);
		String success=res.getString("errmsg");
		if(!"sucess".equals(success)){
			throw new Exception("众包子任务发布，通知mapsppotor失败："+result);
		}
	}
	
//	public Page list(long userId, int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
//			final int curPageNum,int snapshot) throws ServiceException {
//		Connection conn = null;
//		try {
//			conn = DBConnector.getInstance().getManConnection();
//			
//			//获取用户所在组信息
//			UserInfo userInfo = new UserInfo();
//			userInfo.setUserId((int)userId);
//			Map<Object, Object> group = UserInfoOperation.getUserGroup(conn, userInfo);
//			int groupId = (int) group.get("groupId");
//			
//			//返回简略信息
//			if (snapshot==1){
//				Page page = SubtaskOperation.getListSnapshot(conn,userId,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
//				return page;
//			}else{
//				Page page = SubtaskOperation.getList(conn,userId,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
//				return page;
//			}		
//
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
//		} finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}

	/**
	 * @param planStatus 
	 * @param condition
	 * @param filter 
	 * @param pageSize
	 * @param curPageNum
	 * @return
	 */
	public Page list(int planStatus, JSONObject condition, int pageSize,int curPageNum) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
			Page page = SubtaskOperation.getList(conn,planStatus,condition,pageSize,curPageNum);
			return page;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
//	public Page listByGroup(long groupId, int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
//			final int curPageNum,int snapshot) throws ServiceException {
//		Connection conn = null;
//		try {
//			conn = DBConnector.getInstance().getManConnection();
//			
////			//获取用户所在组信息
////			UserInfo userInfo = new UserInfo();
////			userInfo.setUserId((int)userId);
////			Map<Object, Object> group = UserInfoOperation.getUserGroup(conn, userInfo);
////			int groupId = (int) group.get("groupId");
//			
//			Page page = SubtaskOperation.getListByGroup(conn,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
//			return page;
//			
////			//返回简略信息
////			if (snapshot==1){
////				Page page = SubtaskOperation.getListSnapshot(conn,userId,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
////				return page;
////			}else{
////				Page page = SubtaskOperation.getList(conn,userId,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
////				return page;
////			}		
//
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
//		} finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}
	/**
	 * 删除子任务，前端只有草稿状态的子任务有删除按钮
	 * @param subtaskId
	 * @throws ServiceException
	 */
	public void delete(Integer subtaskId) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			delete(conn,subtaskId);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("删除失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 删除子任务，前端只有草稿状态的子任务有删除按钮
	 * @param subtaskId
	 * @throws ServiceException
	 */
	public void delete(Connection conn,int subtaskId) throws ServiceException {
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			//modify by songhe
			//删除子任务的同时，如果有对应的质检子任务也同时删除
			String updateSql = "delete from SUBTASK_grid_mapping S where S.SUBTASK_ID = "+subtaskId+" or "
					+ "S.SUBTASK_ID =(select t.quality_subtask_id from SUBTASK t where t.is_quality = 0 and t.subtask_id = "+subtaskId+")";
			run.update(conn,updateSql);
			updateSql = "delete from SUBTASK S where S.SUBTASK_ID = "+subtaskId+" or "
					+ "S.SUBTASK_ID =(select t.quality_subtask_id from SUBTASK t where t.is_quality = 0 and t.subtask_id = "+subtaskId+")";	
			run.update(conn,updateSql);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("删除失败，原因为:" + e.getMessage(), e);
		}
	}

	public List<HashMap<String,Object>> queryListReferByWkt(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String whereSql="";
			if (json.containsKey("blockId")&&json.getInt("blockId")!=0) {
				whereSql +=  " AND T.block_id = "+json.getInt("blockId");
			}
			if (json.containsKey("wkt")) {
				whereSql +=  " AND SDO_ANYINTERACT(t.geometry,sdo_geometry(?,8307))='TRUE'";
			}
			String selectSql = " WITH TMP AS"
					+ " (SELECT T.ID,"
					+ "         MAX(CASE S.STATUS"
					+ "               WHEN 1 THEN"
					+ "                3"
					+ "               ELSE"
					+ "                NVL(S.STATUS, 0)"
					+ "             END) STATUS"
					+ "    FROM SUBTASK_REFER T, SUBTASK S"
					+ "   WHERE S.REFER_ID(+) = T.ID"
					+ whereSql
					+ "   GROUP BY T.ID)"
					+ " SELECT TMP.ID, TMP.STATUS, R.GEOMETRY"
					+ "  FROM TMP, SUBTASK_REFER R"
					+ " WHERE TMP.ID = R.ID";
			
			ResultSetHandler<List<HashMap<String,Object>>> rsHandler = new ResultSetHandler<List<HashMap<String,Object>>>(){
				public List<HashMap<String,Object>> handle(ResultSet rs) throws SQLException {
					List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
					while(rs.next()){				
						HashMap<String,Object> map = new HashMap<String,Object>();
						map.put("id", rs.getInt("ID"));
						int status=rs.getInt("status");
						if(status==3){map.put("status", 1);}
						else{map.put("status", status);}
						try {
							STRUCT struct=(STRUCT)rs.getObject("geometry");
							String clobStr = GeoTranslator.struct2Wkt(struct);
							map.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}							
						list.add(map);		
					}					
					return list;
				}	    		
	    	}		;
	    	log.info("queryListReferByWkt sql :" + selectSql);
	    	if (json.containsKey("wkt")) {
	    		return run.query(conn, selectSql, rsHandler,json.getString("wkt"));
	    	}else{return run.query(conn, selectSql, rsHandler);}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public Map<String, Object> staticWithType(long userId) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
			//获取用户所在组
			List<Integer> userGroup = UserInfoOperation.getUserGroup(conn, (int)userId);
			//modify by songhe
			//针对专项月编子任务type = 7 的情况，单独需要按批次进行分类操作，并在每条数据里面返回批次lot的结果
			//只有type = 7 的情况下返回正常的库里面存的批次，其他类型的子任务批次全部赋值为0
			StringBuffer sb = new StringBuffer();
			sb.append(" SELECT T.STAGE, T.TYPE, T.LOT, COUNT(1) TYPE_COUNT  FROM (");
			sb.append(" SELECT T.STAGE, T.TYPE, TK.LOT FROM SUBTASK T, TASK TK");
			sb.append(" WHERE (T.EXE_USER_ID = "+userId);
			if(userGroup != null && !userGroup.isEmpty()){
				sb.append(" OR T.EXE_GROUP_ID in "+userGroup.toString().replace("[", "(").replace("]", ")"));
			}
			sb.append(" )AND T.STATUS = 1 AND T.TYPE = 7 AND T.TASK_ID = TK.TASK_ID");
			sb.append(" UNION ALL");
			sb.append(" SELECT T.STAGE, T.TYPE, 0 FROM SUBTASK T");
			sb.append(" WHERE (T.EXE_USER_ID = "+userId);
			if(userGroup != null && !userGroup.isEmpty()){
				sb.append(" OR T.EXE_GROUP_ID in "+userGroup.toString().replace("[", "(").replace("]", ")"));
			}
			sb.append(") AND T.STATUS = 1 AND T.TYPE != 7) T");
			sb.append(" GROUP BY T.STAGE, T.TYPE, T.LOT");
			sb.append(" ORDER BY T.STAGE, T.TYPE");
			String sql = sb.toString();
			
//			String groupSql="";
//			if(userGroup!=null&&!userGroup.isEmpty()){
//				groupSql=" OR T.EXE_GROUP_ID in "+userGroup.toString().replace("[", "(").replace("]", ")");
//			}
//			String sql="SELECT T.STAGE, T.TYPE, COUNT(1) TYPE_COUNT"
//					+ "  FROM SUBTASK T"
//					+ " WHERE (T.EXE_USER_ID = "+userId+groupSql+")"
//					+ "   AND T.STATUS = 1"
////					+ "   AND T.STAGE != 0"
//					+ " GROUP BY T.STAGE, T.TYPE"
//					+ " ORDER BY T.STAGE, T.TYPE";
			QueryRunner run=new QueryRunner();
			log.info("staticWithType swl:" + sql);
			Map<String, Object> result = run.query(conn, sql, new ResultSetHandler<Map<String, Object>>(){

				@Override
				public Map<String, Object> handle(ResultSet rs)
						throws SQLException {
					Map<String, Object> result=new HashMap<String, Object>();
					List<Map<String, Object>> subList=new ArrayList<Map<String,Object>>();
					int total=0;
					while (rs.next()) {
						Map<String, Object> subResult=new HashMap<String, Object>();
						int type=rs.getInt("TYPE");
						int stage=rs.getInt("STAGE");
						int typeCount=rs.getInt("TYPE_COUNT");
						int lot = rs.getInt("LOT");
						String name="";
//						if(stage==1){name+="日编 - ";}
//						else if(stage==0){name+="采集 - ";}
//						else{name+="月编 - ";}
						//0POI_采集，1道路_采集，2一体化_采集，3一体化_Grid粗编_日编，4一体化_区域粗编_日编，5多源POI，6
						//代理店， 7POI专项_月编,8道路_Grid精编，9道路_Grid粗编，10道路区域专项
						//5POI粗编_日编
						if(type==0){name+="POI_采集";}
						else if(type==1){name+="道路_采集";}
						else if(type==2){name+="一体化_采集";}
						else if(type==3){name+="一体化_Grid粗编_日编";}
						else if(type==4){name+="一体化_区域粗编_日编";}
						else if(type==5){name+="POI粗编_日编";}
						else if(type==6){name+="代理店";}
						else if(type==7){name+="POI专项_月编";
						if(lot == 0){
							name += "(无批次)";
						}else if(lot == 1){name += "(一批)";}
						 else if(lot == 2){name += "(二批)";}
						 else if(lot == 3){name += "(三批)";}
						 else{name += "("+lot+"批)";}
						}
						else if(type==8){name+="道路_Grid精编";}
						else if(type==9){name+="道路_Grid粗编";}
						else if(type==10){name+="道路区域专项";}
						else if(type==11){name+="预处理";}
						subResult.put("lot", lot);
						subResult.put("type", type);
						subResult.put("stage", stage);
						subResult.put("name", name);
						subResult.put("total", typeCount);
						subList.add(subResult);
						total+=typeCount;
					}
					result.put("totalCount", total);
					result.put("result", subList);
					return result;
				}});
			return result;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @param subtaskId
	 * @param userId
	 * @return
	 * @throws ServiceException 
	 */
	public String close(int subtaskId, long userId) throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			//查询子任务
			Subtask subtask = queryBySubtaskIdS(subtaskId);
			
			//关闭子任务,如果为采集子任务,需要起job给数据批subtaskId
			if(subtask.getType()==7){
				JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
				JSONObject MonthPoiBatchSyncJobRequestJSON=new JSONObject();
				MonthPoiBatchSyncJobRequestJSON.put("taskId", subtaskId);
				MonthPoiBatchSyncJobRequestJSON.put("userId",userId);
			    int jobId=(int) apiService.createJob("monthPoiBatch", MonthPoiBatchSyncJobRequestJSON, 0,0, "poi月库管理字段批处理");
				return "POI专项_月编子任务关闭进行中";
			}
			
			//修改状态，调整范围，发送消息
			return closeSubtask(conn,subtask,userId);
			

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("关闭失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	
	public void closeSubtask(int subtaskId, long userId) throws ServiceException{
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			//查询子任务
			Subtask subtask = queryBySubtaskIdS(subtaskId);
			//修改状态，调整范围，发送消息
			closeSubtask(conn,subtask,userId);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("关闭失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * @param conn 
	 * @param subtask
	 * @param userId 
	 * @return
	 * @throws Exception 
	 */
	public String closeSubtask(Connection conn, Subtask subtask, long userId) throws Exception {
		//修改子任务状态
		int num=SubtaskOperation.closeBySubtaskId(conn, subtask.getSubtaskId());
		if(num==0){throw new Exception("子任务关闭失败。区域子任务关闭条件：保证其范围内的grid粗编子任务关闭");}
		log.info("=================closeBySubtaskId==========================");

		//动态调整子任务范围
		//20170330 by zxy若是快线子任务，则需调整对应的快线项目
		//这里修改为快线才调整，中线不调整
		int programType = getTaskBySubtaskId(subtask.getSubtaskId()).get("programType");	
		if(programType == 4 && (subtask.getStage() == 0 || subtask.getStage() == 1)){
			//获取规划外GRID信息
			log.info("调整子任务本身范围");

			Map<Integer,Integer> gridIdsToInsert = SubtaskOperation.getGridIdMapBySubtaskFromLog(subtask,programType);
			//调整子任务范围
			SubtaskOperation.insertSubtaskGridMapping(conn,subtask.getSubtaskId(),gridIdsToInsert);
			if(gridIdsToInsert!=null&&gridIdsToInsert.size()>0){
				updateSubtaskGeo(conn,subtask.getSubtaskId());
				//调整任务范围
				log.info("调整子任务对应任务范围");
				int taskChangeNum=TaskOperation.changeTaskGridBySubtask(conn, subtask.getSubtaskId());
//				//modify by songhe
//				//添加中线采集任务范围调整，因为中线采集子任务不进行范围调整，所以上一步的根据子任务调整任务范围更新的数据一定为0
//				if(programType == 1 && subtask.getStage() == 0){
//					List<Integer> grids = new ArrayList<>();
//					grids.addAll(gridIdsToInsert.keySet());
//					TaskOperation.changeTaskGridByGrids(conn, grids, subtask);
//				}
				
				if(taskChangeNum>0){					
					//20170330 by zxy若是快线子任务，则需调整对应的快线项目
					log.info("调整子任务对应快线项目范围");
					int programCount = ProgramService.getInstance().changeProgramGridByTask(conn,subtask.getTaskId());
					if(subtask.getStage()==1){
						//调整区域子任务范围
						log.info("日编子任务 调整区域子任务范围");
						int regionChange=SubtaskOperation.changeRegionSubtaskGridByTask(conn, subtask.getTaskId());
						if(regionChange>0){
							List<Integer> regionSubtaskIds = SubtaskOperation.getRegionSubtaskByTask(conn, subtask.getTaskId());
							for(int tmpSubtaskId:regionSubtaskIds){
								updateSubtaskGeo(conn,tmpSubtaskId);}
						}
					}else if(subtask.getStage()==0){
						//调整日编任务，二代编辑任务
						//modify by songhe
						//sql里面删除了task.type =3 二代编辑任务的筛选条件，二代编辑任务不进行动态调整
						log.info("采集子任务 调整日编任务范围");
						TaskOperation.changeDayCmsTaskGridByCollectTask(conn,subtask.getTaskId());
						//调整日编区域子任务范围		
						log.info("采集子任务 调整日编区域子任务范围");
						int regionChange=SubtaskOperation.changeDayRegionSubtaskByCollectTask(conn, subtask.getTaskId());
						if(regionChange>0){
							List<Integer> regionSubtaskIds = SubtaskOperation.getDayRegionSubtaskByCollectTask(conn, subtask.getTaskId());
							for(int tmpSubtaskId:regionSubtaskIds){
								updateSubtaskGeo(conn,tmpSubtaskId);}
						}
					}
					//modify by songhe
					//原则变更：快线：采集/日编子任务关闭进行动态调整，增加动态调整快线月编任务，月编子任务范围
					//调整快线月编任务以及子任务的范围和项目范围保持一致，根据项目范围调整的个数判断是否执行表便任务的调整操作
					if(programCount > 0){
						log.info("subTaskId:" + subtask.getSubtaskId() + "开始执行快线月编任务范围更新操作");
						int monthChangedTasks = TaskOperation.changeMonthTaskGridByProgram(conn, subtask.getTaskId());
						if(monthChangedTasks > 0){
							//扩充的grid也要扩到图幅范围
							log.info("对应的月编任务扩grid后，将扩后的grid再扩充为图幅范围");
							Task monthTasks = TaskOperation.getMonthTaskGridByOtherTask(conn, subtask.getTaskId());
							
							Set<Integer> myGrid=monthTasks.getGridIds().keySet(); 
							Set<String> meshs=new HashSet<String>();
							for(Integer gridTmp:myGrid){
								meshs.add(String.valueOf(gridTmp/100));
							}
							Map<Integer,Integer> newGrids=new HashMap<Integer,Integer>();
							for(String meshTmp:meshs){
								Set<String> allGrid = CompGridUtil.mesh2Grid(meshTmp);
								for(String gridExt:allGrid){
									if(!myGrid.contains(Integer.valueOf(gridExt))){
										newGrids.put(Integer.valueOf(gridExt), 2);
									}
								}
							}
							TaskOperation.insertTaskGridMapping(conn,monthTasks.getTaskId(), newGrids);
							
							
							log.info("subTaskId:" + subtask.getSubtaskId() + "开始执行快线月编子任务范围更新操作");
							SubtaskOperation.changeMonthSubtaskGridByTask(conn, subtask.getTaskId());
							//获取对应采集/日编子任务对应的同任务下的快线月编子任务
							List<Integer> monthSubtasks = SubtaskOperation.getMonthSubtaskByTask(conn, subtask.getTaskId());
							for(int subTaskId : monthSubtasks){
								updateSubtaskGeo(conn, subTaskId);
							}
						}
					}
				}	
			}
		}	
		
		//记录关闭时间
		TimelineService.recordTimeline(subtask.getSubtaskId(), "subtask",0, conn);
		
		//发送消息
		try {
			//查询分配的作业组组长
			List<Long> groupIdList = new ArrayList<Long>();
			if(subtask.getExeUserId()!=0){
				UserGroup userGroup = UserInfoOperation.getUserGroupByUserId(conn, subtask.getExeUserId());
				if(userGroup.getGroupId()!=0){
					groupIdList.add(Long.valueOf(userGroup.getGroupId()));
				}
			}else{
				groupIdList.add((long)subtask.getExeGroupId());
			}
			Map<Long, UserInfo> leaderIdByGroupId = UserInfoOperation.getLeaderIdByGroupId(conn, groupIdList);
//			log.info("=================getLeaderIdByGroupId==========================");

			/*采集/日编/月编子任务关闭
			* 分配的作业员
			* 采集/日编/月编子任务关闭：XXX(子任务名称)已关闭，请关注*/
			String msgTitle = "";
			String msgContent = "";
			//2web,1手持端消息
			int pushtype=2;
			if(subtask.getStage()== 0){
				pushtype=1;
				msgTitle = "采集子任务关闭";
				msgContent = "采集子任务关闭:" + subtask.getName() + "已关闭,请关注";
			}else if(subtask.getStage()== 1){
				msgTitle = "日编子任务关闭";
				msgContent = "日编子任务关闭:" + subtask.getName() + "已关闭,请关注";
			}else{
				msgTitle = "月编子任务关闭";
				msgContent = "月编子任务关闭:" + subtask.getName() + "已关闭,请关注";
			}
			//关联要素
			JSONObject msgParam = new JSONObject();
			msgParam.put("relateObject", "SUBTASK");
			msgParam.put("relateObjectId", subtask.getSubtaskId());
			//查询用户名称
				
				if(leaderIdByGroupId !=null && leaderIdByGroupId.size()>0){
					for(Long groupId:leaderIdByGroupId.keySet()){
						//发消息
						UserInfo userInfo = leaderIdByGroupId.get(groupId);
						Message message = new Message();
						message.setMsgTitle(msgTitle);
						message.setMsgContent(msgContent);
						message.setPushUserId((int)userId);
						message.setReceiverId(userInfo.getUserId());
						message.setMsgParam(msgParam.toString());
						message.setPushUser(userInfo.getUserRealName());
						
						MessageService.getInstance().push(message, pushtype);
						//发邮件
						//判断邮箱格式
						String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		                Pattern regex = Pattern.compile(check);
		                Matcher matcher = regex.matcher((CharSequence) userInfo.getUserEmail());
		                if(matcher.matches()){
		            		String toMail = userInfo.getUserEmail();
		            		String mailTitle = msgTitle;
		            		String mailContent = msgContent;
		                	EmailPublisher.publishMsg(toMail, mailTitle, mailContent);
		                }
					}
				}
//				log.info("=================sendMessage==========================");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.error("发送失败,原因:"+e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}

		return null;
	}

	/**
	 * @param condition
	 * @param pageSize
	 * @param curPageNum
	 * @return
	 * @throws ServiceException 
	 */
	public Page list(JSONObject condition, int pageSize, int curPageNum) throws ServiceException {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			//查询条件
			String conditionSql = "";
			Iterator<?> conditionKeys = condition.keys();
			List<String> progressList = new ArrayList<String>();
			//boolean collectAndDay=true;
			while (conditionKeys.hasNext()) {
				String key = (String) conditionKeys.next();
				//查询条件
				if ("taskId".equals(key)) {
					conditionSql+=" AND SUBTASK_LIST.TASK_ID="+condition.getInt(key);
				}
				if ("stage".equals(key)) {
					//collectAndDay=false;
					conditionSql+=" AND SUBTASK_LIST.STAGE ="+condition.getInt(key);}
				//子任务名称模糊查询
				if ("subtaskName".equals(key)) {	
					conditionSql+=" AND SUBTASK_LIST.NAME LIKE '%" + condition.getString(key) +"%'";
				}
				//筛选条件
				//"progress":[1,3] //进度。1采集/日编/月编正常，2采集/日编/月编异常，7已关闭，8已完成, 9草稿, 11逾期完成，12按时完成，13提前完成
				if ("progress".equals(key)){
					JSONArray progress = condition.getJSONArray(key);
					if(progress.isEmpty()){
						continue;
					}
					for(Object i:progress){
						int tmp=(int) i;
						if(tmp==1||tmp==3||tmp==5){progressList.add(" (SUBTASK_LIST.PROGRESS = 1 AND SUBTASK_LIST.STATUS=1) ");}
						if(tmp==2||tmp==4||tmp==6){progressList.add(" (SUBTASK_LIST.PROGRESS = 2 AND SUBTASK_LIST.STATUS=1) ");}
						
//						if(tmp==3){progressList.add(" SUBTASK_LIST.PROGRESS = 1 AND SUBTASK_LIST.STATUS=1 ");}
//						if(tmp==4){progressList.add(" SUBTASK_LIST.PROGRESS = 2 AND SUBTASK_LIST.STATUS=1 ");}
//						
//						if(tmp==5){progressList.add(" SUBTASK_LIST.PROGRESS = 1 AND SUBTASK_LIST.STATUS=1 ");}
//						if(tmp==6){progressList.add(" SUBTASK_LIST.PROGRESS = 2 AND SUBTASK_LIST.STATUS=1 ");}
						
						if(tmp==7){progressList.add(" SUBTASK_LIST.STATUS = 0");}
						//if(tmp==8){progressList.add(" SUBTASK_LIST.STATUS = 1 ");}
						if(tmp==9){progressList.add(" SUBTASK_LIST.STATUS = 2 ");}
						
						if(tmp==11){
							progressList.add("SUBTASK_LIST.DIFF_DATE < 0 ");
						}
						if(tmp==12){
							progressList.add("SUBTASK_LIST.DIFF_DATE = 0 ");
						}
						if(tmp==13){
							progressList.add("SUBTASK_LIST.DIFF_DATE > 0 ");
						}
					}
				}
				//if (collectAndDay){conditionSql+=" AND SUBTASK_LIST.STAGE IN (0,1)";}
			}
//			if (collectAndDay){conditionSql+=" AND SUBTASK_LIST.STAGE IN (0,1)";}
			if(!progressList.isEmpty()){
				String tempSql = StringUtils.join(progressList," OR ");
				conditionSql += " AND (" + tempSql + ")";
			}
			QueryRunner run = new QueryRunner();
			long pageStartNum = (curPageNum - 1) * pageSize + 1;
			long pageEndNum = curPageNum * pageSize;
			//质检子任务语句
			
			StringBuilder sb = new StringBuilder();

			sb.append("WITH QUALITY_TASK AS ");
			sb.append(" (SELECT SS.SUBTASK_ID      QUALITY_SUBTASK_ID, ");
			sb.append(" SS.EXE_USER_ID     QUALITY_EXE_USER_ID,"
					+ "SS.EXE_GROUP_ID     QUALITY_EXE_GROUP_ID,"
					+ "G.GROUP_NAME QUALITY_EXE_GROUP_NAME, ");
			sb.append(" SS.PLAN_START_DATE AS QUALITY_PLAN_START_DATE,");
			sb.append(" SS.PLAN_END_DATE   AS QUALITY_PLAN_END_DATE,");
			sb.append(" SS.STATUS          QUALITY_TASK_STATUS,");
			sb.append(" UU.USER_REAL_NAME  AS QUALITY_EXE_USER_NAME");
			sb.append(" FROM SUBTASK SS, USER_INFO UU,USER_GROUP G");
			sb.append(" WHERE SS.IS_QUALITY = 1");
			sb.append(" AND SS.EXE_USER_ID = UU.USER_ID(+) AND SS.EXE_GROUP_ID = G.GROUP_ID(+)),");
			
			sb.append(" SUBTASK_LIST AS");
			sb.append(" (SELECT S.SUBTASK_ID,");
			sb.append(" S.STAGE,");
			sb.append(" S.NAME,");
			sb.append(" S.work_kind,");
			sb.append(" S.TYPE,");
			sb.append(" S.STATUS,");
			sb.append(" S.EXE_USER_ID,");
			sb.append(" U.USER_REAL_NAME AS EXECUTER,");
			sb.append(" UG.GROUP_NAME AS GROUP_EXECUTER,");
			sb.append(" NVL(FSOS.PERCENT, 0) PERCENT,");
			sb.append(" NVL(FSOS.DIFF_DATE, 0) DIFF_DATE,");
			sb.append(" NVL(FSOS.PROGRESS, 1) PROGRESS,");
			sb.append(" S.TASK_ID,");
			sb.append(" NVL(Q.QUALITY_SUBTASK_ID, 0) QUALITY_SUBTASK_ID,");
			sb.append(" NVL(Q.QUALITY_EXE_USER_ID, 0) QUALITY_EXE_USER_ID,");
			sb.append(" Q.QUALITY_PLAN_START_DATE,");
			sb.append(" Q.QUALITY_PLAN_END_DATE,");
			sb.append(" NVL(Q.QUALITY_TASK_STATUS, 0) QUALITY_TASK_STATUS,");
			sb.append(" Q.QUALITY_EXE_USER_NAME,"
			+ "Q.QUALITY_EXE_GROUP_ID,"
			+ "Q.QUALITY_EXE_GROUP_NAME, ");
			/*• 记录默认排序原则：
			 * ①根据状态排序：开启>草稿>100%(已完成)>已关闭
			 * 用order_status来表示这个排序的先后顺序。分别是开启0>草稿1>100%(已完成)2>已关闭3
			 * ②相同状态中根据剩余工期排序，逾期>0天>剩余/提前
			 * ③开启状态相同剩余工期，根据完成度排序，完成度高>完成度低；其它状态，根据名称
			 */
			
			sb.append(" CASE S.STATUS  WHEN 1 THEN CASE NVL(FSOS.PERCENT, 0) when 100 then 2 else 0 end when 2 then 1 when 0 then 3 end order_status,");
			sb.append("NVL((SELECT J.STATUS");
			sb.append(" FROM JOB_RELATION JR,JOB J");
			sb.append(" WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=1 AND J.LATEST=1 AND JR.ITEM_ID=S.SUBTASK_ID AND JR.ITEM_TYPE=3 ),-1) TIPS2MARK");
			sb.append(" FROM SUBTASK                  S,");
			sb.append(" USER_INFO                U,");
			sb.append(" USER_GROUP               UG,");
			sb.append(" FM_STAT_OVERVIEW_SUBTASK FSOS,");
			sb.append(" QUALITY_TASK             Q");
			sb.append(" WHERE Q.QUALITY_SUBTASK_ID(+) = S.QUALITY_SUBTASK_ID");
			sb.append(" AND S.IS_QUALITY = 0");
			sb.append(" AND U.USER_ID(+) = S.EXE_USER_ID");
			sb.append(" AND UG.GROUP_ID(+) = S.EXE_GROUP_ID");
			sb.append(" AND S.SUBTASK_ID = FSOS.SUBTASK_ID(+)),");
			
			sb.append(" FINAL_TABLE AS");
			sb.append(" (SELECT *");
			sb.append(" FROM SUBTASK_LIST");
			sb.append(" WHERE 1 = 1");
			sb.append(conditionSql);

			sb.append(" ORDER BY SUBTASK_LIST.ORDER_STATUS asc,");
			sb.append(" SUBTASK_LIST.DIFF_DATE    desc,");
			sb.append("  SUBTASK_LIST.PERCENT      desc,");
			sb.append(" SUBTASK_LIST.NAME)");
			sb.append(" SELECT /*+FIRST_ROWS ORDERED*/");
			sb.append(" TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM");
			sb.append("  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_");
			sb.append(" FROM FINAL_TABLE");
			sb.append(" WHERE ROWNUM <= "+pageEndNum+") TT");
			sb.append(" WHERE TT.ROWNUM_ >= "+pageStartNum);
	
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
					Page page = new Page();
					//Page page = new Page(curPageNum);
				    //page.setPageSize(pageSize);
				    int totalCount = 0;
				    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					while (rs.next()) {
						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
						subtask.put("subtaskName", rs.getString("NAME"));
						subtask.put("status", rs.getInt("STATUS"));
						subtask.put("stage", rs.getInt("STAGE"));
						subtask.put("workKind", rs.getInt("WORK_KIND"));
						subtask.put("type", rs.getInt("TYPE"));
						int userid=rs.getInt("EXE_USER_ID");
						String executer=rs.getString("EXECUTER");
						if(userid==0){
							executer=rs.getString("GROUP_EXECUTER");
						}
						subtask.put("executer", executer);
						
						subtask.put("percent", rs.getInt("percent"));
						subtask.put("diffDate", rs.getInt("DIFF_DATE"));
						
						int qualityTaskId=rs.getInt("quality_subtask_id");
						if(qualityTaskId!=0){subtask.put("hasQuality", 1);}
						else{subtask.put("hasQuality", 0);}
						
						subtask.put("qualitySubtaskId", qualityTaskId);
						subtask.put("qualityExeUserId", rs.getInt("quality_Exe_User_Id"));
						subtask.put("qualityExeGroupId", rs.getInt("quality_Exe_group_Id"));
						subtask.put("qualityExeGroupName", rs.getString("quality_Exe_group_NAME"));
						
						Timestamp qualityPlanStartDate = rs.getTimestamp("quality_Plan_Start_Date");
						Timestamp qualityPlanEndDate = rs.getTimestamp("quality_Plan_End_Date");
						if(qualityPlanStartDate != null){
							subtask.put("qualityPlanStartDate", df.format(qualityPlanStartDate));
						}else {subtask.put("qualityPlanStartDate", null);}
						if(qualityPlanEndDate != null){
							subtask.put("qualityPlanEndDate",df.format(qualityPlanEndDate));
						}else{subtask.put("qualityPlanEndDate", null);}
						
						subtask.put("qualityTaskStatus", rs.getInt("quality_Task_Status"));
						subtask.put("qualityExeUserName", rs.getString("quality_Exe_User_Name"));

						JSONArray jobs = new JSONArray();
						int tips2markStatus = rs.getInt("TIPS2MARK");
						if(tips2markStatus==-1){
							//关闭的采集子任务才能执行tips转mark
							if(rs.getInt("STATUS")==0 && rs.getInt("STAGE")==0){
								JSONObject job = new JSONObject();
								job.put("type", JobType.TiPS2MARK.value());
								job.put("status", 0);
								jobs.add(job);
							}
						}else{
							JSONObject job = new JSONObject();
							job.put("type", JobType.TiPS2MARK.value());
							job.put("status", tips2markStatus);
							jobs.add(job);
						}
						subtask.put("jobs", jobs);

						totalCount=rs.getInt("TOTAL_RECORD_NUM");
						list.add(subtask);
					}					
					page.setTotalCount(totalCount);
					page.setResult(list);
					return page;
				}

			};
			
			log.info("subtask list sql:" + sb.toString());
			Page page= run.query(conn, sb.toString(), rsHandler);
			page.setPageNum(curPageNum);
		    page.setPageSize(pageSize);
		    return page;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 返回值Map<Integer,Integer> key：taskId，programType：1，中线4，快线
	 * 原则：根据子任务id获取对应的任务id以及任务类型（快线/中线），任务类型和子任务类型相同
	 * 应用场景：采集（poi，tips）成果批任务号
	 * @param subtaskId
	 * @return Map<String,Integer> {taskId:12,programType:1} (programType：1，中线4，快线)
	 * @throws Exception
	 */
	public Map<String, Integer> getTaskBySubtaskId(int subtaskId)
			throws Exception {
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getManConnection();
			return getTaskBySubtaskId(conn,subtaskId);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 返回值Map<Integer,Integer> key：taskId，programType：1，中线4，快线
	 * 原则：根据子任务id获取对应的任务id以及任务类型（快线/中线），任务类型和子任务类型相同
	 * 应用场景：采集（poi，tips）成果批任务号
	 * @param subtaskId
	 * @return Map<String,Integer> {taskId:12,programType:1} (programType：1，中线4，快线)
	 * @throws Exception
	 */
	public Map<String, Integer> getTaskBySubtaskId(Connection conn,int subtaskId)
			throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			String sql="SELECT T.TASK_ID, P.TYPE"
					+ "  FROM SUBTASK S, TASK T, PROGRAM P"
					+ " WHERE S.TASK_ID = T.TASK_ID"
					+ "   AND T.PROGRAM_ID = P.PROGRAM_ID"
					+ "   AND S.SUBTASK_ID = "+subtaskId;
			return run.query(conn, sql, new ResultSetHandler<Map<String, Integer>>(){

				@Override
				public Map<String, Integer> handle(ResultSet rs)
						throws SQLException {
					Map<String, Integer> taskMap=new HashMap<String, Integer>();
					if(rs.next()){
						taskMap.put("taskId", rs.getInt("TASK_ID"));
						taskMap.put("programType", rs.getInt("TYPE"));
					}
					return taskMap;
				}
				
			});
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}
	}

	/**
	 * @param subtaskId
	 * @throws ServiceException 
	 */
	public Set<Integer> getCollectTaskIdByDaySubtask(int subtaskId) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			String sql = "SELECT TT.TASK_ID"
					+ "  FROM SUBTASK S, TASK T, TASK TT"
					+ " WHERE S.TASK_ID = T.TASK_ID"
					+ "   AND TT.BLOCK_ID = T.BLOCK_ID"
					+ "   AND T.PROGRAM_ID = TT.PROGRAM_ID"
					+ "   AND TT.TYPE = 0"
					+ "   AND S.SUBTASK_ID = " + subtaskId;
			
			log.info("getCollectTaskIdByDaySubtask sql :" + sql);
			
			
			ResultSetHandler<Set<Integer>> rsHandler = new ResultSetHandler<Set<Integer>>() {
				public Set<Integer> handle(ResultSet rs) throws SQLException {
					Set<Integer> result = new HashSet<Integer>();
					while(rs.next()) {
						result.add(rs.getInt("TASK_ID"));
					}
					return result;
				}
			};
			Set<Integer> result =  run.query(conn, sql,rsHandler);
			return result;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("getCollectTaskIdByDaySubtask失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 1.根据参数cityName与infor表中的admin_name模糊匹配，获取匹配成功的情报的所有采集子任务列表
	 * 应用场景：独立工具：采集成果中/无转快时，获取快线子任务列表
	 * @param 
	 * @param listAllInforByCity
	 * @throws Exception 
	 */
	public List<Map<String,Object>> listAllInforByCity(String cityName, JSONObject jsonObject) throws Exception{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select st.SUBTASK_ID, st.NAME, t.TASK_ID from TASK t, SUBTASK st, PROGRAM p, INFOR i "
					+ "where i.INFOR_ID = p.INFOR_ID "
					+ "AND p.PROGRAM_ID = t.PROGRAM_ID "
					+ "AND ST.STATUS IN (1,2) "
					+ "AND t.TASK_ID = st.TASK_ID AND ST.STAGE=0  AND st.is_quality=0 "
					+ "AND i.ADMIN_NAME like " +  "\'"+ "%" + cityName + "%" +"\'";
			
			if(jsonObject.containsKey("name") && jsonObject.getString("name").length() > 0){
				String name = " AND st.NAME like " + "\'"+ "%" + jsonObject.getString("name") + "%" +"\'";
				selectSql += name;;
			}
			
			return run.query(conn, selectSql, new ResultSetHandler<List<Map<String, Object>>>(){
				@Override
				public List<Map<String, Object>> handle(ResultSet result) throws SQLException {
					List<Map<String, Object>> res = new ArrayList<Map<String,Object>>();
					while(result.next()){
						Map<String, Object> sTaskMap = new HashMap<String, Object>();
						sTaskMap.put("subtaskId", result.getInt("SUBTASK_ID"));
						sTaskMap.put("name", result.getObject("NAME"));
						sTaskMap.put("taskId", result.getObject("TASK_ID"));
						res.add(sTaskMap);
					}
					return res;
				}});
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 根据grid查询substask
	 * 1、优先找快线开启状态的众包子任务，其次是中线；
	 * 如果快线(中线)存在多个开启的众包子任务，则选择子任务号值最大的众包子任务
	 * 2、如果没有符合的众包子任务，则返回null
	 * @param grid
	 * @return subtask
	 * @author songhe
	 * 
	 * */
	public Subtask queryCrowdSubtaskByGrid(String grid) throws Exception{
		Subtask substask = new Subtask();
		Connection conn = null;
		if(StringUtils.isBlank(grid)){
			return null;
		}
		try{
			conn = DBConnector.getInstance().getManConnection();
			substask = SubtaskService.getInstance().queryOpenSubstaskFast(grid, conn);
			if(substask == null){
				substask = SubtaskService.getInstance().queryOpenSubstaskMid(grid, conn);
				if(substask == null){
					return null;
				}
			}
		}catch(Exception e ){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
		return substask;
	}
	
	/**
	 * 查找快线开启状态的众包子任务
	 * @param grid
	 * @return Map
	 * @author songhe
	 * @throws Exception 
	 * 
	 * */
	public Subtask queryOpenSubstaskFast(String grid, Connection conn) throws Exception{
		String sql = "select st.*, r.DAILY_DB_ID from TASK t, PROGRAM p, SUBTASK st, SUBTASK_GRID_MAPPING sgm, REGION r "
				+ "where t.task_id = st.task_id and t.program_id = p.program_id and sgm.subtask_id = st.subtask_id"
				+ " and t.region_id = r.region_id and p.type = 4 and st.status = 1 and st.work_kind = 2 and sgm.grid_id = " + grid + " order by st.subtask_id desc";
		
		QueryRunner run = new QueryRunner();
		try{
			return run.query(conn, sql, new ResultSetHandler<Subtask>(){
				@Override
				public Subtask handle(ResultSet result) throws SQLException {
					if(result.next()){
						Subtask substask = new Subtask();
						substask.setCreateDate(result.getTimestamp("CREATE_DATE"));
						substask.setCreateUserId(result.getInt("CREATE_USER_ID"));
						substask.setDescp(result.getString("DESCP"));
						substask.setExeGroupId(result.getInt("EXE_GROUP_ID"));
						substask.setExeUserId(result.getInt("EXE_USER_ID"));
						substask.setGeometry(result.getString("GEOMETRY"));
						substask.setIsQuality(result.getInt("IS_QUALITY"));
						substask.setName(result.getString("NAME"));
						substask.setPlanEndDate(result.getTimestamp("PLAN_END_DATE"));
						substask.setPlanStartDate(result.getTimestamp("PLAN_START_DATE"));
						substask.setQualitySubtaskId(result.getInt("QUALITY_SUBTASK_ID"));
						substask.setReferId(result.getInt("REFER_ID"));
						substask.setStage(result.getInt("STAGE"));
						substask.setStatus(result.getInt("STATUS"));
						substask.setSubtaskId(result.getInt("SUBTASK_ID"));
						substask.setTaskId(result.getInt("TASK_ID"));
						substask.setType(result.getInt("TYPE"));
						substask.setDbId(result.getInt("DAILY_DB_ID"));
						substask.setSubType(4);
						
						return substask;
					}
					return null;
				}});
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("快线子任务查询失败，原因为:"+e.getMessage(),e);
		}
	}
	/**
	 * 查找中线开启状态的众包子任务
	 * @param grid
	 * @return Map
	 * @author songhe
	 * @throws Exception 
	 * 
	 * */
	public Subtask queryOpenSubstaskMid(String grid, Connection conn) throws Exception{
		String sql = "select st.*, r.DAILY_DB_ID from TASK t, PROGRAM p, SUBTASK st, SUBTASK_GRID_MAPPING sgm, REGION r "
				+ "where t.task_id = st.task_id and t.program_id = p.program_id and sgm.subtask_id = st.subtask_id"
				+ " and t.region_id = r.region_id and p.type = 1 and st.status = 1 and st.work_kind = 2 and sgm.grid_id = " + grid + " order by st.subtask_id desc";
		
		QueryRunner run = new QueryRunner();
		try{
			return run.query(conn, sql, new ResultSetHandler<Subtask>(){
				@Override
				public Subtask handle(ResultSet result) throws SQLException {
					if(result.next()){
						Subtask substask = new Subtask();
						substask.setCreateDate(result.getTimestamp("CREATE_DATE"));
						substask.setCreateUserId(result.getInt("CREATE_USER_ID"));
						substask.setDescp(result.getString("DESCP"));
						substask.setExeGroupId(result.getInt("EXE_GROUP_ID"));
						substask.setExeUserId(result.getInt("EXE_USER_ID"));
						substask.setGeometry(result.getString("GEOMETRY"));
						substask.setIsQuality(result.getInt("IS_QUALITY"));
						substask.setName(result.getString("NAME"));
						substask.setPlanEndDate(result.getTimestamp("PLAN_END_DATE"));
						substask.setPlanStartDate(result.getTimestamp("PLAN_START_DATE"));
						substask.setQualitySubtaskId(result.getInt("QUALITY_SUBTASK_ID"));
						substask.setReferId(result.getInt("REFER_ID"));
						substask.setStage(result.getInt("STAGE"));
						substask.setStatus(result.getInt("STATUS"));
						substask.setSubtaskId(result.getInt("SUBTASK_ID"));
						substask.setTaskId(result.getInt("TASK_ID"));
						substask.setType(result.getInt("TYPE"));
						substask.setDbId(result.getInt("DAILY_DB_ID"));
						substask.setSubType(1);
						
						return substask;
					}
					return null;
				}});
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("中线子任务查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	//Object转String工具
    public static String objetConvertString(Object a){
    	String result = "";
    	if(a == null){
    		return result;
    	}
    	return a.toString();
    }
	

	/**
	 * @param dbId
	 * @param type
	 * @return
	 * @throws Exception 
	 */
	public Map<Integer, List<Integer>> getOpendMultiSubtaskGridMappingByDbId(int dbId, int type) throws Exception {
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(" SELECT S.SUBTASK_ID,SGM.GRID_ID FROM SUBTASK S, SUBTASK_GRID_MAPPING SGM,TASK T,PROGRAM P,REGION R    ");
			sb.append(" WHERE P.TYPE = " + type);
			sb.append(" AND P.PROGRAM_ID = T.PROGRAM_ID                                                                       ");
			sb.append(" AND T.TASK_ID = S.TASK_ID                                                                             ");
			sb.append(" AND S.SUBTASK_ID = SGM.SUBTASK_ID                                                                     ");
			sb.append(" AND S.WORK_KIND = 4                                                                    ");
			sb.append(" AND S.STATUS = 1                                                                    ");
			sb.append(" AND T.REGION_ID = R.REGION_ID                                                                         ");
			sb.append(" AND (R.DAILY_DB_ID = " + dbId + " OR R.MONTHLY_DB_ID = " + dbId + ") ");
			sb.append(" ORDER BY S.SUBTASK_ID                                                                                 ");
			
			log.info("getOpendMultiSubtaskGridMappingByDbId sql:" + sb.toString());
			return run.query(conn, sb.toString(), new ResultSetHandler<Map<Integer, List<Integer>>>(){
				@Override
				public Map<Integer, List<Integer>> handle(ResultSet result) throws SQLException {
					Map<Integer, List<Integer>> res = new HashMap<Integer, List<Integer>>();
					int subtaskId = 0;
					List<Integer> list = new ArrayList<Integer>();
					while(result.next()){
						if(subtaskId!=result.getInt("SUBTASK_ID")){
							res.put(subtaskId, list);
							subtaskId = result.getInt("SUBTASK_ID");
							list = new ArrayList<Integer>();
						}
						list.add(result.getInt("GRID_ID"));
					}
					res.put(subtaskId, list);
					res.remove(0);
					return res;
				}});
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @param dbId
	 * @param statusList
	 * @param workKind
	 * @return
	 * @throws Exception 
	 */
	public List<Integer> getSubtaskIdListByDbId(int dbId, List<Integer> statusList, int workKind) throws Exception {
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(" SELECT S.SUBTASK_ID FROM SUBTASK S,TASK T,REGION R  ");
			sb.append(" WHERE T.TASK_ID = S.TASK_ID                         ");
			sb.append(" AND T.REGION_ID = R.REGION_ID                       ");
			sb.append(" AND (R.DAILY_DB_ID = " + dbId + " OR R.MONTHLY_DB_ID = " + dbId + ")");
			sb.append(" AND S.STATUS IN (" + StringUtils.join(statusList,",") + ") ");
			sb.append(" AND S.WORK_KIND = " + workKind);
			
			return run.query(conn, sb.toString(), new ResultSetHandler<List<Integer>>(){
				@Override
				public List<Integer> handle(ResultSet result) throws SQLException {
					List<Integer> res = new ArrayList<Integer>();
					while(result.next()){
						res.add(result.getInt("SUBTASK_ID"));
					}
					return res;
				}});
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Subtask queryBySubTaskIdAndIsQuality(Integer taskId, String stage, Integer isQuality) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			return queryBySubTaskIdAndIsQuality(conn,taskId,stage,isQuality);		
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public Subtask queryBySubTaskIdAndIsQuality(Connection conn, Integer taskId, String stage, Integer isQuality) throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT ST.SUBTASK_ID,ST.NAME,ST.STATUS,ST.STAGE,ST.DESCP,ST.PLAN_START_DATE,ST.PLAN_END_DATE,ST.TYPE,ST.GEOMETRY,ST.REFER_ID");
			sb.append(",ST.EXE_USER_ID,ST.EXE_GROUP_ID,ST.IS_QUALITY");
			sb.append(",T.TASK_ID,T.TYPE TASK_TYPE,R.DAILY_DB_ID,R.MONTHLY_DB_ID");
			sb.append(" FROM SUBTASK ST,TASK T,REGION R");
			sb.append(" WHERE ST.TASK_ID = T.TASK_ID");
			sb.append(" AND T.REGION_ID = R.REGION_ID");
			if(isQuality==1){
				sb.append(" AND ST.quality_subtask_id = '"+taskId+"' AND ST.is_quality = '0'");
			}else if(isQuality==0){
				sb.append(" AND ST.SUBTASK_ID = (SELECT quality_subtask_id FROM SUBTASK WHERE subtask_id = '"+taskId+"' AND is_quality = '0')");
			}
			sb.append(" AND ST.STAGE = " + stage);
			if(stage.equals("2")){
				sb.append(" AND ST.TYPE = " + "7");
			}
	
			String selectSql = sb.toString();
			log.info("请求子任务详情SQL："+sb.toString());
			

			ResultSetHandler<Subtask> rsHandler = new ResultSetHandler<Subtask>() {
				public Subtask handle(ResultSet rs) throws SQLException {
					//StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
					if (rs.next()) {
						Subtask subtask = new Subtask();						
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setName(rs.getString("NAME"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						subtask.setDescp(rs.getString("DESCP"));
						subtask.setStatus(rs.getInt("STATUS"));
						subtask.setReferId(rs.getInt("REFER_ID"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setExeUserId(rs.getInt("EXE_USER_ID"));
						subtask.setExeGroupId(rs.getInt("EXE_GROUP_ID"));
						subtask.setIsQuality(rs.getInt("IS_QUALITY"));
						
						//GEOMETRY
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.setGeometry(GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						try {
							Map<Integer,Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
							subtask.setGridIds(gridIds);
//							Map<String,Integer> gridIdMap = new HashMap<String,Integer>();
//							for(Map.Entry<Integer, Integer> entry:gridIds.entrySet()){
//								gridIdMap.put(entry.getKey().toString(), entry.getValue());
//							}
//							subtask.setGridIds(gridIdMap);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						subtask.setTaskId(rs.getInt("TASK_ID"));
						if (2 == rs.getInt("STAGE")) {
							//月编子任务
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
						} else {
							subtask.setDbId(rs.getInt("DAILY_DB_ID"));
						}				
						
						return subtask;
					}
					return null;
				}	
			};
			log.info("queryByTaskId sql:" + sb.toString());
			return run.query(conn, selectSql,rsHandler);			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		}
	}
	
	/**
	 * 通过质检子任务id获取常规子任务相关信息。用于编辑过程中tips质检子任务
	 * @param qualitySubtaskId
	 * @return Map<String, String> returnMap=new HashMap<String, String>();
						returnMap.put("subtaskId", rs.getString("SUBTASK_ID"));
						returnMap.put("exeUserId", rs.getString("EXE_USER_ID"));
						returnMap.put("exeUserName", rs.getString("USER_REAL_NAME"));
						returnMap.put("groupId", rs.getString("GROUP_ID"));
						returnMap.put("groupName", rs.getString("GROUP_NAME"));
						returnMap.put("finishedRoad", rs.getString("FINISHED_ROAD"));
						returnMap.put("subtaskName", rs.getString("SUBTASK_NAME"));
						returnMap.put("taskName", rs.getString("TASK_NAME"));
	 * @throws Exception 
	 */
	public Map<String, String> getCommonSubtaskByQualitySubtask(int qualitySubtaskId) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			Set<Integer> qualitySets=new HashSet<Integer>();
			qualitySets.add(qualitySubtaskId);
			Map<Integer, Map<String, String>> map = SubtaskOperation.getCommonByQuality(conn, qualitySets);
			if(map!=null&&map.size()>0){return map.get(qualitySubtaskId);}
			return null;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("getGroupNameBySubtaskId，原因为:" + e.getMessage(), e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public Map<Integer, Integer> getsubtaskUserMap() throws Exception {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
	
			String selectSql = "SELECT S.SUBTASK_ID,S.EXE_USER_ID FROM SUBTASK S ";
			log.info("getsubtaskUserMap SQL："+selectSql);
			

			ResultSetHandler<Map<Integer, Integer>> rsHandler = new ResultSetHandler<Map<Integer, Integer>>() {
				public Map<Integer, Integer> handle(ResultSet rs) throws SQLException {
					Map<Integer,Integer> subtaskUserMap = new HashMap<Integer,Integer>();
					while (rs.next()) {
						subtaskUserMap.put(rs.getInt("SUBTASK_ID"), rs.getInt("EXE_USER_ID"));
					}
					return subtaskUserMap;
				}	
			};
			return run.query(conn, selectSql,rsHandler);			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("getsubtaskUserMap，原因为:" + e.getMessage(), e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 编辑子任务圈接口
	 * 原则：如果S圈对应的采集子任务已经开启，则不能进行任何操作；草稿状态子任务的S圈如果修改，则删除与采集子任务的关联
	 * 应用场景：独立工具--外业规划--绘制子任务圈—合并/切分等操作
	 * @param blockId
	 * @param condition
	 * @throws Exception
	 */
	public void paintRefer(int taskId, JSONObject condition)  throws Exception {
		Connection conn = null;
		StringBuilder logs = new StringBuilder();
		try {
			String lineWkt="";
			int id1=0;
			int id2=0;
			if(condition.containsKey("lineWkt")){
				lineWkt=condition.getString("lineWkt");
			}
			if(condition.containsKey("id1")){
				id1=condition.getInt("id1");
			}
			if(condition.containsKey("id2")){
				id2=condition.getInt("id2");
			}
			
			logs.append("paintRefer taskId="+taskId+";condition="+condition.toString());
			log.info(logs);
			
			conn=DBConnector.getInstance().getManConnection();
			
			//1.nowait方式锁与blockId下的子任务圈以及对应的采集子任务，并获取范围
			Task task = TaskService.getInstance().queryByTaskId(conn, taskId);
			JSONObject conditionQuery2=new JSONObject();
			conditionQuery2.put("blockId", task.getBlockId());
			
			logs.append(";blockId=");
			logs.append(task.getBlockId());
			
			JSONArray ids=new JSONArray();
			if(id1!=0){ids.add(id1);}
			if(id2!=0){ids.add(id2);}
			if(ids.size()!=0){conditionQuery2.put("ids", ids);}
			List<SubtaskRefer> refers = queryReferByTaskId(conn,conditionQuery2,true);

			JSONObject conditionQuery=new JSONObject();
			conditionQuery.put("taskId", taskId);
			conditionQuery.put("stage", 0);				
			List<Subtask> subtasks = querySubtask(conn,conditionQuery,true);
			
			if(!StringUtils.isEmpty(lineWkt)){
				/*
				 * //wkt,判断要拆分的子任务圈是否已开启，未开启则锁表，并获取geo；已开启则返回
			//若没有子任务，则直接拆分block；否则拆分子任务圈，并删除采集子任务与不规则圈的关联
				 */
				Geometry lineGeo=GeoTranslator.wkt2Geometry(lineWkt);
				if(id1==0){//切分block
					Block block = BlockService.getInstance().queryByBlockId(conn,task.getBlockId());
					Geometry referGeo = block.getOriginGeo();
					logs.append(";blockOriginGeo=");
					logs.append(GeoTranslator.jts2Wkt(referGeo));
					Geometry referGeoLine=GeoTranslator.createLineString(referGeo.getCoordinates());
					//线是否穿过面
					Geometry interGeo=referGeoLine.intersection(lineGeo);						
					if(interGeo==null||interGeo.getCoordinates().length==0){throw new ServiceException("线面没有交点");}
					if(interGeo.getCoordinates().length!=2){
						throw new ServiceException("线面交点大于2个，请重新画线");
					}					
					
					Geometry midLine=referGeo.intersection(lineGeo);
					Geometry unionGeo=GeoTranslator.addCoorToGeo(referGeo, interGeo.getCoordinates()[0]);
					unionGeo=GeoTranslator.addCoorToGeo(unionGeo, interGeo.getCoordinates()[1]);
					boolean isIn=GeometryUtils.InteriorAnd2Intersection(midLine, unionGeo);
					if(!isIn){
						throw new Exception("线不在面内，请重新划线");
					}
					
					//line所切割的面对应的子任务是否开启
					//4.需要切割的不规则圈对应的子任务的状态为草稿，清空不规则圈。					
					List<Geometry> addGeo=GeoTranslator.splitPolygonByLine(lineGeo,referGeo);
					//5.保存信息
					for(Geometry g:addGeo){
						if(!g.isSimple()){throw new ServiceException("切割后不是简单面，请重新画线");}
						SubtaskRefer referNew=new SubtaskRefer();
						referNew.setBlockId(task.getBlockId());
						referNew.setGeometry(g);
						SubtaskReferOperation.create(conn, referNew);
					}
				}else{
					if(refers==null||refers.size()==0){throw new ServiceException("不规则面不存在，请重新选择:id="+id1);}
					
					//交点
					Geometry interGeo=null;
					List<Geometry> addGeo=null;
					List<Subtask> subtaskRelates=new ArrayList<>();
					
					log.info("start 切割选定面");
					SubtaskRefer refer=refers.get(0);
					Geometry referGeo = refer.getGeometry();
					logs.append(";不规则圈geo1=");
					logs.append(GeoTranslator.jts2Wkt(referGeo));
					Geometry referGeoLine=GeoTranslator.createLineString(referGeo.getCoordinates());
					//线是否穿过面
					interGeo=referGeoLine.intersection(lineGeo);						
					if(interGeo==null||interGeo.getCoordinates().length==0){
						log.info("线面没有交点");
						throw new ServiceException("线面没有交点，请重新画线;");
					}
					if(interGeo.getCoordinates().length!=2){
						log.info("线面交点不为2");
						throw new ServiceException("线面交点不为2个，请重新画线;");
					}		
					
					Geometry midLine=referGeo.intersection(lineGeo);
					Geometry unionGeo=GeoTranslator.addCoorToGeo(referGeo, interGeo.getCoordinates()[0]);
					unionGeo=GeoTranslator.addCoorToGeo(unionGeo, interGeo.getCoordinates()[1]);
					boolean isIn=GeometryUtils.InteriorAnd2Intersection(midLine, unionGeo);
					//boolean isIn=GeometryUtils.InteriorAnd2Intersection(midLine, referGeo);
					if(!isIn){
						log.info("线不在面内");
						throw new ServiceException("线不在面内，请重新划线;");
					}
					
					//line所切割的面对应的子任务是否开启
					//4.需要切割的不规则圈对应的子任务的状态为草稿，清空不规则圈。
					for(Subtask s: subtasks){
						if(s.getReferId()==refer.getId()){
							if(s.getStatus()==1){
								throw new ServiceException("不规则圈对应的子任务"+s.getSubtaskId()+"为开启状态，不能做后续操作");
							}
							s.setReferId(0);
							subtaskRelates.add(s);
						}
					}
					addGeo=GeoTranslator.splitPolygonByLine(lineGeo,referGeo);
					log.info("end 切割选定面");
					
					//处理邻接不规则面
					JSONObject conditionQuery3=new JSONObject();
					conditionQuery3.put("blockId", task.getBlockId());
					List<SubtaskRefer> refersOther = queryReferByTaskId(conn,conditionQuery3,true);
					Coordinate[] points = interGeo.getCoordinates();
					Coordinate p1=points[0];
					Coordinate p2=points[1];
					for(SubtaskRefer referTmp:refersOther){
						if(id1==referTmp.getId()){continue;}
						Geometry referGeoTmp = referTmp.getGeometry();
						Geometry referGeoLineTmp=GeoTranslator.createLineString(referGeoTmp.getCoordinates());
						//线面是否有交点
						Geometry interGeoTmp = referGeoLineTmp.intersection(lineGeo);						
						if(interGeoTmp==null||interGeoTmp.getCoordinates().length==0){
							continue;
						}
						List<Coordinate> newLineCors=new ArrayList<Coordinate>();
						//有交点
						List<Coordinate> coordinates = Arrays.asList(referGeoLineTmp.getCoordinates());
						boolean isChange=false;
						for (int i = coordinates.size() - 1; i >= 1; i--) {
							newLineCors.add(coordinates.get(i));
			                if (!p1.equals(coordinates.get(i))&&!p1.equals(coordinates.get(i-1))&&GeoTranslator.isIntersection(coordinates.get(i), coordinates.get(i - 1), p1,0.0000001)) {
			                	newLineCors.add(p1);
			                	isChange=true;
			                }
			                if (!p2.equals(coordinates.get(i))&&!p2.equals(coordinates.get(i-1))&&GeoTranslator.isIntersection(coordinates.get(i), coordinates.get(i - 1), p2,0.0000001)) {
			                	newLineCors.add(p2);
			                	isChange=true;
			                }
			            }
						newLineCors.add(coordinates.get(0));
						if(isChange){
							List<Geometry> subLines=new ArrayList<Geometry>();
							subLines.add(GeoTranslator.createLineString(newLineCors));
							Geometry pTmp = GeoTranslator.getCalLineToPython(subLines);
							referTmp.setGeometry(pTmp);
							SubtaskReferOperation.updateGeo(conn,referTmp);
							log.info("邻接不规则圈进行修改id="+referTmp.getId()+",wkt="+pTmp.toText());
						}						
					}
					
					//5.保存信息
					for(Geometry g:addGeo){
						if(!g.isSimple()){throw new ServiceException("切割后不是简单面，请重新画线");}
						SubtaskRefer referNew=new SubtaskRefer();
						referNew.setBlockId(refer.getBlockId());
						referNew.setGeometry(g);
						SubtaskReferOperation.create(conn, referNew);
					}
					
					Set<Integer> idSet=new HashSet<Integer>();
					idSet.add(id1);
					SubtaskReferOperation.delete(conn, idSet);
					
					for(Subtask s:subtaskRelates){
						SubtaskOperation.updateSubtask(conn, s);
					}
				}				
			}else{
				/*
				 * //若为id1，id2；则判断id对应的子任务是否开启，未开启则锁表，并获取geo；已开启则返回
			//未开启，则删除id1，id2,合并不规则圈生成id3，
				 */
				//1.nowait方式锁id1,id2对应的不规则圈，子任务
				//2.是否开启，开启返回
				List<Subtask> subtaskRelates=new ArrayList<>();
				for(Subtask s: subtasks){
					if(s.getReferId()==id1||s.getReferId()==id2){
						if(s.getStatus()==1){
							throw new ServiceException("不规则圈对应的子任务"+s.getSubtaskId()+"为开启状态，不能做后续操作");
						}
						s.setReferId(0);
						subtaskRelates.add(s);
					}
				}
				//3.合并范围，去除关系
				if(refers==null||refers.size()!=2){throw new ServiceException("未找到对应的不规则圈"); }
				Geometry geo1 = refers.get(0).getGeometry();
				Geometry geo2 = refers.get(1).getGeometry();
				logs.append(";不规则圈geo1=");
				logs.append(GeoTranslator.jts2Wkt(geo1));
				logs.append(";不规则圈geo2=");
				logs.append(GeoTranslator.jts2Wkt(geo2));
				Geometry unionGeo=geo1.union(geo2);
				
				if(!unionGeo.isSimple()){throw new ServiceException("合并后不是简单面，请重新选择");}
				JSONObject geoJson = GeoTranslator.jts2Geojson(unionGeo);
				int ringNum=geoJson.getJSONArray("coordinates").size();
				if(ringNum>1){throw new Exception("合并后形成环，请重新选择");}
				//4.保存
				SubtaskRefer refer=new SubtaskRefer();
				refer.setBlockId(refers.get(0).getBlockId());
				refer.setGeometry(unionGeo);
				SubtaskReferOperation.create(conn, refer);
				
				Set<Integer> idSet=new HashSet<Integer>();
				idSet.add(id1);
				idSet.add(id2);
				SubtaskReferOperation.delete(conn, idSet);
				
				for(Subtask s:subtaskRelates){
					SubtaskOperation.updateSubtask(conn, s);
				}
				
				//若合并后，该block下只有一个不规则圈，则直接将block的不规则圈赋值给该不规则圈
				JSONObject conditionQuery3=new JSONObject();
				conditionQuery3.put("blockId", task.getBlockId());
				List<SubtaskRefer> refersAll = queryReferByTaskId(conn,conditionQuery3,true);
				if(refersAll.size()==1){
					Block block = BlockService.getInstance().queryByBlockId(conn,task.getBlockId());
					SubtaskRefer oneRefer=refersAll.get(0);
					oneRefer.setGeometry(block.getOriginGeo());
					SubtaskReferOperation.updateGeo(conn, oneRefer);
				}
			}			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			logs.append(";error=");
			logs.append(e.getMessage());
			throw new ServiceException("getsubtaskUserMap，原因为:" + e.getMessage(), e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
			ManLogOperation.insertLog("paintRefer", logs.toString());
		}
	}
	
	
	/**
	 * 
	 * @param conn
	 * @param subtaskId
	 * @param isLock true 锁表 false 不锁表
	 * @return
	 * @throws ServiceException
	 */
	public List<Subtask> querySubtask(Connection conn,JSONObject condition,boolean isLock) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			
			String sql="SELECT S.SUBTASK_ID,"
					+ "       S.TASK_ID,"
					+ "       S.GEOMETRY,"
					+ "       S.STAGE,"
					+ "       S.TYPE,"
					+ "       S.STATUS,"
					+ "       S.IS_QUALITY,"
					+ "       S.REFER_ID"
					+ "  FROM SUBTASK S"
					+ " WHERE 1=1";
			if(condition!=null&&condition.size()>0){
				Iterator<?> keyIter = condition.keys();
				while (keyIter.hasNext()) {
					String key=(String) keyIter.next();
					if(key.equals("taskId")){
						sql=sql+" AND S.TASK_ID="+condition.getInt(key);
					}
					if(key.equals("stage")){
						sql=sql+" AND S.STAGE="+condition.getInt(key);
					}
				}
			}
			if(isLock){sql=sql+ "   FOR UPDATE NOWAIT";}
			log.info("请求子任务详情SQL："+sql);
			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>() {
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> subtasks=new ArrayList<Subtask>();
					while (rs.next()) {
						Subtask subtask = new Subtask();						
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setStatus(rs.getInt("STATUS"));
						subtask.setReferId(rs.getInt("REFER_ID"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setIsQuality(rs.getInt("IS_QUALITY"));
						subtask.setTaskId(rs.getInt("TASK_ID"));
						
						//GEOMETRY
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.setGeometry(GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}		
						subtasks.add(subtask);
					}
					return subtasks;
				}	
			};
			return run.query(conn, sql,rsHandler);			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} 
	}
	
	/**
	 * 
	 * @param conn
	 * @param subtaskId
	 * @param isLock true 锁表 false 不锁表
	 * @return
	 * @throws ServiceException
	 */
	public List<SubtaskRefer> queryReferByTaskId(Connection conn,JSONObject condition,boolean isLock) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			
			String sql="SELECT R.ID, R.GEOMETRY, R.BLOCK_ID"
					+ "  FROM SUBTASK_REFER R"
					+ " WHERE 1=1";
			if(condition!=null&&condition.size()>0){
				Iterator<?> keyIter = condition.keys();
				while (keyIter.hasNext()) {
					String key=(String) keyIter.next();
					if(key.equals("blockId")){
						sql=sql+" AND R.BLOCK_ID="+condition.getInt(key);
					}
					if(key.equals("ids")){
						sql=sql+" AND R.ID in "+condition.getJSONArray(key).toString().replace("[", "(").replace("]", ")");
					}
				}
			}
			if(isLock){sql=sql+ "   FOR UPDATE NOWAIT";}
			log.info("queryReferByTaskId SQL："+sql);
			ResultSetHandler<List<SubtaskRefer>> rsHandler = new ResultSetHandler<List<SubtaskRefer>>() {
				public List<SubtaskRefer> handle(ResultSet rs) throws SQLException {
					List<SubtaskRefer> subtasks=new ArrayList<SubtaskRefer>();
					while (rs.next()) {
						SubtaskRefer refer = new SubtaskRefer();						
						refer.setId(rs.getInt("ID"));		
						refer.setBlockId(rs.getInt("BLOCK_ID"));	
						//GEOMETRY
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							refer.setGeometry(GeoTranslator.struct2Jts(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}		
						subtasks.add(refer);
					}
					return subtasks;
				}	
			};
			return run.query(conn, sql,rsHandler);			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} 
	}
	
	
	/**
	 * 获取所有质检子任务列表
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	public JSONObject unPlanQualitylist(Integer taskId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run=new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT DISTINCT s.subtask_id,s.name FROM SUBTASK S WHERE S.TASK_ID ="+taskId);
			sb.append(" AND S.STATUS IN (1, 2) AND S.IS_QUALITY = 1");
			sb.append(" AND S.REFER_ID != 0 AND S.QUALITY_PLAN_STATUS = 0 ");

			String selectSql= sb.toString();
			log.info("unPlanQualitylist sql :" + selectSql);

			ResultSetHandler<JSONObject> rsHandler = new ResultSetHandler<JSONObject>() {
				public JSONObject handle(ResultSet rs) throws SQLException {
					JSONObject jsonObject = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					while (rs.next()) {
						JSONObject jo = new JSONObject();
						jo.put("subtaskId", rs.getInt(1));
						jo.put("sub", rs.getString(2));
						jsonArray.add(jo);
					}
					jsonObject.put("result", jsonArray);
					jsonObject.put("totalCount", jsonArray.size());
					return jsonObject;
				}
			};
			return run.query(conn, selectSql, rsHandler);	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 删除质检圈
	 * @param qualityId
	 * @return
	 * @throws Exception
	 */
	public int qualityDelete(int qualityId)  throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			StringBuilder sb = new StringBuilder();
			sb.append("DELETE FROM SUBTASK_QUALITY WHERE QUALITY_ID = ");
			sb.append(qualityId);

			String sql= sb.toString();
			log.info("qualityDelete sql :" + sql);

			return run.update(conn, sql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("删除质检圈失败，原因为：" + e.getMessage());
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 获取质检圈列表
	 * @param subtaskId
	 * @return
	 * @throws Exception
	 */
	public JSONObject qualitylist(int subtaskId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run=new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT SUBTASK_ID, QUALITY_ID, GEOMETRY FROM SUBTASK_QUALITY WHERE SUBTASK_ID = ");
			sb.append(subtaskId);

			String selectSql= sb.toString();
			log.info("qualitylist sql :" + selectSql);

			ResultSetHandler<JSONObject> rsHandler = new ResultSetHandler<JSONObject>() {
				public JSONObject handle(ResultSet rs) throws SQLException {
					JSONObject jsonObject = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					while (rs.next()) {
						JSONObject jo = new JSONObject();
						jo.put("subtaskId", rs.getInt("SUBTASK_ID"));
						jo.put("qualityId", rs.getInt("QUALITY_ID"));
						try {
							STRUCT struct=(STRUCT)rs.getObject("geometry");
							String clobStr = GeoTranslator.struct2Wkt(struct);
							jo.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						jsonArray.add(jo);
					}
					jsonObject.put("result", jsonArray);
					jsonObject.put("totalCount", jsonArray.size());
					return jsonObject;
				}
			};
			return run.query(conn, selectSql, rsHandler);	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 创建质检圈
	 * @param dataJson
	 * @throws Exception
	 */
	public void qualityCreate(JSONObject dataJson)  throws Exception {
		Integer subtaskId = dataJson.getInt("subtaskId");
		Geometry geometry = GeoTranslator.wkt2Geometry(dataJson.getString("geometry"));
		
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT SR.GEOMETRY FROM SUBTASK S, SUBTASK_REFER SR WHERE S.REFER_ID = SR.ID AND S.SUBTASK_ID = ");
			sb.append(subtaskId);

			String selectSql = sb.toString();
			log.info("查询不规则子任务圈 sql :" + selectSql);

			Geometry geometryRefer = run.query(conn, selectSql, geometryHandler);
			if(geometryRefer != null){
				Geometry newGeometry = geometry.intersection(geometryRefer);
				if(newGeometry==null||newGeometry.isEmpty()){throw new Exception("绘制的质检圈完全超过子任务不规则圈，请重新画");}
				String createSql = "INSERT INTO SUBTASK_QUALITY (QUALITY_ID, SUBTASK_ID, GEOMETRY) VALUES (Subtask_quality_SEQ.Nextval,?,?)";
				run.update(conn, createSql, subtaskId, GeoTranslator.wkt2Struct(conn, GeoTranslator.jts2Wkt(newGeometry,0.00001, 5)));
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建质检圈失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 修改质检圈
	 * @param dataJson
	 * @throws Exception
	 */
	public void qualityUpdate(JSONObject dataJson)  throws Exception {
		Integer qualityId = dataJson.getInt("qualityId");
		Geometry geometry = GeoTranslator.wkt2Geometry(dataJson.getString("geometry"));
		
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT SR.GEOMETRY FROM SUBTASK S, SUBTASK_REFER SR WHERE S.REFER_ID = SR.ID AND S.SUBTASK_ID = ");
			sb.append("(SELECT SUBTASK_ID FROM SUBTASK_QUALITY WHERE QUALITY_ID = ");
			sb.append(qualityId);
			sb.append(")");
			
			String selectSql = sb.toString();
			log.info("查询不规则子任务圈 sql :" + selectSql);
			
			Geometry geometryQuality = run.query(conn, selectSql, geometryHandler);
			if(geometryQuality != null){
				Geometry newGeometry = geometry.intersection(geometryQuality);
				if(newGeometry==null||newGeometry.isEmpty()){throw new Exception("绘制的质检圈完全超过子任务不规则圈，请重新修改");}
				String updateSql = "UPDATE SUBTASK_QUALITY SET GEOMETRY =  ? WHERE QUALITY_ID = ?";
				run.update(conn, updateSql, GeoTranslator.wkt2Struct(conn, GeoTranslator.jts2Wkt(newGeometry,0.00001, 5)), qualityId);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改质检圈失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 创建、修改质检圈的结果集处理器
	 */
	ResultSetHandler<Geometry> geometryHandler = new ResultSetHandler<Geometry>() {
		public Geometry handle(ResultSet rs) throws SQLException {
			while (rs.next()) {
				try {
					STRUCT struct=(STRUCT)rs.getObject("geometry");
					String clobStr = GeoTranslator.struct2Wkt(struct);
					return GeoTranslator.wkt2Geometry(clobStr);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			return null;
		}
	};
	
	/**
	 * 日编子任务未规划grid接口
	 * grid及tips完成情况统计
	 * 筛选出未规划的grid
	 * 按照tips个数从大到小排序，gridid从大到小排序
	 * @param int taskId
	 * @param int pageNum
	 * @param int pageSize
	 * @return page
	 * @throws Exception
	 * 
	 * */
	@SuppressWarnings("unchecked")
	public Map<String, Object> unPlanGridList(int taskId, int pageNum, int pageSize) throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			//已规划的gird
			Set<Integer> grids = queryDailySubTaskGrids(conn, taskId);
			//获取统计量信息
			List<Map> result = StaticsService.getInstance().getDayTaskTipsStatics(conn,taskId);
			
			int gridNum = result.size();
			int totalCount = gridNum;
			//统计tips总数，包含已规划和未规划
			List<Map<String, Object>> convertList = new ArrayList();
			int tipsNum = 0;
			for(int i = 0; i < result.size(); i++){
				Map<String, Object> map = result.get(i);
				tipsNum += Integer.parseInt(map.get("finished").toString());
				tipsNum += Integer.parseInt(map.get("unfinished").toString());
			}
			List<Map> resultReturn = new ArrayList<Map>();
			//从统计信息中移除已规划的gird
			for(int i = 0; i < result.size(); i++){
				Map<String, Object> map = result.get(i);
				int gridId = Integer.parseInt(map.get("gridId").toString());
				if(!grids.contains(gridId)){
					resultReturn.add(map);
				}
			}
			
			//从移除已规划数据的list中统计未规划的grid和tips数量
			int unPlanGridNum = resultReturn.size();
			int unPlanTipsNum = 0;
			for(Map<String, Object> map : resultReturn){
				convertList.add(map);
				unPlanTipsNum += Integer.parseInt(map.get("unfinished").toString());
//				unPlanTipsNum += Integer.parseInt(map.get("finished").toString());
			}
			//根据key倒序排序
			String key = "gridId";
			List<Map<String, Object>> sortListByGridId = QuikSortListUtils.sortListInMapByMapKey(convertList, key);
			key = "unfinished";
			List<Map<String, Object>> sortList = QuikSortListUtils.sortListInMapByMapKey(sortListByGridId, key);
			
			for(int i = 0; i < sortList.size(); i++){
				Map<String, Object> map = sortList.get(i);
				map.remove("finished");
			}
			
			//单独处理分页
			List sublist = new ArrayList();
			if(pageNum != 0 && pageSize != 0 && sortList.size() > 0){
				sublist = PageModelUtils.ListSplit(sortList, pageNum, pageSize);
			}
	        
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("unPlanTipsNum", unPlanTipsNum);
			resultMap.put("tipsNum", tipsNum);
			resultMap.put("gridNum", gridNum);
			resultMap.put("totalCount", totalCount);
			resultMap.put("unPlanGridNum", unPlanGridNum);
			resultMap.put("result", sublist);
			
			return resultMap;
		}catch(Exception e){
			log.error("日编子任务未规划grid接口异常，原因为："+e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception("日编子任务未规划grid接口异常:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 日编子任务自动规划
	 * 根据taskId获取未规划的gridId和tips统计
	 * 将未规划的grid自动分配到几个子任务中，尽量保证每个子任务tips数量相近
	 * 应用场景：管理平台—子任务—日编规划—自动规划按钮
	 * @param taskId
	 * @param subtaskNum
	 * @return
	 * @throws Exception
	 */
	public void autoPlan(int taskId, int subtaskNum) throws Exception{
		Connection conn = null;
		try{
			log.info("taskId:"+taskId+" autoplan start");

			conn = DBConnector.getInstance().getManConnection();
			//已规划的gird
			Set<Integer> grids = queryDailySubTaskGrids(conn, taskId);
			//获取统计量信息
			List<Map> result = StaticsService.getInstance().getDayTaskTipsStatics(conn,taskId);
			//获取未规划的grid
			List<KPoint> pointList = new ArrayList<>();
			for(Map map:result){
				int gridId = Integer.parseInt(map.get("gridId").toString());
				int tipsNum = Integer.parseInt(map.get("unfinished").toString());
				if(!grids.contains(gridId)) {
					KPoint point = new KPoint(gridId, tipsNum);
					pointList.add(point);
				}
			}
			if(pointList.size()==0){
				log.info("taskId:"+taskId+",no grids");
				return;
			}

			if(pointList.size()<subtaskNum){
				throw new Exception("可分配grid个数少于子任务个数，无法分配");
			}

			KPoint[] points = new KPoint[pointList.size()];
			pointList.toArray(points);
			//执行kmeans分类
			log.info("taskId "+taskId+": kmeans start");
			Kmeans kmeans = new Kmeans(points, subtaskNum, true);
			int[] assignments = kmeans.run();
			log.info("taskId "+taskId+": kmeans end");
			//组装每个子任务的grid列表
			Map<Integer,Map<Integer,Integer>> gridMaps = new HashMap<>();
			for(int i=0;i<points.length;i++){
				int assignment = assignments[i];
				if(!gridMaps.containsKey(assignment)){
					Map<Integer, Integer> gridMap = new HashMap<>();
					gridMaps.put(assignment, gridMap);
				}
				Map<Integer, Integer> gridMap = gridMaps.get(assignment);
				gridMap.put(points[i].getGridId(), 1);
			}
			//创建子任务
			int[] sums = kmeans.getCounts();
			Task task=TaskService.getInstance().queryNoGeoByTaskId(conn, taskId);
			for( Integer index : gridMaps.keySet()){
				Map<Integer, Integer> gridMap = gridMaps.get(index);
				Subtask subtask = new Subtask();
				subtask.setGridIds(gridMap);
				subtask.setType(3);//一体化grid粗编
				subtask.setPlanStartDate(task.getPlanStartDate());
				subtask.setPlanEndDate(task.getPlanEndDate());
				subtask.setTaskId(taskId);
				subtask.setStage(1); //日编
				subtask.setDescp("自动规划创建");
				List<Integer> gridList = subtask.getGridIds();
				log.info("taskId:"+taskId+",sum:"+sums[index]+",grids:"+gridList.toString());
				String wkt = GridUtils.grids2Wkt(JSONArray.fromObject(gridList));
				subtask.setGeometry(wkt);
				createSubtaskWithSubtaskId(conn,subtask);
			}

			log.info("taskId:"+taskId+" autoplan end");

		}catch(Exception e){
			log.error("日编子任务自动规划接口异常，原因为："+e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception("日编子任务自动规划接口异常:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 获取日编子任务对应的grid
	 * taskid对应的日编子任务（type=3的grid子任务）
	 * @parame int taskId
	 * @parame Connection
	 * @return  List<Integer> gridIds
	 * @throws Exception
	 * 
	 * */
	public Set<Integer> queryDailySubTaskGrids(Connection conn, int taskId) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select distinct sgm.grid_id from SUBTASK_GRID_MAPPING sgm, SUBTASK st  where sgm.subtask_id = st.subtask_id "
					+ " and st.type = 3 and st.task_id = "+taskId;
			
			ResultSetHandler<Set<Integer>> rsHandler = new ResultSetHandler<Set<Integer>>() {
				public Set<Integer> handle(ResultSet rs) throws SQLException {
					Set<Integer> gids = new HashSet<>();
					while (rs.next()) {
						gids.add(rs.getInt("grid_id"));
					}
					return gids;
				}
			};
			return run.query(conn, sql, rsHandler);	
		}catch(Exception e){
			log.error("获取日编子任务对应的grid异常："+e.getMessage(),e);
			throw e;
		}
	}

	public void qualityCommit(int subtaskId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			String sql="update subtask s set s.quality_plan_status=1 where subtask_id="+subtaskId;
			QueryRunner run=new QueryRunner();
			run.update(conn, sql);
		}catch(Exception e){
			log.error("提交质检圈异常，原因为："+e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception("提交质检圈异常:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 子任务对应任务基地名，子任务省、市、对应的常规子任务作业员、子任务质检方式，当前版本
	 * key：groupName,province,city,userId,version
	 * 应用场景：（采集端）道路外业质检上传获取子任务相关信息
	 * @param qualitySubtaskId 质检子任务号
	 * @returngetSubtaskInfoByQuality
	 * @throws Exception
	 */
	public Map<String, Object> getSubtaskInfoByQuality(int qualitySubtaskId) throws Exception{
		Connection conn = null;
		try{
            conn = DBConnector.getInstance().getManConnection();
            String sql="SELECT G.GROUP_NAME,"
                    + "       C.PROVINCE_NAME,"
                    + "       C.CITY_NAME,"
                    + "       S.EXE_USER_ID,"
                    + "       QS.QUALITY_METHOD,S.PLAN_START_DATE,S.SUBTASK_ID"
                    + "  FROM SUBTASK QS, TASK T, PROGRAM P, CITY C, SUBTASK S, USER_GROUP G"
                    + " WHERE QS.SUBTASK_ID = "+qualitySubtaskId
                    + "   AND QS.SUBTASK_ID = S.QUALITY_SUBTASK_ID"
                    + "   AND QS.TASK_ID = T.TASK_ID"
                    + "   AND T.PROGRAM_ID = P.PROGRAM_ID"
                    + "   AND P.CITY_ID = C.CITY_ID"
                    + "   AND T.GROUP_ID = G.GROUP_ID";
            QueryRunner run=new QueryRunner();
            return run.query(conn, sql, new ResultSetHandler<Map<String, Object>>(){

                @Override
                public Map<String, Object> handle(ResultSet rs) throws SQLException {
                    if(rs.next()){
                        Map<String, Object> returnObj=new HashMap<String, Object>();
                        returnObj.put("groupName", rs.getString("GROUP_NAME"));
                        returnObj.put("province", rs.getString("PROVINCE_NAME"));
                        returnObj.put("city", rs.getString("CITY_NAME"));
                        returnObj.put("exeUserId", rs.getString("EXE_USER_ID"));
                        returnObj.put("qualityMethod", rs.getString("QUALITY_METHOD"));
                        returnObj.put("planStartDate", DateUtils.formatDate(rs.getTimestamp("PLAN_START_DATE")));
                        returnObj.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
                        returnObj.put("collectSubTaskId", rs.getInt("SUBTASK_ID"));
                        return returnObj;
                    }
                    return null;
                }

            });
		}catch(Exception e){
			log.error("提交质检圈异常，原因为："+e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception("提交质检圈异常:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

    /**
     * 计算子任务所属大区库的所有图幅信息
     * 用于限制接边作业时的跨大区作业操作
     * @param subtaskId 子任务ID
     * @return 出现错误时返回空列表
     */
    private static List<Integer> listDbMeshesBySubtask(Connection conn, int subtaskId) {
        //private static Map<String,List<Integer>> listDbMeshesBySubtask() {
        final List<Integer> result;

        final StringBuffer sb = new StringBuffer();
        sb.append("SELECT S.SUBTASK_ID, CM.MESH AS MESH_ID ");
        sb.append("FROM SUBTASK S, TASK T, CP_REGION_PROVINCE C, CP_MESHLIST@METADB_LINK CM ");
        sb.append("WHERE S.SUBTASK_ID = :1 ");
        sb.append("AND S.TASK_ID = T.TASK_ID ");
        //sb.append("WHERE S.TASK_ID = T.TASK_ID ");
        sb.append("AND T.REGION_ID = C.REGION_ID ");
        sb.append("AND C.ADMINCODE = CM.ADMINCODE ");
        sb.append("ORDER BY S.SUBTASK_ID, CM.MESH");


        QueryRunner run = new QueryRunner();
        try {
            result = run.query(conn, sb.toString(), new ResultSetHandler<List<Integer>>() {

                private List<Integer> result = new ArrayList<>();

                @Override
                public List<Integer> handle(ResultSet rs) throws SQLException {
                    rs.setFetchSize(3000);

                    while (rs.next()) {
                        int subtaskId = rs.getInt("SUBTASK_ID");
                        result.add(rs.getInt("MESH_ID"));
                    }

                    return result;
                }
            }, subtaskId);
            log.info(String.format("查询子任务所属大区库图幅，子任务ID: %s, 图幅数量: %s", subtaskId, result.size()));
            return result;
        } catch (SQLException e) {
            log.error(String.format("根据子任务查询所属大区库图幅出错[sql: %s]", sb.toString()), e);
        }

        return new ArrayList<>();
    }
}