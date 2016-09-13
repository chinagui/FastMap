package com.navinfo.dataservice.engin.man.message;

import java.sql.Connection;

import com.navinfo.navicommons.database.QueryRunner;

public class MessageOperation {

	public MessageOperation() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * 
	 * @param conn
	 * @param msgList  
	 * 			Object[][] msgList=new Object[userIdList.size()*msgContentList.size()][3];
	 * 			msgList[num][0]=userId;
				msgList[num][1]=msgTile;
				msgList[num][2]=msgContent;
	 * @throws Exception
	 */
	public static void batchInsert(Connection conn,Object[][] msgList) throws Exception{
		String insertSql="INSERT INTO MESSAGE  (MSG_ID, PUSH_USER, MSG_TITLE, MSG_CONTENT, MSG_STATUS)"
				+ " VALUES  (MESSAGE_SEQ.NEXTVAL, ?, ?, ?, 0)";
		QueryRunner run = new QueryRunner();
		run.batch(conn,insertSql, msgList);
	}

}
