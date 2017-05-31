package com.navinfo.dataservice.dao.fcc.check.operate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

/** 
 * @ClassName: CheckPercentOperator.java
 * @author y
 * @date 2017-5-26 下午2:37:28
 * @Description: 质检-抽检结果操作表 (质检方案变化，暂时不再使用)
 *  
 */
@Deprecated
public class CheckResultOperator {
	

	Logger log = Logger.getLogger(CheckResultOperator.class);
	
	private static  String INSERT_SQL=" INSERT INTO CHECK_RESULT(TASK_ID,TOTAL,TIPS_CODE,ROWKEY,CHECK_STATUS)VALUES (?,?,?,?,?)";

	
	/**
	 * @param conn
	 */
	public CheckResultOperator() {
		super();
	}





	public void save(int checkTaskId,int total,  List<JSONObject> tipsList) throws Exception{
		
		PreparedStatement pst = null;
		Connection conn=null;

		try {
			
			//TASK_ID,TOTAL,TIPS_CODE,ROWKEY,CHECK_STATUS
			conn=DBConnector.getInstance().getCheckConnection();
			
			pst = conn.prepareStatement(INSERT_SQL);
			int count=0;
			for (JSONObject tip : tipsList) {
				
				pst.setInt(0, checkTaskId); //TASK_ID
				pst.setInt(1, total); //TOTAL
				pst.setString(2, tip.getString("s_sourceType"));//TIPS_CODE
				pst.setString(3, tip.getString("id"));//ROWKEY
				pst.setInt(4, 0);//CHECK_STATUS
				
				pst.addBatch();
				if(++count%500==0){
					pst.executeBatch();
					pst.clearBatch();
				}
			}
			
			pst.executeBatch();
			pst.clearBatch();
			
		} catch (Exception e) {
			log.error("保存抽检结果出错，" + e.getMessage(), e);
			throw new Exception("保存抽检结果出错，" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(pst);
			DbUtils.commitAndCloseQuietly(conn);
		}

		
		
	}
	
	
	

}
