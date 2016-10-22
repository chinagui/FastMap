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
 * @ClassName: GLM08004
 * @author songdongyan
 * @date 上午10:28:16
 * @Description: GLM08004.java
 */
public class GLM08004 extends baseRule {

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
		
		String sql = "select form_of_way from rd_link_form "
				+ "where link_pid in ("+StringUtils.join(linkPids,",")+") "
						+ "and form_of_way in (20,22) AND U_RECORD != 2 ";
		
		PreparedStatement pstmt = getConn().prepareStatement(sql);

		ResultSet resultSet = pstmt.executeQuery();

		int formOfWay = 0;

		if (resultSet.next()) {
			
			formOfWay = resultSet.getInt("form_of_way");
		}

		resultSet.close();

		pstmt.close();
		
		if (formOfWay==20) {
			setRuleLog("步行街不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线");
			this.setCheckResult("", "", 0);
			return;
		}
		else if(formOfWay == 22){
			setRuleLog("公交车专用道不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线");
			this.setCheckResult("", "", 0);
			return;
		}



	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		

	}

}
