package com.navinfo.dataservice.engine.check.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM11041
 * @author luyao
 * @date 2017年1月12日
 * @Description TODO
 * 检查原则：分歧名称拼音不能重复。
 * 分歧名称信息编辑 服务端后检查:
 */
public class GLM11041 extends baseRule {

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
		
		String strFormat = "SELECT  DISTINCT RB.BRANCH_PID FROM RD_BRANCH RB WHERE RB.BRANCH_PID = {0} AND RB.U_RECORD <> 2 AND EXISTS (SELECT B.IN_LINK_PID, B.NODE_PID, B.OUT_LINK_PID, N.PHONETIC FROM RD_BRANCH  B, RD_BRANCH_DETAIL D, RD_BRANCH_NAME   N, RD_LINK   L1, RD_LINK   L2 WHERE B.BRANCH_PID = {1} AND B.BRANCH_PID = D.BRANCH_PID AND D.DETAIL_ID = N.DETAIL_ID AND B.IN_LINK_PID = L1.LINK_PID AND B.OUT_LINK_PID = L2.LINK_PID AND L1.KIND IN (1, 2) AND L2.KIND IN (1, 2) AND D.BRANCH_TYPE IN (0, 1, 2) AND N.PHONETIC IS NOT NULL AND B.U_RECORD <> 2 AND D.U_RECORD <> 2 AND N.U_RECORD <> 2 AND L1.U_RECORD <> 2 AND L2.U_RECORD <> 2 GROUP BY B.IN_LINK_PID, B.NODE_PID, B.OUT_LINK_PID, D.BRANCH_TYPE, N.PHONETIC HAVING COUNT(1) <> 1)";
		
		String sql = MessageFormat.format(strFormat, branchPid, branchPid);

		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		resultList = getObj.exeSelect(this.getConn(), sql);

		if (!resultList.isEmpty()) {
			String target = "[RD_BRANCH," + branchPid + "]";
			
			this.setCheckResult("", target, 0);
		}
	}
}
