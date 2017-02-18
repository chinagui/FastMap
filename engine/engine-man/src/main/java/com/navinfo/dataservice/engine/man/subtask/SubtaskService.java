package com.navinfo.dataservice.engine.man.subtask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.SubtaskStatInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.mq.email.EmailPublisher;
import com.navinfo.dataservice.engine.man.message.MessageService;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.engine.man.userGroup.UserGroupService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GridUtils;

/**
 * @ClassName: SubtaskService
 * @author code generator
 * @date 2016-06-06 07:40:14
 * @Description: TODO
 */

public class SubtaskService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

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
		try{
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
			}
			
			//质检子任务信息
			int qualityExeUserId = 0;
			String qualityPlanStartDate = "";
			String qualityPlanEndDate = "";
			if(dataJson.containsKey("qualityExeUserId")){
				qualityExeUserId = dataJson.getInt("qualityExeUserId");
				qualityPlanStartDate = dataJson.getString("qualityPlanStartDate");
				qualityPlanEndDate = dataJson.getString("qualityPlanEndDate");
				//删除传入参数的对应键值对,因为bean中没有这些字段
				dataJson.discard("qualityExeUserId");
				dataJson.discard("qualityPlanStartDate");
				dataJson.discard("qualityPlanEndDate");}
			
			//自采自录子任务
			int isSelfRecord = 0;//是否进行自采自录，0否1是
			int selfRecordType = 0;//自采自录日编子任务作业类型
			String selfRecordName = "";//自采自录日编子任务名称
			if(dataJson.containsKey("isSelfRecord") && 1==dataJson.getInt("isSelfRecord")){
				isSelfRecord = dataJson.getInt("isSelfRecord");
				selfRecordType = dataJson.getInt("selfRecordType");
				selfRecordName = dataJson.getString("selfRecordName");
				//删除传入参数的对应键值对,因为bean中没有这些字段
				dataJson.discard("isSelfRecord");
				dataJson.discard("selfRecordType");
				dataJson.discard("selfRecordName");}
			
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			
			int qualitySubtaskId = 0;
			if(qualityExeUserId != 0 ){//表示要创建质检子任务
				//根据参数生成质检子任务 subtask qualityBean
				Subtask qualityBean = createSubtaskBean(userId,dataJson);
				qualityBean.setName(qualityBean.getName()+"_质检");
				qualityBean.setIsQuality(1);
				qualityBean.setStatus(2);
				qualityBean.setExeUserId(qualityExeUserId);
				qualityBean.setPlanStartDate(new Timestamp(df.parse(qualityPlanStartDate).getTime()));
				qualityBean.setPlanEndDate(new Timestamp(df.parse(qualityPlanEndDate).getTime()));
				//创建质检子任务 subtask	
				qualitySubtaskId = createSubtask(qualityBean);	
			}
			if(isSelfRecord != 0 ){//表示要创建自采自录日编子任务
				//根据参数生成日编子任务 subtask dailyBean
				Subtask dailyBean = createSubtaskBean(userId,dataJson);
				dailyBean.setName(selfRecordName);
				dailyBean.setIsQuality(0);
				dailyBean.setStatus(2);
				dailyBean.setType(selfRecordType);
				//创建质检子任务 subtask	
				createSubtask(dailyBean);	
			}
			
			//根据参数生成subtask bean
			Subtask bean = createSubtaskBean(userId,dataJson);
			bean.setIsQuality(0);
			if(qualitySubtaskId!=0){
				bean.setQualitySubtaskId(qualitySubtaskId);
			}
			//创建subtask	
			createSubtask(bean);
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		}
	}
	
	/**
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
			// 获取subtaskId
			int subtaskId = SubtaskOperation.getSubtaskId(conn, bean);

			bean.setSubtaskId(subtaskId);
			//默认subtask状态为草稿2
			if(bean.getStatus()== null){
				bean.setStatus(2);
			}
			
			// 插入subtask
			SubtaskOperation.insertSubtask(conn, bean);
			
			// 插入SUBTASK_GRID_MAPPING
			if(bean.getGridIds() != null){
				SubtaskOperation.insertSubtaskGridMapping(conn, bean);
			}
			
			//消息发布
			/*if(bean.getStatus()==1){
				SubtaskOperation.pushMessage(conn,bean);
			}*/	
			log.debug("子任务创建成功!");
			return subtaskId;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/*
	 * 根据几何范围,任务类型，作业阶段查询任务列表 参数1：几何范围，String wkt
	 */
	/*public List<Subtask> listByWkt(String wkt) throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			String querySql = "select "
					+ "s.subtask_id"
					+ ",s.name"
					+ ",s.type"
					+ ",s.stage"
					+ ",s.status"
					+ ", s.geometry"
					+ ", s.refer_geometry"
					+ ",s.descp"
					+ " from subtask s "
					+ "where SDO_GEOM.RELATE(geometry, 'ANYINTERACT', "
					+ "sdo_geometry("
					+ "'"
					+ wkt
					+ "',8307)"
					+ ", 0.000005) ='TRUE'";

			ResultSetHandler<List<Map<String, Object>>> rsHandler = new ResultSetHandler<List<Map<String, Object>>>() {
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					while (rs.next()) {
						Map<String, Object> subtask = new HashMap<String, Object>();
						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
						
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.setGeometry(GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						STRUCT referStruct = (STRUCT) rs.getObject("REFER_GEOMETRY");
						try {
							subtask.setReferGeometry(GeoTranslator.struct2Wkt(referStruct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						subtask.setDescp(rs.getString("DESCP"));
						subtask.setName(rs.getString("name"));
						subtask.setType(rs.getInt("type"));
						subtask.setStage(rs.getInt("stage"));
						subtask.setStatus(rs.getInt("status"));
						
						try {
							List<Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
							subtask.setGridIds(gridIds);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						list.add(subtask);
					}
					return list;
				}

			};

			return run.query(conn, querySql, rsHandler);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/
	
	/*
	 * 修改子任务详细信息。 
	 */
	public void update(JSONObject dataJson, long userId) throws ServiceException, ParseException, Exception {
		List<Subtask> subtaskList = new ArrayList<Subtask>();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

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
		}
		
		int qualitySubtaskId = 0;//质检子任务id
		int qualityExeUserId = 0;//是否新建质检子任务标识
		String qualityPlanStartDate = "";
		String qualityPlanEndDate ="";
			
		if(dataJson.containsKey("qualitySubtaskId")){
			qualitySubtaskId = dataJson.getInt("qualitySubtaskId");
			//删除 质检子任务id ,因为质检子任务Subtask实体类里不应该有这个字段
			dataJson.discard("qualitySubtaskId");
		}
		if(dataJson.containsKey("qualityExeUserId")){
			qualityExeUserId = dataJson.getInt("qualityExeUserId");
			qualityPlanStartDate = dataJson.getString("qualityPlanStartDate");
			qualityPlanEndDate = dataJson.getString("qualityPlanEndDate");
			dataJson.discard("qualityExeUserId");//删除 是否新建质检子任务标识 ,因为Subtask实体类里灭幼这个字段
			dataJson.discard("qualityPlanStartDate");//删除 质检子任务计划开始时间 ,因为Subtask实体类里灭幼这个字段
			dataJson.discard("qualityPlanEndDate");//删除 质检子任务计划结束时间 ,因为Subtask实体类里灭幼这个字段
		}				
			
		//正常修改子任务
		Subtask subtask = createSubtaskBean(userId,dataJson);
		//创建或者修改常规任务时，均要调用修改质检任务的代码
		if(qualitySubtaskId != 0){//非0的时候，表示要修改质检子任务
			Subtask qualitySubtask = createSubtaskBean(userId,dataJson);//生成质检子任务的bean
			qualitySubtask.setSubtaskId(qualitySubtaskId);
			qualitySubtask.setExeUserId(qualityExeUserId);
			qualitySubtask.setName(qualitySubtask.getName()+"_质检");
			qualitySubtask.setPlanStartDate(new Timestamp(df.parse(qualityPlanStartDate).getTime()));
			qualitySubtask.setPlanEndDate(new Timestamp(df.parse(qualityPlanEndDate).getTime()));
			subtaskList.add(qualitySubtask);//将质检子任务也加入修改列表
		}else{
			if(qualityExeUserId != 0){//qualitySubtaskId=0，且qualityExeUserId非0的时候，表示要创建质检子任务
				Subtask qualitySubtask = SubtaskService.getInstance().queryBySubtaskIdS(subtask.getSubtaskId());
				qualitySubtask.setName(qualitySubtask.getName()+"_质检");
				qualitySubtask.setSubtaskId(null);
				qualitySubtask.setPlanStartDate(new Timestamp(df.parse(qualityPlanStartDate).getTime()));
				qualitySubtask.setPlanEndDate(new Timestamp(df.parse(qualityPlanEndDate).getTime()));
				qualitySubtask.setIsQuality(1);//表示此bean是质检子任务
				qualitySubtask.setExeUserId(qualityExeUserId);
//				if(dataJson.containsKey("gridIds")){
//					qualitySubtask.setGridIds(subtask.getGridIds());
//					//根据gridIds获取wkt
//					String wkt = GridUtils.grids2Wkt(dataJson.getJSONArray("gridIds"));
//					qualitySubtask.setGeometry(wkt);
//				}
					
				//创建质检子任务 subtask	
				Integer newQualitySubtaskId = createSubtask(qualitySubtask);	
				subtask.setIsQuality(0);
				subtask.setQualitySubtaskId(newQualitySubtaskId);
			}
		}
		subtaskList.add(subtask);

		SubtaskService.getInstance().updateSubtask(subtaskList,userId);
//		String message = "批量修改子任务：" + updatedSubtaskIdList.size() + "个成功，" + (subtaskList.size() - updatedSubtaskIdList.size()) + "个失败。";
//		return message;
	}

	/*
	 * 批量修改子任务详细信息。 参数：Subtask对象列表
	 */
	public List<Integer> updateSubtask(List<Subtask> subtaskList, long userId) throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			conn = DBConnector.getInstance().getManConnection();

			List<Integer> updatedSubtaskIdList = new ArrayList<Integer>();
			for (int i = 0; i < subtaskList.size(); i++) {
				SubtaskOperation.updateSubtask(conn, subtaskList.get(i));
				updatedSubtaskIdList.add(subtaskList.get(i).getSubtaskId());
				
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
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/*
	 * 根据subtaskId查询一个任务的详细信息。 参数为Subtask对象
	 */
	
	public Subtask queryBySubtaskIdS(Integer subtaskId) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT ST.SUBTASK_ID,ST.NAME,ST.STATUS,ST.DESCP,ST.PLAN_START_DATE,ST.PLAN_END_DATE,ST.TYPE,ST.GEOMETRY,ST.REFER_ID");
			sb.append(",T.TASK_ID,T.TYPE TASK_TYPE,R.DAILY_DB_ID,R.MONTHLY_DB_ID");
			sb.append(" FROM SUBTASK ST,TASK T,REGION R");
			sb.append(" WHERE ST.TASK_ID = T.TASK_ID");
			sb.append(" AND T.REGION_ID = R.REGION_ID");
			sb.append(" AND ST.SUBTASK_ID = " + subtaskId);
	
			String selectSql = sb.toString();
			

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
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						subtask.setTaskId(rs.getInt("TASK_ID"));
						if (2 == rs.getInt("TASK_TYPE")) {
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
			return run.query(conn, selectSql,rsHandler);			
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
	
	public Map<String,Object> queryBySubtaskId(Integer subtaskId) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT ST.SUBTASK_ID,ST.NAME,ST.STATUS,ST.STAGE,ST.DESCP,ST.PLAN_START_DATE,ST.PLAN_END_DATE,ST.TYPE,ST.GEOMETRY,ST.REFER_ID");
			sb.append(",ST.EXE_USER_ID,ST.EXE_GROUP_ID,ST.QUALITY_SUBTASK_ID,ST.IS_QUALITY");
			sb.append(",T.TASK_ID,T.TYPE TASK_TYPE,R.DAILY_DB_ID,R.MONTHLY_DB_ID");
			sb.append(" FROM SUBTASK ST,TASK T,REGION R");
			sb.append(" WHERE ST.TASK_ID = T.TASK_ID");
			sb.append(" AND T.REGION_ID = R.REGION_ID");
			sb.append(" AND ST.SUBTASK_ID = " + subtaskId);
	
			String selectSql = sb.toString();

			ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						Map<String,Object> subtask = new HashMap<String,Object>();						
						subtask.put("subtaskId",rs.getInt("SUBTASK_ID"));
						subtask.put("name",rs.getString("NAME"));
						subtask.put("type",rs.getInt("TYPE"));
						subtask.put("planStartDate",rs.getTimestamp("PLAN_START_DATE"));
						subtask.put("planEndDate",rs.getTimestamp("PLAN_END_DATE"));
						subtask.put("descp",rs.getString("DESCP"));
						subtask.put("status",rs.getInt("STATUS"));
						subtask.put("stage",rs.getInt("STAGE"));
						subtask.put("referId",rs.getInt("REFER_ID"));
						
						//作业员/作业组信息
						int exeUserId = rs.getInt("EXE_USER_ID");
						int exeGroupId = rs.getInt("EXE_GROUP_ID");
						if(exeUserId!=0){
							//获取作业员名称
							try {
								UserInfo userInfo = UserInfoService.getInstance().getUserInfoByUserId(exeUserId);
								subtask.put("exeUserId",exeUserId);
								subtask.put("executerId",exeUserId);
								subtask.put("executer",userInfo.getUserRealName());
							} catch (ServiceException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}else{
							//获取作业组名称
							try {
								String userGroupName = UserGroupService.getInstance().getGroupNameByGroupId(exeGroupId);
								subtask.put("exeGroupId",exeUserId);
								subtask.put("executerId",exeUserId);
								subtask.put("executer",userGroupName);
							} catch (ServiceException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						subtask.put("qualitySubtaskId",rs.getInt("QUALITY_SUBTASK_ID"));
						//获取质检任务信息
						if(!subtask.get("qualitySubtaskId").toString().equals("0")){
							try {
								Subtask subtaskQuality = queryBySubtaskIdS((Integer)subtask.get("qualitySubtaskId"));
								subtask.put("qualityExeUserId",subtaskQuality.getExecuterId());
								subtask.put("qualityPlanStartDate",subtaskQuality.getPlanStartDate());
								subtask.put("qualityPlanEndDate",subtaskQuality.getPlanEndDate());
								subtask.put("qualityTaskStatus",subtaskQuality.getStatus());
								UserInfo userInfo = UserInfoService.getInstance().getUserInfoByUserId(exeUserId);
								subtask.put("qualityExeUserName",userInfo.getUserRealName());
							} catch (ServiceException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						//GEOMETRY
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.put("geometry",GeoTranslator.struct2Wkt(struct));
							String clobStr = GeoTranslator.struct2Wkt(struct);
							subtask.put("geometryJSON",Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						try {
							Map<Integer,Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
							subtask.put("gridIds",gridIds.keySet());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						subtask.put("taskId",rs.getInt("TASK_ID"));
						if (2 == rs.getInt("STAGE")) {
							//月编子任务
							subtask.put("dbId",rs.getInt("MONTHLY_DB_ID"));
						} else {
							subtask.put("dbId",rs.getInt("DAILY_DB_ID"));
						}	
						
						if(1 == rs.getInt("STATUS")){
							subtask.put("percent",100);
//							SubtaskStatInfo stat = new SubtaskStatInfo();
//							try{	
//								StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
//								stat = staticApi.getStatBySubtask(rs.getInt("SUBTASK_ID"));
//							} catch (Exception e) {
//								log.warn("subtask query error",e);
//							}
//							subtask.setPercent(stat.getPercent());
						}
						subtask.put("version",SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						return subtask;
					}
					return null;
				}	
			};
			return run.query(conn, selectSql,rsHandler);			
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
					if(subtask.getExeUserId()!=null&&subtask.getExeUserId()!=0){
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
	 * @param bean
	 * @param snapshot
	 * @param platForm
	 * @param pageSize
	 * @param curPageNum 
	 * @return
	 * @throws ServiceException 
	 */
	public Page listByUserPage(Subtask bean, int snapshot, int platForm, int pageSize, int curPageNum) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
			//获取用户所在组
			UserInfo userInfo = new UserInfo();
			userInfo.setUserId(bean.getExeUserId());
			Map<Object,Object> userGroup = UserInfoOperation.getUserGroup(conn, userInfo);
			if(!userGroup.isEmpty()){
				bean.setExeGroupId((int)userGroup.get("groupId"));
			}
			
			Page page = new Page();
			//snapshot=1不返回geometry和gridIds
			if(snapshot==1){
				page = SubtaskOperation.getListByUserSnapshotPage(conn, bean,curPageNum,pageSize,platForm);
			}else{
				page = SubtaskOperation.getListByUserPage(conn, bean,curPageNum,pageSize,platForm);		
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
					Map<String,Integer> gridIdMap = new HashMap<String,Integer>();
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
	public String pushMsg(long userId, JSONArray subtaskIds) throws Exception {
		// TODO Auto-generated method stub
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			//查询子任务
			List<Subtask> subtaskList = SubtaskOperation.getSubtaskListBySubtaskIdList(conn, subtaskIds);
			
			Iterator<Subtask> iter = subtaskList.iterator();
			int success=0;
			while(iter.hasNext()){
				Subtask subtask = (Subtask) iter.next();
				//修改子任务状态
				if( (int)subtask.getStatus()== 2){
					SubtaskOperation.updateStatus(conn,subtask.getSubtaskId());
				}
				
				//发送消息
				SubtaskOperation.pushMessage(conn, subtask, userId);
				success ++;
			}
			return "子任务批量发布"+success+"个成功，"+(subtaskList.size()-success)+"个失败";
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
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
	
	
	public Page listByGroup(long groupId, int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
			final int curPageNum,int snapshot) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
//			//获取用户所在组信息
//			UserInfo userInfo = new UserInfo();
//			userInfo.setUserId((int)userId);
//			Map<Object, Object> group = UserInfoOperation.getUserGroup(conn, userInfo);
//			int groupId = (int) group.get("groupId");
			
			Page page = SubtaskOperation.getListByGroup(conn,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
			return page;
			
//			//返回简略信息
//			if (snapshot==1){
//				Page page = SubtaskOperation.getListSnapshot(conn,userId,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
//				return page;
//			}else{
//				Page page = SubtaskOperation.getList(conn,userId,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
//				return page;
//			}		

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 删除子任务，前端只有草稿状态的子任务有删除按钮
	 * @param subtaskId
	 * @throws ServiceException
	 */
	public void delete(Integer subtaskId) throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			String updateSql = "delete from SUBTASK S where S.SUBTASK_ID =" + subtaskId;	
			run.update(conn,updateSql);
			updateSql = "delete from SUBTASK_grid_mapping S where S.SUBTASK_ID in =" + subtaskId;
			run.update(conn,updateSql);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("删除失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public List<HashMap<String,Object>> queryListReferByWkt(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = " select t.id,t.geometry FROM subtask_refer T "
					+ "WHERE SDO_ANYINTERACT(t.geometry,sdo_geometry(?,8307))='TRUE'";
			ResultSetHandler<List<HashMap<String,Object>>> rsHandler = new ResultSetHandler<List<HashMap<String,Object>>>(){
				public List<HashMap<String,Object>> handle(ResultSet rs) throws SQLException {
					List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
					while(rs.next()){
						try {
							HashMap<String,Object> map = new HashMap<String,Object>();
							map.put("id", rs.getInt("ID"));							
							try {
								STRUCT struct=(STRUCT)rs.getObject("geometry");
								String clobStr = GeoTranslator.struct2Wkt(struct);
								map.put("geometry", Geojson.wkt2Geojson(clobStr));
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							list.add(map);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}
					return list;
				}	    		
	    	}		;

	    	return run.query(conn, selectSql, rsHandler,json.getString("wkt"));
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
			UserInfo userInfo = new UserInfo();
			userInfo.setUserId((int)userId);
			Map<Object,Object> userGroup = UserInfoOperation.getUserGroup(conn, userInfo);
			int userGroupId=0;
			if(!userGroup.isEmpty()){
				userGroupId = (int)userGroup.get("groupId");
			}
			String sql="SELECT T.STAGE, T.TYPE, COUNT(1) TYPE_COUNT"
					+ "  FROM SUBTASK T"
					+ " WHERE (T.EXE_USER_ID = "+userId+" OR T.EXE_GROUP_ID = "+userGroupId+")"
					+ "   AND T.STATUS = 1"
					+ "   AND T.STAGE != 0"
					+ " GROUP BY T.STAGE, T.TYPE"
					+ " ORDER BY T.STAGE, T.TYPE";
			QueryRunner run=new QueryRunner();
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
						String name="";
						if(stage==1){name+="日编 - ";}
						else{name+="月编 - ";}
						//0POI，1道路，2一体化，3一体化_grid粗编，4一体化_区域粗编，5多源POI，6
						//代理店， 7POI专项,8道路_grid精编，9道路_grid粗编，10道路区域专项
						if(type==0){name+="POI";}
						else if(type==1){name+="道路";}
						else if(type==2){name+="一体化";}
						else if(type==3){name+="一体化grid粗编";}
						else if(type==4){name+="一体化区域粗编";}
						else if(type==5){name+="多源POI";}
						else if(type==6){name+="代理店";}
						else if(type==7){name+="POI专项";}
						else if(type==8){name+="道路grid精编";}
						else if(type==9){name+="道路grid粗编";}
						else if(type==10){name+="道路区域专项";}
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
			
			//关闭子任务
			SubtaskOperation.closeBySubtaskId(conn, subtaskId);

			//动态调整子任务范围
			//日编子任务关闭，调整日编任务本身，调整日编任务,调整日编区域子任务
			if(subtask.getStage()==1){
				EditApi editApi = (EditApi) ApplicationContextUtil.getBean("editApi");
				List<Integer> gridIds = editApi.getGridIdListBySubtaskIdFromLog(subtask.getDbId(),subtask.getSubtaskId());
				///获得需要调整的gridMap
				Map<Integer,Integer> gridIdsBefore = subtask.getGridIds();
				Map<Integer,Integer> gridIdsToInsert = null ;
				for(Integer gridId:gridIds){
					if(gridIdsBefore.containsKey(gridId)){
						continue;
					}else{
						gridIdsToInsert.put(gridId,2);
					}
				}
				
				//调整子任务范围
//				SubtaskOperation.adjustSubtaskRegion(conn,subtask,gridIds);
				SubtaskOperation.insertSubtaskGridMapping(conn,subtask.getSubtaskId(),gridIdsToInsert);
				//调整任务范围
				TaskOperation.insertTaskGridMapping(conn,subtask.getTaskId(),gridIdsToInsert);
				//调整区域子任务范围
				List<Subtask> subtaskList = TaskOperation.getSubTaskListByType(conn,subtask.getTaskId(),4);
				for(Subtask subtaskType4:subtaskList){
					SubtaskOperation.insertSubtaskGridMapping(conn, subtaskType4.getSubtaskId(), gridIdsToInsert);
				}
			}
			
			
			//发送消息
			try {
				//查询分配的作业组组长
				List<Long> groupIdList = null;
				if(subtask.getExeUserId()!=null&&subtask.getExeUserId()!=0){
					UserGroup userGroup = UserInfoOperation.getUserGroupByUserId(conn, subtask.getExeUserId());
					groupIdList.add(Long.valueOf(userGroup.getGroupId()));
				}else{
					groupIdList.add((long)subtask.getExeGroupId());
				}
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
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				log.error("发送失败,原因:"+e.getMessage(), e);
			}
	
			return null;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("关闭失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
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
			boolean collectAndDay=true;
			while (conditionKeys.hasNext()) {
				String key = (String) conditionKeys.next();
				//查询条件
				if ("taskId".equals(key)) {
					conditionSql+=" AND SUBTASK_LIST.TASK_ID="+condition.getInt(key);
				}
				if ("stage".equals(key)) {
					collectAndDay=false;
					conditionSql+=" AND SUBTASK_LIST.STAGE ="+condition.getInt(key);}
				//子任务名称模糊查询
				if ("subtaskName".equals(key)) {	
					conditionSql+=" AND SUBTASK_LIST.NAME LIKE '%" + condition.getString(key) +"%'";
				}
				//筛选条件
				//"progress":[1,3] //进度。1采集正常，2采集异常，3日编正常，4日编异常， 5月编正常，6月编异常，7已关闭，8已完成, 9草稿, 11逾期完成，12按时完成，13提前完成
				if ("progress".equals(key)){
					JSONArray progress = condition.getJSONArray(key);
					if(progress.isEmpty()){
						continue;
					}
					List<String> progressList = new ArrayList<String>();
					for(Object i:progress){
						int tmp=(int) i;
						if(tmp==1){progressList.add(" SUBTASK_LIST.PROGRESS = 1 AND SUBTASK_LIST.STAGE=0 ");}
						if(tmp==2){progressList.add(" SUBTASK_LIST.PROGRESS = 2 AND SUBTASK_LIST.STAGE=0");}
						
						if(tmp==3){progressList.add(" SUBTASK_LIST.PROGRESS = 1 AND SUBTASK_LIST.STAGE=1");}
						if(tmp==4){progressList.add(" SUBTASK_LIST.PROGRESS = 2 AND SUBTASK_LIST.STAGE=1");}
						
						if(tmp==5){progressList.add(" SUBTASK_LIST.PROGRESS = 1 AND SUBTASK_LIST.STAGE=2");}
						if(tmp==6){progressList.add(" SUBTASK_LIST.PROGRESS = 2 AND SUBTASK_LIST.STAGE=2");}
						
						if(tmp==7){progressList.add(" SUBTASK_LIST.STATUS = 0");}
						if(tmp==8){progressList.add(" SUBTASK_LIST.STATUS = 1 AND SUBTASK_LIST.PERCENT = 100 ");}
						if(tmp==9){progressList.add(" SUBTASK_LIST.STATUS = 2 ");}
						
						if(tmp==11){
							progressList.add("SUBTASK_LIST.DIFF_DATE < 0 AND SUBTASK_LIST.PERCENT = 100 ");
						}
						if(tmp==12){
							progressList.add("SUBTASK_LIST.DIFF_DATE = 0 AND SUBTASK_LIST.PERCENT = 100 ");
						}
						if(tmp==13){
							progressList.add("SUBTASK_LIST.DIFF_DATE > 0 AND SUBTASK_LIST.PERCENT = 100 ");
						}
	
						if(!progressList.isEmpty()){
							String tempSql = StringUtils.join(progressList," OR ");
							conditionSql = " AND (" + tempSql + ")";
						}
					}
				}
				if (collectAndDay){conditionSql+=" AND SUBTASK_LIST.STAGE IN (0,1)";}
			}
			
			
			QueryRunner run = new QueryRunner();
			long pageStartNum = (curPageNum - 1) * pageSize + 1;
			long pageEndNum = curPageNum * pageSize;
			//质检子任务语句
			
			StringBuilder sb = new StringBuilder();

			sb.append("WITH QUALITY_TASK AS ");
			sb.append(" (SELECT SS.SUBTASK_ID      QUALITY_SUBTASK_ID, ");
			sb.append(" SS.EXE_USER_ID     QUALITY_EXE_USER_ID,");
			sb.append(" SS.PLAN_START_DATE AS QUALITY_PLAN_START_DATE,");
			sb.append(" SS.PLAN_END_DATE   AS QUALITY_PLAN_END_DATE,");
			sb.append(" SS.STATUS          QUALITY_TASK_STATUS,");
			sb.append(" UU.USER_REAL_NAME  AS QUALITY_EXE_USER_NAME");
			sb.append(" FROM SUBTASK SS, USER_INFO UU");
			sb.append(" WHERE SS.IS_QUALITY = 1");
			sb.append(" AND SS.EXE_USER_ID = UU.USER_ID),");
			
			sb.append(" SUBTASK_LIST AS");
			sb.append(" (SELECT S.SUBTASK_ID,");
			sb.append(" S.STAGE,");
			sb.append(" S.NAME,");
			sb.append(" S.TYPE,");
			sb.append(" S.STATUS,");
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
			sb.append(" Q.QUALITY_EXE_USER_NAME,");
			/*• 记录默认排序原则：
			 * ①根据状态排序：开启>草稿>100%(已完成)>已关闭
			 * 用order_status来表示这个排序的先后顺序。分别是开启0>草稿1>100%(已完成)2>已关闭3
			 * ②相同状态中根据剩余工期排序，逾期>0天>剩余/提前
			 * ③开启状态相同剩余工期，根据完成度排序，完成度高>完成度低；其它状态，根据名称
			 */
			sb.append(" CASE S.STATUS  WHEN 1 THEN CASE NVL(FSOS.PERCENT, 0) when 100 then 2 WHEN 2 THEN 0 end when 2 then 1 when 0 then 3 end order_status");
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
						subtask.put("type", rs.getInt("TYPE"));
						String executer=rs.getString("EXECUTER");
						if(executer==null||executer.isEmpty()){
							executer=rs.getString("GROUP_EXECUTER");
						}
						subtask.put("executer", executer);
						
						subtask.put("percent", rs.getInt("percent"));
						subtask.put("diffDate", rs.getInt("DIFF_DATE"));
						
						subtask.put("qualitySubtaskId", rs.getInt("quality_subtask_id"));
						subtask.put("qualityExeUserId", rs.getInt("quality_Exe_User_Id"));
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
						totalCount=rs.getInt("TOTAL_RECORD_NUM");
						list.add(subtask);
					}					
					page.setTotalCount(totalCount);
					page.setResult(list);
					return page;
				}

			};
			
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
}
