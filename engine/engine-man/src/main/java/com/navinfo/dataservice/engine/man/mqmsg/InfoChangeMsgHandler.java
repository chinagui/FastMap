package com.navinfo.dataservice.engine.man.mqmsg;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

/** 
 * 同步消费消息
* @ClassName: InfoChangeMsgHandler 
* @author Xiao Xiaowen 
* @date 2016年6月25日 上午10:42:43 
* @Description: TODO
*  
*/
public class InfoChangeMsgHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	String sql = "INSERT INTO INFO_CHANGE(INFO_ID,INFO_MSG) VALUES (INFO_CHANGE_SEQ.NEXTVAL,?)";
	@Override
	public void handle(String message) {
		try{
			//暂时直接保存到man库info_change表中
			save(message);
		}catch(Exception e){
			log.warn("接收到info_change消息,但保存失败，该消息已消费。message："+message);
			log.error(e.getMessage(),e);
			
		}
	}
	
	public void save(String message)throws SQLException{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, message);
			conn.commit();
		}catch(SQLException e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

}
