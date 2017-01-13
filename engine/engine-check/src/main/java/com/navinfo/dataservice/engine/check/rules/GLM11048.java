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
 * @ClassName GLM11048
 * @author luyao
 * @date 2017年1月12日
 * @Description TODO
 * 分歧名称不能超过35个汉字，拼音不能超过206个字符（去掉空格）；
 * 分歧名称信息编辑 服务端后检查:
 */
public class GLM11048 extends baseRule {

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
		
		String strFormat = "SELECT B.BRANCH_PID FROM RD_BRANCH B, RD_BRANCH_DETAIL D, RD_BRANCH_NAME N WHERE B.BRANCH_PID = RDBRANCH_PID AND B.BRANCH_PID = D.BRANCH_PID AND D.DETAIL_ID = N.DETAIL_ID AND B.U_RECORD != 2 AND D.U_RECORD != 2 AND N.U_RECORD != 2 AND LENGTH(N.NAME) > 35 AND N.LANG_CODE IN ('CHI', 'CHT') UNION SELECT B.BRANCH_PID FROM RD_BRANCH B, RD_BRANCH_DETAIL D, RD_BRANCH_NAME N WHERE B.BRANCH_PID =  RDBRANCH_PID  AND B.BRANCH_PID = D.BRANCH_PID AND D.DETAIL_ID = N.DETAIL_ID AND B.U_RECORD != 2 AND D.U_RECORD != 2 AND N.U_RECORD != 2 AND LENGTH(REPLACE(N.PHONETIC, ' ', '')) > 206 AND N.LANG_CODE IN ('CHI', 'CHT')";

		String sql =strFormat.replaceAll("RDBRANCH_PID", branchPid);

		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		resultList = getObj.exeSelect(this.getConn(), sql);

		if (!resultList.isEmpty()) {
			String target = "[RD_BRANCH," + branchPid + "]";
			
			this.setCheckResult("", target, 0);
		}
	}
}
