package com.navinfo.dataservice.dao.plus.selector.custom;

import java.sql.Clob;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: IxSamePoiSelector
 * @author xiaoxiaowen4127
 * @date 2017年5月8日
 * @Description: IxSamePoiSelector.java
 */
public class IxSamePoiSelector {
	
	/**
	 * 获取SAMEPOI的GroupID
	 * @param conn
	 * @param fids
	 * @return
	 * @throws Exception
	 */
	public static Map<String,Long> getPidByFids(Connection conn,Collection<String> fids,boolean filtDeleted)throws Exception{
		if(fids==null|fids.size()==0)return new HashMap<String,Long>();
		
		String urecordSql = "AND S.U_RECORD<>2";
		if(fids.size()>1000){
			String sql= "SELECT DISTINCT GROUP_ID PID FROM IX_SAMEPOI_PART S,IX_POI P WHERE S.POI_PID=P.PID "+(filtDeleted?urecordSql:"")+" AND P.POI_NUM IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)))";
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(fids, ","));
			return new QueryRunner().query(conn, sql, new FidPidSelHandler(),clob);
		}else{
			String sql= "SELECT DISTINCT GROUP_ID PID FROM IX_SAMEPOI_PART S,IX_POI P WHERE S.POI_PID=P.PID "+(filtDeleted?urecordSql:"")+" AND P.POI_NUM IN ('"+StringUtils.join(fids, "','")+"')";
			return new QueryRunner().query(conn,sql,new FidPidSelHandler());
		}
	}
}
