package com.navinfo.dataservice.engine.edit.luyao.poi;



import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

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
	
	@Test
	public void UpdatePoi0627_1(){
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"IXPOI\",\"objId\":100000073,\"data\":{\"kindCode\":\"120202\",\"pid\":100000073,\"objStatus\":\"UPDATE\",\"chain\":\"\",\"open24h\":\"1\",\"postCode\":\"123456\",\"poiMemo\":\"备注\",\"level\":\"B1\",\"indoor\":\"3\",\"names\":[{\"nameId\":0,\"poiPid\":0,\"nameGroupid\":1,\"langCode\":\"CHI\",\"nameClass\":1,\"nameType\":2,\"name\":\"名称\",\"namePhonetic\":null,\"keywords\":null,\"nidbPid\":null,\"pid\":100000073,\"objStatus\":\"INSERT\"}],\"addresses\":[{\"pid\":100000073,\"nameGroupid\":1,\"poiPid\":0,\"langCode\":\"CHI\",\"fullname\":\"地址\",\"objStatus\":\"INSERT\"}],\"photos\":[{\"photoId\":0,\"status\":null,\"memo\":0,\"pid\":100000073,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":null,\"memo\":0,\"pid\":100000073,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":null,\"memo\":0,\"pid\":100000073,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":null,\"memo\":0,\"pid\":100000073,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":null,\"memo\":0,\"pid\":100000073,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":null,\"memo\":0,\"pid\":100000073,\"objStatus\":\"INSERT\"}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void UpdatePoi0627_2(){
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"IXPOI\",\"objId\":100000073,\"data\":{\"kindCode\":\"110102\",\"pid\":100000073,\"objStatus\":\"UPDATE\",\"chain\":\"3030\",\"open24h\":\"1\",\"postCode\":\"发多少\",\"poiMemo\":\"发的\",\"level\":\"B2\",\"indoor\":\"3\",\"names\":[{\"pid\":100000073,\"poiPid\":0,\"nameGroupid\":1,\"langCode\":\"CHI\",\"nameClass\":1,\"nameType\":2,\"name\":\"名臣\",\"namePhonetic\":null,\"keywords\":null,\"nidbPid\":null,\"objStatus\":\"INSERT\"}],\"addresses\":[{\"pid\":100000073,\"nameGroupid\":1,\"poiPid\":0,\"langCode\":\"CHI\",\"fullname\":\"双方都\",\"objStatus\":\"INSERT\"}],\"photos\":[{\"photoId\":0,\"status\":null,\"memo\":0,\"pid\":100000073,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":null,\"memo\":0,\"pid\":100000073,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":null,\"memo\":0,\"pid\":100000073,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":null,\"memo\":0,\"pid\":100000073,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":null,\"memo\":0,\"pid\":100000073,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":null,\"memo\":0,\"pid\":100000073,\"objStatus\":\"INSERT\"}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void UpdatePoi0701_1(){
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"IXPOI\",\"objId\":24674,\"data\":{\"poiMemo\":\"1\",\"rowId\":\"3524E56B583F6E1AE050A8C08304BA17\",\"pid\":24674,\"objStatus\":\"UPDATE\",\"gasstations\":[{\"pid\":0,\"fuelType\":\"\",\"oilType\":\"\",\"egType\":\"\",\"mgType\":\"\",\"payment\":\"\",\"service\":\"\",\"objStatus\":\"INSERT\"}],\"hotels\":[{\"pid\":0,\"poiPid\":0,\"rating\":0,\"checkinTime\":\"14:00\",\"checkoutTime\":\"12:00\",\"roomCount\":0,\"breakfast\":0,\"parking\":0,\"travelguideFlag\":0,\"objStatus\":\"INSERT\"}],\"restaurants\":[{\"pid\":0,\"poiPid\":0,\"foodType\":\"\",\"creditCard\":\"\",\"avgCost\":0,\"parking\":0,\"travelguideFlag\":0,\"objStatus\":\"INSERT\"}],\"parkings\":[{\"pid\":0,\"tollStd\":\"\",\"tollWay\":\"\",\"remark\":\"\",\"resHigh\":0,\"resWidth\":0,\"resWeigh\":0,\"certificate\":0,\"vehicle\":0,\"rowId\":\"\",\"objStatus\":\"INSERT\"}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
