package com.navinfo.dataservice.engine.check.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;


/**
 * @ClassName GLM11024
 * @author luyao 
 * @date 2017年1月11日 
 * @Description TODO
 * 该方面分歧有分歧模式图号，那么进入线和退出线都必须是高速或城高，否则报Log1；该方面分歧没有分歧模式图号，那么该分歧应该是普通道路方面分歧且进入线是普通道路，否则报log2
 * 新增\修改方面分歧 服务端后检查:
 * Link种别变更 服务端后检查:
 */
public class GLM11024 extends baseRule {
	
	protected Logger log = Logger.getLogger(this.getClass());

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

		Set<Integer> linkPids = new HashSet<Integer>();

		for (IRow row : checkCommand.getGlmList()) {

			if (row instanceof RdBranch) {

				RdBranch rdBranch = (RdBranch) row;

				this.checkRdBranch(rdBranch);

			} else if (row instanceof RdLink) {

				RdLink rdLink = (RdLink) row;

				if (rdLink.changedFields().containsKey("kind")) {

					linkPids.add(rdLink.getPid());
				}
			}
		}

		this.checkRdLink(linkPids);
	}
	
	/**
	 * @author luyao
	 * @param rdNode
	 * @throws Exception 
	 */
	private void checkRdBranch(RdBranch rdBranch) throws Exception {
		
		String strLog = check(rdBranch.getPid());

		if (strLog != null) {

			String target = "[RD_BRANCH," + rdBranch.getPid() + "]";

			this.setCheckResult("", target, 0, strLog);
		}
	}

	/**
	 * @author luyao
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(Set<Integer>linkPids) throws Exception {
		
		String strFormat = "SELECT T.BRANCH_PID FROM RD_BRANCH T, RD_BRANCH_DETAIL D WHERE (T.IN_LINK_PID = {0} OR T.OUT_LINK_PID = {1}) AND D.BRANCH_PID = T.BRANCH_PID AND D.BRANCH_TYPE = 1 AND T.U_RECORD <> 2 AND D.U_RECORD <> 2";

		Set<Integer> branchPids = new HashSet<Integer>();

		for (int linkPid : linkPids) {

			String sql = MessageFormat.format(strFormat,String.valueOf( linkPid), String.valueOf( linkPid));

			DatabaseOperator getObj = new DatabaseOperator();
			
			List<Object> resultList = new ArrayList<Object>();
			
			resultList = getObj.exeSelect(this.getConn(), sql);

			for (Object obj : resultList) {

				int pid = Integer.parseInt(String.valueOf(obj));
				
				branchPids.add(pid);
			}
		}
		for (int branchPid : branchPids) {

			String strLog = check(branchPid);

			if (strLog != null) {

				String target = "[RD_BRANCH," + branchPid + "]";

				this.setCheckResult("", target, 0, strLog);
			}
		}
	}
	
	/**
	 * @author luyao
	 * @param 
	 * @throws Exception 
	 */
	private String check(int branchPid) throws Exception {
		// TODO Auto-generated method stub
		String strLog = null;
		
		String strFormat="SELECT ''高速分歧进入退出线必须都是高速或成高'' AS LOG FROM RD_BRANCH B, RD_BRANCH_DETAIL D, RD_LINK L1, RD_LINK L2 WHERE B.BRANCH_PID = {0} AND B.BRANCH_PID = D.BRANCH_PID AND B.IN_LINK_PID = L1.LINK_PID AND B.OUT_LINK_PID = L2.LINK_PID AND D.PATTERN_CODE IS NOT NULL AND D.BRANCH_TYPE = 1 AND (L1.KIND NOT IN (1, 2) OR L2.KIND NOT IN (1, 2)) AND B.U_RECORD <> 2 AND D.U_RECORD <> 2 AND L1.U_RECORD <> 2 AND L2.U_RECORD <> 2 UNION ALL SELECT ''普通分歧进入退出线必须都是普通道路'' AS LOG FROM RD_BRANCH B, RD_BRANCH_DETAIL D, RD_LINK L1 WHERE B.BRANCH_PID = {1} AND B.BRANCH_PID = D.BRANCH_PID AND B.IN_LINK_PID = L1.LINK_PID AND D.BRANCH_TYPE = 1 AND D.PATTERN_CODE IS NULL AND L1.KIND IN (1, 2) AND B.U_RECORD <> 2 AND D.U_RECORD <> 2 AND L1.U_RECORD <> 2";
		
		String sql = MessageFormat.format(strFormat, String.valueOf(branchPid),
				String.valueOf(branchPid), String.valueOf(branchPid));	
	
		log.info("后检查GLM11024--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if (!resultList.isEmpty()) {
			strLog = String.valueOf(resultList.get(0));
		}
		return strLog;
	}

}
