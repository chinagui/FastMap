package com.navinfo.dataservice.engine.sys.msg.handle;

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
import org.springframework.web.socket.TextMessage;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.engine.sys.msg.SysMsg;
import com.navinfo.dataservice.engine.sys.msg.websocket.MsgManWebSocketHandler;
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
		Integer msgType = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			queryRunner = new QueryRunner();
			//获取插入数据的id
			String idSql = "SELECT SYS_MSG_SEQ.NEXTVAL FROM DUAL";
			Object[] idParams = {};
			id = queryRunner.queryForLong(conn, idSql, idParams);
			log.info("sysMsg消息队列接受消息:"+message);
			//解析message
			JSONObject jo = JSONObject.fromObject(message);
			String msgTitle = (String) jo.get("msgTitle");
			String msgContent = (String) jo.get("msgContent");
			long pushUserId = (Integer) jo.get("pushUserId");
			long targetUserId = (Integer) jo.get("targetUserId");
			msgType = (Integer) jo.get("msgType");
			String msgParam = null;
			if(jo.get("msgParam") != null){
				msgParam = jo.get("msgParam").toString();
			}
			//String msgParam = jo.get("msgParam").toString();
			String pushUserName = (String) jo.get("pushUserName");
			//日志
			log.info("参数----msgType:"+msgType+",targetUserId:"+targetUserId);
			
			String sql = "INSERT INTO SYS_MESSAGE(MSG_ID,MSG_TYPE,MSG_TITLE,MSG_CONTENT,PUSH_USER_ID,TARGET_USER_ID,CREATE_TIME,MSG_PARAM,PUSH_USER_NAME) "
					+ "VALUES(?,?,?,?,?,?,SYSDATE,?,?)";
			Object[] params = {id,msgType,msgTitle,msgContent,pushUserId,targetUserId,msgParam,pushUserName};
			queryRunner.update(conn, sql, params);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			log.error(e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
			}
		//实时推送消息
		try {
			if(msgType != null){
				//判断消息类型
				if(msgType==0){
					//系统消息
					sendMessage(id);
				}else if(msgType==1){
					//job消息
					sendMessage(id);
				}else if(msgType==2){
					//管理消息
					sendManMsg(id);
				}
			}
		} catch (Exception e2) {
			// TODO: handle exception
			log.error(e2.getMessage(),e2);
		}
	}
	
	/**
	 * 发送系统及job消息
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
			if(sysMsg != null&&sysMsg.size()>0){
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
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 发送管理消息
	 * @author Han Shaoming
	 * @param id
	 */
	private void sendManMsg(long id){
		Connection conn = null;
		QueryRunner queryRunner = null;
		try {
			//查询消息并将消息发送给用户
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			queryRunner = new QueryRunner();
			String userSql = "SELECT * FROM SYS_MESSAGE WHERE MSG_ID=?";
			Object[] userParams = {id};
			//查询管理消息
			List<Map<String, Object>> sysMsg = queryRunner.query(conn, userSql, userParams, new ManMsgHandler());
			for (Map<String, Object> map : sysMsg) {
				String jsonSysMsg = JSONArray.fromObject(map).toString();
				if(map.get("targetUserId") != null){
					if((long)map.get("targetUserId")==0){
						//发给所有人
						MsgManWebSocketHandler.getInstance().sendMessageToUsers(new TextMessage(jsonSysMsg));
					}
					if((long)map.get("targetUserId") > 0){
						//发给指定用户
						MsgManWebSocketHandler.getInstance().sendMessageToUser(Long.toString((long) map.get("targetUserId")), new TextMessage(jsonSysMsg));
					}
				}
			}
		} catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			log.error("查询失败,原因为:"+e.getMessage(), e);
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
				msg.setMsgType(rs.getLong("MSG_TYPE"));
				msg.setMsgContent(rs.getString("MSG_CONTENT"));
				msg.setCreateTime(rs.getTimestamp("CREATE_TIME"));
				msg.setTargetUserId(rs.getLong("TARGET_USER_ID"));
				msg.setMsgTitle(rs.getString("MSG_TITLE"));
				msg.setPushUserId(rs.getLong("PUSH_USER_ID"));
				msg.setMsgParam(rs.getString("MSG_PARAM"));
				msg.setPushUserName(rs.getString("PUSH_USER_NAME"));
				msgs.add(msg);
			}
			return msgs;
		}
	}

/**
 * MAN管理消息的结果集
 * @ClassName ManMsgHandler
 * @author Han Shaoming
 * @date 2016年11月14日 下午1:59:30
 * @Description TODO
 */
class ManMsgHandler implements ResultSetHandler<List<Map<String,Object>>>{
		
		public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
			List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				Map<String,Object> msg = new HashMap<String, Object>();
				msg.put("msgId",rs.getLong("MSG_ID"));
				msg.put("msgContent",rs.getString("MSG_CONTENT"));
				msg.put("createTime",rs.getTimestamp("CREATE_TIME"));
				msg.put("msgTitle",rs.getString("MSG_TITLE"));
				msg.put("targetUserId",rs.getLong("TARGET_USER_ID"));
				msg.put("pushUserId",rs.getLong("PUSH_USER_ID"));
				msg.put("pushUserName",rs.getString("PUSH_USER_NAME"));
				msg.put("type", "message");
				msgs.add(msg);
			}
			return msgs;
		}
	}


}
