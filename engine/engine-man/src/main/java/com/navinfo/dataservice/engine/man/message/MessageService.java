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
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
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
	public List<Object> list(int userId,int status) throws ServiceException {
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

			return run.query(conn, selectSql,rsHandler);
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	//创建消息
	public void create(Message message) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
			int msgId = MessageOperation.getMessageId(conn);
			
			message.setMsgId(msgId);
			
			if(message.getMsgStatus()== null){
				message.setMsgStatus(0);
			}
			
			// 插入message
			MessageOperation.insertMessage(conn, message);

			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
