package com.navinfo.dataservice.engine.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGateCondition;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneCondition;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoVia;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.engine.check.core.CheckRule;
import com.navinfo.dataservice.engine.check.rules.CrossingLaneOutlinkDirect;
import com.navinfo.dataservice.engine.check.rules.GLM01017;
import com.navinfo.dataservice.engine.check.rules.GLM01091;
import com.navinfo.dataservice.engine.check.rules.GLM01570;
import com.navinfo.dataservice.engine.check.rules.GLM04003;
import com.navinfo.dataservice.engine.check.rules.GLM04006;
import com.navinfo.dataservice.engine.check.rules.GLM04008_1;
import com.navinfo.dataservice.engine.check.rules.GLM04008_2;
import com.navinfo.dataservice.engine.check.rules.GLM08004;
import com.navinfo.dataservice.engine.check.rules.GLM19001_2;
import com.navinfo.dataservice.engine.check.rules.GLM19001_3;
import com.navinfo.dataservice.engine.check.rules.GLM19014;
import com.navinfo.dataservice.engine.check.rules.GLM26044;
import com.navinfo.dataservice.engine.check.rules.GLM28018;
import com.navinfo.dataservice.engine.check.rules.GLM32005;
import com.navinfo.dataservice.engine.check.rules.GLM32006;
import com.navinfo.dataservice.engine.check.rules.GLM32020;
import com.navinfo.dataservice.engine.check.rules.GLM32021;
import com.navinfo.dataservice.engine.check.rules.GLM32038;
import com.navinfo.dataservice.engine.check.rules.GLM32049;
import com.navinfo.dataservice.engine.check.rules.GLM32051;
import com.navinfo.dataservice.engine.check.rules.GLM32052;
import com.navinfo.dataservice.engine.check.rules.GLM32060;
import com.navinfo.dataservice.engine.check.rules.GLM32071;
import com.navinfo.dataservice.engine.check.rules.GLM32092;
import com.navinfo.dataservice.engine.check.rules.GLM32093;
import com.navinfo.dataservice.engine.check.rules.PERMIT_CHECK_LANE_PASSLINKS_LESS15;
import com.navinfo.dataservice.engine.check.rules.RELATING_CHECK_CROSS_RELATION_MUST_CONNECTED1;
import com.navinfo.dataservice.engine.check.rules.RdLane002;

/** 
 * @ClassName: CheckRuleTest
 * @author songdongyan
 * @date 2016年8月20日
 * @Description: CheckRuleTest.java
 */
public class CheckRuleTest {

	/**
	 * 
	 */
	public CheckRuleTest() {
		// TODO Auto-generated constructor stub
	}

	@Before
	public void init() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	
	@Test
	public void exeRdBranchCheck() throws Exception{
		List<IRow> vias = new ArrayList<IRow>();
		RdBranchVia rdBranchVia = new RdBranchVia();
		rdBranchVia.setLinkPid(284053);
		vias.add(rdBranchVia);
		RdBranchVia rdBranchVia_1 = new RdBranchVia();
		rdBranchVia_1.setLinkPid(277564);
		vias.add(rdBranchVia_1);
		
		
		RdBranch rdBranch = new RdBranch();
		rdBranch.setInLinkPid(277563);
		rdBranch.setNodePid(280385);
		rdBranch.setOutLinkPid(273327);
		rdBranch.setVias(vias);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdBranch);
		
		Connection conn = DBConnector.getInstance().getConnectionById(42);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(ObjType.RDBRANCH);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		System.out.println(checkEngine.preCheck());
	}
	
	@Test
	public void exeRdGateCheck() throws Exception{
		RdGate rdGate = new RdGate();
		rdGate.setInLinkPid(313164);
		rdGate.setOutLinkPid(303428);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdGate);
		
		Connection conn = DBConnector.getInstance().getConnectionById(42);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(ObjType.RDGATE);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.postCheck();
		System.out.println(checkEngine.preCheck());
	}
	
	@Test
	public void exeRdGateUpdateCheck() throws Exception{
		RdGate rdGate = new RdGate();
		rdGate.setInLinkPid(58155839);
		rdGate.setOutLinkPid(59193232);
		rdGate.setNodePid(2030755);
		rdGate.setDir(1);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdGate);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.UPDATE);
		checkCommand.setObjType(ObjType.RDGATE);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.postCheck();
		System.out.println(checkEngine.preCheck());
	}
	
	
	@Test
	public void testGLM32005() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLane rdLane  = new RdLane();
		rdLane.setLaneDir(2);
		rdLane.setPid(31434086);
		rdLane.setLinkPid(39257545);
		glmList.add(rdLane);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(39257545);
		rdLink.setDirect(2);
		glmList.add(rdLink);
		
		cc.setGlmList(glmList);
		GLM32005 c = new GLM32005();
		c.setConn(conn);
		c.postCheck(cc);
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM32038() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLane rdLane  = new RdLane();
		rdLane.setLaneDivider(1);;
		rdLane.setPid(31434086);
		rdLane.setLinkPid(39257545);
		glmList.add(rdLane);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(39257545);
		glmList.add(rdLink);
		
		RdLinkForm rdLinkForm = new RdLinkForm();
		rdLinkForm.setFormOfWay(50);
		rdLinkForm.setLinkPid(39257545);
		glmList.add(rdLinkForm);
		
		cc.setGlmList(glmList);
		GLM32038 c = new GLM32038();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM01570() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdGate rdGate = new RdGate();
		rdGate.setInLinkPid(39257545);
		rdGate.setOutLinkPid(39257546);
		glmList.add(rdGate);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(39257545);
		glmList.add(rdLink);
		
		RdLinkForm rdLinkForm = new RdLinkForm();
		rdLinkForm.setFormOfWay(60);
		rdLinkForm.setLinkPid(39257545);
		glmList.add(rdLinkForm);
		
		cc.setGlmList(glmList);
		GLM01570 c = new GLM01570();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM01091() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdGate rdGate = new RdGate();
		rdGate.setInLinkPid(39257545);
		rdGate.setOutLinkPid(39257546);
		glmList.add(rdGate);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(39257545);
		rdLink.setFunctionClass(4);
		glmList.add(rdLink);
		
		
		cc.setGlmList(glmList);
		GLM01091 c = new GLM01091();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM04003() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdGate rdGate = new RdGate();
		rdGate.setInLinkPid(39257545);
		rdGate.setOutLinkPid(39257546);
		rdGate.setType(0);
		glmList.add(rdGate);

		cc.setGlmList(glmList);
		GLM04003 c = new GLM04003();
		c.setConn(conn);
		c.preCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM04006() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdGate rdGate = new RdGate();
		rdGate.setInLinkPid(39257545);
		rdGate.setOutLinkPid(39257546);
		rdGate.setType(0);
		glmList.add(rdGate);
		
		RdGateCondition rdGateCondition = new RdGateCondition();
		rdGateCondition.setValidObj(0);
		glmList.add(rdGateCondition);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(39257545);
		glmList.add(rdLink);

		cc.setGlmList(glmList);
		GLM04006 c = new GLM04006();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM04008_2() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdGate rdGate = new RdGate();
		rdGate.setInLinkPid(39257545);
		rdGate.setOutLinkPid(39257546);
		rdGate.setType(0);
		glmList.add(rdGate);
		
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(39257545);
		glmList.add(rdLink);

		cc.setGlmList(glmList);
		GLM04008_2 c = new GLM04008_2();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM04008_1() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdGate rdGate = new RdGate();
		rdGate.setInLinkPid(39257545);
		rdGate.setOutLinkPid(39257546);
		rdGate.setDir(1);
		glmList.add(rdGate);
		
		RdRestriction rdRestriction = new RdRestriction();
		rdRestriction.setPid(39257545);
		glmList.add(rdRestriction);
		
		RdRestrictionDetail rdRestrictionDetail = new RdRestrictionDetail();
		rdRestrictionDetail.setRestricPid(39257545);
		rdRestrictionDetail.setOutLinkPid(39257546);
		rdRestrictionDetail.setType(1);
		glmList.add(rdRestrictionDetail);
		
		RdRestrictionDetail rdRestrictionDetail1 = new RdRestrictionDetail();
		rdRestrictionDetail1.setRestricPid(39257545);
		rdRestrictionDetail1.setOutLinkPid(39257546);
		rdRestrictionDetail1.setType(2);
		glmList.add(rdRestrictionDetail1);

		cc.setGlmList(glmList);
		GLM04008_1 c = new GLM04008_1();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM32006() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLane rdLane  = new RdLane();
		rdLane.setPid(31434086);
		rdLane.setLinkPid(59193232);
		rdLane.setLaneDir(0);
		glmList.add(rdLane);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(59193232);
		rdLink.setDirect(1);
		glmList.add(rdLink);

		cc.setGlmList(glmList);
		GLM32006 c = new GLM32006();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM32020() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLane rdLane  = new RdLane();
		rdLane.setPid(31434086);
		glmList.add(rdLane);
		
		RdLaneCondition rdLineCondition = new RdLaneCondition();
		rdLineCondition.setVehicle(214748416);
		glmList.add(rdLineCondition);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(59193232);
		glmList.add(rdLink);
		
		RdLinkForm rdLinkForm = new RdLinkForm();
		rdLinkForm.setLinkPid(59193232);
		rdLinkForm.setFormOfWay(22);
		glmList.add(rdLinkForm);

		cc.setGlmList(glmList);
		GLM32020 c = new GLM32020();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM32021() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLane rdLane  = new RdLane();
		rdLane.setPid(31434086);
		glmList.add(rdLane);
		
		RdLaneCondition rdLineCondition = new RdLaneCondition();
		rdLineCondition.setVehicle(214748416);
		rdLineCondition.setLanePid(31434086);
		glmList.add(rdLineCondition);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(59193232);
		glmList.add(rdLink);
		
		RdLinkForm rdLinkForm = new RdLinkForm();
		rdLinkForm.setLinkPid(59193232);
		rdLinkForm.setFormOfWay(20);
		glmList.add(rdLinkForm);

		cc.setGlmList(glmList);
		GLM32021 c = new GLM32021();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM32093() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdRestriction rdRestriction  = new RdRestriction();
		rdRestriction.setPid(302000037);
		glmList.add(rdRestriction);
		
		RdRestrictionDetail rdRestrictionDetail = new RdRestrictionDetail();
		rdRestrictionDetail.setRestricPid(302000037);
		glmList.add(rdRestrictionDetail);
		
		RdRestrictionCondition rdRestrictionCondition = new RdRestrictionCondition();
		rdRestrictionCondition.setTimeDomain("22");
		rdRestrictionCondition.setVehicle(2147483652L);
		glmList.add(rdRestrictionCondition);
		
		RdRestrictionCondition rdRestrictionCondition1 = new RdRestrictionCondition();
		rdRestrictionCondition.setTimeDomain("22");
		rdRestrictionCondition1.setVehicle(4);
		glmList.add(rdRestrictionCondition);
		
		RdLaneTopoDetail rdLaneTopoDetail = new RdLaneTopoDetail();
		rdLaneTopoDetail.setPid(61919591);
		glmList.add(rdLaneTopoDetail);

		RdLaneTopoVia rdLaneTopoVia = new RdLaneTopoVia();
		rdLaneTopoVia.setTopoId(61919591);
		glmList.add(rdLaneTopoVia);
		
		cc.setGlmList(glmList);
		GLM32093 c = new GLM32093();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM32092() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLane rdLane  = new RdLane();
		rdLane.setPid(203000240);
		rdLane.setLinkPid(208002934);
		glmList.add(rdLane);
		
		RdLaneCondition rdLineCondition = new RdLaneCondition();
		rdLineCondition.setVehicle(214748416);
		rdLineCondition.setLanePid(203000240);
		glmList.add(rdLineCondition);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(208002934);
		glmList.add(rdLink);
		
		RdLinkForm rdLinkForm = new RdLinkForm();
		rdLinkForm.setLinkPid(208002934);
		rdLinkForm.setFormOfWay(22);
		glmList.add(rdLinkForm);
		
		cc.setGlmList(glmList);
		GLM32092 c = new GLM32092();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	@Test
	public void testGLM32071() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLane rdLane  = new RdLane();
		rdLane.setPid(32478680);
		glmList.add(rdLane);
		
		RdLaneConnexity rdLaneConnexity = new RdLaneConnexity();
		rdLaneConnexity.setPid(305000018);
		glmList.add(rdLaneConnexity);
		
		cc.setGlmList(glmList);
		GLM32071 c = new GLM32071();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM32049() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLane rdLane  = new RdLane();
		rdLane.setPid(138912159);
		glmList.add(rdLane);
		
		RdTollgate rdTollgate = new RdTollgate();
		rdTollgate.setPid(202000009);
		glmList.add(rdTollgate);
		
		cc.setGlmList(glmList);
		GLM32049 c = new GLM32049();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM32060() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLane rdLane  = new RdLane();
		rdLane.setPid(46621466);
		glmList.add(rdLane);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(322290);
		glmList.add(rdLink);
		
		cc.setGlmList(glmList);
		GLM32060 c = new GLM32060();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM32051() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLane rdLane  = new RdLane();
		rdLane.setPid(46621466);
		glmList.add(rdLane);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(322290);
		glmList.add(rdLink);
		
		cc.setGlmList(glmList);
		GLM32051 c = new GLM32051();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM32052() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLane rdLane  = new RdLane();
		rdLane.setPid(46621466);
		glmList.add(rdLane);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(322290);
		glmList.add(rdLink);
		
		cc.setGlmList(glmList);
		GLM32052 c = new GLM32052();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void exeRdLaneCreateCheck() throws Exception{
		RdLane rdLane = new RdLane();
		rdLane.setPid(31434101);

		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdLane);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(ObjType.RDLANE);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		System.out.println("ok");
	}
	
	@Test
	public void exeRdLaneUpdateCheck() throws Exception{
		RdLane rdLane = new RdLane();
		rdLane.setPid(31434101);

		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdLane);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.UPDATE);
		checkCommand.setObjType(ObjType.RDLANE);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		System.out.println("ok");
	}
	
	@Test
	public void exeRdLaneDeleteCheck() throws Exception{
		RdLane rdLane = new RdLane();
		rdLane.setPid(31434101);

		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdLane);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.DELETE);
		checkCommand.setObjType(ObjType.RDLANE);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		System.out.println("ok");
	}
	
	@Test
	public void exeRdLaneTopoDetailUpdateCheck() throws Exception{
		RdLaneTopoDetail rdLaneTopoDetail = new RdLaneTopoDetail();
		rdLaneTopoDetail.setPid(31434101);

		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdLaneTopoDetail);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.UPDATE);
		checkCommand.setObjType(ObjType.RDLANETOPODETAIL);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		System.out.println("ok");
	}
	
	@Test
	public void exeRdLaneTopoDetailCreateCheck() throws Exception{
		RdLaneTopoDetail rdLaneTopoDetail = new RdLaneTopoDetail();
		rdLaneTopoDetail.setPid(31434101);

		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdLaneTopoDetail);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(ObjType.RDLANETOPODETAIL);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		System.out.println("ok");
	}
	
	@Test
	public void exeRdTollgateCreateCheck() throws Exception{
		RdTollgate rdTollGate = new RdTollgate();
		rdTollGate.setPid(31434101);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdTollGate);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(ObjType.RDTOLLGATE);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		System.out.println("ok");
	}
	
	@Test
	public void exeRdTollgateUpdateCheck() throws Exception{
		RdTollgate rdTollGate = new RdTollgate();
		rdTollGate.setPid(31434101);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdTollGate);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.UPDATE);
		checkCommand.setObjType(ObjType.RDTOLLGATE);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		System.out.println("ok");
	}
	
	@Test
	public void exeRdLaneConnexityUpdateCheck() throws Exception{
		RdLaneConnexity rdLaneConnexity = new RdLaneConnexity();
		rdLaneConnexity.setPid(31434101);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdLaneConnexity);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.UPDATE);
		checkCommand.setObjType(ObjType.RDLANECONNEXITY);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		System.out.println("ok");
	}
	
	@Test
	public void exeRdLaneConnexityCreateCheck() throws Exception{
		RdLaneConnexity rdLaneConnexity = new RdLaneConnexity();
		rdLaneConnexity.setPid(31434101);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdLaneConnexity);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(ObjType.RDLANECONNEXITY);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		System.out.println("ok");
	}
	
	@Test
	public void exeRdRestrictionCreateCheck() throws Exception{
		RdRestriction rdRestriction = new RdRestriction();
		rdRestriction.setPid(31434101);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdRestriction);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(ObjType.RDRESTRICTION);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		System.out.println("ok");
	}
	
	@Test
	public void exeRdRestrictionUpdateCheck() throws Exception{
		RdRestriction rdRestriction = new RdRestriction();
		rdRestriction.setPid(31434101);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdRestriction);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.UPDATE);
		checkCommand.setObjType(ObjType.RDRESTRICTION);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		System.out.println("ok");
	}
	
	@Test
	public void testGLM01017() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdRestriction rdRestriction  = new RdRestriction();
		rdRestriction.setPid(302000037);
		glmList.add(rdRestriction);
		
		RdLink rdLink = new RdLink();
		rdLink.setPid(39257545);
		rdLink.setFunctionClass(4);
		glmList.add(rdLink);
		
		cc.setGlmList(glmList);
		GLM01017 c = new GLM01017();
		c.setConn(conn);
		c.preCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM08004() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdRestriction rdRestriction  = new RdRestriction();
		rdRestriction.setPid(302000037);
		rdRestriction.setInLinkPid(292857);
		glmList.add(rdRestriction);
		
		RdRestrictionDetail rdRestrictionDetail  = new RdRestrictionDetail();
		rdRestrictionDetail.setOutLinkPid(306844);;
		glmList.add(rdRestrictionDetail);
		
		RdRestrictionDetail rdRestrictionDetail1  = new RdRestrictionDetail();
		rdRestrictionDetail1.setOutLinkPid(296498);;
		glmList.add(rdRestrictionDetail1);
		
		cc.setGlmList(glmList);
		GLM08004 c = new GLM08004();
		c.setConn(conn);
		c.preCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM26044() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdCross rdCross  = new RdCross();
		rdCross.setPid(8557);
		glmList.add(rdCross);
		
		RdCrossLink rdCrossLink  = new RdCrossLink();
		rdCrossLink.setPid(1446213);
		glmList.add(rdCrossLink);
		
		RdLink rdLink  = new RdLink();
		rdLink.setPid(1286634);;
		glmList.add(rdLink);
		
		RdLinkForm rdLinkForm  = new RdLinkForm();
		rdLinkForm.setLinkPid(1292760);
		rdLinkForm.setFormOfWay(33);
		glmList.add(rdLinkForm);
		
		cc.setGlmList(glmList);
		GLM26044 c = new GLM26044();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void exeRdCrossCreateCheck() throws Exception{
		RdCross rdCross = new RdCross();
		rdCross.setPid(8557);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdCross);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(ObjType.RDCROSS);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		List<CheckRule> checkRuleList = checkEngine.checkRuleList;
		System.out.println("ok");
	}
	
	@Test
	public void exeRdCrossUpdateCheck() throws Exception{
		RdCross rdCross = new RdCross();
		rdCross.setPid(8557);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdCross);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.UPDATE);
		checkCommand.setObjType(ObjType.RDCROSS);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.preCheck();
		checkEngine.postCheck();
		List<CheckRule> checkRuleList = checkEngine.checkRuleList;
		System.out.println("ok");
	}
	
	@Test
	public void testPERMIT_CHECK_LANE_PASSLINKS_LESS15() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLaneConnexity rdLaneConnexity  = new RdLaneConnexity();
		rdLaneConnexity.setStatus(ObjStatus.INSERT);
		
		List<IRow> topos = new ArrayList<IRow>();
		RdLaneTopology rdLaneTopology = new RdLaneTopology();
		rdLaneTopology.setPid(1);
		rdLaneTopology.setRelationshipType(2);
		rdLaneTopology.setStatus(ObjStatus.INSERT);
		
		List<IRow> vias = new ArrayList<IRow>();
		
		RdLaneVia rdLaneVia = new RdLaneVia();
		rdLaneVia.setTopologyId(1);
		rdLaneVia.setStatus(ObjStatus.INSERT);
		vias.add(rdLaneVia);
		glmList.add(rdLaneVia);
		RdLaneVia rdLaneVia2 = new RdLaneVia();
		rdLaneVia2.setTopologyId(1);
		rdLaneVia2.setStatus(ObjStatus.UPDATE);
		vias.add(rdLaneVia2);
		glmList.add(rdLaneVia2);
		
		rdLaneTopology.setVias(vias);
		topos.add(rdLaneTopology);
		rdLaneConnexity.setTopos(topos);
		
		glmList.add(rdLaneConnexity);

		
		RdLaneTopology rdLaneTopology2 = new RdLaneTopology();
		rdLaneTopology2.setPid(1);
		rdLaneTopology2.setStatus(ObjStatus.UPDATE);
		rdLaneTopology2.setVias(vias);
		glmList.add(rdLaneTopology2);
		
		RdLaneVia rdLaneVia3 = new RdLaneVia();
		rdLaneVia3.setTopologyId(1);
		rdLaneVia3.setStatus(ObjStatus.DELETE);
		glmList.add(rdLaneVia3);

		RdLaneVia rdLaneVia4 = new RdLaneVia();
		rdLaneVia4.setTopologyId(1);
		rdLaneVia4.setStatus(ObjStatus.INSERT);
		glmList.add(rdLaneVia4);

		cc.setGlmList(glmList);
		PERMIT_CHECK_LANE_PASSLINKS_LESS15 c = new PERMIT_CHECK_LANE_PASSLINKS_LESS15();
		c.setConn(conn);
		c.preCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testRELATING_CHECK_CROSS_RELATION_MUST_CONNECTED1() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLaneConnexity rdLaneConnexity  = new RdLaneConnexity();
		rdLaneConnexity.setInLinkPid(1);
		rdLaneConnexity.setNodePid(2);
		rdLaneConnexity.setStatus(ObjStatus.INSERT);
		glmList.add(rdLaneConnexity);

		List<IRow> topos = new ArrayList<IRow>();
		RdLaneTopology rdLaneTopology = new RdLaneTopology();
		rdLaneTopology.setOutLinkPid(2);
		rdLaneTopology.setRelationshipType(1);
		rdLaneTopology.setStatus(ObjStatus.INSERT);
		topos.add(rdLaneTopology);
		rdLaneConnexity.setTopos(topos);

		RdLaneTopology rdLaneTopology2 = new RdLaneTopology();
		rdLaneTopology2.setPid(1);
		rdLaneTopology2.setOutLinkPid(2);
		rdLaneTopology2.setConnexityPid(1);
		rdLaneTopology2.setStatus(ObjStatus.INSERT);
		glmList.add(rdLaneTopology2);

		cc.setGlmList(glmList);
		RELATING_CHECK_CROSS_RELATION_MUST_CONNECTED1 c = new RELATING_CHECK_CROSS_RELATION_MUST_CONNECTED1();
		c.setConn(conn);
		c.preCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM19001_2() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLaneConnexity rdLaneConnexity  = new RdLaneConnexity();
		rdLaneConnexity.setInLinkPid(1);
		rdLaneConnexity.setStatus(ObjStatus.INSERT);
		
		List<IRow> topos = new ArrayList<IRow>();
		RdLaneTopology rdLaneTopology = new RdLaneTopology();
		rdLaneTopology.setPid(1);
		rdLaneTopology.setOutLinkPid(2);
		rdLaneTopology.setRelationshipType(2);
		rdLaneTopology.setStatus(ObjStatus.INSERT);
		
		List<IRow> vias = new ArrayList<IRow>();
		
		RdLaneVia rdLaneVia = new RdLaneVia();
		rdLaneVia.setTopologyId(1);
		rdLaneVia.setLinkPid(3);
		rdLaneVia.setStatus(ObjStatus.INSERT);
		vias.add(rdLaneVia);
		glmList.add(rdLaneVia);
		RdLaneVia rdLaneVia2 = new RdLaneVia();
		rdLaneVia2.setTopologyId(1);
		rdLaneVia2.setStatus(ObjStatus.UPDATE);
		rdLaneVia2.setLinkPid(4);
		vias.add(rdLaneVia2);
		glmList.add(rdLaneVia2);
		
		rdLaneTopology.setVias(vias);
		topos.add(rdLaneTopology);
		rdLaneConnexity.setTopos(topos);
		
		glmList.add(rdLaneConnexity);

		
		RdLaneTopology rdLaneTopology2 = new RdLaneTopology();
		rdLaneTopology2.setPid(1);
		rdLaneTopology2.setStatus(ObjStatus.UPDATE);
		rdLaneTopology2.setVias(vias);
		glmList.add(rdLaneTopology2);
		
		RdLaneVia rdLaneVia3 = new RdLaneVia();
		rdLaneVia3.setTopologyId(1);
		rdLaneVia3.setStatus(ObjStatus.DELETE);
		rdLaneVia3.setGroupId(5);
		glmList.add(rdLaneVia3);

		RdLaneVia rdLaneVia4 = new RdLaneVia();
		rdLaneVia4.setTopologyId(1);
		rdLaneVia4.setStatus(ObjStatus.INSERT);
		rdLaneVia3.setGroupId(6);
		glmList.add(rdLaneVia4);

		cc.setGlmList(glmList);
		GLM19001_2 c = new GLM19001_2();
		c.setConn(conn);
		c.preCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM19001_3() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLaneConnexity rdLaneConnexity  = new RdLaneConnexity();
		rdLaneConnexity.setInLinkPid(1);
		rdLaneConnexity.setStatus(ObjStatus.INSERT);
		
		List<IRow> topos = new ArrayList<IRow>();
		RdLaneTopology rdLaneTopology = new RdLaneTopology();
		rdLaneTopology.setPid(1);
		rdLaneTopology.setOutLinkPid(2);
		rdLaneTopology.setRelationshipType(2);
		rdLaneTopology.setStatus(ObjStatus.INSERT);
		
		List<IRow> vias = new ArrayList<IRow>();
		
		RdLaneVia rdLaneVia = new RdLaneVia();
		rdLaneVia.setTopologyId(1);
		rdLaneVia.setLinkPid(3);
		rdLaneVia.setStatus(ObjStatus.INSERT);
		vias.add(rdLaneVia);
		glmList.add(rdLaneVia);
		RdLaneVia rdLaneVia2 = new RdLaneVia();
		rdLaneVia2.setTopologyId(1);
		rdLaneVia2.setStatus(ObjStatus.UPDATE);
		rdLaneVia2.setLinkPid(4);
		vias.add(rdLaneVia2);
		glmList.add(rdLaneVia2);
		
		rdLaneTopology.setVias(vias);
		topos.add(rdLaneTopology);
		rdLaneConnexity.setTopos(topos);
		
		glmList.add(rdLaneConnexity);

		
		RdLaneTopology rdLaneTopology2 = new RdLaneTopology();
		rdLaneTopology2.setPid(1);
		rdLaneTopology2.setStatus(ObjStatus.UPDATE);
		rdLaneTopology2.setVias(vias);
		glmList.add(rdLaneTopology2);
		
		RdLaneVia rdLaneVia3 = new RdLaneVia();
		rdLaneVia3.setTopologyId(1);
		rdLaneVia3.setStatus(ObjStatus.DELETE);
		rdLaneVia3.setGroupId(5);
		glmList.add(rdLaneVia3);

		RdLaneVia rdLaneVia4 = new RdLaneVia();
		rdLaneVia4.setTopologyId(1);
		rdLaneVia4.setStatus(ObjStatus.INSERT);
		rdLaneVia3.setGroupId(6);
		glmList.add(rdLaneVia4);

		cc.setGlmList(glmList);
		GLM19001_3 c = new GLM19001_3();
		c.setConn(conn);
		c.preCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM19014() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLinkForm rdLinkForm = new RdLinkForm();
		rdLinkForm.setFormOfWay(50);
		rdLinkForm.setLinkPid(1);
		glmList.add(rdLinkForm);

		cc.setGlmList(glmList);
		GLM19014 c = new GLM19014();
		c.setConn(conn);
		c.preCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testCrossingLaneOutlinkDirect() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLaneConnexity rdLaneConnexity  = new RdLaneConnexity();
		rdLaneConnexity.setInLinkPid(1);
		rdLaneConnexity.setNodePid(1);
		rdLaneConnexity.setStatus(ObjStatus.INSERT);
		
		List<IRow> topos = new ArrayList<IRow>();
		RdLaneTopology rdLaneTopology = new RdLaneTopology();
		rdLaneTopology.setPid(1);
		rdLaneTopology.setOutLinkPid(2);
		rdLaneTopology.setRelationshipType(1);
		rdLaneTopology.setStatus(ObjStatus.INSERT);

		topos.add(rdLaneTopology);
		rdLaneConnexity.setTopos(topos);
		
		glmList.add(rdLaneConnexity);

		
		RdLaneTopology rdLaneTopology2 = new RdLaneTopology();
		rdLaneTopology2.setPid(2);
		rdLaneTopology2.setConnexityPid(1);
		rdLaneTopology2.setOutLinkPid(5);
		rdLaneTopology2.setStatus(ObjStatus.INSERT);
		rdLaneTopology.setRelationshipType(1);
		glmList.add(rdLaneTopology2);

		cc.setGlmList(glmList);
		CrossingLaneOutlinkDirect c = new CrossingLaneOutlinkDirect();
		c.setConn(conn);
		c.preCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testRdLane002() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLaneConnexity rdLaneConnexity  = new RdLaneConnexity();
		rdLaneConnexity.setInLinkPid(1);
		rdLaneConnexity.setStatus(ObjStatus.INSERT);
		
		List<IRow> topos = new ArrayList<IRow>();
		RdLaneTopology rdLaneTopology = new RdLaneTopology();
		rdLaneTopology.setPid(1);
		rdLaneTopology.setOutLinkPid(2);
		rdLaneTopology.setRelationshipType(2);
		rdLaneTopology.setStatus(ObjStatus.INSERT);
		
		List<IRow> vias = new ArrayList<IRow>();
		
		RdLaneVia rdLaneVia = new RdLaneVia();
		rdLaneVia.setTopologyId(1);
		rdLaneVia.setLinkPid(3);
		rdLaneVia.setStatus(ObjStatus.INSERT);
		vias.add(rdLaneVia);
		glmList.add(rdLaneVia);
		RdLaneVia rdLaneVia2 = new RdLaneVia();
		rdLaneVia2.setTopologyId(1);
		rdLaneVia2.setStatus(ObjStatus.UPDATE);
		rdLaneVia2.setLinkPid(4);
		vias.add(rdLaneVia2);
		glmList.add(rdLaneVia2);
		
		rdLaneTopology.setVias(vias);
		topos.add(rdLaneTopology);
		rdLaneConnexity.setTopos(topos);
		
		glmList.add(rdLaneConnexity);

		
		RdLaneTopology rdLaneTopology2 = new RdLaneTopology();
		rdLaneTopology2.setPid(1);
		rdLaneTopology2.setRelationshipType(2);
		rdLaneTopology2.setStatus(ObjStatus.UPDATE);
		rdLaneTopology2.setVias(vias);
		glmList.add(rdLaneTopology2);
		
		RdLaneVia rdLaneVia3 = new RdLaneVia();
		rdLaneVia3.setTopologyId(1);
		rdLaneVia3.setStatus(ObjStatus.DELETE);
		rdLaneVia3.setGroupId(5);
		glmList.add(rdLaneVia3);

		RdLaneVia rdLaneVia4 = new RdLaneVia();
		rdLaneVia4.setTopologyId(1);
		rdLaneVia4.setStatus(ObjStatus.INSERT);
		rdLaneVia3.setGroupId(6);
		glmList.add(rdLaneVia4);

		cc.setGlmList(glmList);
		RdLane002 c = new RdLane002();
		c.setConn(conn);
		c.preCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM28018() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(19);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdRoad rdRoad  = new RdRoad();
//		rdRoad.setPid(6493);
		rdRoad.setPid(307000007);
		rdRoad.setStatus(ObjStatus.INSERT);
		glmList.add(rdRoad);

		cc.setGlmList(glmList);
		GLM28018 c = new GLM28018();
		c.setConn(conn);
		c.postCheck(cc);
		List result = c.getCheckResultList();
		
		System.out.println("end");
	}
	
}

