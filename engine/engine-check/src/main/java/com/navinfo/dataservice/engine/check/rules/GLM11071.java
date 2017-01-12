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
 * @ClassName GLM11071
 * @author luyao
 * @date 2017年1月12日
 * @Description TODO
 * 进入线相同的线线方面分歧名称不允许重复（普通道路方面名称的不查）；
 * 分歧名称信息编辑 服务端后检查:
 */
public class GLM11071 extends baseRule {

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
		
		String strFormat = "SELECT DISTINCT B.BRANCH_PID FROM RD_BRANCH B, RD_BRANCH_DETAIL D, RD_BRANCH_NAME N, RD_BRANCH B1, RD_BRANCH_DETAIL D1, RD_BRANCH_NAME N1, RD_LINK L1, RD_LINK L2, RD_LINK L3 WHERE B.BRANCH_PID = {0} AND B.BRANCH_PID = D.BRANCH_PID AND D.DETAIL_ID = N.DETAIL_ID AND B.RELATIONSHIP_TYPE = 2 AND D.BRANCH_TYPE = 1 AND B1.BRANCH_PID = D1.BRANCH_PID AND D1.DETAIL_ID = N1.DETAIL_ID AND B1.RELATIONSHIP_TYPE = 2 AND D1.BRANCH_TYPE = 1 AND B.IN_LINK_PID = B1.IN_LINK_PID AND B.BRANCH_PID <> B1.BRANCH_PID AND N.NAME = N1.NAME AND L1.LINK_PID = B.IN_LINK_PID AND L2.LINK_PID = B.OUT_LINK_PID AND L3.LINK_PID = B1.OUT_LINK_PID AND L1.KIND IN (1, 2) AND L2.KIND IN (1, 2) AND L3.KIND IN (1, 2) AND B.U_RECORD <> 2 AND D.U_RECORD <> 2 AND N.U_RECORD <> 2 AND B1.U_RECORD <> 2 AND D1.U_RECORD <> 2 AND N1.U_RECORD <> 2 AND L1.U_RECORD <> 2 AND L2.U_RECORD <> 2 AND L3.U_RECORD <> 2 ";

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
