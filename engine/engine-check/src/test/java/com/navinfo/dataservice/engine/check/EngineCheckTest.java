package com.navinfo.dataservice.engine.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.engine.check.core.CheckRule;
import com.navinfo.dataservice.engine.check.core.CheckSuitLoader;
import com.navinfo.dataservice.engine.check.core.VariableName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.ChainLoader;
import com.vividsolutions.jts.geom.Geometry;

public class EngineCheckTest {
	@Before
	public void init() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	/**
	 * 批量跑某种操作的检查规则列表
	 * @throws Exception
	 */
	@Test
	public void exeCheckEngine() throws Exception{
		System.out.println("start");
		/*String str= "{ \"type\": \"LineString\",\"coordinates\": [ [116.17659, 39.97508], [116.16144, 39.94844],[116.20427, 39.94322],[116.20427, 39.94322], [116.17659, 39.97508] ]}";
		JSONObject geometry = JSONObject.fromObject(str);
		Geometry geometry2=GeoTranslator.geojson2Jts(geometry, 1, 5);
		link.setGeometry(geometry2);
		link.setPid(13474047);
		link.setsNodePid(2);
		link.seteNodePid(2);*/
		
		Connection conn = DBConnector.getInstance().getConnectionById(42);		
		
		RdBranchSelector selector=new RdBranchSelector(conn);
		IRow link=selector.loadById(3770, false, false);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(link);
		
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.UPDATE);
		checkCommand.setObjType(link.objType());
		
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.checkRuleList.add(getRule());;
		checkEngine.saveCheckResult(checkEngine.exePostCheck());
		conn.commit();
		System.out.println("end");
	}
	
	public CheckRule getRule(){
		String ruleCode="GLM08049";
		String ruleLog="log";
		String preRuleClass="com.navinfo.dataservice.engine.check.rules.GLM08049";
		String postRuleClass=" SELECT '','[RD_BRANCH,' || RB.BRANCH_PID || ']' TARGET,0,'方面分歧与IC分歧的向导代码不一致' LOGMSG"
				+ "  FROM RD_BRANCH RB, RD_BRANCH_DETAIL RBD1, RD_BRANCH_DETAIL RBD2"
				+ " WHERE RBD1.BRANCH_PID = RB.BRANCH_PID"
				+ " AND RBD2.BRANCH_PID = RB.BRANCH_PID"
				+ " AND RBD1.BRANCH_TYPE = 1"
				+ " AND RBD2.BRANCH_TYPE = 2"
				+ " AND RB.BRANCH_PID=RDBRANCH_PID"
				+ " AND RBD1.GUIDE_CODE <> RBD2.GUIDE_CODE";
		
		CheckRule rule=new CheckRule(ruleCode,ruleLog,1,"JAVA",preRuleClass,null,"SQL",postRuleClass,"RDBRANCH_PID");
		return rule;
	}
	/**
	 * 直接测试.java文件规则，主要用于单个规则的测试。java文件路径需修改方法public CheckRule getRule()
	 * @throws Exception
	 */
	@Test
	public void exeCheckRule() throws Exception{
		System.out.println("start");
		/*RdLink link=new RdLink();
		String str= "{ \"type\": \"LineString\",\"coordinates\": [ [116.17659, 39.97508], [116.16144, 39.94844],[116.20427, 39.94322],[116.20427, 39.94322], [116.17659, 39.97508] ]}";
		JSONObject geometry = JSONObject.fromObject(str);
		Geometry geometry2=GeoTranslator.geojson2Jts(geometry, 1, 5);
		link.setGeometry(geometry2);
		link.setPid(233335);
		link.setsNodePid(2);
		link.seteNodePid(2);
		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		int linkPid=226110;
		link = (RdLink) linkSelector.loadById(linkPid,false);*/
		Connection conn = DBConnector.getInstance().getConnectionById(42);
		
		RdBranchSelector selector=new RdBranchSelector(conn);
		IRow link=selector.loadById(3770, false, false);
		
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add((RdRestriction)link);
		
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(link.objType());
		
		CheckRule rule=getRule();
		ChainLoader loader=new ChainLoader();
		baseRule obj = (baseRule) rule.getPostRuleClass().newInstance();
		obj.setLoader(loader);
		obj.setRuleDetail(rule);
		obj.setConn(conn);
		//调用规则的后检查
		obj.postCheck(checkCommand);
		System.out.println("end");
	}
	
	public static void main(String[] args) throws Exception{
		EngineCheckTest test=new EngineCheckTest();
		test.init();
		test.exeCheckEngine();
		//test.exeCheckRule();
	}
}
