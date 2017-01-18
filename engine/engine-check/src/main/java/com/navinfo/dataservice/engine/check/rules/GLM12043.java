package com.navinfo.dataservice.engine.check.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;


/**
 * @ClassName GLM12043
 * @author luyao 
 * @date 2017年1月4日 
 * @Description TODO
 * 当有分歧名称时，分歧名称和名称发音字段均不能为空
 * Link种别变更 服务端后检查:
 */
public class GLM12043 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

		Set<Integer> detailIds = new HashSet<Integer>();

		for (IRow row : checkCommand.getGlmList()) {

			if (row instanceof RdBranchName) {

				if (row.status() == ObjStatus.DELETE) {
					continue;
				}

				RdBranchName branchName = (RdBranchName) row;

				String name = branchName.getName();

				if (branchName.changedFields().containsKey("name")) {

					name = String.valueOf(branchName.changedFields()
							.get("name"));
				}

				if (StringUtils.isEmpty(name)) {

					detailIds.add(branchName.getDetailId());

					continue;
				}

				String phonetic = branchName.getPhonetic();

				String langCode = branchName.getLangCode();

				if (branchName.changedFields().containsKey("langCode")) {

					langCode = String.valueOf(branchName.changedFields().get(
							"langCode"));
				}

				if (branchName.changedFields().containsKey("phonetic")) {

					phonetic = String.valueOf(branchName.changedFields().get(
							"phonetic"));
				}

				if (StringUtils.isEmpty(phonetic)
						&& (langCode.equals("CHI") || langCode.equals("CHT"))) {
					detailIds.add(branchName.getDetailId());
				}
			}
		}

		Set<String> pids = new HashSet<String>();

		for (int detailId : detailIds) {
			String RdBranchPid = getRdBranchPid(detailId);
			if (RdBranchPid != null) {
				pids.add(RdBranchPid);
			}
		}

		for (String branchPid : pids) {

			String target = "[RD_BRANCH," + branchPid + "]";

			this.setCheckResult("", target, 0);
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
}
