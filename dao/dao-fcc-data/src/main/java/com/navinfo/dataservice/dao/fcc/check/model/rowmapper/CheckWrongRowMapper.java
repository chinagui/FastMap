package com.navinfo.dataservice.dao.fcc.check.model.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.fcc.check.model.CheckWrong;

/** 
 * @ClassName: CheckWrongRowMapper.java
 * @author y
 * @date 2017-5-25 下午3:07:33
 * @Description: TODO
 *  
 */
public class CheckWrongRowMapper {
	
	public static CheckWrongRowMapper instance=new CheckWrongRowMapper();
	
	public static CheckWrongRowMapper getInstance(){
		
		return instance;
	}
	
	
	
	public CheckWrong mapRow(ResultSet rs) throws SQLException{
		
		CheckWrong wrong=new CheckWrong();
		wrong.setLogId(rs.getString("LOG_ID"));
		wrong.setCheckTaskId(rs.getInt("CHECK_TASK_ID"));
		wrong.setObjectType(rs.getString("OBJECT_TYPE"));
		wrong.setObjectId(rs.getString("OBJECT_ID"));
		wrong.setErType(rs.getInt("ER_TYPE"));
		wrong.setQuDesc(rs.getString("QU_DESC"));
		wrong.setReason(rs.getString("REASON"));
		wrong.setErContent(rs.getString("ER_CONTENT"));
		wrong.setQuRank(rs.getString("QU_RANK"));
		wrong.setWorkTime(DateUtils.format(rs.getTimestamp("WORK_TIME"), "yyyyMMddHHmmss") );
		wrong.setCheckTime(DateUtils.format(rs.getTimestamp("CHECK_TIME"), "yyyyMMddHHmmss")  );
		wrong.setIsPrefer(rs.getInt("IS_PREFER"));
		wrong.setChecker(rs.getString("CHECKER"));
		wrong.setWorker(rs.getString("WORKER"));
		return wrong;
	}

}
