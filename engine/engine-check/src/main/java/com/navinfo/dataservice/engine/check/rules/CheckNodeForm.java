package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/** 
 * @ClassName: CheckNodeForm
 * @author songdongyan
 * @date 上午11:25:08
 * @Description: CheckNodeForm.java
 */
public class CheckNodeForm extends baseRule {
	
	public void preCheck(CheckCommand checkCommand) throws Exception {
		String s = "";
		List<IRow> objList = checkCommand.getGlmList();
		for(int i=0; i<objList.size(); i++){
			IRow obj = objList.get(i);
			if (obj instanceof RdNode){
				RdNode rdNode = (RdNode)obj;
				int pid = rdNode.getPid();
				s += pid;
				if (i != objList.size()-1){
					s += ",";
				}
			}
		}

		String sql = "select count(1) count from rd_node_form where node_pid in ("+s+") and form_of_way=15";
		
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
		
		RdNode node = new RdNode();
		node.setPid(430174);
		
		Coordinate coord = new Coordinate(109.013388, 32.715519);
		GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint( coord );
        
        node.setGeometry(point);
		
		List<IRow> objList = new ArrayList<IRow>();
		objList.add(node);
		
		//ConfigLoader.initDBConn("E:/Users/songdongyan/java/DataService/DataService/web/edit-web/target/classes/config.properties");
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setProjectId(12);
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
//		checkCommand.setObjType(node.objType());
		checkCommand.setObjType(ObjType.RDCROSS);
		CheckEngine checkEngine=new CheckEngine(checkCommand);
		System.out.println(checkEngine.preCheck());
		
	}
}
