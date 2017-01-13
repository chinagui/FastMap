package com.navinfo.dataservice.engine.check.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchName;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/*
 * BRANCH_CHECK_SAME_LANGCODE	同一组分歧名称中，不能存在两条语言代码相同的名称
 */


public class BRANCH_CHECK_SAME_LANGCODE extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

		Set<String> pids = new HashSet<String>();

		for (IRow row : checkCommand.getGlmList()) {

			if (row instanceof RdBranchName) {

				RdBranchName name = (RdBranchName) row;

				String RdBranchPid = getRdBranchPid(name.getDetailId());
				
				if (RdBranchPid != null) {
					pids.add(RdBranchPid);
				}
			}
		}

		for (String branchPid : pids) {

			check(branchPid);
		}
	}
	
	private String getRdBranchPid(int detailId) throws Exception {

		String strFormat = "SELECT D.BRANCH_PID FROM RD_BRANCH_DETAIL D WHERE D.DETAIL_ID = {0} AND D.U_RECORD <> 2";

		String sql = MessageFormat.format(strFormat, String.valueOf(detailId));

		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		resultList = getObj.exeSelect(this.getConn(), sql);

		if (resultList.size() > 0) {

			return String.valueOf(resultList.get(0));
		}

		return null;
	}
	
	/**
	 * @author luyao
	 * @param
	 * @throws Exception
	 */
	private void check(String branchPid) throws Exception {
		
		String strFormat = "SELECT DISTINCT RB.BRANCH_PID FROM RD_BRANCH RB, RD_BRANCH_DETAIL RBD, RD_BRANCH_NAME RBN1,RD_BRANCH_NAME RBN2 WHERE RB.BRANCH_PID = {0} AND RB.BRANCH_PID = RBD.BRANCH_PID AND RBD.DETAIL_ID = RBN1.DETAIL_ID AND RBD.DETAIL_ID = RBN2.DETAIL_ID AND RBN1.NAME_GROUPID=RBN2.NAME_GROUPID AND RBN1.NAME_ID<>RBN2.NAME_ID AND RBN1.LANG_CODE=RBN2.LANG_CODE AND RB.U_RECORD != 2 AND RBD.U_RECORD != 2 AND RBN1.U_RECORD != 2 AND RBN2.U_RECORD != 2";
		
		String sql = MessageFormat.format(strFormat, branchPid);

		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		resultList = getObj.exeSelect(this.getConn(), sql);

		if (!resultList.isEmpty()) {
			String target = "[RD_BRANCH," + branchPid + "]";
			
			this.setCheckResult("", target, 0);
		}
	}
}
