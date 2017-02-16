package com.navinfo.dataservice.engine.man.program;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Program;
import com.navinfo.dataservice.api.man.model.Task;
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
import com.navinfo.dataservice.engine.man.inforMan.InforManOperation;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

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
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			Program bean = (Program) JsonOperation.jsonToBean(dataJson,Program.class);
			bean.setCreateUserId(Integer.valueOf(String.valueOf(userId)));
			
			String updateSql = "UPDATE PROGRAM SET LATEST=0 WHERE ";
			if (bean!=null&&bean.getCityId()!=0){
				updateSql+=" CITY_ID ="+bean.getCityId();
			};
			
			if (bean!=null&&bean.getInforId()!=null && StringUtils.isNotEmpty(bean.getInforId().toString())){
				updateSql+=" infor_id ='" + bean.getInforId() + "'";
			};
			run.update(conn,updateSql);
			
			bean.setProgramId(getNewProgramId(conn));
			
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
			
			if (bean!=null&&bean.getInforId()!=null && StringUtils.isNotEmpty(bean.getInforId().toString())){
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
			if (bean!=null&&bean.getLot()!=0){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" lot ";
				valuePart+=bean.getLot();
			};
			String createSql = "insert into program ("+insertPart+") values("+valuePart+")";
			run.update(conn,createSql);
			
			if (bean!=null&&bean.getCityId()!=0){
				CityOperation.updatePlanStatus(conn, bean.getCityId(), 1);
			};
			
			if (bean!=null&&bean.getInforId()!=null && StringUtils.isNotEmpty(bean.getInforId().toString())){
				InforManOperation.updatePlanStatus(conn,bean.getInforId(),1);
			};		
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
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
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";setPart+=" , ";}
				setPart+=" Produce_Plan_Start_Date =to_timestamp('"+ bean.getProducePlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getProducePlanEndDate().toString())){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" Produce_Plan_End_Date=to_timestamp('"+ bean.getProducePlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getLot()!=0){
				if(StringUtils.isNotEmpty(setPart)){setPart+=" , ";}
				setPart+=" lot ="+bean.getLot();
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
		String querySql="SELECT GROUP_ID FROM TASK T WHERE T.STATUS= ? AND T.PROGRAM_ID = ? and T.LATEST=1";
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
			String selectSql="SELECT TASK_ID  FROM TASK WHERE PROGRAM_ID = "+programId+" AND STATUS != 0";
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
					+ " WHERE CITY_ID IN (SELECT CITY_ID FROM PROGRAM WHERE PROGRAM_ID = 0)";
			run.update(conn,updateSql);
			
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
					+ "   ORDER BY PROGRAM_LIST.CITY_NAME DESC)"
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
					+ "       F.DIFF_DATE,"
					+ "       P.PLAN_START_DATE,"
					+ "       P.PLAN_END_DATE,"
					+ "       0                   COLLECT_STAT,"
					+ "       0                   DAILY_STAT,"
					+ "       0                   MONTHLY_STAT,"
					+ "       F.ACTUAL_START_DATE,"
					+ "       F.ACTUAL_END_DATE,"
					+ "       F.COLLECT_PERCENT,"
					+ "       F.COLLECT_PROGRESS,"
					+ "       F.DAILY_PERCENT,"
					+ "       F.DAILY_PROGRESS,"
					+ "       F.MONTHLY_PERCENT,"
					+ "       F.MONTHLY_PROGRESS,"
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
					+ "       F.COLLECT_PERCENT,"
					+ "       F.COLLECT_PROGRESS,"
					+ "       F.DAILY_PERCENT,"
					+ "       F.DAILY_PROGRESS,"
					+ "       F.MONTHLY_PERCENT,"
					+ "       F.MONTHLY_PROGRESS,"
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
					+ "   ORDER BY PROGRAM_LIST.INFOR_NAME DESC)"
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
						map.put("inforName", rs.getString("INFOR_NAME"));}
					map.put("planStatus", rs.getInt("PLAN_STATUS"));
					map.put("type", rs.getInt("TYPE"));
					map.put("status", rs.getInt("STATUS"));
					map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
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
					+ "       F.PERCENT,"
					+ "       F.DIFF_DATE,"
					+ "       P.PLAN_START_DATE,"
					+ "       P.PLAN_END_DATE,"
					+ "       0                   COLLECT_STAT,"
					+ "       0                   DAILY_STAT,"
					+ "       0                   MONTHLY_STAT,"
					+ "       F.ACTUAL_START_DATE,"
					+ "       F.ACTUAL_END_DATE,"
					+ "       F.COLLECT_PERCENT,"
					+ "       F.COLLECT_PROGRESS,"
					+ "       F.DAILY_PERCENT,"
					+ "       F.DAILY_PROGRESS"
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
					+ "       F.COLLECT_PERCENT,"
					+ "       F.COLLECT_PROGRESS,"
					+ "       F.DAILY_PERCENT,"
					+ "       F.DAILY_PROGRESS"
					+ "  FROM INFOR C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F,TASK T"
					+ " WHERE C.INFOR_ID = P.INFOR_ID"
					+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
					+ "   AND T.PROGRAM_ID = P.PROGRAM_ID"
					+ "   AND P.LATEST = 1"
					+ "   AND P.STATUS = 1"
					+ "   AND EXISTS (SELECT 1"
					+ "          FROM TASK T"
					+ "         WHERE T.PROGRAM_ID = P.PROGRAM_ID"
					+ "           AND T.LATEST = 1"
					+ "           AND T.STATUS = 1)"
					+ "   GROUP BY P.PROGRAM_ID,P.NAME,P.TYPE,C.INFOR_NAME,C.INFOR_ID,F.PERCENT,F.DIFF_DATE,"
					+ "            P.PLAN_START_DATE,P.PLAN_END_DATE,F.ACTUAL_START_DATE,F.ACTUAL_END_DATE,"
					+ "            F.COLLECT_PERCENT,F.COLLECT_PROGRESS,F.DAILY_PERCENT,F.DAILY_PROGRESS),"
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
					+ "                F.PERCENT,"
					+ "                F.DIFF_DATE,"
					+ "                P.PLAN_START_DATE,"
					+ "                P.PLAN_END_DATE,"
					+ "                F.ACTUAL_START_DATE,"
					+ "                F.ACTUAL_END_DATE"
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
						map.put("inforName", rs.getString("INFOR_NAME"));}
					map.put("type", rs.getInt("TYPE"));
					map.put("status", 1);					
					map.put("percent", rs.getInt("PERCENT"));
					map.put("diffDate", rs.getInt("DIFF_DATE"));
					map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
					map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
					map.put("actualStartDate", DateUtils.dateToString(rs.getTimestamp("ACTUAL_START_DATE")));
					map.put("actualEndDate", DateUtils.dateToString(rs.getTimestamp("ACTUAL_END_DATE")));
					map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
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
					+ "       F.DIFF_DATE,"
					+ "       P.PLAN_START_DATE,"
					+ "       P.PLAN_END_DATE,"
					+ "       F.ACTUAL_START_DATE,"
					+ "       F.ACTUAL_END_DATE"
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
						map.put("inforName", rs.getString("INFOR_NAME"));}
					map.put("type", rs.getInt("TYPE"));
					map.put("status", 0);				
					map.put("diffDate", rs.getInt("DIFF_DATE"));
					map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
					map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
					map.put("actualStartDate", DateUtils.dateToString(rs.getTimestamp("ACTUAL_START_DATE")));
					map.put("actualEndDate", DateUtils.dateToString(rs.getTimestamp("ACTUAL_END_DATE")));
					map.put("version", version);
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
	
	public Page commonList(Connection conn,int planStatus, JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize)throws Exception{
		//常规未发布
		Page page = new Page();
		if(planStatus==1){
			page=commonUnPushList(conn,conditionJson,currentPageNum,pageSize);
		}else if(planStatus==2){
			//常规已发布
			page=commonPushList(conn,conditionJson,currentPageNum,pageSize);
		}else if(planStatus==3){
			//常规已完成
			page=commonOverList(conn,conditionJson,currentPageNum,pageSize);
		}else if(planStatus==4){
			//常规已关闭
			page=commonCloseList(conn,conditionJson,currentPageNum,pageSize);
		}
		return page;
	}
	
	public Page inforList(Connection conn,int planStatus, JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize)throws Exception{
		//情报未发布
		Page page = new Page();
		if(planStatus==1){
			page=inforUnPushList(conn,conditionJson,currentPageNum,pageSize);
		}else if(planStatus==2){
			//情报已发布
			page=inforPushList(conn,conditionJson,currentPageNum,pageSize);
		}else if(planStatus==3){
			//情报已完成
			page=inforOverList(conn,conditionJson,currentPageNum,pageSize);
		}else if(planStatus==4){
			//情报已关闭
			page=inforCloseList(conn,conditionJson,currentPageNum,pageSize);
		}
		return page;
	}
		
	public Page list(int type, int planStatus, JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			if(type==4){
				//情报任务查询列表
				return this.inforList(conn,planStatus, conditionJson, orderJson, currentPageNum, pageSize);
			}else{
				//常规任务查询列表
				return this.commonList(conn,planStatus, conditionJson, orderJson, currentPageNum, pageSize);
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
			String sql="SELECT P.PROGRAM_ID,"
					+ "         P.NAME                       PROGRAM_NAME,"
					+ "         P.DESCP                      PROGRAM_DESCP,"
					+ "         P.TYPE,"
					+ "         C.CITY_NAME,"
					+ "         C.CITY_ID,"
					+ "         I.INFOR_ID,"
					+ "         I.INFOR_NAME,"
					+ "         P.CREATE_USER_ID,"
					+ "         U.USER_REAL_NAME             CREATE_USER_NAME,"
					+ "         P.PLAN_START_DATE,"
					+ "         P.PLAN_END_DATE,"
					+ "         P.COLLECT_PLAN_START_DATE,"
					+ "         P.COLLECT_PLAN_END_DATE,"
					+ "         P.DAY_EDIT_PLAN_START_DATE,"
					+ "         P.DAY_EDIT_PLAN_END_DATE,"
					+ "         P.MONTH_EDIT_PLAN_START_DATE,"
					+ "         P.MONTH_EDIT_PLAN_END_DATE,"
					+ "         P.PRODUCE_PLAN_START_DATE,"
					+ "         P.PRODUCE_PLAN_END_DATE"
					+ "    FROM CITY C, PROGRAM P, USER_INFO U, INFOR I"
					+ "   WHERE C.CITY_ID(+) = P.CITY_ID"
					+ "     AND I.INFOR_ID(+) = P.INFOR_ID"
					+ "     AND P.LATEST = 1"
					+ "     AND P.CREATE_USER_ID = U.USER_ID"
					+ "     AND P.PROGRAM_ID = "+programId;
			ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>(){
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					Map<String, Object> map = new HashMap<String, Object>();
					while(rs.next()){
						map.put("programId", rs.getInt("PROGRAM_ID"));
						map.put("name", rs.getString("PROGRAM_NAME"));
						map.put("descp", rs.getString("PROGRAM_DESCP"));
						map.put("type", rs.getInt("TYPE"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("cityName", rs.getString("CITY_NAME"));
						map.put("inforId", rs.getString("INFOR_ID"));
						map.put("inforName", rs.getString("INFOR_NAME"));
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
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
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
			List<Integer> inforPrograms=new ArrayList<Integer>();
			for(Program p:programs){
				if(p.getType()==4){
					inforPrograms.add(p.getProgramId());
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
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("任务发布消息发送失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		return "项目批量发布"+programIds.size()+"个成功，0个失败";
		
	}
	/**
	 * 情报项目发布时，自动创建两条任务：采集任务和日编任务，均为草稿状态（若情报跨大区，则按照大区拆分成多个任务）
	 * @param conn
	 * @param programIds
	 * @param userId
	 * @throws Exception
	 */
	private void splitInforTasks(Connection conn,List<Integer> programIds,final Long userId)throws Exception{
		try{
			if(programIds==null||programIds.size()==0){return;}
			String selectSql="SELECT P.PROGRAM_ID, M.GRID_ID, G.REGION_ID"
					+ "  FROM PROGRAM P, INFOR_GRID_MAPPING M, GRID G"
					+ " WHERE P.INFOR_ID = M.INFOR_ID"
					+ "   AND M.GRID_ID = G.GRID_ID"
					+ "   AND P.PROGRAM_ID IN "+programIds.toString().replace("[", "(").replace("]", ")")
					+ " ORDER BY P.PROGRAM_ID, G.REGION_ID";
			
			ResultSetHandler<List<Task>> rsHandler = new ResultSetHandler<List<Task>>(){
				public List<Task> handle(ResultSet rs) throws SQLException {
					List<Task> list = new ArrayList<Task>();
					Map<Integer, Integer> gridMap =new HashMap<Integer, Integer>();
					int programId=0;
					int regionId=0;
					while(rs.next()){
						int programIdTmp=rs.getInt("PROGRAM_ID");
						int regionIdTmp=rs.getInt("REGION_ID");
						if(programId!=programIdTmp||regionId!=regionIdTmp){
							Task collectTask=new Task();
							collectTask.setProgramId(programId);
							collectTask.setRegionId(regionId);
							collectTask.setGridIds(gridMap);
							collectTask.setCreateUserId(Integer.valueOf(userId.toString()));
							collectTask.setType(0);
							list.add(collectTask);
							Task dailyTask=new Task();
							dailyTask.setProgramId(programId);
							dailyTask.setRegionId(regionId);
							dailyTask.setGridIds(gridMap);
							dailyTask.setCreateUserId(Integer.valueOf(userId.toString()));
							dailyTask.setType(1);
							list.add(dailyTask);
							gridMap =new HashMap<Integer, Integer>();
							programId=programIdTmp;
							regionId=regionIdTmp;
						}
						gridMap.put(rs.getInt("GRID_ID"), 1);
					}
					if(programId!=0){
						Task collectTask=new Task();
						collectTask.setProgramId(programId);
						collectTask.setRegionId(regionId);
						collectTask.setGridIds(gridMap);
						collectTask.setCreateUserId(Integer.valueOf(userId.toString()));
						collectTask.setType(0);
						list.add(collectTask);
						Task dailyTask=new Task();
						dailyTask.setProgramId(programId);
						dailyTask.setRegionId(regionId);
						dailyTask.setGridIds(gridMap);
						dailyTask.setCreateUserId(Integer.valueOf(userId.toString()));
						dailyTask.setType(1);
						list.add(dailyTask);
					}
					return list;
				}
	    	};			
			QueryRunner run=new QueryRunner();
			List<Task> list=run.query(conn, selectSql, rsHandler);
			if(list!=null&&list.size()>0){
				for(Task t:list){TaskService.getInstance().createWithBean(conn, t);}
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("任务发布消息发送失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/*项目创建/编辑/关闭
	 * 1.所有生管角色
	 * 2.2.项目包含的所有任务作业组组长
	 * 项目:XXX(任务名称)内容发生变更，请关注*/
	public void programPushMsg(Connection conn,String msgTitle,List<Map<String, Object>> msgContentList, List<Long> groupIdList,long pushUser) throws Exception {
		//查询所有生管角色
		String userSql="SELECT DISTINCT M.USER_ID, I.USER_REAL_NAME,I.USER_EMAIL"
				+ "  FROM ROLE_USER_MAPPING M, USER_INFO I"
				+ " WHERE M.ROLE_ID = 3"
				+ "   AND M.USER_ID = I.USER_ID";
		Map<Long, UserInfo> userIdList=UserInfoOperation.getUserInfosBySql(conn, userSql);
		for(Long userId:userIdList.keySet()){
			String pushUserName =userIdList.get(userId).getUserRealName();
			for(Map<String, Object> map:msgContentList){
				//发送消息到消息队列
				String msgContent = (String) map.get("msgContent");
				String msgParam = (String) map.get("msgParam");
				SysMsgPublisher.publishMsg(msgTitle, msgContent, pushUser, new long[]{userId}, 2, msgParam, pushUserName);
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
					SysMsgPublisher.publishMsg(msgTitle, msgContent, pushUser,new long[]{Long.valueOf(leaderIdByGroupId.get(groupId).getUserId())},
							2, msgParam,leaderIdByGroupId.get(groupId).getUserRealName());
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
		
		String selectSql="SELECT P.PROGRAM_ID, P.NAME,P.INFOR_ID,P.TYPE FROM PROGRAM P where 1=1 "+conditionSql;
		
		ResultSetHandler<List<Program>> rsHandler = new ResultSetHandler<List<Program>>(){
			public List<Program> handle(ResultSet rs) throws SQLException {
				List<Program> list = new ArrayList<Program>();
				while(rs.next()){
					Program map = new Program();
					map.setProgramId(rs.getInt("PROGRAM_ID"));
					map.setName(rs.getString("NAME"));
					map.setInforId(rs.getString("INFOR_ID"));
					map.setType(rs.getInt("TYPE"));
					list.add(map);
				}
				return list;
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
}
