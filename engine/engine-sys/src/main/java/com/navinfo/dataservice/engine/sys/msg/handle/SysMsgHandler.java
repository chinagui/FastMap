package com.navinfo.dataservice.engine.sys.msg.handle;

import java.sql.Connection;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.navicommons.database.QueryRunner;

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
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.mq.MsgHandler#handle(java.lang.String)
	 */
	@Override
	public void handle(String message) {
		Connection conn = null;
		try{
			//解析message
			JSONObject jo = JSONObject.fromObject(message);
			String msgTitle = jo.getString("msgTitle");
			String msgContent = jo.getString("msgContent");
			long pushUserId = jo.getLong("pushUserId");
			long targetUserId = jo.getLong("targetUserId");
			String sql = "INSERT INTO SYS_MESSAGE(MSG_ID,MSG_TITLE,MSG_CONTENT,PUSH_USER_ID,TARGET_USER_ID,CREATE_TIME) "
					+ "VALUES(SYS_MSG_SEQ.NEXTVAL,?,?,?,?,SYSDATE)";
			Object[] params = {msgTitle,msgContent,pushUserId,targetUserId};
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			QueryRunner queryRunner = new QueryRunner();
			queryRunner.update(conn, sql, params);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);;
		}
	}

}
