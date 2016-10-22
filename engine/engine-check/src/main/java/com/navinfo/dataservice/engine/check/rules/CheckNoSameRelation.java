package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
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
		
		PreparedStatement pstmt = getConn().prepareStatement(sql);
		for(IRow obj:checkCommand.getGlmList()){
			if(obj instanceof RdRestriction ){
				RdRestriction rdRestriction = (RdRestriction)obj;
				int linkPid = rdRestriction.getInLinkPid();
				int nodePid = rdRestriction.getNodePid();
				
				pstmt.setInt(1, linkPid);
				
				pstmt.setInt(2, nodePid);

				ResultSet resultSet = pstmt.executeQuery();

				boolean flag = false;

				if (resultSet.next()) {
					flag = true;
				}

				resultSet.close();

				pstmt.close();
						
				if (flag) {

					this.setCheckResult("", "", 0);
					return;
				}
			}
							
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
