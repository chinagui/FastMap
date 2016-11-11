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
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.xinge.XingeUtil;
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
import com.navinfo.dataservice.engine.man.userDevice.UserDeviceService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

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
}
