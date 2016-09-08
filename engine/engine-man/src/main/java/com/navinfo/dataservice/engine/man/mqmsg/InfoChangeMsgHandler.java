package com.navinfo.dataservice.engine.man.mqmsg;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.dao.mq.MsgSubscriber;
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
	String sql ="INSERT INTO INFOR(INFOR_ID,INFOR_NAME,GEOMETRY,INFOR_LEVEL,INFOR_STATUS,INFO_CONTENT) "
			    + "VALUES (?,?,?,?,0,?)";
	@Override
	public void handle(String message) {
		try{
			//解析保存到man库infor表中
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
			Clob c = ConnectionUtil.createClob(conn);
			JSONObject dataJson = JSONObject.fromObject(message);
			c.setString(1, dataJson.getString("geometry"));
			List<Object> values=new ArrayList<Object>();
			values.add(dataJson.getString("rowkey"));
			values.add(dataJson.getString("INFO_NAME"));
			values.add(c);
			values.add(dataJson.getString("i_level"));
			values.add(dataJson.getString("INFO_CONTENT"));
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, values.toArray());
			conn.commit();
		}catch(SQLException e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

	public static void main(String[] args){
		try{

			final InfoChangeMsgHandler sub = new InfoChangeMsgHandler();
			String message="{\"geometry\":\"POINT (120.712884 31.363296);POINT (123.712884 32.363296);\",\"rowkey\":\"5f2086de-23a4-4c02-8c08-995bfe4c6f0b\",\"i_level\":2,\"b_sourceCode\":1,\"b_sourceId\":\"sfoiuojkw89234jkjsfjksf\",\"b_reliability\":3,\"INFO_NAME\":\"道路通车\",\"INFO_CONTENT\":\"广泽路通过广泽桥到来广营东路路段已经通车，需要更新道路要素\"}";
			sub.save(message);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
