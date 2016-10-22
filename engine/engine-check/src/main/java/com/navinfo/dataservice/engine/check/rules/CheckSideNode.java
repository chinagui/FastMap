package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: CheckSideNode
 * @author songdongyan
 * @date 下午3:02:10
 * @Description: CheckSideNode.java
 */
public class CheckSideNode extends baseRule {
	
	public void preCheck(CheckCommand checkCommand) throws Exception {
		List<Integer> nodePids = new ArrayList<Integer>();
		
		for(IRow obj:checkCommand.getGlmList()){
			if(obj instanceof RdCross ){
				RdCross rdCross = (RdCross)obj;
						
				for(IRow deObj:rdCross.getNodes()){
					if(deObj instanceof RdCrossNode){
						RdCrossNode rdCrossNode = (RdCrossNode)deObj;
						nodePids.add(rdCrossNode.getNodePid());
					}
				}
			}
					
		}
		
		String sql = "select count(1) count from rd_link where (e_node_pid=:1 or s_node_pid=:2) AND U_RECORD != 2";

		PreparedStatement pstmt = getConn().prepareStatement(sql);
		
		for (int nodePid : nodePids) {

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, nodePid);

			ResultSet resultSet = pstmt.executeQuery();

			boolean flag = false;

			if (resultSet.next()) {

				int count = resultSet.getInt("count");

				if (count <= 1) {
					flag = true;
				}
			}

			resultSet.close();

			if (flag) {		
					
				this.setCheckResult("", "", 0);
				return;

			}
				
		}
		
		pstmt.close();

	}

	
	public void postCheck(CheckCommand checkCommand) {
		
	}

}
