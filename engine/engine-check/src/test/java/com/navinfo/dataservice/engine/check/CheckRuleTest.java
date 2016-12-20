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
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGateCondition;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneCondition;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoVia;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.engine.check.rules.GLM01017;
import com.navinfo.dataservice.engine.check.rules.GLM01091;
import com.navinfo.dataservice.engine.check.rules.GLM01570;
import com.navinfo.dataservice.engine.check.rules.GLM04003;
import com.navinfo.dataservice.engine.check.rules.GLM04006;
import com.navinfo.dataservice.engine.check.rules.GLM04008_1;
import com.navinfo.dataservice.engine.check.rules.GLM04008_2;
import com.navinfo.dataservice.engine.check.rules.GLM08004;
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
}

