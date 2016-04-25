package com.navinfo.dataservice.engine.check.rules;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Coordinate;;

/** 
 * @ClassName: CheckSideNode
 * @author songdongyan
 * @date 下午3:02:10
 * @Description: CheckSideNode.java
 */
public class CheckSideNode extends baseRule {
	
	private String ruleLog = "盲端不允许创建路口";
	
	public void preCheck(CheckCommand checkCommand) throws Exception {
//		this.conn = DBOraclePoolManager.getConnection(checkCommand.getProjectId());
		
		String sql = "select count(1) count from rd_link where e_node_pid=:1 or s_node_pid=:2";

		PreparedStatement pstmt = getConn().prepareStatement(sql);
		
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdNode){
				RdNode rdNode = (RdNode)obj;
				
				int nodePid = rdNode.getPid();
				
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
//					throwException("盲端不允许创建路口");
//					throw new Exception(ruleLog);
					this.setCheckResult("", "[RD_NODE,"+nodePid+"]", rdNode.mesh());
					return;

				}
				
			}
		}
		
		pstmt.close();
		
	}

	
	public void postCheck(CheckCommand checkCommand) {
		
	}

	
	public static void main(String[] args) throws Exception{
		
		RdNode node = new RdNode();
		node.setPid(430174);
		
		List<IRow> objList = new ArrayList<IRow>();
		objList.add(node);
		
//		ConfigLoader.initDBConn("D:/workfiles/0_svn/fastmap-hithub/web/edit-web/target/classes/config.properties");
		ConfigLoader.initDBConn("E:/Users/songdongyan/java/DataService/DataService/web/edit-web/target/classes/config.properties");
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setProjectId(12);
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(node.objType());
		CheckEngine checkEngine=new CheckEngine(checkCommand);
		System.out.println(checkEngine.preCheck());	
	}
}
