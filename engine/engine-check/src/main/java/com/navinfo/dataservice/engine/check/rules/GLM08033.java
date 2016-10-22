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
 * @Description: GLM08033	交限	错误的交限进入线或退出线		路口交限的进入线和退出线不能为交叉口link	路口交限的进入线或退出线为交叉口link	1
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
				List<Integer> linkPidsTmp = new ArrayList<Integer>();
				boolean isCrossRelate=false;				
				for(IRow deObj:rdRestriction.getDetails()){
					if(deObj instanceof RdRestrictionDetail){
						RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail)deObj;
						if(rdRestrictionDetail.getRelationshipType()==1){
							isCrossRelate=true;
							linkPidsTmp.add(rdRestrictionDetail.getOutLinkPid());
						}
					}
				}
				if(isCrossRelate){
					linkPidsTmp.add(rdRestriction.getInLinkPid());
					linkPids.addAll(linkPidsTmp);
					linkPidsTmp=new ArrayList<Integer>(); 
					}
			}							
		}
		//为0说明没有符合条件的路口交限，不进行后续查询
		if(linkPids.size()==0){return;}
		String sql = "select link_pid from rd_link_form "
				+ "where FORM_OF_WAY = 50 AND U_RECORD != 2 and link_pid in ("+StringUtils.join(linkPids, ",")+")";
				
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
