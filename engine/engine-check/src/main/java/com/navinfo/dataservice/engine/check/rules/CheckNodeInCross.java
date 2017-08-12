package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: CheckNodeInCross
 * @author songdongyan
 * @date 下午2:07:37
 * @Description: CheckNodeInCross.java
 */
public class CheckNodeInCross extends baseRule {
	
	public void preCheck(CheckCommand checkCommand) throws Exception {
		List<Integer> nodePids = new ArrayList<Integer>();
		
		for(IRow obj:checkCommand.getGlmList()){
			if(obj instanceof RdCross ){
				RdCross rdCross = (RdCross)obj;
						
				for(IRow deObj:rdCross.getNodes()){
					if(deObj instanceof RdCrossNode){
						RdCrossNode rdCrossNode = (RdCrossNode)deObj;
						nodePids.add(rdCrossNode.getPid());
					}
				}
			}
					
		}

		String sql = "select count(1) count from rd_cross_node a where a.node_pid in ("+StringUtils.join(nodePids,",")+") and a.u_record!=2 and exists (select null from rd_cross c where c.pid=a.pid and c.kg_flag=0 and c.u_record!=2)";
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = getConn().prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			boolean flag = false;
			if (resultSet.next()) {
				int count = resultSet.getInt("count");
				if (count > 0) {
					flag = true;
				}
			}
			if (flag) {
				this.setCheckResult("", "", 0);
				return;
			}
		}catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
		
	}
	

}
