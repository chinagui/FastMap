package com.navinfo.dataservice.engine.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.check.core.CheckRule;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Geometry;

public class Test {
	public static void exeCheckEngine() throws Exception{
		RdLink link=new RdLink();
		String str= "{ \"type\": \"LineString\",\"coordinates\": [ [116.17659, 39.97508], [116.16144, 39.94844],[116.20427, 39.94322],[116.20427, 39.94322], [116.17659, 39.97508] ]}";
		JSONObject geometry = JSONObject.fromObject(str);
		Geometry geometry2=GeoTranslator.geojson2Jts(geometry, 1, 5);
		link.setGeometry(geometry2);
		link.setPid(13474047);
		link.setsNodePid(2);
		link.seteNodePid(2);
		
		Connection conn = GlmDbPoolManager.getInstance().getConnection(11);
		
		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		link = (RdLink) linkSelector.loadById(233335,false);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(link);
		
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setProjectId(11);
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(link.objType());
		
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.postCheck();
		conn.commit();
	}
	
	public static CheckRule getRule(){
		String ruleCode="GLM01197";
		String ruleLog="log";
		String ruleClass="com.navinfo.dataservice.engine.check.rules.GLM01197";
		
		CheckRule rule=new CheckRule(ruleCode,ruleLog,1,ruleClass,null,null,null);
		return rule;
	}
	
	public static void exeCheckRule() throws Exception{
		RdLink link=new RdLink();
		String str= "{ \"type\": \"LineString\",\"coordinates\": [ [116.17659, 39.97508], [116.16144, 39.94844],[116.20427, 39.94322],[116.20427, 39.94322], [116.17659, 39.97508] ]}";
		JSONObject geometry = JSONObject.fromObject(str);
		Geometry geometry2=GeoTranslator.geojson2Jts(geometry, 1, 5);
		link.setGeometry(geometry2);
		link.setPid(233335);
		link.setsNodePid(2);
		link.seteNodePid(2);
		
		Connection conn = GlmDbPoolManager.getInstance().getConnection(11);
		
		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		link = (RdLink) linkSelector.loadById(233335,false);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(link);
		
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setProjectId(11);
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(link.objType());
		
		CheckRule rule=getRule();
		baseRule obj = (baseRule) rule.getRuleClass().newInstance();
		obj.setRuleDetail(rule);
		obj.setConn(conn);
		//调用规则的后检查
		obj.postCheck(checkCommand);
		System.out.println("end");
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("start");
		exeCheckEngine();
		
	}
}
