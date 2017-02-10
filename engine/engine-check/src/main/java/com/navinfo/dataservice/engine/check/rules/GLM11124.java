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
 * @ClassName GLM11124
 * @author luyao 
 * @date 2017年1月11日 
 * @Description TODO
 * 进入线、退出线任意一条是8级及8级以下道路等级，则报log
 * 新增\修改高速实景图分歧 服务端后检查:
 * Link种别变更 服务端后检查:
 */
public class GLM11124 extends baseRule {
	
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

				if (kind >= 8) {
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
		
		String strFormat = "SELECT T.BRANCH_PID FROM RD_BRANCH T, RD_BRANCH_REALIMAGE R WHERE (T.IN_LINK_PID = {0} OR T.OUT_LINK_PID = {1}) AND R.BRANCH_PID = T.BRANCH_PID AND T.U_RECORD <> 2 AND R.U_RECORD <> 2";
		
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

			boolean check = check(branchPid);

			if (check) {

				String target = "[RD_BRANCH," + branchPid + "]";

				this.setCheckResult("", target, 0);
			}
		}
	}
	
	
	
	private boolean check(int branchPid) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		
		String strFormat="SELECT  RB.BRANCH_PID FROM RD_BRANCH RB, RD_BRANCH_REALIMAGE RBR, RD_LINK RL WHERE RB.BRANCH_PID = {0} AND RB.BRANCH_PID = RBR.BRANCH_PID AND RB.U_RECORD != 2 AND RBR.U_RECORD != 2 AND RL.U_RECORD != 2 AND RBR.IMAGE_TYPE = 0 AND (RL.LINK_PID = RB.IN_LINK_PID OR RL.LINK_PID = RB.OUT_LINK_PID) AND RL.KIND >= 8";
		
		String sql =MessageFormat.format(strFormat,String.valueOf( branchPid),String.valueOf( branchPid));			
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}

}
