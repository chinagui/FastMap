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
 * @ClassName GLM12004
 * @author luyao 
 * @date 2017年1月4日 
 * @Description TODO
 * 3d的进入线和退出线种别不能同时是高速（包括高速和城高种别）或者是9级路
 * 新增\修改3D分歧 服务端后检查:
 * Link种别变更 服务端后检查:
 */
public class GLM12004 extends baseRule {
	
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

				if (!rdLink.changedFields().containsKey("kind")) {

					continue;
				}

				int kind = (int) rdLink.changedFields().get("kind");
				
				if (kind == 1 || kind == 2 || kind == 9) {
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
		
		boolean check = this.check(rdBranch.getPid());

		if(check){
			
			String target = "[RD_BRANCH," + rdBranch.getPid() + "]";
			
			this.setCheckResult("", target, 0);
		}
	}

	/**
	 * @author luyao
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(Set<Integer>linkPids) throws Exception {
		
		String strFormat = "SELECT T.BRANCH_PID FROM RD_BRANCH T, RD_BRANCH_DETAIL D WHERE (T.IN_LINK_PID = {0} OR T.OUT_LINK_PID = {1}) AND D.BRANCH_PID = T.BRANCH_PID AND D.BRANCH_TYPE = 3 AND T.U_RECORD <> 2 AND D.U_RECORD <> 2";

		Set<Integer> branchPids = new HashSet<Integer>();

		for (int linkPid : linkPids) {

			String sql = MessageFormat.format(strFormat,String.valueOf( linkPid), String.valueOf( linkPid));

			log.info("后检查GLM12004--sql:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			
			List<Object> resultList = new ArrayList<Object>();
			
			resultList = getObj.exeSelect(this.getConn(), sql);

			for (Object obj : resultList) {

				int pid = Integer.parseInt(String.valueOf(obj));
				
				branchPids.add(pid);
			}
		}
		for (int branchPid : branchPids) {
			
			boolean check =check(branchPid);
			
			if(check){
				
				String target = "[RD_BRANCH," + branchPid + "]";
				
				this.setCheckResult("", target, 0);
			}
		}
	}
	
	/**
	 * @author luyao
	 * @param 
	 * @throws Exception 
	 */
	private boolean check(int branchPid) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		
		String strFormat="SELECT B.BRANCH_PID FROM RD_BRANCH B, RD_BRANCH_DETAIL D, RD_LINK L1, RD_LINK L2 WHERE B.BRANCH_PID = D.BRANCH_PID AND D.BRANCH_TYPE = 3 AND B.IN_LINK_PID = L1.LINK_PID AND B.OUT_LINK_PID = L2.LINK_PID AND L1.KIND IN (1, 2) AND L2.KIND IN (1, 2) AND B.BRANCH_PID = {0} AND (D.PATTERN_CODE IS NULL OR D.PATTERN_CODE NOT IN ('80000001', '80000002', '80000003', '80000004', '80000200', '80100000', '80100001', '80000800', '80000802', '80000801', '80000803', '80000000')) AND L1.U_RECORD <> 2 AND L2.U_RECORD <> 2 AND B.U_RECORD <> 2 AND D.U_RECORD <> 2 UNION ALL SELECT B.BRANCH_PID FROM RD_BRANCH B, RD_BRANCH_DETAIL D, RD_LINK L1, RD_LINK L2 WHERE B.BRANCH_PID = D.BRANCH_PID AND D.BRANCH_TYPE = 3 AND B.IN_LINK_PID = L1.LINK_PID AND B.OUT_LINK_PID = L2.LINK_PID AND L1.KIND = 9 AND L2.KIND = 9 AND B.BRANCH_PID = {1} AND (D.PATTERN_CODE IS NULL OR D.PATTERN_CODE NOT IN ('80000001', '80000002', '80000003', '80000004', '80000200', '80100000', '80100001', '80000800', '80000802', '80000801', '80000803', '80000000')) AND B.U_RECORD <> 2 AND D.U_RECORD <> 2 AND L1.U_RECORD <> 2 AND L2.U_RECORD <> 2 ";
		
		String sql =MessageFormat.format(strFormat,String.valueOf( branchPid),String.valueOf( branchPid));		
	
		log.info("后检查GLM12004--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}

}
