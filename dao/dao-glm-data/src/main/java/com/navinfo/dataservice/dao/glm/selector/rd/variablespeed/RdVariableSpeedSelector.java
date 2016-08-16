/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector.rd.variablespeed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

/** 
* @ClassName: RdVariableSpeedSelector 
* @author Zhang Xiaolong
* @date 2016年8月15日 下午9:13:52 
* @Description: TODO
*/
public class RdVariableSpeedSelector extends AbstractSelector {
	
	/**
	 * @param cls
	 * @param conn
	 */
	public RdVariableSpeedSelector(Connection conn) {
		super(RdVariableSpeed.class, conn);
	}
	
	/**
	 * 根据进入线、进入点、退出现查询可变限速
	 * @param inLinkPid
	 * @param nodePid
	 * @param outLinkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public RdVariableSpeed loadByInLinkNodeOutLinkPid(int inLinkPid,int nodePid,int outLinkPid,boolean isLock) throws Exception
	{
		RdVariableSpeed rdVariableSpeed = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder(
					"select * from RD_VARIABLE_SPEED where in_link_pid =:1 and node_pid = :2 and out_link_pid = :3 and u_record !=2");
			if (isLock) {
				sb.append(" for update nowait");
			}
			pstmt = getConn().prepareStatement(sb.toString());
			
			pstmt.setInt(1, inLinkPid);
			
			pstmt.setInt(2, nodePid);
			
			pstmt.setInt(3, outLinkPid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				rdVariableSpeed = new RdVariableSpeed();
				ReflectionAttrUtils.executeResultSet(rdVariableSpeed, resultSet);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rdVariableSpeed;
	}

}
