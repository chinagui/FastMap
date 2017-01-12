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
 * @ClassName GLM05031
 * @author luyao
 * @date 2017年1月12日
 * @Description TODO
 * 检查原则：
1.汉字数字“〇”名称中包含阿拉伯 数字“0”，如二0三北路应为二〇三北路
2.阿拉伯 数字“0”名称中包含汉字数字“〇”，如G1〇9应为G109
 *  分歧名称信息编辑 服务端后检查
 */
public class GLM05104 extends baseRule {

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

		String strFormat = "SELECT 'log1' log FROM RD_BRANCH RB, RD_BRANCH_DETAIL RBD, RD_BRANCH_NAME RBN WHERE RB.BRANCH_PID = RDBRANCH_PID AND RB.BRANCH_PID = RBD.BRANCH_PID AND RBD.DETAIL_ID = RBN.DETAIL_ID AND ((REGEXP_LIKE(RBN.NAME, '[〇一二三四五六七八九]+０') OR REGEXP_LIKE(RBN.NAME, '０[〇一二三四五六七八九]+'))) AND RB.U_RECORD <> 2 AND RBD.U_RECORD <> 2 AND RBN.U_RECORD <> 2 UNION SELECT 'log2' log FROM RD_BRANCH RB, RD_BRANCH_DETAIL RBD, RD_BRANCH_NAME RBN WHERE RB.BRANCH_PID =  RDBRANCH_PID  AND RB.BRANCH_PID = RBD.BRANCH_PID AND RBD.DETAIL_ID = RBN.DETAIL_ID AND ((REGEXP_LIKE(RBN.NAME, '[０１２３４５６７８９]+〇') OR REGEXP_LIKE(RBN.NAME, '〇[０１２３４５６７８９]+'))) AND RB.U_RECORD <> 2 AND RBD.U_RECORD <> 2 AND RBN.U_RECORD <> 2";

		String sql  =strFormat.replaceAll("RDBRANCH_PID", branchPid);
		
		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		resultList = getObj.exeSelect(this.getConn(), sql);

		for (Object obj : resultList) {
			String target = "[RD_BRANCH," + branchPid + "]";

			String log = String.valueOf(obj);

			if (log.equals("log1")) {
				this.setCheckResult("", target, 0,
						"分歧名称存在汉字数字“〇”名称中包含阿拉伯 数字“0”");
			} else if (log.equals("log2")) {
				this.setCheckResult("", target, 0,
						"分歧名称中存在阿拉伯 数字“0”名称中包含汉字数字“〇”");
			}
		}
	}
}
