package com.navinfo.dataservice.engine.check.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;


/**
 * @ClassName GLM05088
 * @author luyao 
 * @date 2017年1月4日 
 * @Description TODO
 * 3实景看板的进入线必须为高速或城高
 * Link种别变更 服务端后检查:
 */
public class GLM05088 extends baseRule {
	
	protected Logger log = Logger.getLogger(this.getClass());

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

		Set<Integer> linkPids = new HashSet<Integer>();

		for (IRow row : checkCommand.getGlmList()) {

			if (row instanceof RdLink) {

				RdLink rdLink = (RdLink) row;

				int kind = rdLink.getKind();

				if (rdLink.changedFields().containsKey("kind")) {

					kind = (int) rdLink.changedFields().get("kind");

					if (kind != 1 || kind != 2) {
						linkPids.add(rdLink.getPid());
					}
				}
			}
		}
		
		ArrayList<Integer> pidTmp = new ArrayList<Integer>();

		for (Integer pid : linkPids) {

			pidTmp.add(pid);

			if (pidTmp.size() > 99) {

				this.checkRdLink(pidTmp);

				pidTmp.clear();
			}
		}

		this.checkRdLink(pidTmp);
	}
	


	/**
	 * @author luyao
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(List<Integer>linkPids) throws Exception {
		
		if (linkPids.size() < 1) {
			return;
		}
		
		String strFormat = "SELECT DISTINCT B.BRANCH_PID FROM RD_SIGNASREAL S, RD_BRANCH B, RD_LINK L WHERE L.LINK_PID IN ({0}) AND L.KIND NOT IN (1, 2) AND B.IN_LINK_PID = L.LINK_PID AND S.BRANCH_PID = B.BRANCH_PID AND S.U_RECORD <> 2 AND B.U_RECORD <> 2 AND L.U_RECORD <> 2";
		
		String ids = org.apache.commons.lang.StringUtils.join(linkPids, ",");
		
		Set<Integer> branchPids = new HashSet<Integer>();
	
		String sql = MessageFormat.format(strFormat, ids);

		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		resultList = getObj.exeSelect(this.getConn(), sql);

		for (Object obj : resultList) {

			int pid = Integer.parseInt(String.valueOf(obj));

			branchPids.add(pid);
		}
		
		for (int branchPid : branchPids) {

			String target = "[RD_BRANCH," + branchPid + "]";

			this.setCheckResult("", target, 0);
		}
	}
}
