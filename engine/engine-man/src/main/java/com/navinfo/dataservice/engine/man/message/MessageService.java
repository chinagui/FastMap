package com.navinfo.dataservice.engine.man.message;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.xinge.XingeUtil;
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
import com.navinfo.dataservice.engine.man.userDevice.UserDeviceService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

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
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
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
			//查询消息
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
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
			//查询消息
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
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
				sql.append(" AND A.APPLY_STATUS= IN (2,3,4) ");
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
