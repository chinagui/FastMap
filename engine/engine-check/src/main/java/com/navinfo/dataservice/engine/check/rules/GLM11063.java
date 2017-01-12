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
 * @ClassName GLM11063
 * @author luyao
 * @date 2017年1月12日
 * @Description TODO
 * 分歧名称中“中文”不允许有分号、逗号、空格、tab符；
 * 分歧名称信息编辑 服务端后检查:
 */
public class GLM11063  extends baseRule {

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
		
		String strFormat = "SELECT RB.BRANCH_PID FROM RD_BRANCH RB, RD_BRANCH_DETAIL RBD, RD_BRANCH_NAME RBN WHERE RB.BRANCH_PID =  RDBRANCH_PID  AND RB.BRANCH_PID = RBD.BRANCH_PID AND RBD.DETAIL_ID = RBN.DETAIL_ID AND RBN.LANG_CODE IN ('CHI','CHT') AND (INSTR(RBN.NAME, ' ') > 0 OR INSTR(RBN.NAME, '，') > 0 OR INSTR(RBN.NAME, '；') > 0 OR INSTR(RBN.NAME, CHR(9)) > 0 OR INSTR(RBN.NAME, ',') > 0 OR INSTR(RBN.NAME, ';') > 0) AND RB.U_RECORD <> 2 AND RBD.U_RECORD <> 2 AND RBN.U_RECORD <> 2";

		String sql = strFormat.replaceAll("RDBRANCH_PID", branchPid);

		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		resultList = getObj.exeSelect(this.getConn(), sql);

		if (!resultList.isEmpty()) {
			
			String target = "[RD_BRANCH," + branchPid + "]";
			
			this.setCheckResult("", target, 0);
		}
	}
}