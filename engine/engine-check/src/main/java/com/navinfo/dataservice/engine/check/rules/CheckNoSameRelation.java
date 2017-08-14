package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: CheckNoSameRelation
 * @author songdongyan
 * @date 下午3:39:46
 * @Description: CheckNoSameRelation.java
 */
public class CheckNoSameRelation extends baseRule {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		String sql = "select pid from rd_restriction where in_link_pid=:1 and node_pid=:2 and U_RECORD != 2";
		
		PreparedStatement pstmt = null;
		try {
			pstmt = getConn().prepareStatement(sql);
			for(IRow obj:checkCommand.getGlmList()){
				ResultSet resultSet = null;
				try {
					if(obj instanceof RdRestriction ){
						RdRestriction rdRestriction = (RdRestriction)obj;
						int linkPid = rdRestriction.getInLinkPid();
						int nodePid = rdRestriction.getNodePid();
						pstmt.setInt(1, linkPid);
						pstmt.setInt(2, nodePid);
						resultSet = pstmt.executeQuery();
						boolean flag = false;
						if (resultSet.next()) {
							flag = true;
						}
						if (flag) {
							this.setCheckResult("", "", 0);
							return;
						}
					}
				}catch (SQLException e) {
					throw e;
				} finally {
					DbUtils.closeQuietly(resultSet);
				}
			}
		}catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt);
		}

	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

}
