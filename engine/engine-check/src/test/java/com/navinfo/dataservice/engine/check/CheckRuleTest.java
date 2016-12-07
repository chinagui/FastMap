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
}
