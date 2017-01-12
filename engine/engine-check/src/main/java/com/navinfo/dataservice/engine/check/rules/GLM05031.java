package com.navinfo.dataservice.engine.check.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM05031
 * @author luyao
 * @date 2017年1月12日
 * @Description TODO
 * 分歧类型为“0”、“3”或“4”时必须无分歧名称，否则报log
 * 分歧类型编辑 服务端后检查: 分歧名称信息编辑 服务端后检查:
 */
public class GLM05031 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

		Set<String> pids = new HashSet<String>();

		for (IRow row : checkCommand.getGlmList()) {

			if (row instanceof RdBranchDetail) {

				RdBranchDetail detail = (RdBranchDetail) row;

				pids.add(String.valueOf( detail.getBranchPid()));

			} else if (row instanceof RdBranchName) {

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
		
		String strFormat = "SELECT RBD.BRANCH_PID FROM RD_BRANCH_DETAIL RBD WHERE RBD.BRANCH_PID = {0} AND RBD.BRANCH_TYPE IN (0, 3, 4) AND RBD.U_RECORD <> 2 AND EXISTS (SELECT 1 FROM RD_BRANCH_NAME RBN WHERE RBN.DETAIL_ID = RBD.DETAIL_ID AND RBN.U_RECORD <> 2)";
		
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
