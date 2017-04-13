package com.navinfo.dataservice.engine.check.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;


/** 
 * @ClassName: RDBRANCHOutLinkUnidirectional
 * @author songdongyan
 * @date 2016年8月19日
 * @Description: 退出Link为单方向且通行方向不能进入路口
 * 理解：退出link为单方向，则退出link沿通行方向与进入node联通
 */
public class RDBRANCHOutLinkUnidirectional extends baseRule {

	/**
	 * 
	 */
	public RDBRANCHOutLinkUnidirectional() {
		// TODO Auto-generated constructor stub
	}
	
	

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		
		for (IRow obj : checkCommand.getGlmList()) {
			// 获取新建RdBranch信息
			if (obj instanceof RdBranch) {
				
				RdBranch rdBranch = (RdBranch) obj;

				if (check(rdBranch)) {
					
					this.setCheckResult("", "", 0);
					
					return;
				}
			}
		}
	}
	
	private boolean check(RdBranch rdBranch)throws Exception
	{
		int relationshipType = rdBranch.getRelationshipType();
	
		if (rdBranch.changedFields().containsKey("relationshipType")) {
			
			relationshipType = Integer.parseInt(rdBranch.changedFields().get("relationshipType").toString());
		}
		
		if(relationshipType==2)
		{
			return false;
		}
		
		int outLinkPid = rdBranch.getOutLinkPid();
		
		if (rdBranch.changedFields().containsKey("outLinkPid")) {

			outLinkPid = Integer.parseInt(rdBranch.changedFields().get("outLinkPid").toString());
		}
		
		// 退出线通行方向终点Pid
		int outNodeFlag = getOutNodeFlag(outLinkPid);

		if (outNodeFlag == 0) {
			return false;
		}
	
		// 分歧进入点所在路口的路口组成nodePid
		Set<Integer> crossNodePids = getCrossNode(rdBranch.getNodePid());

		if (crossNodePids.contains(outNodeFlag)) {
			return true;
		}
		
		return false;	
	}
	
	private int getOutNodeFlag(Integer outLinkPid) throws Exception {

		String strFormat = "select t.e_node_pid as nodePid from rd_link t where t.link_pid = {0} and t.direct = 2 and t.u_record <> 2 UNION select t.s_node_pid as nodePid  from rd_link t where t.link_pid = {1} and t.direct = 3 and t.u_record <> 2";

		int outNodeFlag = 0;
		
		String sql = MessageFormat.format(strFormat, String.valueOf(outLinkPid),String.valueOf(outLinkPid));
	
		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList!=null &&resultList.size()>0)
		{
			outNodeFlag = Integer.parseInt(String.valueOf(resultList.get(0)));
		}
	

		return outNodeFlag;
	}

	
	
	private Set<Integer> getCrossNode(Integer nodePid) throws Exception {

		String strFormat = "select t.node_pid from rd_cross_node t where t.u_record <> 2 and t.pid in (select rc.pid from rd_cross_node rc where rc.node_pid = {0} and rc.u_record <> 2)";

		Set<Integer> crossNodePids = new HashSet<Integer>();

		String sql = MessageFormat.format(strFormat, String.valueOf(nodePid));

		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		resultList = getObj.exeSelect(this.getConn(), sql);

		for (Object obj : resultList) {

			int pid = Integer.parseInt(String.valueOf(obj));

			crossNodePids.add(pid);
		}

		return crossNodePids;
	}


	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}


}
