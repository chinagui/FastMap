package com.navinfo.dataservice.engine.man.message;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.xinge.XingeUtil;
import com.navinfo.dataservice.engine.man.userDevice.UserDeviceService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class MessageService {

	private Logger log = LoggerRepos.getLogger(this.getClass());
	private MessageService() {
	}

	private static class SingletonHolder {
		private static final MessageService INSTANCE = new MessageService();
	}

	public static MessageService getInstance() {
		return SingletonHolder.INSTANCE;
	}
	//获取消息列表
	public Map<String,Object> list(long userId,int status) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			String selectSql = "SELECT M.MSG_ID, M.MSG_TITLE, M.MSG_CONTENT, M.PUSH_USER, M.PUSH_TIME, M.MSG_STATUS,U.USER_REAL_NAME"
					+ " FROM MESSAGE M, USER_INFO U"
					+ " WHERE U.USER_ID = M.PUSH_USER"
					+ " AND M.MSG_RECERVER = " + userId
					+ " AND M.MSG_STATUS =" + status
					+ " ORDER BY M.MSG_ID";

			ResultSetHandler<List<Object>> rsHandler = new ResultSetHandler<List<Object>>() {
				public List<Object> handle(ResultSet rs) throws SQLException {
					List<Object> result = new ArrayList<Object>();
					while (rs.next()) {
						Map<Object,Object> msg = new HashMap<Object,Object>();
						msg.put("msgId", rs.getInt("MSG_ID"));
						msg.put("msgTitle", rs.getString("MSG_TITLE"));
						msg.put("msgContent", rs.getString("MSG_CONTENT"));
						msg.put("time", rs.getString("PUSH_TIME"));
						msg.put("status", rs.getInt("MSG_STATUS"));
						msg.put("pushUser", rs.getString("USER_REAL_NAME"));
						
						result.add(msg);
					}
					return result;
				}
			};

			List<Object> data = run.query(conn, selectSql,rsHandler);
			Map<String,Object> result = new HashMap<String,Object>();
			result.put("data", data);
			result.put("total",data.size());
			return result;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	//创建消息
	public void push(Message message,Integer push) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
			/*
			int msgId = MessageOperation.getMessageId(conn);
			
			message.setMsgId(msgId);
			if(message.getMsgStatus()== null){
				message.setMsgStatus(0);
			}
			*/
			//推送消息
			if(1 == push){
				UserDeviceService userDeviceService=new UserDeviceService();
				userDeviceService.pushMessage(message.getReceiverId(), message.getMsgTitle(), message.getMsgContent(), 
						XingeUtil.PUSH_MSG_TYPE_PROJECT, "");
			}
			// 插入message
			else{
				//message.setMsgId(MessageOperation.getMessageId(conn));
				MessageOperation.insertMessage(conn, message);
			}

			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("插入消息失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * @param msgId
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> query(int msgId) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			String selectSql = "SELECT M.MSG_ID, M.MSG_TITLE, M.MSG_CONTENT, M.PUSH_USER, M.PUSH_TIME, M.MSG_STATUS,M.MSG_RECERVER,U.USER_REAL_NAME"
					+ " FROM MESSAGE M, USER_INFO U"
					+ " WHERE U.USER_ID = M.PUSH_USER"
					+ " AND M.MSG_ID = " + msgId;

			ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> msg = new HashMap<String,Object>();
					//SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					if (rs.next()) {
						msg.put("msgId", rs.getInt("MSG_ID"));
						msg.put("msgTitle", rs.getString("MSG_TITLE"));
						msg.put("msgContent", rs.getString("MSG_CONTENT"));
						msg.put("time", rs.getString("PUSH_TIME"));
						msg.put("status", rs.getInt("MSG_STATUS"));
						msg.put("pushUser", rs.getString("USER_REAL_NAME"));
					}
					return msg;
				}
			};

			Map<String,Object> message = run.query(conn, selectSql,rsHandler);
			
			MessageOperation.updateMessageStatus(conn,msgId);
			
			return message;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 根据申请人查询业务申请列表
	 * @author Han Shaoming
	 * @param userId
	 * @param pageNum
	 * @param pageSize
	 * @param condition
	 * @return
	 * @throws ServiceException 
	 */
	public Page getApplyListByApplyUserId(long userId, int pageNum, int pageSize, String condition) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			queryRunner = new QueryRunner();
			
			//拼接sql语句
			JSONObject jo = JSONObject.fromObject(condition);
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT A.*,U.USER_REAL_NAME FROM APPLICATION A,USER_INFO U WHERE A.OPERATOR=U.USER_ID");
			//添加筛选条件
			if(jo.get("deleteFlag") != null){
				//筛选消息
				sql.append(" AND A.DELETE_FLAG="+jo.get("deleteFlag"));
			}else{
				sql.append(" AND A.DELETE_FLAG=0");
			}
			//添加搜索条件
			if(StringUtils.isNotEmpty((String) jo.get("operatorUserName"))){
				//模糊搜索处理人
				sql.append(" AND U.USER_REAL_NAME LIKE '%"+jo.get("operatorUserName")+"%'");
			}
			if(StringUtils.isNotEmpty((String)jo.get("applyTitle") )){
				//模糊搜索标题内容
				sql.append(" AND A.APPLY_TITLE LIKE '%"+jo.get("applyTitle")+"%'");
			}
			if(jo.get("applyType") !=null){
				//精确搜索申请类型
				sql.append(" AND A.APPLY_TYPE="+jo.get("applyType"));
			}
			if(jo.get("applyStatus") !=null){
				//精确搜索申请状态
				sql.append(" AND A.APPLY_STATUS="+jo.get("applyStatus"));
			}
			sql.append(" AND A.APPLY_USER_ID="+userId+" ORDER BY A.OPERATE_TIME");
			String querySql = sql.toString();
			Object[] params = {};
			//日志
			log.info("根据申请人查询业务申请列表的sql:"+sql.toString());
			Page page = queryRunner.query(pageNum,pageSize,conn, querySql, new ApplyMsgWithPageHandler(pageNum, pageSize), params);
			return page;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 根据审核人查询业务申请列表
	 * @author Han Shaoming
	 * @param userId
	 * @param pageNum
	 * @param pageSize
	 * @param condition
	 * @return
	 * @throws ServiceException
	 */
	public Page getApplyListByByAuditor(long userId, int pageNum, int pageSize, String condition) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			queryRunner = new QueryRunner();
			
			//拼接sql语句
			JSONObject jo = JSONObject.fromObject(condition);
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT A.*,U.USER_REAL_NAME FROM APPLICATION A,USER_INFO U WHERE A.AUDITOR=U.USER_ID AND A.DELETE_FLAG=0 ");
			//添加搜索条件
			if(StringUtils.isNotEmpty((String) jo.get("auditorName"))){
				//模糊搜索处理人
				sql.append(" AND U.USER_REAL_NAME LIKE '%"+jo.get("auditorName")+"%'");
			}
			if(StringUtils.isNotEmpty((String)jo.get("applyTitle") )){
				//模糊搜索标题内容
				sql.append(" AND A.APPLY_TITLE LIKE '%"+jo.get("applyTitle")+"%'");
			}
			if(jo.get("applyType") !=null){
				//精确搜索申请类型
				sql.append(" AND A.APPLY_TYPE="+jo.get("applyType"));
			}
			if(jo.get("applyStatus") !=null){
				//精确搜索申请状态
				sql.append(" AND A.APPLY_STATUS="+jo.get("applyStatus"));
			}else{
				sql.append(" AND A.APPLY_STATUS IN (2,3,4) ");
			}
			sql.append(" AND A.AUDITOR="+userId+" ORDER BY A.OPERATE_TIME");
			String querySql = sql.toString();
			Object[] params = {};
			//日志
			log.info("根据审核人查询业务申请列表的sql:"+sql.toString());
			Page page = queryRunner.query(pageNum,pageSize,conn, querySql, new AuditorMsgWithPageHandler(pageNum, pageSize), params);
			return page;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 业务申请创建
	 * @author Han Shaoming
	 * @param userId
	 * @param paraJson
	 * @return
	 * @throws ServiceException 
	 */
	public String createApply(long userId, JSONObject paraJson) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			queryRunner = new QueryRunner();
			//日志
			log.info("业务申请创建:"+paraJson.toString());
			//获取插入数据的id
			String idSql = "SELECT APPLICATION_SEQ.NEXTVAL FROM DUAL";
			Object[] idParams = {};
			long applyId = queryRunner.queryForLong(conn, idSql, idParams);
			//获取数据
			String applytitle = paraJson.getString("applyTitle");
			long applyType = paraJson.getLong("applyType");
			long severity = paraJson.getLong("severity");
			long auditRoleId = paraJson.getLong("auditRoleId");
			String auditor = paraJson.getString("auditor");
			long applyGroupId = paraJson.getLong("applyGroupId");
			String relateObject = paraJson.getString("relateObject");
			long relateObjectId = paraJson.getLong("relateObjectId");
			String applyContent = null;
			//1作业申请2计划变更
			if(applyType == 1){
				//作业申请
				String workContent = paraJson.getString("workContent");
				Long poiPlanTotal= null;
				Long roadPlanTotal= null;
				String memo= null;
				if(paraJson.containsKey("poiPlanTotal")){
					poiPlanTotal = paraJson.getLong("poiPlanTotal");
				}
				if(paraJson.containsKey("roadPlanTotal")){
					roadPlanTotal = paraJson.getLong("roadPlanTotal");
				}
				if(paraJson.containsKey("memo")){
					memo = paraJson.getString("memo");
				}
				//处理申请内容
				JSONObject jo = new JSONObject();
				jo.put("workContent", workContent);
				jo.put("poiPlanTotal", poiPlanTotal);
				jo.put("roadPlanTotal", roadPlanTotal);
				jo.put("memo", memo);
				applyContent = jo.toString();
			}else if(applyType == 2){
				//计划变更
				String changeType = paraJson.getString("changeType");
				String name = paraJson.getString("name"); 
				String reason = paraJson.getString("reason");
				//处理申请内容
				JSONObject jo = new JSONObject();
				jo.put("changeType", changeType);
				jo.put("name", name);
				jo.put("reason", reason);
				applyContent = jo.toString();
			}
			//保存数据到application表
			String applySql = "INSERT INTO APPLICATION (APPLY_ID, APPLY_TITLE, APPLY_TYPE, APPLY_STATUS, SEVERITY, OPERATE_TIME, "
					+ "OPERATOR, AUDIT_ROLE_ID, AUDITOR, CREATE_TIME, APPLY_GROUP_ID, APPLY_USER_ID, DELETE_FLAG) "
					+ "VALUES (?,?,?,1,?,SYSDATE,?,?,?,SYSDATE,?,?,0)";
			Object[] applyParams = {applyId,applytitle,applyType,severity,userId,auditRoleId,auditor,applyGroupId,userId};
			queryRunner.update(conn, applySql, applyParams);
			//保存数据到application_detail表
			String applyDetailSql = "INSERT INTO APPLICATION_DETAIL (APPLY_ID, RELATE_OBJECT, RELATE_OBJECT_ID,"
					+ " APPLY_CONTENT) VALUES (?,?,?,?)";
			Object[] applyDetailParams = {applyId,relateObject,relateObjectId,applyContent};
			queryRunner.update(conn, applyDetailSql, applyDetailParams);
			//保存数据到application_timeLine表
			String content = "创建";
			this.createApplyTimeLine(userId,applyId,content);
			
			return "创建成功";
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 业务申请修改
	 * @author Han Shaoming
	 * @param userId
	 * @param paraJson
	 * @return
	 * @throws ServiceException 
	 */
	public String updateApply(long userId, JSONObject paraJson) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			queryRunner = new QueryRunner();
			//日志
			log.info("业务申请修改:"+paraJson.toString());
			//获取修改数据的id
			long applyId = paraJson.getLong("applyId");
			//获取数据
			String applytitle = paraJson.getString("applyTitle");
			long applyType = paraJson.getLong("applyType");
			long severity = paraJson.getLong("severity");
			long auditRoleId = paraJson.getLong("auditRoleId");
			String auditor = paraJson.getString("auditor");
			String relateObject = paraJson.getString("relateObject");
			long relateObjectId = paraJson.getLong("relateObjectId");
			String applyContent = null;
			//1作业申请2计划变更
			if(applyType == 1){
				//作业申请
				String workContent = paraJson.getString("workContent");
				Long poiPlanTotal= null;
				Long roadPlanTotal= null;
				String memo= null;
				if(paraJson.containsKey("poiPlanTotal")){
					poiPlanTotal = paraJson.getLong("poiPlanTotal");
				}
				if(paraJson.containsKey("roadPlanTotal")){
					roadPlanTotal = paraJson.getLong("roadPlanTotal");
				}
				if(paraJson.containsKey("memo")){
					memo = paraJson.getString("memo");
				}
				//处理申请内容
				JSONObject jo = new JSONObject();
				jo.put("workContent", workContent);
				jo.put("poiPlanTotal", poiPlanTotal);
				jo.put("roadPlanTotal", roadPlanTotal);
				jo.put("memo", memo);
				applyContent = jo.toString();
			}else if(applyType == 2){
				//计划变更
				String changeType = paraJson.getString("changeType");
				String name = paraJson.getString("name"); 
				String reason = paraJson.getString("reason");
				//处理申请内容
				JSONObject jo = new JSONObject();
				jo.put("changeType", changeType);
				jo.put("name", name);
				jo.put("reason", reason);
				applyContent = jo.toString();
			}
			//更新数据到application表
			String applySql = "UPDATE APPLICATION SET APPLY_TITLE=?, APPLY_TYPE=?, SEVERITY=?, OPERATE_TIME=SYSDATE, "
					+ "OPERATOR=?, AUDIT_ROLE_ID=?, AUDITOR=? WHERE APPLY_ID=?";
			Object[] applyParams = {applytitle,applyType,severity,userId,auditRoleId,auditor,applyId};
			queryRunner.update(conn, applySql, applyParams);
			//更新数据到application_detail表
			String applyDetailSql = "UPDATE APPLICATION_DETAIL SET RELATE_OBJECT=?, RELATE_OBJECT_ID=?, APPLY_CONTENT=?"
					+ " WHERE APPLY_ID=?";
			Object[] applyDetailParams = {relateObject,relateObjectId,applyContent,applyId};
			queryRunner.update(conn, applyDetailSql, applyDetailParams);
			
			return "修改成功";
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 业务申请详细信息查看
	 * @author Han Shaoming
	 * @param userId
	 * @param applyId
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> queryApply(long userId, long applyId) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			queryRunner = new QueryRunner();
			
			//根据id查询申请数据
			String sql = "SELECT A.*,D.RELATE_OBJECT,D.RELATE_OBJECT_ID,D.APPLY_CONTENT,D.AUDIT_REASON,"
					+ "(SELECT R.ROLE_NAME FROM ROLE R WHERE R.ROLE_ID=A.AUDIT_ROLE_ID) AUDIT_ROLE_NAME,"
					+ "(SELECT G.GROUP_NAME FROM USER_GROUP G WHERE G.GROUP_ID=A.APPLY_GROUP_ID) APPLY_GROUP_NAME "
					+ "FROM APPLICATION A,APPLICATION_DETAIL D WHERE A.APPLY_ID=D.APPLY_ID AND A.APPLY_ID=?";
			Object[] params = {applyId};
			//处理结果集
			ResultSetHandler<Map<String,Object>> rsh = new ResultSetHandler<Map<String,Object>>() {
				@Override
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					Map<String,Object> map = new HashMap<String,Object>();
					while(rs.next()){
						map.put("applyId",rs.getLong("APPLY_ID"));
						map.put("applyTitle",rs.getString("APPLY_TITLE"));
						map.put("applyType",rs.getLong("APPLY_TYPE"));
						map.put("severity",rs.getLong("SEVERITY"));
						map.put("auditRoleId",rs.getLong("AUDIT_ROLE_ID"));
						map.put("auditRoleName",rs.getString("AUDIT_ROLE_NAME"));
						map.put("auditor",rs.getLong("AUDITOR"));
						map.put("applyGroupId",rs.getLong("APPLY_GROUP_ID"));
						map.put("applyGroupName",rs.getString("APPLY_GROUP_NAME"));
						map.put("relateObject",rs.getString("RELATE_OBJECT"));
						map.put("relateObjectId",rs.getLong("RELATE_OBJECT_ID"));
						map.put("applyContent",rs.getString("APPLY_CONTENT"));
					}
					return map;
				}
			};
			//获取数据
			Map<String, Object> apply = queryRunner.query(conn, sql, rsh, params);
			//日志
			log.info("查询的申请数据"+apply.toString());
			//处理申请内容
			//1作业申请2计划变更
			if(apply.get("applyType") != null){
				if((long)apply.get("applyType") == 1){
					//作业申请
					JSONObject json = JSONObject.fromObject(apply.get("applyContent"));
					String workContent = json.getString("workContent");
					Long poiPlanTotal= json.getLong("poiPlanTotal");
					Long roadPlanTotal= json.getLong("roadPlanTotal");
					String memo= json.getString("memo");
					
					apply.remove("applyContent");
					
					apply.put("workContent", workContent);
					apply.put("poiPlanTotal", poiPlanTotal);
					apply.put("roadPlanTotal", roadPlanTotal);
					apply.put("memo", memo);
				}else if((long)apply.get("applyType")==2){
					//计划变更
					JSONObject json = JSONObject.fromObject(apply.get("applyContent"));
					String changeType = json.getString("changeType");
					String name = json.getString("name"); 
					String reason = json.getString("reason");
					
					apply.remove("applyContent");
					
					apply.put("changeType", changeType);
					apply.put("name", name);
					apply.put("reason", reason);
				}
			}
			//查询applicationTimeLine
			List<Map<String, Object>> queryApplyTimeLine = this.queryApplyTimeLine(applyId);
			apply.put("applyTimeLine", queryApplyTimeLine);
			return apply;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 批量修改申请删除状态
	 * @author Han Shaoming
	 * @param userId
	 * @param applyIds
	 * @param applyId
	 * @return
	 * @throws ServiceException 
	 */
	public String updateDeleteFlag(long userId, JSONArray applyIds, long deleteFlag) throws ServiceException {
		// TODO Auto-generated method stub
		int total = 0;
		try{
			//编辑消息状态
			for(int i=0;i<applyIds.size();i++){
				long applyId = applyIds.getLong(i);
				if(deleteFlag == 0 || deleteFlag == 1){
					//非删除或恢复
					this.updateApplyDeleteFlag(applyId, deleteFlag);
				}else if(deleteFlag == 2){
					//永久删除
					this.deleteForeverMsg(applyId);
				}
				total+=1;
			}
			return "批量修改消息删除状态："+total+"个成功,"+(applyIds.size()-total)+"个失败!";
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new ServiceException("编辑失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 批量修改申请状态
	 * @author Han Shaoming
	 * @param userId
	 * @param applyIds
	 * @param deleteFlag
	 * @return
	 * @throws ServiceException 
	 */
	public String updateApplyStatus(long userId, JSONObject paraJson) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			queryRunner = new QueryRunner();
			//日志
			log.info("修改申请状态:"+paraJson.toString());
			long applyId = paraJson.getLong("applyId");
			long applyStatus = paraJson.getLong("applyStatus");
			//编辑申请消息状态
			String auditReason = null;
			if(applyStatus == 4){
				//审核不通过
				//添加审核不通过原因
				if(paraJson.containsKey("auditReason")){
					auditReason = paraJson.getString("auditReason");
				}
				String applyDetailSql = "UPDATE APPLICATION_DETAIL SET AUDIT_REASON=? WHERE APPLY_ID=?";
				Object[] applyDetailParams = {auditReason,applyId};
				queryRunner.update(conn, applyDetailSql, applyDetailParams);
			}
			//更改状态
			this.updateStatus(applyId, applyStatus);
			//保存数据到application_timeLine表
			String content = null;
			if(applyStatus == 2){
				//提交
				content = "提交审核";
			}else if(applyStatus == 3){
				//通过
				content = "审核通过";
			}else if(applyStatus == 4){
				//不通过
				content = "审核不通过,原因为:"+auditReason;
			}
			this.createApplyTimeLine(userId,applyId,content);
			return "修改申请状态成功";
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 创建applicationTimeLine数据
	 * @author Han Shaoming
	 * @param applyId
	 * @return
	 * @throws ServiceException
	 */
	public void createApplyTimeLine(long userId,long applyId,String content) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			//查询消息
			conn = DBConnector.getInstance().getManConnection();
			queryRunner = new QueryRunner();
			String applyTimeLineSql = "INSERT INTO APPLICATION_TIMELINE (APPLY_ID, OPERATE_TIME, OPERATOR, CONTENT) "
					+ "VALUES (?,SYSDATE,?,?)";
			Object[] applyTimeLineParams = {applyId,userId,content};
			queryRunner.update(conn, applyTimeLineSql, applyTimeLineParams);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询applicationTimeLine数据
	 * @author Han Shaoming
	 * @param applyId
	 * @return
	 * @throws ServiceException
	 */
	public List<Map<String,Object>> queryApplyTimeLine(long applyId) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			//查询消息
			conn = DBConnector.getInstance().getManConnection();
			queryRunner = new QueryRunner();
			//根据id查询申请数据
			String sql = "SELECT T.*,U.USER_REAL_NAME FROM APPLICATION_TIMELINE T,USER_INFO U "
					+ "WHERE T.OPERATOR=U.USER_ID AND APPLY_ID=?";
			Object[] params = {applyId};
			//处理结果集
			ResultSetHandler<List<Map<String,Object>>> rsh = new ResultSetHandler<List<Map<String,Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
					while(rs.next()){
						Map<String,Object> map = new HashMap<String,Object>();
						map.put("operateTime",rs.getTimestamp("OPERATE_TIME"));
						map.put("operatorName",rs.getString("USER_REAL_NAME"));
						map.put("content",rs.getString("CONTENT"));
						list.add(map);
					}
					return list;
				}
			};
			//获取数据
			List<Map<String, Object>> applyTimeline = queryRunner.query(conn, sql, rsh, params);
			return applyTimeline;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 永久删除消息
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public void deleteForeverMsg(long applyId)throws ServiceException{
		Connection sysConn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			sysConn = DBConnector.getInstance().getManConnection();
			//永久删除消息
			//application表
			String deleteSql = "DELETE FROM APPLICATION WHERE APPLY_ID=?";
			Object[] deleteParams={applyId};
			queryRunner.update(sysConn, deleteSql, deleteParams);
			//application_detail表
			String applyDetailSql = "DELETE FROM APPLICATION_DETAIL WHERE APPLY_ID=?";
			Object[] applyDetailParams = {applyId};
			queryRunner.update(sysConn, applyDetailSql, applyDetailParams);
			//application_timeLine表
			String applyTimeLineSql = "DELETE FROM APPLICATION_TIMELINE WHERE APPLY_ID=?";
			Object[] applyTimeLineParams = {applyId};
			queryRunner.update(sysConn, applyTimeLineSql, applyTimeLineParams);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(sysConn);
			log.error(e.getMessage(), e);
			throw new ServiceException("永久删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(sysConn);
		}
	}
	
	/**
	 * 更改申请消息删除状态
	 * @param userId
	 * @throws ServiceException
	 */
	public void updateApplyDeleteFlag(long applyId,long deleteFlag)throws ServiceException{
		Connection sysConn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			sysConn = DBConnector.getInstance().getManConnection();
			//更改状态
			String updateSql = "UPDATE APPLICATION SET DELETE_FLAG=? WHERE APPLY_ID=?";
			Object[] updateParams={deleteFlag,applyId};
			queryRunner.update(sysConn, updateSql, updateParams);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(sysConn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更改失败，原因为:"+e.getMessage());
		}finally{
			DbUtils.commitAndCloseQuietly(sysConn);
		}
	}
	
	/**
	 * 更改申请消息状态
	 * @author Han Shaoming
	 * @param applyId
	 * @param applyStatus
	 * @throws ServiceException
	 */
	public void updateStatus(long applyId,long applyStatus)throws ServiceException{
		Connection sysConn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			sysConn = DBConnector.getInstance().getManConnection();
			//更改状态
			String updateSql = "UPDATE APPLICATION SET APPLY_STATUS=? WHERE APPLY_ID=?";
			Object[] updateParams={applyStatus,applyId};
			queryRunner.update(sysConn, updateSql, updateParams);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(sysConn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更改失败，原因为:"+e.getMessage());
		}finally{
			DbUtils.commitAndCloseQuietly(sysConn);
		}
	}
	
	/**
	 * 
	 * @ClassName ApplyMsgWithPageHandler
	 * @author Han Shaoming
	 * @date 2016年11月9日 上午10:35:38
	 * @Description TODO
	 */
	class ApplyMsgWithPageHandler implements ResultSetHandler<Page>{
		private int pageNum;
		private int pageSize;
		ApplyMsgWithPageHandler(int pageNum,int pageSize){
			this.pageNum=pageNum;
			this.pageSize=pageSize;
		}
		public Page handle(ResultSet rs) throws SQLException {
			Page page = new Page(pageNum);
			page.setPageSize(pageSize);
			int total = 0;
			List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				Map<String,Object> msg = new HashMap<String, Object>();
				msg.put("applyId",rs.getLong("APPLY_ID"));
				msg.put("applyTitle",rs.getString("APPLY_TITLE"));
				msg.put("applyType",rs.getLong("APPLY_TYPE"));
				msg.put("applyStatus",rs.getLong("APPLY_STATUS"));
				msg.put("operateTime",rs.getTimestamp("OPERATE_TIME"));
				msg.put("operator",rs.getLong("OPERATOR"));
				msg.put("operatorUserName",rs.getString("USER_REAL_NAME"));
				msgs.add(msg);
				if(total==0){
					total=rs.getInt("TOTAL_RECORD_NUM_");
				}
			}
			page.setResult(msgs);
			page.setTotalCount(total);
			return page;
		}
	}
	
	/**
	 * 
	 * @ClassName ApplyMsgWithPageHandler
	 * @author Han Shaoming
	 * @date 2016年11月9日 上午10:35:38
	 * @Description TODO
	 */
	class AuditorMsgWithPageHandler implements ResultSetHandler<Page>{
		private int pageNum;
		private int pageSize;
		AuditorMsgWithPageHandler(int pageNum,int pageSize){
			this.pageNum=pageNum;
			this.pageSize=pageSize;
		}
		public Page handle(ResultSet rs) throws SQLException {
			Page page = new Page(pageNum);
			page.setPageSize(pageSize);
			int total = 0;
			List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				Map<String,Object> msg = new HashMap<String, Object>();
				msg.put("applyId",rs.getLong("APPLY_ID"));
				msg.put("applyTitle",rs.getString("APPLY_TITLE"));
				msg.put("applyType",rs.getLong("APPLY_TYPE"));
				msg.put("applyStatus",rs.getLong("APPLY_STATUS"));
				msg.put("operateTime",rs.getTimestamp("OPERATE_TIME"));
				msg.put("auditor",rs.getLong("AUDITOR"));
				msg.put("auditorName",rs.getString("USER_REAL_NAME"));
				msgs.add(msg);
				if(total==0){
					total=rs.getInt("TOTAL_RECORD_NUM_");
				}
			}
			page.setResult(msgs);
			page.setTotalCount(total);
			return page;
		}
	}

	

	


	
}
