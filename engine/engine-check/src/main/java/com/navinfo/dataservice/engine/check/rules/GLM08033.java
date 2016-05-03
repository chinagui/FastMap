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
 * @ClassName: GLM08033
 * @author songdongyan
 * @date 下午3:33:54
 * @Description: GLM08033.java
 */
public class GLM08033 extends baseRule {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		//获取inLinkPid\outLinkPid
		List<Integer> linkPids = new ArrayList<Integer>();
						
		for(IRow obj:checkCommand.getGlmList()){
			if(obj instanceof RdRestriction ){
				RdRestriction rdRestriction = (RdRestriction)obj;
				linkPids.add(rdRestriction.getInLinkPid());
								
				for(IRow deObj:rdRestriction.getDetails()){
					if(deObj instanceof RdRestrictionDetail){
						RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail)deObj;
						linkPids.add(rdRestrictionDetail.getOutLinkPid());
					}
				}
			}
							
		}
		String sql = "select link_pid from rd_cross_link where link_pid in ("+StringUtils.join(linkPids, ",")+")";
				
		PreparedStatement pstmt = getConn().prepareStatement(sql);

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

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

}
