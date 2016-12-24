package com.navinfo.dataservice.engine.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
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
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.rules.GLM08052;
import com.navinfo.dataservice.engine.check.rules.GLM14007;
import com.navinfo.dataservice.engine.check.rules.GLM32005;
import com.navinfo.dataservice.engine.check.rules.GLM34001;

/**
 * @ClassName CheckTest
 * @author Han Shaoming
 * @date 2016年12月15日 上午9:41:58
 * @Description TODO
 */
public class CheckTest {
	@Before
	public void init() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] {"dubbo-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testGLM34001() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		RdLink rdLink  = new RdLink();
		rdLink.setPid(59193232);
		rdLink.setKind(1);
		glmList.add(rdLink);
		
		cc.setGlmList(glmList);
		GLM34001 c = new GLM34001();
		c.setConn(conn);
		c.postCheck(cc);
		
		System.out.println("end");
	}
	
	@Test
	public void testGLM14007() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		/*RdLink rdLink  = new RdLink();
		rdLink.setPid(246342);*/
		RdLinkForm rdLinkForm = new RdLinkForm();
		rdLinkForm.setLinkPid(246342);
		rdLinkForm.setFormOfWay(50);
		glmList.add(rdLinkForm);
		cc.setGlmList(glmList);
		GLM14007 c = new GLM14007();
		c.setConn(conn);
		c.preCheck(cc);
		
		CheckCommand cc1 = new CheckCommand();
		List<IRow> glmList1 = new ArrayList<IRow>();
		RdDirectroute rdDirectroute = new RdDirectroute();
		rdDirectroute.setPid(11542);
		rdDirectroute.setInLinkPid(246342);
		rdDirectroute.setOutLinkPid(246344);
		glmList1.add(rdDirectroute);
		cc1.setGlmList(glmList1);
		GLM14007 c1 = new GLM14007();
		c1.setConn(conn);
		c1.postCheck(cc1);
		
		System.out.println("end");
	}
	
	@Test
	public void testCheck() throws Exception{
		//List<IRow> vias = new ArrayList<IRow>();
		RdDirectroute rdDirectroute = new RdDirectroute();
		rdDirectroute.setPid(11540);
		rdDirectroute.setProcessFlag(0);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(rdDirectroute);
		
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.UPDATE);
		checkCommand.setObjType(ObjType.RDDIRECTROUTE);
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.postCheck();
		//System.out.println(checkEngine.postCheck());
	}
	
	@Test
	public void testGLM08052() throws Exception{
		Connection conn=DBConnector.getInstance().getConnectionById(17);
		
		CheckCommand cc = new CheckCommand();
		List<IRow> glmList = new ArrayList<IRow>();
		/*RdLink rdLink  = new RdLink();
		rdLink.setPid(246342);*/
		RdLinkForm rdLinkForm = new RdLinkForm();
		rdLinkForm.setLinkPid(246342);
		rdLinkForm.setFormOfWay(50);
		
//		glmList.add(rdLinkForm);
//		cc.setGlmList(glmList);
//		GLM08052 c = new GLM08052();
//		c.setConn(conn);
//		c.preCheck(cc);
		
		System.out.println("end");
	}
	
}
