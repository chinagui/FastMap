package com.navinfo.dataservice.engine.sys.msg.handle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.engine.sys.msg.SysMsg;
import com.navinfo.dataservice.engine.sys.msg.websocket.SysMsgWebSocketHandler;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName RunSysHandler
 * @author Han Shaoming
 * @date 2016年9月21日 下午7:31:52
 * @Description TODO
 */
public class SysMsgHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private Long id = null;
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.mq.MsgHandler#handle(java.lang.String)
	 */
	@Override
	public void handle(String message) {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			queryRunner = new QueryRunner();
			//获取插入数据的id
			String idSql = "SELECT SYS_MSG_SEQ.NEXTVAL FROM DUAL";
			Object[] idParams = {};
			id = queryRunner.queryForLong(conn, idSql, idParams);
			//解析message
			JSONObject jo = JSONObject.fromObject(message);
			String msgTitle = jo.getString("msgTitle");
			String msgContent = jo.getString("msgContent");
			long pushUserId = jo.getLong("pushUserId");
			long targetUserId = jo.getLong("targetUserId");
			String sql = "INSERT INTO SYS_MESSAGE(MSG_ID,MSG_TITLE,MSG_CONTENT,PUSH_USER_ID,TARGET_USER_ID,CREATE_TIME) "
					+ "VALUES(?,?,?,?,?,SYSDATE)";
			Object[] params = {id,msgTitle,msgContent,pushUserId,targetUserId};
			queryRunner.update(conn, sql, params);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
			sendMessage(id);
		}
	}
	
	/**
	 * 发送消息
	 */
	private void sendMessage(long id){
		Connection conn = null;
		QueryRunner queryRunner = null;
		try {
			//查询消息并将消息发送给用户
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			queryRunner = new QueryRunner();
			String userSql = "SELECT * FROM SYS_MESSAGE WHERE MSG_ID=?";
			Object[] userParams = {id};
			List<SysMsg> sysMsg = queryRunner.query(conn, userSql, userParams, new MultiRowHandler());
			if(sysMsg != null){
				String jsonSysMsg = JSONArray.fromObject(sysMsg).toString();
				if(sysMsg.get(0).getTargetUserId()==0){
					//发给所有人
					SysMsgWebSocketHandler.getInstance().sendMessageToUsers(new TextMessage(jsonSysMsg));
				}
				if(sysMsg.get(0).getTargetUserId() > 0){
					//发给指定用户
					SysMsgWebSocketHandler.getInstance().sendMessageToUser(Long.toString(sysMsg.get(0).getTargetUserId()), new TextMessage(jsonSysMsg));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/* resultset handler */
	class MultiRowHandler implements ResultSetHandler<List<SysMsg>>{
		
		public List<SysMsg> handle(ResultSet rs) throws SQLException {
			List<SysMsg> msgs = new ArrayList<SysMsg>();
			while(rs.next()){
				SysMsg msg = new SysMsg();
				msg.setMsgId(rs.getLong("MSG_ID"));
				msg.setMsgType(rs.getInt("MSG_TYPE"));
				msg.setMsgContent(rs.getString("MSG_CONTENT"));
				msg.setCreateTime(rs.getTimestamp("CREATE_TIME"));
				msg.setTargetUserId(rs.getLong("TARGET_USER_ID"));
				msg.setMsgTitle(rs.getString("MSG_TITLE"));
				msg.setPushUserId(rs.getLong("PUSH_USER_ID"));
				msgs.add(msg);
			}
			return msgs;
		}
		
	}
}
