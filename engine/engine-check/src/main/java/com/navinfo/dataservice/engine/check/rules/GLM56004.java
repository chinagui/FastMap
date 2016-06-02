package com.navinfo.dataservice.engine.check.rules;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
//GLM56004	修形中产生自相交，要提示立交或打断	修形中产生自相交，要提示立交或打断	
//1	LC_LINK、RW_LINK、RD_LINK、CMG_BUILDLINK、ADAS_LINK、AD_LINK、ZONE_LINK、LU_LINK	新增link


public class GLM56004 extends baseRule {

	public GLM56004() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				
				StringBuilder sb = new StringBuilder();
		        sb.append("select a.link_pid from rd_link a,rd_link b where a.link_pid =");
		        sb.append(rdLink.getPid());
		        sb.append(" and a.u_record!=2 and b.link_pid !=");
		        sb.append(rdLink.getPid());
		        sb.append("and b.u_record!=2 and b.s_node_pid not in (a.s_node_pid,a.e_node_pid) and b.e_node_pid not in (a.s_node_pid,a.e_node_pid) and sdo_relate(b.geometry,a.geometry,'mask=anyinteract')='TRUE'");
				String sql = sb.toString();
				
		        DatabaseOperator getObj=new DatabaseOperator();
				List<Object> resultList=new ArrayList<Object>();
				resultList=getObj.exeSelect(this.getConn(), sql);
				
				if (resultList.size()>0){
					Geometry pointGeo=GeoHelper.getPointFromGeo(rdLink.getGeometry());
					String pointWkt = GeoTranslator.jts2Wkt(pointGeo, 0.00001, 5);
					this.setCheckResult(pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
					}
				}
			}
		}
	
	public static void main(String[] args) throws Exception{
		RdLink link=new RdLink();
		String str= "{ \"type\": \"LineString\",\"coordinates\": [ [116.17659, 39.97508], [116.16144, 39.94844],[116.20427, 39.94322],[116.20427, 39.94322], [116.17659, 39.97508] ]}";
		JSONObject geometry = JSONObject.fromObject(str);
		Geometry geometry2=GeoTranslator.geojson2Jts(geometry, 1, 5);
		link.setGeometry(geometry2);
		link.setPid(12962846);
		link.setsNodePid(2);
		link.seteNodePid(2);
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(link);
		
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setProjectId(11);
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.UPDATE);
		checkCommand.setObjType(link.objType());
		
		//ConfigLoader.initDBConn("D:/workfiles/0_svn/fastmap-hithub/web/edit-web/target/classes/config.properties");
		Connection conn = GlmDbPoolManager.getInstance().getConnection(checkCommand.getProjectId());
		GLM56004 glm=new GLM56004();
		glm.setConn(conn);
		glm.postCheck(checkCommand);	
		glm.getCheckResultList();
	}
	
	}

class getResultList extends DatabaseOperator{
	
	public List<Object> settleResultSet(ResultSet resultSet) throws Exception{
		List<Object> resultList=new ArrayList<Object>();
		while (resultSet.next()){
			resultList.add(resultSet.getString(1));
		} 
		return resultList;
	}
	
	public static void main(String[] args) throws Exception{
		String sql="select a.link_pid from rd_link a";
		Connection conn=GlmDbPoolManager.getInstance().getConnection(11);
		getResultList getObj=new getResultList();
		List<Object> resultList=new ArrayList<Object>();
		resultList=getObj.exeSelect(conn, sql);
		System.out.println("end");
	}
}


