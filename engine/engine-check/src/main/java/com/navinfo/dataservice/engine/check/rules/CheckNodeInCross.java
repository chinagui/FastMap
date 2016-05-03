package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

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
		
		PreparedStatement pstmt = getConn().prepareStatement(sql);

		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		if (resultSet.next()) {

			int count = resultSet.getInt("count");

			if (count > 0) {
				flag = true;
			}
		}

		resultSet.close();

		pstmt.close();

		if (flag) {
			
			this.setCheckResult("", "", 0);
			return;

		}
		
		
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
		
	}
	

}
