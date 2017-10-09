package com.navinfo.dataservice.engine.man.program;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.engine.man.job.bean.JobType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Infor;
import com.navinfo.dataservice.api.man.model.Program;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.mq.email.EmailPublisher;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.dataservice.engine.man.city.CityOperation;
import com.navinfo.dataservice.engine.man.infor.InforService;
import com.navinfo.dataservice.engine.man.inforMan.InforManOperation;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.engine.man.timeline.TimelineService;
import com.navinfo.dataservice.engine.man.userGroup.UserGroupService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;

public class ProgramService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private ProgramService() {
	}

	private static class SingletonHolder {
		private static final ProgramService INSTANCE = new ProgramService();
	}

	public static ProgramService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void create(long userId, JSONObject dataJson) throws ServiceException {
		Connection conn = null;
		try {
			
			conn = DBConnector.getInstance().getManConnection();
			Program bean = (Program) JsonOperation.jsonToBean(dataJson,Program.class);
			bean.setCreateUserId(Integer.valueOf(String.valueOf(userId)));
			//创建项目，并维护相关状态
			create(conn, bean);					
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 创建新项目，并维护相关状态
	 * @param conn
	 * @param bean
	 * @return int 项目id
	 */
	public int create(Connection conn,Program bean) throws Exception{
		QueryRunner run = new QueryRunner();
		//将旧项目状态修改为失效
		String updateSql = "UPDATE PROGRAM SET LATEST=0 WHERE ";
		if (bean!=null&&bean.getCityId()!=0){
			updateSql+=" CITY_ID ="+bean.getCityId();
		};
		
		if (bean!=null&&bean.getInforId()!=0){
			updateSql+=" infor_id ='" + bean.getInforId() + "'";
		};
		run.update(conn,updateSql);
		//创建项目		
		int programId=getNewProgramId(conn);
		bean.setProgramId(programId);
		//情报项目为空时，需要后台自动创建名称
		if(!StringUtils.isNotEmpty(bean.getName())&&bean.getType()==4){
			Infor infor = InforService.getInstance().getInforByInforId(conn, bean.getInforId());
			bean.setName(infor.getInforName()+"_"+DateUtils.dateToString(infor.getPublishDate(), "yyyyMMdd")+"_"+programId);
		}
			
		String insertPart="";
		String valuePart="";
		if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
		insertPart+=" PROGRAM_ID ";
		valuePart+=bean.getProgramId();			
		
		if (bean!=null&&bean.getCityId()!=0){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" CITY_ID ";
			valuePart+=bean.getCityId();
		};
		
		if (bean!=null&&bean.getInforId()!=0){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" infor_id ";
			valuePart+= "'" + bean.getInforId() + "'";
		};
		if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
		insertPart+=" CREATE_USER_ID,CREATE_DATE,STATUS,LATEST ";
		valuePart+=bean.getCreateUserId()+",sysdate,2,1";
		
		if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" name ";
			valuePart+="'"+bean.getName()+"'";
		};
		if (bean!=null&&bean.getType()!=0){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" type ";
			valuePart+=bean.getType();
		};
		
		if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" DESCP ";
			valuePart+="'"+bean.getDescp()+"'";
		};
		if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" PLAN_START_DATE ";
			valuePart+="to_timestamp('"+ bean.getPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" PLAN_END_DATE ";
			valuePart+="to_timestamp('"+ bean.getPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getCollectPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanStartDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" Collect_PLAN_START_DATE ";
			valuePart+="to_timestamp('"+ bean.getCollectPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getCollectPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanEndDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" Collect_PLAN_END_DATE";
			valuePart+="to_timestamp('"+ bean.getCollectPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getDayEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanStartDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" day_EDIT_PLAN_START_DATE ";
			valuePart+="to_timestamp('"+ bean.getDayEditPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getDayEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanEndDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" Day_EDIT_PLAN_END_DATE";
			valuePart+="to_timestamp('"+ bean.getDayEditPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getMonthEditPlanStartDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" MONTH_EDIT_PLAN_START_DATE ";
			valuePart+="to_timestamp('"+ bean.getMonthEditPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getMonthEditPlanEndDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" MONTH_EDIT_PLAN_END_DATE";
			valuePart+="to_timestamp('"+ bean.getMonthEditPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getProducePlanStartDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" Produce_Plan_Start_Date ";
			valuePart+="to_timestamp('"+ bean.getProducePlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getProducePlanEndDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" Produce_Plan_End_Date";
			valuePart+="to_timestamp('"+ bean.getProducePlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};

		String createSql = "insert into program ("+insertPart+") values("+valuePart+")";
		run.update(conn,createSql);
	//修改项目对应city的状态为已规划
		if (bean!=null&&bean.getCityId()!=0){
			CityOperation.updatePlanStatus(conn, bean.getCityId(), 1);
		};
	//修改项目对应情报为已规划
		if (bean!=null&&bean.getInforId()!=0){
			InforManOperation.updatePlanStatus(conn,bean.getInforId(),1);
		};	
		return programId;
		}
	
	public void update(long userId,JSONObject dataJson) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			Program bean = (Program) JsonOperation.jsonToBean(dataJson,Program.class);
			
			String setPart="";
			
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" name= '"+bean.getName()+"'";
			};
			
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" DESCP ='"+bean.getDescp()+"'";
			};
			if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" PLAN_START_DATE =to_timestamp('"+ bean.getPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" PLAN_END_DATE =to_timestamp('"+ bean.getPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getCollectPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" Collect_PLAN_START_DATE =to_timestamp('"+ bean.getCollectPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getCollectPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" Collect_PLAN_END_DATE=to_timestamp('"+ bean.getCollectPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getDayEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" day_EDIT_PLAN_START_DATE =to_timestamp('"+ bean.getDayEditPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getDayEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" Day_EDIT_PLAN_END_DATE=to_timestamp('"+ bean.getDayEditPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getMonthEditPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" MONTH_EDIT_PLAN_START_DATE =to_timestamp('"+ bean.getMonthEditPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getMonthEditPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" MONTH_EDIT_PLAN_END_DATE=to_timestamp('"+ bean.getMonthEditPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getProducePlanStartDate().toString())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" Produce_Plan_Start_Date =to_timestamp('"+ bean.getProducePlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getProducePlanEndDate().toString())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" Produce_Plan_End_Date=to_timestamp('"+ bean.getProducePlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};

			String updateSql = "update program set "+setPart+" where program_id="+bean.getProgramId();
			run.update(conn,updateSql);
			
			try {
				//发送消息
				JSONObject condition=new JSONObject();
				JSONArray status=new JSONArray();
				status.add(1);//任务是开启状态
				condition.put("status", status);
				JSONArray programIds=new JSONArray();
				programIds.add(bean.getProgramId());
				condition.put("programIds",programIds);
				List<Program> programList = queryProgramTable(conn, condition);
				/*编辑
				 *1.所有生管角色
				 *2.项目包含的所有任务作业组组长
				 *项目变更:XXX(任务名称)信息发生变更，请关注*/	
				List<Map<String,Object>> msgContentList=new ArrayList<Map<String,Object>>();
				String msgTitle="项目编辑";	
				List<Long> groupIdList = new ArrayList<Long>();
				if(programList!=null&&programList.size()>0){					
					Map<String,Object> map = new HashMap<String, Object>();
					String msgContent = "项目变更:"+programList.get(0).getName()+"信息发生变更,请关注";
					map.put("msgContent", msgContent);
					//关联要素
					JSONObject msgParam = new JSONObject();
					msgParam.put("relateObject", "PROGRAM");
					msgParam.put("relateObjectId", programList.get(0).getProgramId());
					map.put("msgParam", msgParam.toString());
					List<Long> groupIds = new ArrayList<Long>();
					//查询block分配的采集和日编作业组组长id
					if(programList.get(0).getProgramId() != 0){
						List<Long> programGroup = getTaskGroupByProgramId(conn, (long) programList.get(0).getProgramId(), 1);
						if(programGroup != null&&programGroup.size()>0){
							groupIdList.addAll(programGroup);
							groupIds.addAll(programGroup);
						}
					}
					map.put("groupIds", groupIds);
					msgContentList.add(map);
				}
				if(msgContentList.size()>0){
					programPushMsg(conn,msgTitle,msgContentList, groupIdList, userId);
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("项目编辑消息发送失败,原因:"+e.getMessage(), e);
			}

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	private List<Long> getTaskGroupByProgramId(Connection conn,
			long programId, int i) throws Exception {
		QueryRunner run = new QueryRunner();
		String querySql="SELECT DISTINCT GROUP_ID"
				+ "  FROM TASK T"
				+ " WHERE T.STATUS = ?"
				+ "   AND T.PROGRAM_ID = ?"
				+ "   AND T.LATEST = 1"
				+ "   AND T.GROUP_ID != 0";
		Object[] params = {i,programId};		
		ResultSetHandler<List<Long>> rsh = new ResultSetHandler<List<Long>>() {
			@Override
			public List<Long> handle(ResultSet rs) throws SQLException {
				// TODO Auto-generated method stub
				List<Long> map = new ArrayList<Long>();
				while(rs.next()){
					map.add(rs.getLong("GROUP_ID"));
				}
				return map;
			}
		};
		List<Long> userInfo = run.query(conn, querySql, params, rsh);
		return userInfo;
	}
	/**
	 * 关闭原则：判断该项目下面所有的任务，子项目均关闭，则可以关闭项目
	 *  city规划状态变更为已关闭
	 *  注：任务关闭原则：任务下的所有子任务关闭，所以此处仅判断任务关闭即可
	 *  项目无法关闭，返回原因：未关闭任务列表
	 * @param programId
	 * @return String null,关闭成功。not null,未关闭任务列表
	 * @throws ServiceException
	 */
	public String close(Long userId,int programId) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			//快线项目关闭仅判断日编/采集任务，不判断月编任务
			String selectSql="SELECT t.task_id"
					+ "  FROM TASK T, PROGRAM P"
					+ " WHERE T.STATUS != 0"
					+ "   AND P.PROGRAM_ID = T.PROGRAM_ID"
					+ "   AND (P.TYPE = 1 OR (P.TYPE = 4 AND T.TYPE IN (0, 1)))"
					+ " and p.PROGRAM_ID = "+programId;
			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> taskList = new ArrayList<Integer>();
					while (rs.next()) {
						taskList.add(rs.getInt("TASK_ID"));
					}
					return taskList;
				}
			};
			List<Integer> taskList = run.query(conn, selectSql, rsHandler);
			if(taskList!=null&&taskList.size()>0){
				return "项目关闭失败：任务"+taskList.toString().replace("[", "").replace("]", "")+"未关闭";
			}
			String updateSql = "update program set status=0 where program_id="+programId;
			run.update(conn,updateSql);
			updateSql = "UPDATE CITY"
					+ "   SET PLAN_STATUS = 2"
					+ " WHERE CITY_ID IN (SELECT CITY_ID FROM PROGRAM WHERE PROGRAM_ID = "+programId+")";
			run.update(conn,updateSql);
			updateSql = "UPDATE INFOR"
					+ "   SET PLAN_STATUS = 2"
					+ " WHERE INFOR_ID IN (SELECT INFOR_ID FROM PROGRAM WHERE PROGRAM_ID = "+programId+")";
			run.update(conn,updateSql);
			
			//记录关闭时间
			TimelineService.recordTimeline(programId, "program", 0, conn);
			
			try {
				//发送消息
				JSONObject condition=new JSONObject();
				JSONArray status=new JSONArray();
				status.add(0);//关闭状态
				condition.put("status", status);
				JSONArray programIds=new JSONArray();
				programIds.add(programId);
				condition.put("programIds",programIds);
				List<Program> programList = queryProgramTable(conn, condition);
				/*编辑
				 *1.所有生管角色
				 *2.项目包含的所有任务作业组组长
				 *项目变更:XXX(任务名称)信息发生变更，请关注*/	
				List<Map<String,Object>> msgContentList=new ArrayList<Map<String,Object>>();
				String msgTitle="项目关闭";	
				List<Long> groupIdList = new ArrayList<Long>();
				if(programList!=null&&programList.size()>0){					
					Map<String,Object> map = new HashMap<String, Object>();
					String msgContent = "项目关闭:"+programList.get(0).getName()+"已关闭,请关注";
					map.put("msgContent", msgContent);
					//关联要素
					JSONObject msgParam = new JSONObject();
					msgParam.put("relateObject", "PROGRAM");
					msgParam.put("relateObjectId", programId);
					map.put("msgParam", msgParam.toString());
					List<Long> groupIds = new ArrayList<Long>();
					//查询task分配的采集和日编/月编作业组组长id
					if(programId != 0){
						List<Long> programGroup = getTaskGroupByProgramId(conn, (long) programId, 1);
						if(programGroup != null&&programGroup.size()>0){
							groupIdList.addAll(programGroup);
							groupIds.addAll(programGroup);
						}
					}
					map.put("groupIds", groupIds);
					msgContentList.add(map);
				}
				if(msgContentList.size()>0){
					programPushMsg(conn,msgTitle,msgContentList, groupIdList, userId);
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("项目编辑消息发送失败,原因:"+e.getMessage(), e);
			}
			
			return null;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 常规未发布
	 * @param conn
	 * @param conditionJson
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	public Page commonUnPushList(Connection conn,JSONObject conditionJson,final int currentPageNum,final int pageSize) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			//未规划，草稿，根据城市名称\项目名称模糊查询
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (PROGRAM_LIST.CITY_NAME LIKE '%"+conditionJson.getString(key)+"%' OR PROGRAM_LIST.NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("planStatus".equals(key)) {
						conditionSql=conditionSql+" AND PROGRAM_LIST.PLAN_STATUS IN ("+conditionJson.getJSONArray(key).join(",")+")";
					}
				}
			}
			String selectSql="WITH PROGRAM_LIST AS"
					+ " (SELECT 0 PROGRAM_ID,"
					+ "         C.CITY_ID,"
					+ "         '' NAME,"
					+ "         1 TYPE,"
					+ "         0 STATUS,"
					+ "         C.CITY_NAME,"
					+ "         C.PLAN_STATUS"
					+ "    FROM CITY C"
					+ "   WHERE C.PLAN_STATUS = 0"
					+ "  UNION ALL"
					+ "  SELECT P.PROGRAM_ID,"
					+ "         C.CITY_ID,"
					+ "         P.NAME,"
					+ "         P.TYPE,"
					+ "         P.STATUS,"
					+ "         C.CITY_NAME,"
					+ "         C.PLAN_STATUS"
					+ "    FROM PROGRAM P, CITY C"
					+ "   WHERE P.CITY_ID = C.CITY_ID"
					+ "     AND P.LATEST = 1"
					+ "     AND P.STATUS = 2),"
					+ "FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM PROGRAM_LIST"
					+ "   WHERE 1 = 1"
					+ conditionSql
					+ "   ORDER BY PROGRAM_LIST.PLAN_STATUS DESC,PROGRAM_LIST.CITY_NAME DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			
			Page programList = run.query(conn, selectSql, getUnPushQuery(currentPageNum,pageSize));
			return programList;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 常规已发布
	 * @param conn
	 * @param conditionJson
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	public Page commonPushList(Connection conn,JSONObject conditionJson,final int currentPageNum,final int pageSize) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			//根据城市名称\项目名称模糊查询
			//1-9采集正常,采集异常,采集完成，日编正常,日编异常,日编完成，月编正常,月编异常,月编完成
			//progress:1正常(实际作业小于预期)，2异常(实际作业等于或大于预期),3完成（percent=100）
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (PROGRAM_LIST.CITY_NAME LIKE '%"+conditionJson.getString(key)+"%' OR PROGRAM_LIST.PROGRAM_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("selectParam".equals(key)) {
						JSONArray selectParam1=conditionJson.getJSONArray(key);
						JSONArray collectProgress=new JSONArray();
						JSONArray dailyProgress=new JSONArray();
						JSONArray monthlyProgress=new JSONArray();
						for(Object i:selectParam1){
							int tmp=(int) i;
							if(tmp==1||tmp==2){collectProgress.add(tmp);}
							if(tmp==3){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" COLLECT_STAT=3";
							}
							if(tmp==4||tmp==5){dailyProgress.add(tmp-3);}
							if(tmp==6){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" daily_STAT=3";
							}
							if(tmp==7||tmp==8){monthlyProgress.add(tmp-6);}
							if(tmp==9){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" monthly_STAT=3";
							}
						}
						if(!collectProgress.isEmpty()){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" (COLLECT_STAT!=3 AND PROGRAM_LIST.collect_Progress IN ("+collectProgress.join(",")+"))";}
						if(!dailyProgress.isEmpty()){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" (daily_STAT!=3 AND PROGRAM_LIST.daily_Progress IN ("+dailyProgress.join(",")+"))";}
						if(!monthlyProgress.isEmpty()){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" (monthly_STAT!=3 AND PROGRAM_LIST.monthly_Progress IN ("+monthlyProgress.join(",")+"))";}
					}
				}
			}
			if(!statusSql.isEmpty()){//有非status
				conditionSql+=" and ("+statusSql+")";}
			String selectSql="WITH PROGRAM_LIST AS"
					+ " (SELECT P.PROGRAM_ID,"
					+ "       P.NAME PROGRAM_NAME,"
					+ "       P.TYPE,"
					+ "       C.CITY_NAME,"
					+ "       C.CITY_ID,"
					+ "       F.PERCENT,"
					+ "       NVL(F.DIFF_DATE,0) DIFF_DATE,"
					+ "       P.PLAN_START_DATE,"
					+ "       P.PLAN_END_DATE,"
					+ "       0                   COLLECT_STAT,"
					+ "       0                   DAILY_STAT,"
					+ "       0                   MONTHLY_STAT,"
					+ "       F.ACTUAL_START_DATE,"
					+ "       F.ACTUAL_END_DATE,"
					+ "       NVL(F.COLLECT_PERCENT,0) COLLECT_PERCENT,"
					+ "       NVL(F.COLLECT_PROGRESS,1) COLLECT_PROGRESS,"
					+ "       NVL(F.DAILY_PERCENT,0) DAILY_PERCENT,"
					+ "       NVL(F.DAILY_PROGRESS,1) DAILY_PROGRESS,"
					+ "       NVL(F.MONTHLY_PERCENT,0) MONTHLY_PERCENT,"
					+ "       NVL(F.MONTHLY_PROGRESS,1) MONTHLY_PROGRESS,"
					+ "       F.ROAD_PLAN_TOTAL,"
					+ "       F.POI_PLAN_TOTAL"
					+ "  FROM CITY C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F"
					+ " WHERE C.CITY_ID = P.CITY_ID"
					+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
					+ "   AND P.LATEST = 1"
					+ "   AND P.STATUS = 1"
					+ "   AND NOT EXISTS (SELECT 1"
					+ "          FROM TASK T"
					+ "         WHERE T.PROGRAM_ID = P.PROGRAM_ID"
					+ "           AND T.LATEST = 1)"
					+ " UNION ALL"
					+ " SELECT P.PROGRAM_ID,"
					+ "       P.NAME PROGRAM_NAME,"
					+ "       P.TYPE,"
					+ "       C.CITY_NAME,"
					+ "       C.CITY_ID,"
					+ "       F.PERCENT,"
					+ "       NVL(F.DIFF_DATE,0) DIFF_DATE,"
					+ "       P.PLAN_START_DATE,"
					+ "       P.PLAN_END_DATE,"
					+ "       CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE 1 END)"
					+ "         WHEN 0 THEN 2"
					+ "         ELSE CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE T.STATUS END)"
					+ "            WHEN 0 THEN 3 ELSE 2 END END COLLECT_STAT,"
					+ "       CASE SUM(CASE T.TYPE WHEN 0 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE 1 END)"
					+ "         WHEN 0 THEN 2 "
					+ "         ELSE CASE SUM(CASE T.TYPE WHEN 0 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE T.STATUS END)"
					+ "            WHEN 0 THEN 3 ELSE 2 END"
					+ "       END DAILY_STAT,"
					+ "       CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 0 THEN 0 ELSE 1 END) "
					+ "         WHEN 0 THEN 2"
					+ "         ELSE CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 0 THEN 0 ELSE T.STATUS END)"
					+ "            WHEN 0 THEN 3 ELSE 2 END"
					+ "       END MONTHLY_STAT,"
					+ "       F.ACTUAL_START_DATE,"
					+ "       F.ACTUAL_END_DATE,"
					+ "       NVL(F.COLLECT_PERCENT,0) COLLECT_PERCENT,"
					+ "       NVL(F.COLLECT_PROGRESS,1) COLLECT_PROGRESS,"
					+ "       NVL(F.DAILY_PERCENT,0) DAILY_PERCENT,"
					+ "       NVL(F.DAILY_PROGRESS,1) DAILY_PROGRESS,"
					+ "       NVL(F.MONTHLY_PERCENT,0) MONTHLY_PERCENT,"
					+ "       NVL(F.MONTHLY_PROGRESS,1) MONTHLY_PROGRESS,"
					+ "       F.ROAD_PLAN_TOTAL,"
					+ "       F.POI_PLAN_TOTAL"
					+ "  FROM CITY C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F,TASK T"
					+ " WHERE C.CITY_ID = P.CITY_ID"
					+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
					+ "   AND P.PROGRAM_ID = T.PROGRAM_ID"
					+ "   AND P.LATEST = 1"
					+ "   AND P.STATUS = 1"
					+ "   AND EXISTS (SELECT 1"
					+ "          FROM TASK T"
					+ "         WHERE T.PROGRAM_ID = P.PROGRAM_ID"
					+ "           AND T.LATEST = 1"
					+ "           AND T.STATUS !=0)"
					+ "   GROUP BY P.PROGRAM_ID,P.NAME,P.TYPE,C.CITY_NAME,C.CITY_ID,F.PERCENT,F.DIFF_DATE,"
					+ "            P.PLAN_START_DATE,P.PLAN_END_DATE,F.ACTUAL_START_DATE,F.ACTUAL_END_DATE,"
					+ "            F.COLLECT_PERCENT,F.COLLECT_PROGRESS,F.DAILY_PERCENT,F.DAILY_PROGRESS,"
					+ "            F.MONTHLY_PERCENT,F.MONTHLY_PROGRESS,F.ROAD_PLAN_TOTAL,F.POI_PLAN_TOTAL),"
					+ "FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM PROGRAM_LIST"
					+ "   WHERE 1 = 1"
					+ conditionSql
					+ "   ORDER BY PROGRAM_LIST.DIFF_DATE ASC,PROGRAM_LIST.PERCENT DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			log.info("program list:"+selectSql);
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					Page page = new Page(currentPageNum);
				    page.setPageSize(pageSize);
				    int total=0;
					while(rs.next()){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("programId", rs.getInt("PROGRAM_ID"));
						map.put("name", rs.getString("PROGRAM_NAME"));
						if(rs.getInt("TYPE")==1){
							map.put("cityId", rs.getInt("CITY_ID"));
							map.put("cityName", rs.getString("CITY_NAME"));}
						else if(rs.getInt("TYPE")==4){
							map.put("inforId", rs.getInt("INFOR_ID"));
							map.put("inforName", rs.getString("INFOR_NAME"));}
						map.put("type", rs.getInt("TYPE"));
						map.put("status", 1);					
						map.put("percent", rs.getInt("PERCENT"));
						map.put("diffDate", rs.getInt("DIFF_DATE"));
						map.put("collectProgress", rs.getInt("COLLECT_PROGRESS"));
						map.put("collectPercent", rs.getInt("COLLECT_PERCENT"));
						map.put("dailyProgress", rs.getInt("DAILY_PROGRESS"));
						map.put("dailyPercent", rs.getInt("DAILY_PERCENT"));
						map.put("monthlyProgress", rs.getInt("MONTHLY_PROGRESS"));
						map.put("monthlyPercent", rs.getInt("MONTHLY_PERCENT"));
						map.put("roadPlanTotal", rs.getInt("ROAD_PLAN_TOTAL"));
						map.put("poiPlanTotal", rs.getInt("POI_PLAN_TOTAL"));
						map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
						map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
						map.put("actualStartDate", DateUtils.dateToString(rs.getTimestamp("ACTUAL_START_DATE")));
						map.put("actualEndDate", DateUtils.dateToString(rs.getTimestamp("ACTUAL_END_DATE")));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
						map.put("jobs", new JSONArray());
						total=rs.getInt("TOTAL_RECORD_NUM");
						list.add(map);
					}
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}
	    	};
			Page programList = run.query(conn, selectSql, rsHandler);
			return programList;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 常规已完成
	 * @param conn
	 * @param conditionJson
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	public Page commonOverList(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			//根据城市名称\项目名称模糊查询
			//1-3 按时完成 diff_date=0			提前完成 diff_date>0			逾期完成 diff_date<0
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (PROGRAM_LIST.CITY_NAME LIKE '%"+conditionJson.getString(key)+"%' OR PROGRAM_LIST.PROGRAM_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("selectParam".equals(key)) {
						JSONArray selectParam1=conditionJson.getJSONArray(key);
						for(Object i:selectParam1){
							int tmp=(int) i;												
							if(tmp==1){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" PROGRAM_LIST.diff_date=0";
							}
							if(tmp==2){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" PROGRAM_LIST.diff_date>0";
							}
							if(tmp==3){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" PROGRAM_LIST.diff_date<0";
							}
						}
					}
				}
			}
			if(!statusSql.isEmpty()){//有非status
				conditionSql+=" and ("+statusSql+")";}
			String selectSql="WITH PROGRAM_LIST AS"
					+ " (SELECT DISTINCT P.PROGRAM_ID,"
					+ "                P.NAME PROGRAM_NAME,"
					+ "                P.TYPE,"
					+ "                C.CITY_NAME,"
					+ "                C.CITY_ID,"
					+ "                F.PERCENT,"
					+ "                F.DIFF_DATE,"
					+ "                P.PLAN_START_DATE,"
					+ "                P.PLAN_END_DATE,"
					+ "                F.ACTUAL_START_DATE,"
					+ "                F.ACTUAL_END_DATE"
					+ "  FROM CITY C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F, TASK T"
					+ " WHERE C.CITY_ID = P.CITY_ID"
					+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
					+ "   AND P.PROGRAM_ID = T.PROGRAM_ID"
					+ "   AND T.LATEST = 1"
					+ "   AND P.LATEST = 1"
					+ "   AND P.STATUS = 1"
					+ "   AND NOT EXISTS (SELECT 1"
					+ "          FROM TASK T"
					+ "         WHERE T.PROGRAM_ID = P.PROGRAM_ID"
					+ "           AND T.LATEST = 1"
					+ "           AND T.STATUS != 0)),"
					+ "FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM PROGRAM_LIST"
					+ "   WHERE 1 = 1"
					+ conditionSql
					+ "   ORDER BY PROGRAM_LIST.DIFF_DATE ASC,PROGRAM_LIST.PROGRAM_NAME DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			Page programList = run.query(conn, selectSql, getOverQuery(currentPageNum,pageSize));
			return programList;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 常规已关闭
	 * @param conn
	 * @param conditionJson
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	public Page commonCloseList(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			//根据城市名称\项目名称模糊查询
			//1-3 按时完成 diff_date=0			提前完成 diff_date>0			逾期完成 diff_date<0
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (PROGRAM_LIST.CITY_NAME LIKE '%"+conditionJson.getString(key)+"%' OR PROGRAM_LIST.PROGRAM_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("selectParam".equals(key)) {
						JSONArray selectParam1=conditionJson.getJSONArray(key);
						for(Object i:selectParam1){
							int tmp=(int) i;												
							if(tmp==1){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" PROGRAM_LIST.diff_date=0";
							}
							if(tmp==2){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" PROGRAM_LIST.diff_date>0";
							}
							if(tmp==3){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" PROGRAM_LIST.diff_date<0";
							}
						}
					}
				}
			}
			if(!statusSql.isEmpty()){//有非status
				conditionSql+=" and ("+statusSql+")";}
			String selectSql="WITH PROGRAM_LIST AS"
					+ " (SELECT P.PROGRAM_ID,"
					+ "       P.NAME PROGRAM_NAME,"
					+ "       P.TYPE,"
					+ "       C.CITY_NAME,"
					+ "       C.CITY_ID,"
					+ "       F.DIFF_DATE,"
					+ "       P.PLAN_START_DATE,"
					+ "       P.PLAN_END_DATE,"
					+ "       F.ACTUAL_START_DATE,"
					+ "       F.ACTUAL_END_DATE"
					+ "  FROM CITY C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F"
					+ " WHERE C.CITY_ID = P.CITY_ID"
					+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
					+ "   AND P.LATEST = 1"
					+ "   AND P.STATUS = 0),"
					+ "FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM PROGRAM_LIST"
					+ "   WHERE 1 = 1"
					+ conditionSql
					+ "   ORDER BY PROGRAM_LIST.DIFF_DATE ASC,PROGRAM_LIST.PROGRAM_NAME DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			Page programList = run.query(conn, selectSql, getCloseQuery(currentPageNum,pageSize));
			return programList;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 情报未发布
	 * @param conn
	 * @param conditionJson
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	public Page inforUnPushList(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			//未规划，草稿，根据城市名称\项目名称模糊查询
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (PROGRAM_LIST.INFOR_NAME LIKE '%"+conditionJson.getString(key)+"%' OR PROGRAM_LIST.NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("planStatus".equals(key)) {
						conditionSql=conditionSql+" AND PROGRAM_LIST.PLAN_STATUS IN ("+conditionJson.getJSONArray(key).join(",")+")";
					}
				}
			}
			String selectSql="WITH PROGRAM_LIST AS"
					+ " (SELECT 0 PROGRAM_ID,"
					+ "         C.INFOR_ID,"
					+ "         '' NAME,"
					+ "         4 TYPE,"
					+ "         0 STATUS,"
					+ "         C.INFOR_NAME,"
					+ "         C.FEATURE_KIND,"
					+ "         C.PLAN_STATUS"
					+ "    FROM INFOR C"
					+ "   WHERE C.PLAN_STATUS = 0"
					+ "  UNION ALL"
					+ "  SELECT P.PROGRAM_ID,"
					+ "         C.INFOR_ID,"
					+ "         P.NAME,"
					+ "         P.TYPE,"
					+ "         P.STATUS,"
					+ "         C.INFOR_NAME,"
					+ "         C.FEATURE_KIND,"
					+ "         C.PLAN_STATUS"
					+ "    FROM PROGRAM P, INFOR C"
					+ "   WHERE P.INFOR_ID = C.INFOR_ID"
					+ "     AND P.LATEST = 1"
					+ "     AND P.STATUS = 2),"
					+ "FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM PROGRAM_LIST"
					+ "   WHERE 1 = 1"
					+ conditionSql
					+ "   ORDER BY PROGRAM_LIST.PLAN_STATUS DESC,PROGRAM_LIST.INFOR_NAME DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			Page programList = run.query(conn, selectSql, getUnPushQuery(currentPageNum,pageSize));
			return programList;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	private static ResultSetHandler<Page> getUnPushQuery(final int currentPageNum,final int pageSize){
		final String version=SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
			public Page handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Page page = new Page(currentPageNum);
			    page.setPageSize(pageSize);
			    int total=0;
				while(rs.next()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("programId", rs.getInt("PROGRAM_ID"));
					map.put("name", rs.getString("NAME"));
					if(rs.getInt("TYPE")==1){
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("cityName", rs.getString("CITY_NAME"));}
					else if(rs.getInt("TYPE")==4){
						map.put("inforId", rs.getString("INFOR_ID"));
						map.put("inforName", rs.getString("INFOR_NAME"));
						map.put("featureKind", rs.getInt("FEATURE_KIND"));}
					map.put("planStatus", rs.getInt("PLAN_STATUS"));
					map.put("type", rs.getInt("TYPE"));
					map.put("status", rs.getInt("STATUS"));
					map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
					map.put("jobs", new JSONArray());
					total=rs.getInt("TOTAL_RECORD_NUM");
					list.add(map);
				}
				page.setTotalCount(total);
				page.setResult(list);
				return page;
			}
    	};
    	return rsHandler;
	}
	
	/**
	 * 情报已发布
	 * @param conn
	 * @param conditionJson
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	public Page inforPushList(Connection conn,JSONObject conditionJson,final int currentPageNum,final int pageSize) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			//根据城市名称\项目名称模糊查询
			//1-6采集正常,采集异常,采集完成，日编正常,日编异常,日编完成
			//progress:1正常(实际作业小于预期)，2异常(实际作业等于或大于预期),3完成（percent=100）
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (PROGRAM_LIST.INFOR_NAME LIKE '%"+conditionJson.getString(key)+"%' OR PROGRAM_LIST.PROGRAM_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("selectParam".equals(key)) {
						JSONArray selectParam1=conditionJson.getJSONArray(key);
						JSONArray collectProgress=new JSONArray();
						JSONArray dailyProgress=new JSONArray();
						for(Object i:selectParam1){
							int tmp=(int)i;
							if(tmp==1||tmp==2){collectProgress.add(tmp);}
							if(tmp==3){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" COLLECT_STAT=3";
							}
							if(tmp==4||tmp==5){dailyProgress.add(tmp-3);}
							if(tmp==6){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" daily_STAT=3";
							}
						}
						if(!collectProgress.isEmpty()){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" PROGRAM_LIST.collect_Progress IN ("+collectProgress.join(",")+")";}
						if(!dailyProgress.isEmpty()){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" PROGRAM_LIST.daily_Progress IN ("+dailyProgress.join(",")+")";}
					}
				}
			}
			if(!statusSql.isEmpty()){//有非status
				conditionSql+=" and ("+statusSql+")";}
			String selectSql="WITH PROGRAM_LIST AS"
					+ " (SELECT P.PROGRAM_ID,"
					+ "       P.NAME PROGRAM_NAME,"
					+ "       P.TYPE,"
					+ "       C.INFOR_NAME,"
					+ "       C.INFOR_ID,"
					+ "       C.FEATURE_KIND,"
					+ "       F.PERCENT,"
					+ "       F.DIFF_DATE,"
					+ "       P.PLAN_START_DATE,"
					+ "       P.PLAN_END_DATE,"
					+ "       0                   COLLECT_STAT,"
					+ "       0                   DAILY_STAT,"
					+ "       0                   MONTHLY_STAT,"
					+ "       F.ACTUAL_START_DATE,"
					+ "       F.ACTUAL_END_DATE,"
					+ "       NVL(F.COLLECT_PERCENT,0) COLLECT_PERCENT,"
					+ "       NVL(F.COLLECT_PROGRESS,1) COLLECT_PROGRESS,"
					+ "       NVL(F.DAILY_PERCENT,0) DAILY_PERCENT,"
					+ "       NVL(F.DAILY_PROGRESS,1) DAILY_PROGRESS,"
					+ "       (SELECT COUNT(1) FROM TASK T WHERE T.PROGRAM_ID=P.PROGRAM_ID AND T.STATUS!=0 AND T.TYPE=0) OPEN_TASK,"
					+ "       NVL((SELECT J.STATUS"
					+ "            FROM JOB_RELATION JR,JOB J"
					+ "            WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=1 AND J.LATEST=1 AND JR.ITEM_ID=P.PROGRAM_ID AND JR.ITEM_TYPE=1 ),-1) TIPS2MARK,"
					+ "       NVL((SELECT J.STATUS"
					+ "            FROM JOB_RELATION JR,JOB J"
					+ "            WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=2 AND J.LATEST=1 AND JR.ITEM_ID=P.PROGRAM_ID AND JR.ITEM_TYPE=1 ),-1) DAY2MONTH"
					+ "  FROM INFOR C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F"
					+ " WHERE C.INFOR_ID = P.INFOR_ID"
					+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
					+ "   AND P.LATEST = 1"
					+ "   AND P.STATUS = 1"
					+ "   AND NOT EXISTS (SELECT 1"
					+ "          FROM TASK T"
					+ "         WHERE T.PROGRAM_ID = P.PROGRAM_ID"
					+ "           AND T.LATEST = 1)"
					+ " UNION ALL"
					+ " SELECT P.PROGRAM_ID,"
					+ "       P.NAME PROGRAM_NAME,"
					+ "       P.TYPE,"
					+ "       C.INFOR_NAME,"
					+ "       C.INFOR_ID,"
					+ "       C.FEATURE_KIND,"
					+ "       F.PERCENT,"
					+ "       F.DIFF_DATE,"
					+ "       P.PLAN_START_DATE,"
					+ "       P.PLAN_END_DATE,"
					+ "       CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE 1 END)"
					+ "         WHEN 0 THEN 2"
					+ "         ELSE CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE T.STATUS END)"
					+ "            WHEN 0 THEN 3 ELSE 2 END END COLLECT_STAT,"
					+ "       CASE SUM(CASE T.TYPE WHEN 0 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE 1 END)"
					+ "         WHEN 0 THEN 2 "
					+ "         ELSE CASE SUM(CASE T.TYPE WHEN 0 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE T.STATUS END)"
					+ "            WHEN 0 THEN 3 ELSE 2 END"
					+ "       END DAILY_STAT,"
					+ "       CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 0 THEN 0 ELSE 1 END) "
					+ "         WHEN 0 THEN 2"
					+ "         ELSE CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 0 THEN 0 ELSE T.STATUS END)"
					+ "            WHEN 0 THEN 3 ELSE 2 END"
					+ "       END MONTHLY_STAT,"
					+ "       F.ACTUAL_START_DATE,"
					+ "       F.ACTUAL_END_DATE,"
					+ "       NVL(F.COLLECT_PERCENT,0) COLLECT_PERCENT,"
					+ "       NVL(F.COLLECT_PROGRESS,1) COLLECT_PROGRESS,"
					+ "       NVL(F.DAILY_PERCENT,0) DAILY_PERCENT,"
					+ "       NVL(F.DAILY_PROGRESS,1) DAILY_PROGRESS,"
					+ "       (SELECT COUNT(1) FROM TASK T WHERE T.PROGRAM_ID=P.PROGRAM_ID AND T.STATUS!=0 AND T.TYPE=0) OPEN_TASK,"
					+ "       NVL((SELECT J.STATUS"
					+ "            FROM JOB_RELATION JR,JOB J"
					+ "            WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=1 AND J.LATEST=1 AND JR.ITEM_ID=P.PROGRAM_ID AND JR.ITEM_TYPE=1 ),-1) TIPS2MARK,"
					+ "       NVL((SELECT J.STATUS"
					+ "            FROM JOB_RELATION JR,JOB J"
					+ "            WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=2 AND J.LATEST=1 AND JR.ITEM_ID=P.PROGRAM_ID AND JR.ITEM_TYPE=1 ),-1) DAY2MONTH"
					+ "  FROM INFOR C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F,TASK T"
					+ " WHERE C.INFOR_ID = P.INFOR_ID"
					+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
					+ "   AND T.PROGRAM_ID = P.PROGRAM_ID"
					+ "   AND P.LATEST = 1"
					+ "   AND P.STATUS = 1"
					+ "   AND EXISTS (SELECT 1"
					+ "          FROM TASK T"
					+ "         WHERE T.PROGRAM_ID = P.PROGRAM_ID"
					+ "           AND T.LATEST = 1 and t.type!=2"
					+ "           AND T.STATUS !=0)"
					+ "   GROUP BY P.PROGRAM_ID,P.NAME,P.TYPE,C.INFOR_NAME,C.INFOR_ID,F.PERCENT,F.DIFF_DATE,"
					+ "            P.PLAN_START_DATE,P.PLAN_END_DATE,F.ACTUAL_START_DATE,F.ACTUAL_END_DATE,"
					+ "            F.COLLECT_PERCENT,F.COLLECT_PROGRESS,F.DAILY_PERCENT,F.DAILY_PROGRESS,C.FEATURE_KIND),"
					+ "FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM PROGRAM_LIST"
					+ "   WHERE 1 = 1"
					+ conditionSql
					+ "   ORDER BY PROGRAM_LIST.DIFF_DATE ASC,PROGRAM_LIST.PERCENT DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					Page page = new Page(currentPageNum);
				    page.setPageSize(pageSize);
				    int total=0;
					while(rs.next()){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("programId", rs.getInt("PROGRAM_ID"));
						map.put("name", rs.getString("PROGRAM_NAME"));
						map.put("inforId", rs.getString("INFOR_ID"));
						map.put("inforName", rs.getString("INFOR_NAME"));
						map.put("featureKind", rs.getInt("FEATURE_KIND"));
						map.put("type", rs.getInt("TYPE"));
						map.put("status", 1);					
						map.put("percent", rs.getInt("PERCENT"));
						map.put("diffDate", rs.getInt("DIFF_DATE"));
						map.put("collectProgress", rs.getInt("COLLECT_PROGRESS"));
						map.put("collectPercent", rs.getInt("COLLECT_PERCENT"));
						map.put("dailyProgress", rs.getInt("DAILY_PROGRESS"));
						map.put("dailyPercent", rs.getInt("DAILY_PERCENT"));
						map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
						map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
						map.put("actualStartDate", DateUtils.dateToString(rs.getTimestamp("ACTUAL_START_DATE")));
						map.put("actualEndDate", DateUtils.dateToString(rs.getTimestamp("ACTUAL_END_DATE")));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
						map.put("jobs", getJobArray(rs));

						total=rs.getInt("TOTAL_RECORD_NUM");
						list.add(map);
					}
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}
	    	};
			Page programList = run.query(conn, selectSql, rsHandler);
			return programList;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 情报已完成
	 * @param conn
	 * @param conditionJson
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	public Page inforOverList(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			//根据城市名称\项目名称模糊查询
			//1-3 按时完成 diff_date=0			提前完成 diff_date>0			逾期完成 diff_date<0
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (PROGRAM_LIST.INFOR_NAME LIKE '%"+conditionJson.getString(key)+"%' OR PROGRAM_LIST.PROGRAM_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("selectParam".equals(key)) {
						JSONArray selectParam1=conditionJson.getJSONArray(key);
						for(Object i:selectParam1){
							int tmp=(int) i;												
							if(tmp==1){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" PROGRAM_LIST.diff_date=0";
							}
							if(tmp==2){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" PROGRAM_LIST.diff_date>0";
							}
							if(tmp==3){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" PROGRAM_LIST.diff_date<0";
							}
						}
					}
				}
			}
			if(!statusSql.isEmpty()){//有非status
				conditionSql+=" and ("+statusSql+")";}
			String selectSql="WITH PROGRAM_LIST AS"
					+ " (SELECT DISTINCT P.PROGRAM_ID,"
					+ "                P.NAME PROGRAM_NAME,"
					+ "                P.TYPE,"
					+ "                C.INFOR_NAME,"
					+ "                C.INFOR_ID,"
					+ "         C.FEATURE_KIND,"
					+ "                F.PERCENT,"
					+ "                F.DIFF_DATE,"
					+ "                P.PLAN_START_DATE,"
					+ "                P.PLAN_END_DATE,"
					+ "                F.ACTUAL_START_DATE,"
					+ "                F.ACTUAL_END_DATE,"
					+ "       (SELECT COUNT(1) FROM TASK T WHERE T.PROGRAM_ID=P.PROGRAM_ID AND T.STATUS!=0 AND T.TYPE=0) OPEN_TASK,"
					+ "       NVL((SELECT J.STATUS"
					+ "            FROM JOB_RELATION JR,JOB J"
					+ "            WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=1 AND J.LATEST=1 AND JR.ITEM_ID=P.PROGRAM_ID AND JR.ITEM_TYPE=1 ),-1) TIPS2MARK,"
					+ "       NVL((SELECT J.STATUS"
					+ "            FROM JOB_RELATION JR,JOB J"
					+ "            WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=2 AND J.LATEST=1 AND JR.ITEM_ID=P.PROGRAM_ID AND JR.ITEM_TYPE=1 ),-1) DAY2MONTH"
					+ "  FROM INFOR C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F, TASK T"
					+ " WHERE C.INFOR_ID = P.INFOR_ID"
					+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
					+ "   AND P.PROGRAM_ID = T.PROGRAM_ID"
					+ "   AND T.LATEST = 1"
					+ "   AND P.LATEST = 1"
					+ "   AND P.STATUS = 1"
					+ "   AND NOT EXISTS (SELECT 1"
					+ "          FROM TASK T"
					+ "         WHERE T.PROGRAM_ID = P.PROGRAM_ID"
					+ "           AND T.LATEST = 1 and t.type!=2"
					+ "           AND T.STATUS != 0)),"
					+ "FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM PROGRAM_LIST"
					+ "   WHERE 1 = 1"
					+ conditionSql
					+ "   ORDER BY PROGRAM_LIST.DIFF_DATE ASC,PROGRAM_LIST.PROGRAM_NAME DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			Page programList = run.query(conn, selectSql, getOverQuery(currentPageNum,pageSize));
			return programList;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	private static ResultSetHandler<Page> getOverQuery(final int currentPageNum,final int pageSize){
		final String version=SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
			public Page handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Page page = new Page(currentPageNum);
			    page.setPageSize(pageSize);
			    int total=0;
				while(rs.next()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("programId", rs.getInt("PROGRAM_ID"));
					map.put("name", rs.getString("PROGRAM_NAME"));
					if(rs.getInt("TYPE")==1){
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("cityName", rs.getString("CITY_NAME"));}
					else if(rs.getInt("TYPE")==4){
						map.put("inforId", rs.getString("INFOR_ID"));
						map.put("inforName", rs.getString("INFOR_NAME"));
						map.put("featureKind", rs.getInt("FEATURE_KIND"));}
					map.put("type", rs.getInt("TYPE"));
					map.put("status", 1);					
					map.put("percent", rs.getInt("PERCENT"));
					map.put("diffDate", rs.getInt("DIFF_DATE"));
					map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
					map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
					map.put("actualStartDate", DateUtils.dateToString(rs.getTimestamp("ACTUAL_START_DATE")));
					map.put("actualEndDate", DateUtils.dateToString(rs.getTimestamp("ACTUAL_END_DATE")));
					map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
					map.put("jobs", getJobArray(rs));
					total=rs.getInt("TOTAL_RECORD_NUM");
					list.add(map);
				}
				page.setTotalCount(total);
				page.setResult(list);
				return page;
			}
    	};
    	return rsHandler;
	}
	
	/**
	 * 情报已关闭
	 * @param conn
	 * @param conditionJson
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	public Page inforCloseList(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			//根据城市名称\项目名称模糊查询
			//1-3 按时完成 diff_date=0			提前完成 diff_date>0			逾期完成 diff_date<0
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (PROGRAM_LIST.INFOR_NAME LIKE '%"+conditionJson.getString(key)+"%' OR PROGRAM_LIST.PROGRAM_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("selectParam".equals(key)) {
						JSONArray selectParam1=conditionJson.getJSONArray(key);
						for(Object i:selectParam1){
							int tmp=(int) i;												
							if(tmp==1){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" PROGRAM_LIST.diff_date=0";
							}
							if(tmp==2){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" PROGRAM_LIST.diff_date>0";
							}
							if(tmp==3){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" PROGRAM_LIST.diff_date<0";
							}
						}
					}
				}
			}
			if(!statusSql.isEmpty()){//有非status
				conditionSql+=" and ("+statusSql+")";}
			String selectSql="WITH PROGRAM_LIST AS"
					+ " (SELECT P.PROGRAM_ID,"
					+ "       P.NAME PROGRAM_NAME,"
					+ "       P.TYPE,"
					+ "       C.INFOR_NAME,"
					+ "       C.INFOR_ID,"
					+ "       C.FEATURE_KIND,"
					+ "       F.DIFF_DATE,"
					+ "       P.PLAN_START_DATE,"
					+ "       P.PLAN_END_DATE,"
					+ "       F.ACTUAL_START_DATE,"
					+ "       F.ACTUAL_END_DATE,"
					+ "       (SELECT COUNT(1) FROM TASK T WHERE T.PROGRAM_ID=P.PROGRAM_ID AND T.STATUS!=0 AND T.TYPE=0) OPEN_TASK,"
					+ "       NVL((SELECT J.STATUS"
					+ "            FROM JOB_RELATION JR,JOB J"
					+ "            WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=1 AND J.LATEST=1 AND JR.ITEM_ID=P.PROGRAM_ID AND JR.ITEM_TYPE=1 ),-1) TIPS2MARK,"
					+ "       NVL((SELECT J.STATUS"
					+ "            FROM JOB_RELATION JR,JOB J"
					+ "            WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=2 AND J.LATEST=1 AND JR.ITEM_ID=P.PROGRAM_ID AND JR.ITEM_TYPE=1 ),-1) DAY2MONTH"
					+ "  FROM INFOR C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F"
					+ " WHERE C.INFOR_ID = P.INFOR_ID"
					+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
					+ "   AND P.LATEST = 1"
					+ "   AND P.STATUS = 0),"
					+ "FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM PROGRAM_LIST"
					+ "   WHERE 1 = 1"
					+ conditionSql
					+ "   ORDER BY PROGRAM_LIST.DIFF_DATE ASC,PROGRAM_LIST.PROGRAM_NAME DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			Page programList = run.query(conn, selectSql, getCloseQuery(currentPageNum,pageSize));
			return programList;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	private static ResultSetHandler<Page> getCloseQuery(final int currentPageNum,final int pageSize){
		final String version=SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion);
		ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
			public Page handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Page page = new Page(currentPageNum);
			    page.setPageSize(pageSize);
			    int total=0;
				while(rs.next()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("programId", rs.getInt("PROGRAM_ID"));
					map.put("name", rs.getString("PROGRAM_NAME"));
					if(rs.getInt("TYPE")==1){
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("cityName", rs.getString("CITY_NAME"));}
					else if(rs.getInt("TYPE")==4){
						map.put("inforId", rs.getString("INFOR_ID"));
						map.put("inforName", rs.getString("INFOR_NAME"));
						map.put("featureKind", rs.getInt("FEATURE_KIND"));}
					map.put("type", rs.getInt("TYPE"));
					map.put("status", 0);				
					map.put("diffDate", rs.getInt("DIFF_DATE"));
					map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
					map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
					map.put("actualStartDate", DateUtils.dateToString(rs.getTimestamp("ACTUAL_START_DATE")));
					map.put("actualEndDate", DateUtils.dateToString(rs.getTimestamp("ACTUAL_END_DATE")));
					map.put("version", version);
					map.put("jobs", getJobArray(rs));
					total=rs.getInt("TOTAL_RECORD_NUM");
					list.add(map);
				}
				page.setTotalCount(total);
				page.setResult(list);
				return page;
			}
    	};
    	return rsHandler;
	}

	private static JSONArray getJobArray(ResultSet rs) throws SQLException{
		JSONArray jobs = new JSONArray();

		if(rs.getInt("TYPE")!=4){
			return jobs;
		}

		int tips2markStatus = rs.getInt("TIPS2MARK");
		int day2monthStatus = rs.getInt("DAY2MONTH");
		int opentaskCount = rs.getInt("OPEN_TASK");
		if(tips2markStatus==-1){
			//所有采集任务都关闭才能执行tips转mark
			if(opentaskCount==0){
				JSONObject job = new JSONObject();
				job.put("status", 0);
				job.put("type", JobType.TiPS2MARK.value());
				jobs.add(job);
			}
		}else{
			JSONObject job = new JSONObject();
			job.put("status", tips2markStatus);
			job.put("type", JobType.TiPS2MARK.value());
			jobs.add(job);
		}
		if(day2monthStatus==-1){
			//所有采集任务都关闭才能执行日落月
			if(opentaskCount==0){
				JSONObject job = new JSONObject();
				job.put("status", 0);
				job.put("type", JobType.DAY2MONTH.value());
				jobs.add(job);
			}
		}else{
			JSONObject job = new JSONObject();
			job.put("status", day2monthStatus);
			job.put("type", JobType.DAY2MONTH.value());
			jobs.add(job);
		}
		return jobs;
	}
	
	public Page commonList(Connection conn,int planningStatus, JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize)throws Exception{
		//常规未发布
		Page page = new Page();
		if(planningStatus==1){
			page=commonUnPushList(conn,conditionJson,currentPageNum,pageSize);
		}else if(planningStatus==2){
			//常规已发布
			page=commonPushList(conn,conditionJson,currentPageNum,pageSize);
		}else if(planningStatus==3){
			//常规已完成
			page=commonOverList(conn,conditionJson,currentPageNum,pageSize);
		}else if(planningStatus==4){
			//常规已关闭
			page=commonCloseList(conn,conditionJson,currentPageNum,pageSize);
		}
		return page;
	}
	
	public Page inforList(Connection conn,int planningStatus, JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize)throws Exception{
		//情报未发布
		Page page = new Page();
		if(planningStatus==1){
			page=inforUnPushList(conn,conditionJson,currentPageNum,pageSize);
		}else if(planningStatus==2){
			//情报已发布
			page=inforPushList(conn,conditionJson,currentPageNum,pageSize);
		}else if(planningStatus==3){
			//情报已完成
			page=inforOverList(conn,conditionJson,currentPageNum,pageSize);
		}else if(planningStatus==4){
			//情报已关闭
			page=inforCloseList(conn,conditionJson,currentPageNum,pageSize);
		}
		return page;
	}
		
	public Page list(int type, int planningStatus, JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			if(type==4){
				//情报任务查询列表
				return this.inforList(conn,planningStatus, conditionJson, orderJson, currentPageNum, pageSize);
			}else{
				//常规任务查询列表
				return this.commonList(conn,planningStatus, conditionJson, orderJson, currentPageNum, pageSize);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Map<String, Object> query(int programId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
	    	return query(conn, programId);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	//查询情报项目
	public Map<String, Object> queryIntelligence(JSONObject dataJson) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
	    	return queryIntelligence(conn, dataJson);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Map<String, Object> queryIntelligence(Connection conn,JSONObject dataJson) throws Exception {
		try{
			String appendSql="";
			if(dataJson.containsKey("programId")){
				appendSql=appendSql+" AND T.PROGRAM_ID="+dataJson.getInt("programId");
			}
			if(dataJson.containsKey("inforId")){
				appendSql=appendSql+" AND T.INFOR_ID="+dataJson.getInt("inforId");
			}
			//modify by songhe 2017/09/25 需求变更添加feedbackDate，isAdopted，denyReason字段
			String sql="WITH T AS"
					+ " (SELECT P.PROGRAM_ID,"
					+ "         P.NAME                     PROGRAM_NAME,"
					+ "         P.DESCP                    PROGRAM_DESCP,"
					+ "         P.TYPE,"
					+ "         I.INFOR_ID,"
					+ "         I.INFOR_NAME,I.INFOR_STAGE,"
					+ "         I.FEATURE_KIND,"
					+ "         I.METHOD,"
					+ "         I.ADMIN_NAME,"
					+ "         I.INFOR_CODE,"
					+ "         I.SOURCE_CODE,"
					+ "         I.INFO_TYPE_NAME,"
					+ "         I.TOPIC_NAME,"
					+ "         I.PUBLISH_DATE,"
					+ "         I.PLAN_STATUS,"
					+ "         I.NEWS_DATE,"		
					+ "         I.EXPECT_DATE,"
					+ "         I.FEEDBACK_DATE,"
					+ "         I.DENY_REASON,"
					+ "         I.IS_ADOPTED,"
					+ "         P.CREATE_USER_ID,"
					+ "         U.USER_REAL_NAME           CREATE_USER_NAME,"
					+ "         P.PLAN_START_DATE,"
					+ "         P.PLAN_END_DATE,"
					+ "         P.COLLECT_PLAN_START_DATE,"
					+ "         P.COLLECT_PLAN_END_DATE,"
					+ "         P.DAY_EDIT_PLAN_START_DATE,"
					+ "         P.DAY_EDIT_PLAN_END_DATE,"
					+ "         P.PRODUCE_PLAN_START_DATE,"
					+ "         P.PRODUCE_PLAN_END_DATE"
					+ "    FROM PROGRAM P, USER_INFO U, INFOR I"
					+ "   WHERE I.INFOR_ID = P.INFOR_ID"
					+ "     AND P.LATEST = 1"
					+ "     AND P.CREATE_USER_ID = U.USER_ID(+)"
					+ "  UNION ALL"
					+ "  SELECT 0,"
					+ "         NULL,"
					+ "         NULL,"
					+ "         4,"
					+ "         I.INFOR_ID,"
					+ "         I.INFOR_NAME,I.INFOR_STAGE,"
					+ "         I.FEATURE_KIND,"
					+ "         I.METHOD,"
					+ "         I.ADMIN_NAME,"
					+ "         I.INFOR_CODE,"
					+ "         I.SOURCE_CODE,"
					+ "         I.INFO_TYPE_NAME,"
					+ "         I.TOPIC_NAME,"
					+ "         I.PUBLISH_DATE,"
					+ "         I.PLAN_STATUS,"
					+ "         I.NEWS_DATE,"
					+ "         I.EXPECT_DATE,"
					+ "         I.FEEDBACK_DATE,"
					+ "         I.DENY_REASON,"
					+ "         I.IS_ADOPTED,"
					+ "         0,"
					+ "         NULL,"
					+ "         NULL,"
					+ "         NULL,"
					+ "         NULL,"
					+ "         NULL,"
					+ "         NULL,"
					+ "         NULL,"
					+ "         NULL,"
					+ "         NULL"
					+ "    FROM INFOR I"
					+ "   WHERE I.PLAN_STATUS = 0)"
					+ "SELECT *"
					+ "  FROM T"
					+ " WHERE 1=1"+appendSql;
			log.info("program query sql :" + sql);
			ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>(){
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					Map<String, Object> map = new HashMap<String, Object>();
					while(rs.next()){
						map.put("programId", rs.getInt("PROGRAM_ID"));
						map.put("name", rs.getString("PROGRAM_NAME"));
						map.put("descp", rs.getString("PROGRAM_DESCP"));
						map.put("type", rs.getInt("TYPE"));
						map.put("inforId", rs.getString("INFOR_ID"));
						map.put("inforName", rs.getString("INFOR_NAME"));
						map.put("inforStage", rs.getInt("INFOR_STAGE"));
						map.put("featureKind", rs.getInt("FEATURE_KIND"));	
						
						map.put("method", rs.getString("METHOD"));	
						map.put("adminName", rs.getString("ADMIN_NAME"));	
						map.put("inforCode", rs.getString("INFOR_CODE"));	
						map.put("sourceCode", rs.getInt("SOURCE_CODE"));	
						map.put("infoTypeName", rs.getString("INFO_TYPE_NAME"));	
						map.put("topicName", rs.getString("TOPIC_NAME"));	
						map.put("planStatus", rs.getInt("PLAN_STATUS"));	
						map.put("publishDate", DateUtils.dateToString(rs.getTimestamp("PUBLISH_DATE")));
						map.put("newsDate", DateUtils.dateToString(rs.getTimestamp("NEWS_DATE")));
						map.put("expectDate",DateUtils.dateToString(rs.getTimestamp("EXPECT_DATE")));	
						
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createUserName", rs.getString("CREATE_USER_NAME"));
						map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
						map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
						map.put("collectPlanStartDate", DateUtils.dateToString(rs.getTimestamp("COLLECT_PLAN_START_DATE")));
						map.put("collectPlanEndDate", DateUtils.dateToString(rs.getTimestamp("COLLECT_PLAN_END_DATE")));
						map.put("dayEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE")));
						map.put("dayEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE")));
						map.put("producePlanStartDate", DateUtils.dateToString(rs.getTimestamp("PRODUCE_PLAN_START_DATE")));
						map.put("producePlanEndDate", DateUtils.dateToString(rs.getTimestamp("PRODUCE_PLAN_END_DATE")));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
						//情报反馈状态=未采纳时，显示未采纳原因
						map.put("isAdopted", rs.getInt("IS_ADOPTED"));
						map.put("denyReason", rs.getString("DENY_REASON"));
						map.put("feedbackDate", DateUtils.dateToString(rs.getTimestamp("FEEDBACK_DATE")));
						return map;
					}
					return map;
				}
	    	};
	    	QueryRunner run = new QueryRunner();
	    	return run.query(conn, sql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Map<String, Object> query(Connection conn,int programId) throws Exception {
		try{
			StringBuilder sb = new StringBuilder();
			sb.append(" SELECT P.PROGRAM_ID,                                      ");
			sb.append("          P.NAME                       PROGRAM_NAME,       ");
			sb.append("          P.DESCP                      PROGRAM_DESCP,      ");
			sb.append("          P.TYPE,                                          ");
			sb.append("          P.STATUS,                                           ");
			sb.append("          C.CITY_NAME,                                     ");
			sb.append("          C.CITY_ID,                                       ");
			sb.append("          I.INFOR_ID,                                      ");
			sb.append("          I.INFOR_NAME,                                    ");
			sb.append("          I.INFOR_stage,                                    ");
			sb.append("          I.FEATURE_KIND,                                  ");
			sb.append("          P.CREATE_USER_ID,                                ");
			sb.append("          U.USER_REAL_NAME             CREATE_USER_NAME,   ");
			sb.append("          P.PLAN_START_DATE,                               ");
			sb.append("          P.PLAN_END_DATE,                                 ");
			sb.append("          P.COLLECT_PLAN_START_DATE,                       ");
			sb.append("          P.COLLECT_PLAN_END_DATE,                         ");
			sb.append("          P.DAY_EDIT_PLAN_START_DATE,                      ");
			sb.append("          P.DAY_EDIT_PLAN_END_DATE,                        ");
			sb.append("          P.MONTH_EDIT_PLAN_START_DATE,                    ");
			sb.append("          P.MONTH_EDIT_PLAN_END_DATE,                      ");
			sb.append("          P.PRODUCE_PLAN_START_DATE,                       ");
			sb.append("          P.PRODUCE_PLAN_END_DATE                          ");
			sb.append("     FROM CITY C, PROGRAM P, USER_INFO U, INFOR I          ");
			sb.append("    WHERE C.CITY_ID(+) = P.CITY_ID                         ");
			sb.append("      AND I.INFOR_ID(+) = P.INFOR_ID                       ");
			sb.append("      AND P.LATEST = 1                                     ");
			sb.append("      AND P.CREATE_USER_ID = U.USER_ID(+)                     ");
			sb.append("      AND P.PROGRAM_ID = "+programId);
			
			String sql = sb.toString();
			log.info("program query sql :" + sql);
			ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>(){
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					Map<String, Object> map = new HashMap<String, Object>();
					while(rs.next()){
						map.put("programId", rs.getInt("PROGRAM_ID"));
						map.put("name", rs.getString("PROGRAM_NAME"));
						map.put("descp", rs.getString("PROGRAM_DESCP"));
						map.put("type", rs.getInt("TYPE"));
						map.put("status", rs.getInt("STATUS"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("cityName", rs.getString("CITY_NAME"));
						map.put("inforId", rs.getString("INFOR_ID"));
						map.put("inforName", rs.getString("INFOR_NAME"));
						map.put("inforStage", rs.getInt("INFOR_STAGE"));
						map.put("featureKind", rs.getInt("FEATURE_KIND"));						
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createUserName", rs.getString("CREATE_USER_NAME"));
						map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
						map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
						map.put("collectPlanStartDate", DateUtils.dateToString(rs.getTimestamp("COLLECT_PLAN_START_DATE")));
						map.put("collectPlanEndDate", DateUtils.dateToString(rs.getTimestamp("COLLECT_PLAN_END_DATE")));
						map.put("dayEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE")));
						map.put("dayEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE")));
						map.put("monthEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE")));
						map.put("monthEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE")));
						map.put("producePlanStartDate", DateUtils.dateToString(rs.getTimestamp("PRODUCE_PLAN_START_DATE")));
						map.put("producePlanEndDate", DateUtils.dateToString(rs.getTimestamp("PRODUCE_PLAN_END_DATE")));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
						return map;
					}
					return map;
				}
	    	};
	    	QueryRunner run = new QueryRunner();
	    	Map<String, Object> programMap = run.query(conn, sql, rsHandler);
	    	return programMap;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public List<Map<String, Object>> queryNameList(String name) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			String sql="SELECT P.PROGRAM_ID,"
					+ "         P.NAME                       PROGRAM_NAME"
					+ "    FROM PROGRAM P where name like '%"+name+"%'";
			ResultSetHandler<List<Map<String, Object>>> rsHandler = new ResultSetHandler<List<Map<String, Object>>>(){
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
					while(rs.next()){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("programId", rs.getInt("PROGRAM_ID"));
						map.put("name", rs.getString("PROGRAM_NAME"));
						mapList.add(map);
					}
					return mapList;
				}
	    	};
	    	QueryRunner run = new QueryRunner();
	    	List<Map<String, Object>> programMap = run.query(conn, sql, rsHandler);
	    	return programMap;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public void pushMsgWithConnection(Connection conn,long userId,List<Program> programs, JSONArray programIds) throws Exception{
		try{
			Map<Integer,Program> inforPrograms=new HashMap<Integer,Program>();
			for(Program p:programs){
				if(p.getType()==4){
					inforPrograms.put(p.getProgramId(), p);
				}
			}
			splitInforTasks(conn,inforPrograms,userId);
			/*项目发布1.所有生管角色 新增项目：XXX(项目名称)，请关注*/			
			String msgTitle="项目发布";
			List<Map<String,Object>> msgContentList=new ArrayList<Map<String,Object>>();
			for(Program program:programs){
				Map<String,Object> map = new HashMap<String, Object>();
				String msgContent = "新增项目:"+program.getName()+",请关注";
				map.put("msgContent", msgContent);
				//关联要素
				JSONObject msgParam = new JSONObject();
				msgParam.put("relateObject", "PROGRAM");
				msgParam.put("relateObjectId", program.getProgramId());
				map.put("msgParam", msgParam.toString());
				msgContentList.add(map);
			}
			if(msgContentList.size()>0){
				programPushMsg(conn,msgTitle,msgContentList,null,userId);
			}		
			openStatus(conn,programIds);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("任务发布消息发送失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public String pushMsg(long userId,JSONArray programIds) throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			//发送消息
			JSONObject condition=new JSONObject();
			condition.put("programIds",programIds);
			JSONArray status=new JSONArray();
			status.add(2);
			condition.put("status",status);
			List<Program> programs = queryProgramTable(conn, condition);
			
			pushMsgWithConnection( conn, userId, programs,programIds);
			
//			Map<Integer,Program> inforPrograms=new HashMap<Integer,Program>();
//			for(Program p:programs){
//				if(p.getType()==4){
//					inforPrograms.put(p.getProgramId(), p);
//				}
//			}
//			splitInforTasks(conn,inforPrograms,userId);
//			/*项目发布1.所有生管角色 新增项目：XXX(项目名称)，请关注*/			
//			String msgTitle="项目发布";
//			List<Map<String,Object>> msgContentList=new ArrayList<Map<String,Object>>();
//			for(Program program:programs){
//				Map<String,Object> map = new HashMap<String, Object>();
//				String msgContent = "新增项目:"+program.getName()+",请关注";
//				map.put("msgContent", msgContent);
//				//关联要素
//				JSONObject msgParam = new JSONObject();
//				msgParam.put("relateObject", "PROGRAM");
//				msgParam.put("relateObjectId", program.getProgramId());
//				map.put("msgParam", msgParam.toString());
//				msgContentList.add(map);
//			}
//			if(msgContentList.size()>0){
//				programPushMsg(conn,msgTitle,msgContentList,null,userId);
//			}		
//			openStatus(conn,programIds);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("任务发布消息发送失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}//项目发布成功*个，失败*个
		return "项目发布成功"+programIds.size()+"个，失败0个";
		
	}
	/**
	 * 情报项目发布时，自动创建两条任务：采集任务和日编任务，均为草稿状态（若情报跨大区，则按照大区拆分成多个任务）
	 * @param conn
	 * @param inforPrograms
	 * @param userId
	 * @throws Exception
	 */
	private void splitInforTasks(Connection conn,final Map<Integer, Program> inforPrograms,final Long userId)throws Exception{
		try{
			if(inforPrograms==null||inforPrograms.size()==0){return;}
			String selectSql="SELECT P.PROGRAM_ID, M.GRID_ID, G.REGION_ID,r.region_name"
					+ "  FROM PROGRAM P, INFOR_GRID_MAPPING M, GRID G,region r"
					+ " WHERE P.INFOR_ID = M.INFOR_ID"
					+ "   AND M.GRID_ID = G.GRID_ID"
					+ "   AND g.region_ID = r.region_ID"
					+ "   AND P.PROGRAM_ID IN (" + StringUtils.join(inforPrograms.keySet().toArray(),",") + ")"
					+ " ORDER BY P.PROGRAM_ID, G.REGION_ID";
			log.info("splitInforTasks sql:" + selectSql);

			ResultSetHandler<List<Task>> rsHandler = new ResultSetHandler<List<Task>>(){
				public List<Task> handle(ResultSet rs) throws SQLException {
					List<Task> list = new ArrayList<Task>();
					Map<Integer, Integer> gridMap =new HashMap<Integer, Integer>();
					Map<Integer, Integer> monthGridMap =new HashMap<Integer, Integer>();
					int programId=0;
					int regionId=0;
					//String regionName="";
					while(rs.next()){
						int programIdTmp=rs.getInt("PROGRAM_ID");
						int regionIdTmp=rs.getInt("REGION_ID");
						//String regionNameTmp=rs.getString("region_name");
						if(programId==0){
							programId=programIdTmp;
							regionId=regionIdTmp;
							//regionName= regionNameTmp;
						}
						if(programId!=programIdTmp){
							//创建月编任务
					    	Task monthTask=new Task();
					    	monthTask.setProgramId(programId);
					    	monthTask.setRegionId(regionId);
					    	monthTask.setGridIds(monthGridMap);
					    	monthTask.setCreateUserId(Integer.valueOf(userId.toString()));
					    	monthTask.setType(2);
					    	monthGridMap =new HashMap<Integer, Integer>();
						}
						if(programId!=programIdTmp||regionId!=regionIdTmp){
							Task collectTask=new Task();
							collectTask.setProgramId(programId);
							collectTask.setRegionId(regionId);
							collectTask.setGridIds(gridMap);
							collectTask.setCreateUserId(Integer.valueOf(userId.toString()));
							collectTask.setType(0);
							collectTask.setPlanStartDate(inforPrograms.get(programId).getCollectPlanStartDate());
							collectTask.setPlanEndDate(inforPrograms.get(programId).getCollectPlanEndDate());
//							collectTask.setName(inforPrograms.get(programId).getName() + regionId);
							
							list.add(collectTask);
							Task dailyTask=new Task();
							dailyTask.setProgramId(programId);
							dailyTask.setRegionId(regionId);
							dailyTask.setGridIds(gridMap);
							dailyTask.setCreateUserId(Integer.valueOf(userId.toString()));
							dailyTask.setType(1);
							dailyTask.setPlanStartDate(inforPrograms.get(programId).getDayEditPlanStartDate());
							dailyTask.setPlanEndDate(inforPrograms.get(programId).getDayEditPlanEndDate());
//							dailyTask.setName(inforPrograms.get(programId).getName() +regionId);
							list.add(dailyTask);
							gridMap =new HashMap<Integer, Integer>();
							programId=programIdTmp;
							regionId=regionIdTmp;
							//regionName= regionNameTmp;
						}
						gridMap.put(rs.getInt("GRID_ID"), 1);
						monthGridMap.put(rs.getInt("GRID_ID"), 1);
					}
					if(programId!=0){
						Task collectTask=new Task();
						collectTask.setProgramId(programId);
						collectTask.setRegionId(regionId);
						collectTask.setGridIds(gridMap);
						collectTask.setCreateUserId(Integer.valueOf(userId.toString()));
						collectTask.setType(0);
						collectTask.setPlanStartDate(inforPrograms.get(programId).getCollectPlanStartDate());
						collectTask.setPlanEndDate(inforPrograms.get(programId).getCollectPlanEndDate());
//						collectTask.setName(inforPrograms.get(programId).getName() + regionId);
						list.add(collectTask);
						Task dailyTask=new Task();
						dailyTask.setProgramId(programId);
						dailyTask.setRegionId(regionId);
						dailyTask.setGridIds(gridMap);
						dailyTask.setCreateUserId(Integer.valueOf(userId.toString()));
						dailyTask.setType(1);
						dailyTask.setPlanStartDate(inforPrograms.get(programId).getDayEditPlanStartDate());
						dailyTask.setPlanEndDate(inforPrograms.get(programId).getDayEditPlanEndDate());
//						dailyTask.setName(inforPrograms.get(programId).getName() + regionId);
						list.add(dailyTask);
						
						//创建月编任务
				    	Task monthTask=new Task();
				    	monthTask.setProgramId(programId);
				    	monthTask.setRegionId(regionId);
				    	monthTask.setGridIds(monthGridMap);
				    	monthTask.setCreateUserId(Integer.valueOf(userId.toString()));
				    	monthTask.setType(2);
				    	list.add(monthTask);
					}
					return list;
				}
	    	};
	    	
			QueryRunner run=new QueryRunner();
			List<Task> list=run.query(conn, selectSql, rsHandler);
			if(list!=null&&list.size()>0){
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
				for(Task t:list){
//					TaskService.getInstance().createWithBean(conn, t);
					Infor infor = InforService.getInstance().getInforByProgramId(conn,t.getProgramId());
					if(t.getType()==0&&infor.getSourceCode()==2){
						t.setWorkKind("1|0|0|0");
					}else if(t.getType()==0&&"矢量制作".equals(infor.getMethod())){//采集任务，且情报为矢量制作
						t.setWorkKind("0|0|1|0");
					}else if(t.getType()==0){
						t.setWorkKind("1|0|0|0");
					}
					UserGroup group =null;
					/*web端有人触发的情报项目发布，导致的采集任务创建，则不赋组；
					 *否则均是后台自动触发的项目，任务，子任务创建，需要根据如下原则赋值:
					 *     根据INFOR表“情报对应方式”字段，进行赋值：
					 *     1.“矢量制作”：赋值=空
					 *     2.其它：根据INFOR表情报“情报省份城市”字段，参考<行政与作业组配置表>，取作业组赋值
					 */
					int groupType = 0;
					if(t.getType()==0&&userId==0 && !"矢量制作".equals(infor.getMethod())){
						groupType = 1;
					}else if(t.getType()==1){
						groupType = 2;
					}else if(t.getType() == 2){
						groupType = 6;
					}
					group = UserGroupService.getInstance().getGroupByAminCode(conn,infor.getAdminCode(), groupType);
					if(group!=null){
						t.setGroupId(group.getGroupId());
					}
					if(t.getType()==0){
						t.setRoadPlanTotal(infor.getRoadLength());
						if(infor.getFeatureKind()==1){
							t.setPoiPlanTotal(5);
						}else{
							t.setPoiPlanTotal(20);
						}
					}
					int taskId=TaskOperation.getNewTaskId(conn);
					t.setTaskId(taskId);
					t.setName(infor.getInforName()+"_"+df.format(infor.getPublishDate())+"_"+taskId);	
					//快线月编任务需要按照图幅扩展grid
					if(t.getType()==2){
						Set<Integer> myGrid=t.getGridIds().keySet(); 
						Set<String> meshs=new HashSet<String>();
						for(Integer gridTmp:myGrid){
							meshs.add(String.valueOf(gridTmp/100));
						}
						for(String meshTmp:meshs){
							Set<String> allGrid = CompGridUtil.mesh2Grid(meshTmp);
							for(String gridExt:allGrid){
								if(!myGrid.contains(Integer.valueOf(gridExt))){
									t.getGridIds().put(Integer.valueOf(gridExt), 2);
								}
							}
						}
					}
					TaskService.getInstance().createWithBean(conn, t);
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("任务发布消息发送失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * @param programId
	 * @return
	 * @throws ServiceException 
	 */
	private Map<String,String> getInforByProgramId(Integer programId) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(" SELECT I.INFOR_NAME,I.ADMIN_NAME");
			sb.append("   FROM PROGRAM P, INFOR I       ");
			sb.append("  WHERE P.INFOR_ID = I.INFOR_ID  ");
			sb.append("    AND P.PROGRAM_ID = " + programId);
			
			String sql = sb.toString();
			
			log.info("getInforNameByProgramId sql :" + sql);
			
			
			ResultSetHandler<Map<String,String>> rsHandler = new ResultSetHandler<Map<String,String>>() {
				public Map<String,String> handle(ResultSet rs) throws SQLException {
					Map<String,String> result = new HashMap<String,String>();
					if(rs.next()) {
						result.put("inforName", rs.getString("INFOR_NAME"));
						result.put("adminName", rs.getString("ADMIN_NAME"));
					}
					return result;
				}
			};
			Map<String,String> result =  run.query(conn, sql,rsHandler);
			return result;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("getInforNameByProgramId失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/*项目创建/编辑/关闭
	 * 1.所有生管角色
	 * 2.2.项目包含的所有任务作业组组长
	 * 项目:XXX(任务名称)内容发生变更，请关注*/
	public void programPushMsg(Connection conn,String msgTitle,List<Map<String, Object>> msgContentList, List<Long> groupIdList,long pushUser) throws Exception {
		UserInfo pushObj = UserInfoOperation.getUserInfoByUserId(conn, pushUser);
		//查询所有生管角色
		String userSql="SELECT DISTINCT M.USER_ID, I.USER_REAL_NAME,I.USER_EMAIL"
				+ "  FROM ROLE_USER_MAPPING M, USER_INFO I"
				+ " WHERE M.ROLE_ID = 3"
				+ "   AND M.USER_ID = I.USER_ID";
		Map<Long, UserInfo> userIdList=UserInfoOperation.getUserInfosBySql(conn, userSql);
		for(Long userId:userIdList.keySet()){
			//String pushUserName =userIdList.get(userId).getUserRealName();
			for(Map<String, Object> map:msgContentList){
				//发送消息到消息队列
				String msgContent = (String) map.get("msgContent");
				String msgParam = (String) map.get("msgParam");
				SysMsgPublisher.publishMsg(msgTitle, msgContent, pushUser, new long[]{userId}, 2, msgParam, pushObj.getUserRealName());
			}
		}
		Map<Long, UserInfo> leaderIdByGroupId=null;
		if(groupIdList!=null&&groupIdList.size()>0){
			//查询分配的作业组组长
			leaderIdByGroupId = UserInfoOperation.getLeaderIdByGroupId(conn, groupIdList);
			//分别发送给对应的日编/采集/月编组长
			for(Map<String, Object> map:msgContentList){
				//发送消息到消息队列
				String msgContent = (String) map.get("msgContent");
				String msgParam = (String) map.get("msgParam");
				List<Long> groupIds=(List<Long>) map.get("groupIds");
				for(Long groupId:groupIds){
					//if(Long.valueOf(leaderIdByGroupId.get(groupId).getUserId())==0){continue;}
					try{
						SysMsgPublisher.publishMsg(msgTitle, msgContent, pushUser,new long[]{Long.valueOf(leaderIdByGroupId.get(groupId).getUserId())},
								2, msgParam,pushObj.getUserRealName());
					}catch (Exception e) {
						log.warn("项目推送消息错误，groupId="+groupId, e);
					}
				}
			}
		}
		
		//发送邮件
		String toMail = null;
		String mailTitle = null;
		String mailContent = null;
		//查询用户详情
		for (Long userId : userIdList.keySet()) {
			UserInfo userInfo = userIdList.get(userId);
			if(userInfo.getUserEmail()!= null&&!userInfo.getUserEmail().isEmpty()){
				for (Map<String, Object> map : msgContentList) {
					//判断邮箱格式
					String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
	                Pattern regex = Pattern.compile(check);
	                Matcher matcher = regex.matcher((CharSequence) userInfo.getUserEmail());
	                if(matcher.matches()){
	                	toMail = userInfo.getUserEmail();
	                	mailTitle = msgTitle;
	                	mailContent = (String) map.get("msgContent");
	                	//发送邮件到消息队列
	                	//SendEmail.sendEmail(toMail, mailTitle, mailContent);
	                	EmailPublisher.publishMsg(toMail, mailTitle, mailContent);
	                }
				}
			}
		}
		if(groupIdList!=null&&groupIdList.size()>0){
			//分别发送给对应的日编/采集/月编组长
			for(Map<String, Object> map:msgContentList){
				//发送消息到消息队列
				List<Long> groupIds=(List<Long>) map.get("groupIds");
				for(Long groupId:groupIds){
					//if(Long.valueOf(leaderIdByGroupId.get(groupId).getUserId())==0){continue;}
					UserInfo userInfo = leaderIdByGroupId.get(groupId);
					//判断邮箱格式
					String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
	                Pattern regex = Pattern.compile(check);
	                Matcher matcher = regex.matcher((CharSequence) userInfo.getUserEmail());
	                if(matcher.matches()){
	                	toMail = userInfo.getUserEmail();
	                	mailTitle = msgTitle;
	                	mailContent = (String) map.get("msgContent");
	                	//发送邮件到消息队列
	                	//SendEmail.sendEmail(toMail, mailTitle, mailContent);
	                	EmailPublisher.publishMsg(toMail, mailTitle, mailContent);
	                }
				}
			}
		}
	}
	
	/**
	 * 
	 * @param conn
	 * @param condition 搜索条件{"programIds":[1,2,3]}
	 * @return List<Program>  [{"programId":12,"name":"123"}]
	 */
	public List<Program> queryProgramTable(Connection conn,JSONObject condition) throws Exception{
		
		String conditionSql="";
		if(null!=condition && !condition.isEmpty()){
			Iterator keys = condition.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				
				if ("programIds".equals(key)) {conditionSql+=" AND p.program_id IN ("+condition.getJSONArray(key).join(",")+")";}
				if ("status".equals(key)) {conditionSql+=" AND p.STATUS IN ("+condition.getJSONArray(key).join(",")+")";}
			}
		}
		
		String selectSql="SELECT P.PROGRAM_ID, P.NAME,P.INFOR_ID,P.TYPE,p.collect_plan_start_date,"
				+ "p.collect_plan_end_date,p.day_edit_plan_start_date,p.day_edit_plan_end_date,"
				+ "P.MONTH_EDIT_PLAN_START_DATE,P.MONTH_EDIT_PLAN_END_DATE,"
				+ "P.PLAN_START_DATE,P.PLAN_END_DATE,P.PRODUCE_PLAN_START_DATE,P.PRODUCE_PLAN_END_DATE,p.DESCP  "
				+ "FROM PROGRAM P where p.latest=1 "+conditionSql;
		
		ResultSetHandler<List<Program>> rsHandler = new ResultSetHandler<List<Program>>(){
			public List<Program> handle(ResultSet rs) throws SQLException {
				List<Program> list = new ArrayList<Program>();
				while(rs.next()){
					Program map = new Program();
					map.setProgramId(rs.getInt("PROGRAM_ID"));
					map.setName(rs.getString("NAME"));
					map.setInforId(rs.getInt("INFOR_ID"));
					map.setType(rs.getInt("TYPE"));
					map.setCollectPlanStartDate(rs.getTimestamp("collect_plan_start_date"));
					map.setCollectPlanEndDate(rs.getTimestamp("collect_plan_end_date"));
					map.setDayEditPlanStartDate(rs.getTimestamp("day_edit_plan_start_date"));
					map.setDayEditPlanEndDate(rs.getTimestamp("day_edit_plan_end_date"));
					map.setMonthEditPlanStartDate(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE"));
					map.setMonthEditPlanEndDate(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE"));
					map.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
					map.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
					map.setProducePlanStartDate(rs.getTimestamp("PRODUCE_PLAN_START_DATE"));
					map.setProducePlanEndDate(rs.getTimestamp("PRODUCE_PLAN_END_DATE"));
					map.setDescp(rs.getString("DESCP"));
					
					list.add(map);
				}
				return list;
			}
    	};
		
		QueryRunner run=new QueryRunner();
		return run.query(conn, selectSql, rsHandler);
	}
	
	/**
	 * 
	 * @param conn
	 * @param condition 搜索条件{"programId":[1,2,3]}
	 * @return List<Integer>  [123,456]
	 */
	public Map<Integer,Set<Integer>> queryInforProgramGridById(Connection conn,int programId) throws Exception{
		
		String selectSql="SELECT M.GRID_ID, R.DAILY_DB_ID"
				+ "  FROM PROGRAM_GRID_MAPPING M, GRID G, REGION R"
				+ " WHERE M.PROGRAM_ID = "+programId
				+ "   AND M.GRID_ID = G.GRID_ID"
				+ "   AND G.REGION_ID = R.REGION_ID"
				+ " UNION"
				+ " SELECT G.GRID_ID, R.DAILY_DB_ID"
				+ "  FROM INFOR_GRID_MAPPING I, PROGRAM P, GRID G, REGION R"
				+ " WHERE I.INFOR_ID = P.INFOR_ID"
				+ "   AND P.PROGRAM_ID = "+programId
				+ "   AND I.GRID_ID = G.GRID_ID"
				+ "   AND G.REGION_ID = R.REGION_ID";
		
		ResultSetHandler<Map<Integer,Set<Integer>>> rsHandler = new ResultSetHandler<Map<Integer,Set<Integer>>>(){
			public Map<Integer,Set<Integer>> handle(ResultSet rs) throws SQLException {
				Map<Integer,Set<Integer>> map=new HashMap<Integer, Set<Integer>>();
				while(rs.next()){
					int dbId = rs.getInt("DAILY_DB_ID");
					if(!map.containsKey(dbId)){map.put(dbId, new HashSet<Integer>());}
					map.get(dbId).add(rs.getInt("GRID_ID"));
				}
				return map;
			}
    	};
		
		QueryRunner run=new QueryRunner();
		return run.query(conn, selectSql, rsHandler);
	}
	
	public void openStatus(Connection conn,JSONArray programIds) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String updateSql="UPDATE PROGRAM SET STATUS=1 WHERE PROGRAM_ID IN ("+programIds.join(",")+")";
			run.update(conn,updateSql);			
			updateSql="UPDATE CITY SET PLAN_STATUS=3 WHERE CITY_ID IN ("
						+ "SELECT CITY_ID FROM PROGRAM WHERE CITY_ID！=0 AND PROGRAM_ID IN ("+programIds.join(",")+"))";
			run.update(conn,updateSql);	
			updateSql="UPDATE INFOR SET PLAN_STATUS=3 WHERE INFOR_ID IN ("
					+ "SELECT INFOR_ID FROM PROGRAM WHERE INFOR_ID IS NOT NULL AND PROGRAM_ID IN ("+programIds.join(",")+"))";
			run.update(conn,updateSql);	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public int getNewProgramId(Connection conn) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();

			String querySql = "select PROGRAM_SEQ.NEXTVAL as programId from dual";

			int programId = Integer.valueOf(run
					.query(conn, querySql, new MapHandler()).get("programId")
					.toString());
			return programId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * 调整项目范围
	 * @param conn
	 * @param programId
	 * @param gridIdMap:<gridId,类型>
	 * @throws Exception 
	 */
	public void updateProgramRegion(Connection conn, Integer programId, Map<Integer, Integer> gridIdMap) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			String sql = "insert into PROGRAM_GRID_MAPPING (PROGRAM_ID, GRID_ID,TYPE) VALUES (?,?,?)";
			Object[][] inParam = new Object[gridIdMap.size()][];
			int i = 0;
			for(Map.Entry<Integer, Integer> entry:gridIdMap.entrySet()){
				Object[] temp = new Object[3];
				temp[0] = programId;
				temp[1] = entry.getKey();
				temp[2] = entry.getValue();
				inParam[i] = temp;
				i++;
			}
			log.info("调整项目范围:" + sql);
			run.batch(conn, sql, inParam);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("插入失败，原因为:"+e.getMessage(),e);
		}
		
	}

	/**
	 * @param cityId
	 * @return
	 * @throws Exception 
	 */
	public Program getProgramByCityId(int cityId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run=new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT P.PROGRAM_ID,P.NAME,P.TYPE FROM PROGRAM P WHERE P.LATEST = 1 AND P.CITY_ID = " + cityId);
			String selectSql= sb.toString();
			log.info("getProgramByCityId sql :" + selectSql);

			ResultSetHandler<Program> rsHandler = new ResultSetHandler<Program>() {
				public Program handle(ResultSet rs) throws SQLException {
					Program program = new Program();
					program.setProgramId(0);
					program.setName("");
					program.setType(0);
					if (rs.next()) {
						program.setProgramId(rs.getInt("PROGRAM_ID"));
						program.setName(rs.getString("NAME"));
						program.setType(rs.getInt("TYPE"));
					}
					return program;
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
	 * 调整子任务对应快线项目范围.若子任务非快线项目，则不进行调整
	 * @param conn
	 * @param taskId
	 * @throws Exception
	 */
	public int changeProgramGridByTask(Connection conn, int taskId) throws Exception {
		try{
			QueryRunner run=new QueryRunner();
			String sql="INSERT INTO PROGRAM_GRID_MAPPING"
					+ "  (PROGRAM_ID, GRID_ID, TYPE)"
					+ "  SELECT S.PROGRAM_ID, GRID_ID, 2"
					+ "    FROM TASK_GRID_MAPPING M, TASK S"
					+ "   WHERE M.TASK_ID = "+taskId
					+ "     AND S.TASK_ID = M.TASK_ID"
					+ "     AND S.BLOCK_ID = 0"
					+ "  MINUS (SELECT P.PROGRAM_ID, M.GRID_ID, 2"
					+ "           FROM INFOR_GRID_MAPPING M, TASK T, PROGRAM P"
					+ "          WHERE M.INFOR_ID = P.INFOR_ID"
					+ "            AND P.PROGRAM_ID = T.PROGRAM_ID"
					+ "            AND T.TASK_ID = "+taskId
					+ "         UNION ALL"
					+ "         SELECT T.PROGRAM_ID, M.GRID_ID, 2"
					+ "           FROM PROGRAM_GRID_MAPPING M, TASK T"
					+ "          WHERE M.PROGRAM_ID = T.PROGRAM_ID"
					+ "            AND T.TASK_ID = "+taskId+")";
			return run.update(conn, sql);	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * @param conn
	 * @param program
	 * @throws Exception 
	 */
	public void createWithProgramId(Connection conn, Program bean) throws Exception {
		QueryRunner run = new QueryRunner();
		//将旧项目状态修改为失效
		String updateSql = "UPDATE PROGRAM SET LATEST=0 WHERE ";
		if (bean!=null&&bean.getCityId()!=0){
			updateSql+=" CITY_ID ="+bean.getCityId();
		};

		if (bean!=null&&bean.getInforId()!=0){
			updateSql+=" infor_id ='" + bean.getInforId() + "'";
		};
		run.update(conn,updateSql);
		
		//创建项目		
		String insertPart="";
		String valuePart="";
		if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
		insertPart+=" PROGRAM_ID ";
		valuePart+=bean.getProgramId();			

		if (bean!=null&&bean.getCityId()!=0){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" CITY_ID ";
			valuePart+=bean.getCityId();
		};

		if (bean!=null&&bean.getInforId()!=0){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" infor_id ";
			valuePart+= "'" + bean.getInforId() + "'";
		};
		if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
		insertPart+=" CREATE_USER_ID,CREATE_DATE,STATUS,LATEST ";
		valuePart+=bean.getCreateUserId()+",sysdate,2,1";

		if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" name ";
			valuePart+="'"+bean.getName()+"'";
		};
		if (bean!=null&&bean.getType()!=0){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" type ";
			valuePart+=bean.getType();
		};

		if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" DESCP ";
			valuePart+="'"+bean.getDescp()+"'";
		};
		if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" PLAN_START_DATE ";
			valuePart+="to_timestamp('"+ bean.getPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" PLAN_END_DATE ";
			valuePart+="to_timestamp('"+ bean.getPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getCollectPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanStartDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" Collect_PLAN_START_DATE ";
			valuePart+="to_timestamp('"+ bean.getCollectPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getCollectPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanEndDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" Collect_PLAN_END_DATE";
			valuePart+="to_timestamp('"+ bean.getCollectPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getDayEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanStartDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" day_EDIT_PLAN_START_DATE ";
			valuePart+="to_timestamp('"+ bean.getDayEditPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getDayEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanEndDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" Day_EDIT_PLAN_END_DATE";
			valuePart+="to_timestamp('"+ bean.getDayEditPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getMonthEditPlanStartDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" MONTH_EDIT_PLAN_START_DATE ";
			valuePart+="to_timestamp('"+ bean.getMonthEditPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getMonthEditPlanEndDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" MONTH_EDIT_PLAN_END_DATE";
			valuePart+="to_timestamp('"+ bean.getMonthEditPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getProducePlanStartDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" Produce_Plan_Start_Date ";
			valuePart+="to_timestamp('"+ bean.getProducePlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};
		if (bean!=null&&bean.getProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getProducePlanEndDate().toString())){
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" Produce_Plan_End_Date";
			valuePart+="to_timestamp('"+ bean.getProducePlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
		};

		String createSql = "insert into program ("+insertPart+") values("+valuePart+")";
		run.update(conn,createSql);
		//修改项目对应city的状态为已规划
		if (bean!=null&&bean.getCityId()!=0){
			CityOperation.updatePlanStatus(conn, bean.getCityId(), 1);
		};
		//修改项目对应情报为已规划
		if (bean!=null&&bean.getInforId()!=0){
			InforManOperation.updatePlanStatus(conn,bean.getInforId(),1);
		};	
	}
	
	public Program queryProgramByTaskId(Connection conn,int taskId) throws Exception{
		String sql="select p.* from program p,task t where p.program_id=t.program_id"
				+ " and t.task_id="+taskId;
		QueryRunner run = new QueryRunner();
		log.info("queryProgramByTaskId:"+sql);
		return run.query(conn, sql, new ResultSetHandler<Program>(){

			@Override
			public Program handle(ResultSet rs) throws SQLException {
				if(rs.next()){
					Program program=new Program();
					program.setProgramId(rs.getInt("program_id"));
					program.setInforId(rs.getInt("infor_id"));
					program.setCityId(rs.getInt("city_id"));
					program.setType(rs.getInt("type"));
					return program;
				}
				return null;
			}});
	}
	
	/**
	 * 获取待数据规划项目列表
	 * 应用场景：中线项目下，具有同时满足草稿状态+未进行数据规划的采集任务的项目列表
	 * @author songhe
	 * @return List
	 * @throws SQLException 
	 */
	public List<Map<String, Object>> unPlanlist(JSONObject json) throws SQLException{
		Connection con = null;
		try{
			con = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			StringBuffer sb = new StringBuffer();
			//未规划草稿状态
			sb.append("select distinct p.name, t.program_id,r.daily_db_id from PROGRAM p, TASK t,region r where t.data_plan_status = 0 ");
			//中线采集任务
			sb.append("and p.type = 1 and t.type = 0 and t.work_kind like '%1|%'");
			sb.append("and r.region_id = t.region_id ");
			sb.append("and t.program_id = p.program_id");
			if(json.containsKey("name") && json.getString("name").length() > 0){
				String name = json.getString("name");
				sb.append(" and p.name like '%"+name+"%'");
			}
			String sql = sb.toString();
			ResultSetHandler<List<Map<String, Object>>> rs = new ResultSetHandler<List<Map<String, Object>>>(){
			@Override
			public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
				while(rs.next()){
					Map<String, Object> map = new HashMap<>();
					map.put("programId", rs.getInt("program_id"));
					map.put("name", rs.getString("name"));
					map.put("dbId",rs.getInt("daily_db_id"));
					result.add(map);
				}
				return result;
			}
		};
		log.info("获取待数据规划项目列表SQL:"+ sql);
		return run.query(con, sql, rs);
		}catch(Exception e){
			DbUtils.rollback(con);
			throw e;
		}finally{
			DbUtils.close(con);
		}
	}
	
	
	/**
	 * 获取质检子任务的项目列表
	 * @return
	 * @throws Exception
	 */
	public JSONObject unPlanQualitylist() throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run=new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT DISTINCT R.DAILY_DB_ID, P.PROGRAM_ID,P.NAME FROM REGION R, PROGRAM P, TASK T, SUBTASK S");
			sb.append(" WHERE t.REGION_ID = R.REGION_ID AND P.TYPE = 1 AND P.PROGRAM_ID = T.PROGRAM_ID  AND T.TASK_ID = S.TASK_ID");
			sb.append(" AND T.TYPE = 0 AND T.DATA_PLAN_STATUS = 1 AND S.STATUS IN (1, 2) AND S.IS_QUALITY = 1 ");
			sb.append(" AND S.REFER_ID != 0 AND S.QUALITY_PLAN_STATUS = 0 ");
			
			String selectSql= sb.toString();
			log.info("unPlanQualitylist sql :" + selectSql);

			ResultSetHandler<JSONObject> rsHandler = new ResultSetHandler<JSONObject>() {
				public JSONObject handle(ResultSet rs) throws SQLException {
					JSONObject jsonObject = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					while (rs.next()) {
						JSONObject jo = new JSONObject();
						jo.put("programId", rs.getInt("PROGRAM_ID"));
						jo.put("name", rs.getString("NAME"));
						jo.put("dbId", rs.getString("DAILY_DB_ID"));
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
	
	//获取待规划子任务的项目列表
	public JSONObject unPlanSubtasklist() throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT DISTINCT R.DAILY_DB_ID, P.PROGRAM_ID, P.NAME FROM REGION R, TASK T, PROGRAM P ");
			sb.append("WHERE t.REGION_ID = R.REGION_ID AND T.PROGRAM_ID = P.PROGRAM_ID AND P.TYPE = 1 AND T.TYPE = 0 ");
			sb.append("AND T.STATUS IN (1, 2) AND T.DATA_PLAN_STATUS = 1");
			
			String selectSql= sb.toString();
			log.info("unPlanSubtasklist sql :" + selectSql);

			ResultSetHandler<JSONObject> rsHandler = new ResultSetHandler<JSONObject>() {
				public JSONObject handle(ResultSet rs) throws SQLException {
					JSONObject jsonObject = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					while(rs.next()){
						JSONObject jo = new JSONObject();
						jo.put("programId", rs.getInt("PROGRAM_ID"));
						jo.put("name", rs.getString("NAME"));
						jo.put("dbId", rs.getInt("DAILY_DB_ID"));
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
	 * 根据子任务获取同项目下的粗编子任务列表
	 * @param int
	 * @throws Exception 
	 * 
	 * */
	public List<Integer> queryRudeSubTaskBySubTask(int subTaskId) throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			StringBuffer sb = new StringBuffer();
			sb.append("select st.subtask_id from  SUBTASK st where st.task_id in (");
			sb.append("select t.task_id from TASK t where t.program_id =(");
			sb.append("SELECT T.PROGRAM_ID FROM SUBTASK S, TASK T, PROGRAM P");
			sb.append(" WHERE S.TASK_ID = T.TASK_ID AND T.PROGRAM_ID = P.PROGRAM_ID");
			sb.append(" AND S.SUBTASK_ID = "+subTaskId+"))");
			sb.append(" and st.type = 4");
			
			log.info("queryRudeSubTaskBySubTask sql :" + sb.toString());

			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> result = new ArrayList<Integer>();
					while(rs.next()){
						result.add(rs.getInt("subtask_id"));
					}
					return result;
				}
			};
			return run.query(conn, sb.toString(), rsHandler);	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询项目下的统计信息
	 * @throws Exception 
	 * 
	 * */
	public List<Map<String, Object>> queryProgramStat() throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			StringBuffer sb = new StringBuffer();
			sb.append("select t.type, t.program_id, t.city_id, t.infor_id, c.plan_status city_plan, i.is_adopted, i.deny_reason,");
			sb.append(" i.plan_status infor_plan, t.status, i.info_type_name, i.method,p.create_date produce_date, i.admin_name,");
			sb.append(" p.produce_status, ft.diff_date, t.produce_plan_end_date, ft.type tasktype, tk.overdue_reason, b.plan_status,");
			sb.append("  i.insert_time, i.expect_date, tk.create_date taskcreatdate, t.create_date programcreatdate, t.name programname");
			sb.append(" from PROGRAM t, PRODUCE p, FM_STAT_OVERVIEW_TASK ft, INFOR i, CITY c, TASK tk, BLOCK b");
			sb.append(" where t.program_id = p.program_id(+) and t.program_id = ft.program_id(+)");
			sb.append(" and t.city_id = c.city_id(+) and t.infor_id = i.infor_id(+) and ft.task_id = tk.task_id(+) and tk.block_id = b.block_id(+)");
			
			log.info("queryProgramStat sql :" + sb.toString());

			ResultSetHandler<List<Map<String, Object>>> rsHandler = new ResultSetHandler<List<Map<String, Object>>>() {
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> result = new ArrayList<>();
					while(rs.next()){
						Map<String, Object> program = new HashMap<>();
						program.put("programId", rs.getInt("program_id"));
						program.put("cityId", rs.getInt("city_id"));
						program.put("inforId", rs.getInt("infor_id"));
						program.put("cityPlan", rs.getInt("city_plan"));
						program.put("inforPlan", rs.getInt("infor_plan"));
						program.put("status", rs.getInt("status"));
						program.put("inforTypeName", rs.getString("info_type_name"));
						program.put("method", rs.getString("method"));
						program.put("produceStatus", rs.getInt("produce_status"));
						program.put("diffDate", rs.getInt("diff_date"));
						program.put("producePlanEndDate", rs.getTimestamp("produce_plan_end_date"));
						program.put("type", rs.getInt("type"));
						program.put("produceDate", rs.getTimestamp("produce_date"));
						program.put("overdue_reason", rs.getString("overdue_reason"));
						program.put("taskType", rs.getInt("tasktype"));
						
						program.put("isAdopted", rs.getInt("is_adopted"));
						String adminName = rs.getString("admin_name");
						String inforCity = "";
						if(adminName != null){
							inforCity = adminName.substring(adminName.indexOf("|")+1, adminName.length());
						}
						program.put("inforCity", inforCity);
						program.put("denyReason", rs.getString("deny_reason"));
						program.put("planStatus", rs.getInt("plan_status"));
						
						program.put("inforInsertTime", rs.getTimestamp("insert_time"));
						program.put("inforExpectDate", rs.getTimestamp("expect_date"));
						program.put("taskCreateDate", rs.getTimestamp("taskcreatdate"));
						program.put("createDate", rs.getTimestamp("programcreatdate"));
						program.put("name", rs.getString("programname"));
						result.add(program);
					}
					return result;
				}
			};
			return run.query(conn, sb.toString(), rsHandler);	
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
//	/**
//	 * 根据项目id自动给月编任务的作业组赋值
//	 * @param int programId
//	 * @param Connection
//	 * @param Task
//	 * 
//	 * */
//	public void updateMonthTaskGroupIdByProgram(Connection conn, int programId, Task monthTask){
//		//modify by songhe
//		//月编任务创建自动赋值作业组id
//		try {
//			String adminCode = TaskService.getInstance().selectAdminCode(conn, programId);
//			UserGroup userGrop = UserGroupService.getInstance().getGroupByAminCode(conn, adminCode, 6);
//			monthTask.setGroupId(userGrop.getGroupId());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

}
