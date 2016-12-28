package com.navinfo.dataservice.engine.check.rules;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.database.sql.DBUtils;


/** 
 * @ClassName: GLM13002
 * @author luyao
 * @date 2016年12月27日
 * @Description: 
 * 收费站	word	GLM13034	后台	
 * 检查对象:RD_LINK
 * 检查原则：关系型收费站主点的挂接link数必须是2
 * */
public class GLM13002 extends baseRule {

	/**
	 * 
	 */
	public GLM13002() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		

		Map<Integer, Set<Integer>> nodeLinkMap = new HashMap<Integer, Set<Integer>>();

		for (IRow obj : checkCommand.getGlmList()) {

			if (obj instanceof RdLink) {

				RdLink link = (RdLink) obj;

				int sNodePid = link.getsNodePid();

				int eNodePid = link.geteNodePid();

				if (link.changedFields().containsKey("sNodePid")) {
					sNodePid = (Integer) link.changedFields().get("sNodePid");
				}
				if (link.changedFields().containsKey("eNodePid")) {
					eNodePid = (Integer) link.changedFields().get("eNodePid");
				}

				if (!nodeLinkMap.containsKey(sNodePid)) {
					Set<Integer> linkPids = new HashSet<Integer>();
					nodeLinkMap.put(sNodePid, linkPids);
				}
				if (!nodeLinkMap.containsKey(eNodePid)) {
					Set<Integer> linkPids = new HashSet<Integer>();
					nodeLinkMap.put(eNodePid, linkPids);
				}
				nodeLinkMap.get(sNodePid).add(link.getPid());

				nodeLinkMap.get(eNodePid).add(link.getPid());
			}
		}		
		
		if(nodeLinkMap.size()<1)
		{
			return;
		}

		preCheck(nodeLinkMap);
	}
	
	private void preCheck(Map<Integer, Set<Integer>> nodeLinkMap)
			throws Exception {

		List<Integer> nodePids = new ArrayList<Integer>();

		nodePids.addAll(nodeLinkMap.keySet());

		String inClause = null;

		Clob pidClod = null;

		String ids = StringUtils.getInteStr(nodePids);

		if (nodePids.size() > 1000) {

			pidClod = this.getConn().createClob();

			pidClod.setString(1, ids);

			inClause = " IN (select to_number(pid) from table(clob_to_table(?))) ";

		} else {

			inClause = " IN ( " + ids + " ) ";
		}

		String sql = "SELECT LINK_PID,N.NODE_PID FROM RD_LINK T, RD_NODE N WHERE N.NODE_PID IN (SELECT T.NODE_PID FROM RD_TOLLGATE T WHERE T.NODE_PID  "
				+ inClause
				+ " ) AND (T.S_NODE_PID = N.NODE_PID OR T.E_NODE_PID = N.NODE_PID)";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.getConn().prepareStatement(sql);

			if (nodePids.size() > 1000) {
				
				pstmt.setClob(1, pidClod);
			}

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				int nodePid = resultSet.getInt("NODE_PID");
				
				int linkPid = resultSet.getInt("LINK_PID");

				if (nodeLinkMap.containsKey(nodePid)) {
					
					nodeLinkMap.get(nodePid).add(linkPid);

					if (nodeLinkMap.get(nodePid).size() > 2) {
						
						this.setCheckResult("", "", 0);
						
						return;
					}
				}
			}
			
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			
			DBUtils.closeResultSet(resultSet);
			
			DBUtils.closeStatement(pstmt);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

}
