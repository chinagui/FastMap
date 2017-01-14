package com.navinfo.dataservice.engine.check.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM05035
 * @author luyao
 * @date 2017年1月12日
 * @Description TODO
 * 名称来源字段值只能为“0”，否则报log；
 * 分歧名称信息编辑 服务端后检查:
 */
public class GLM05035 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

		for (IRow row : checkCommand.getGlmList()) {

			if (row instanceof RdBranchName) {
				
				if (row.status() == ObjStatus.DELETE) {
					continue;
				}

				RdBranchName name = (RdBranchName) row;

				int srcFlag = name.getSrcFlag();

				if (name.changedFields().containsKey("srcFlag")) {

					srcFlag = (int) name.changedFields().get("srcFlag");
				}
				
				if (srcFlag != 0) {

					String branchPid = getRdBranchPid(name.getDetailId());

					String target = "[RD_BRANCH," + branchPid + "]";

					this.setCheckResult("", target, 0);
				}
			}
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
