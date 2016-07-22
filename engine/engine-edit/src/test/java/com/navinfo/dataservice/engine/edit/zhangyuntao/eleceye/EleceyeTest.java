package com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye;

import java.sql.Connection;
import java.util.List;

import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;

public class EleceyeTest extends InitApplication {

	@Override
	public void init() {
		super.initContext();
	}

	@Test
	public void createEleceye() {
		String requester = "{'dbId':43,'command':'CREATE','type':'RDELECTRONICEYE','data':{'direct':3,'longitude':116.48378868782932,'latitude':40.30710911418436,'linkPid':13677569}}";
		TestUtil.run(requester);
	}
	
	@Test
	public void updateEleceye(){
		String requester = "{'dbId':43,'command':'UPDATE','type':'RDELECTRONICEYE','data':{'pid':100281918,'kind':20,'objStatus':'UPDATE'}}";
		TestUtil.run(requester);
	}
	
	@Test
	public void deleteEleceye(){
		String requester = "{'dbId':43,'command':'DELETE','type':'RDELECTRONICEYE','data':{'pid':100281916}}";
		TestUtil.run(requester);
	}
	
	@Test
	public void getEleceye(){
		try {
			Connection conn = DBConnector.getInstance().getConnectionById(43);
			RdElectroniceyeSelector selector = new RdElectroniceyeSelector(conn);
			RdElectroniceye eleceye = (RdElectroniceye)selector.loadById(100281916, false);
			eleceye.getPairs();
			eleceye.getParts();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void getEleceyes(){
		try {
			Connection conn = DBConnector.getInstance().getConnectionById(43);
			RdElectroniceyeSelector selector = new RdElectroniceyeSelector(conn);
			List<RdElectroniceye> list = selector.loadListByRdLinkId(13677569, false);
			for(RdElectroniceye eleceye : list){
				System.out.println(eleceye.pid());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
