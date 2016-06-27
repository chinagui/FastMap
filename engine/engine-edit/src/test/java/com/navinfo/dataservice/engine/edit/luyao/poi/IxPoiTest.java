package com.navinfo.dataservice.engine.edit.luyao.poi;



import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class IxPoiTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	@Test
	public void addPoi(){
		String parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOI\",\"dbId\":8,\"data\":{\"longitude\":116.39552235603331,\"latitude\":39.90676527744907,\"linkPid\":625962}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void MovePoi(){
		String parameter = "{\"command\":\"MOVE\",\"type\":\"IXPOI\",\"dbId\":8,\"data\":{\"longitude\":116.39552235603331,\"latitude\":39.90676527744907,\"linkPid\":625962}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void UpdatePoi(){
		String parameter = "{\"dbId\":42,\"command\":\"UPDATE\",\"type\":\"IXPOI\",\"objId\":88581671,\"data\":{\"pid\":88581671,\"objStatus\":\"UPDATE\",\"names\":[{\"pid\":0,\"poiPid\":88581671,\"nameGroupid\":3,\"langCode\":\"CHI\",\"nameClass\":1,\"nameType\":2,\"name\":\"充电桩555\",\"namePhonetic\":\"Chong+Dian+Zhuang\",\"keywords\":\"\",\"nidbPid\":\"\",\"objStatus\":\"INSERT\"},{\"pid\":0,\"poiPid\":88581671,\"nameGroupid\":3,\"langCode\":\"CHI\",\"nameClass\":1,\"nameType\":1,\"name\":\"充电桩\",\"namePhonetic\":\"Chong+Dian+Zhuang\",\"keywords\":\"\",\"nidbPid\":\"\",\"objStatus\":\"INSERT\"},{\"pid\":0,\"poiPid\":88581671,\"nameGroupid\":3,\"langCode\":\"ENG\",\"nameClass\":1,\"nameType\":2,\"name\":\"Charging+Pile\",\"namePhonetic\":\"\",\"keywords\":\"\",\"nidbPid\":\"\",\"objStatus\":\"INSERT\"}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void UpdatePoi0623(){
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"IXPOI\",\"objId\":88581672,\"data\":{\"names\":[{\"name\":\"充电桩eee\",\"rowId\":\"3524EA0D21C06E1AE050A8C08304BA17\",\"objStatus\":\"UPDATE\"}],\"pid\":88581672}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void UpdatePoi0624(){
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"IXPOI\",\"objId\":1626,\"data\":{\"addresses\":[{\"pid\":0,\"nameGroupId\":1,\"poiPid\":0,\"langCode\":\"CHI\",\"fullname\":\"eewerew\",\"pid\":1626,\"objStatus\":\"INSERT\"}],\"pid\":1626}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void UpdatePoi0624_2(){
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"IXPOI\",\"objId\":47040440,\"data\":{\"level\":\"B3\",\"pid\":47040440,\"objStatus\":\"UPDATE\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
