package com.navinfo.dataservice.engine.man.message;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: MessageOperation
 * @author songdongyan
 * @date 2016年9月13日
 * @Description: MessageOperation.java
 */
public class MessageOperation {

	private static Logger log = LoggerRepos.getLogger(TaskOperation.class);
	
	/**
	 * 
	 */
	public MessageOperation() {
		// TODO Auto-generated constructor stub
	}



	/**
	 * @param conn
	 * @param message
	 * @throws Exception 
	 */
	public static void insertMessage(Connection conn, Message message) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			List<Object> value = new ArrayList<Object>();
			
			value.add(message.getMsgId());
			value.add(message.getMsgTitle());
			value.add(message.getMsgContent());
			value.add(message.getPushUserId());
			value.add(message.getReceiverId());
			value.add(message.getMsgStatus());

			value.add(new Timestamp(System.currentTimeMillis()).toString().substring(0, 10));

			String createSql = "insert into MESSAGE " ;
			String column = "(MSG_ID, MSG_TITLE, MSG_CONTENT,PUSH_USER, MSG_RECERVER,MSG_STATUS,PUSH_TIME";
			String values = "values(?,?,?,?,?,?,?";

			run.update(conn, createSql,value.toArray());

		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
		
	}

	/**
	 * @param conn
	 * @return
	 * @throws Exception 
	 */
	public static int getMessageId(Connection conn) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();

			String querySql = "select MESSAGE_SEQ.NEXTVAL as messageId from dual";

			int messageId = Integer.valueOf(run
					.query(conn, querySql, new MapHandler()).get("messageId")
					.toString());
			return messageId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}

}
