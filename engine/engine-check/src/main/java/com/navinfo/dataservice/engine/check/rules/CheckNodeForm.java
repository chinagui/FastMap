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
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: CheckNodeForm
 * @author songdongyan
 * @date 上午11:25:08
 * @Description: CheckNodeForm.java
 */
public class CheckNodeForm extends baseRule {
	
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
		

		String sql = "select count(1) count from rd_node_form where U_RECORD != 2 AND node_pid in ("+StringUtils.join(nodePids,",")+") and form_of_way=15";
		
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
	
	
	public static void main(String[] args) throws Exception {
		RdCross rdCross = new RdCross();
		List<IRow> rdCrossNodes = new ArrayList<IRow>();
		
		RdCrossNode rdCrossNode1 = new RdCrossNode();
		rdCrossNode1.setNodePid(503216);
		rdCrossNodes.add(rdCrossNode1);
		
		RdCrossNode rdCrossNode2 = new RdCrossNode();
		rdCrossNode2.setNodePid(503294);
		rdCrossNodes.add(rdCrossNode2);
		
		RdCrossNode rdCrossNode3 = new RdCrossNode();
		rdCrossNode3.setNodePid(505591);
		rdCrossNodes.add(rdCrossNode3);
		
		RdCrossNode rdCrossNode4 = new RdCrossNode();
		rdCrossNode4.setNodePid(507090);
		rdCrossNodes.add(rdCrossNode4);
		
		rdCross.setNodes(rdCrossNodes);
		
		List<IRow> objList = new ArrayList<IRow>();
		objList.add(rdCross);

		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
//		checkCommand.setObjType(node.objType());
		checkCommand.setObjType(ObjType.RDCROSS);
		CheckEngine checkEngine=new CheckEngine(checkCommand);
		System.out.println(checkEngine.preCheck());
		
	}
}
