package com.navinfo.dataservice.engine.edit.xiaolong.poi;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

public class POITest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testGetByPid() {
		try {
			Connection conn = DBConnector.getInstance().getConnectionById(42);

			IxPoiSelector selector = new IxPoiSelector(conn);

			IRow jsonObject = selector.loadById(88553093, false);

			System.out.println(jsonObject.Serialize(ObjLevel.FULL));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void getTitleWithGap()
	{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getConnectionById(8);
			
			SearchProcess p = new SearchProcess(conn);
			
			List<ObjType> objType = new ArrayList<>();
			
			objType.add(ObjType.IXPOI);

			System.out.println(p.searchDataByTileWithGap(objType, 107937, 49616, 17, 80));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDeletePoi()
	{
		String parameter = "{\"command\":\"DELETE\",\"type\":\"IXPOI\",\"dbId\":42,\"objId\":642692}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void addPoi(){
		String parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOI\",\"dbId\":42,\"data\":{\"longitude\":116.39552235603331,\"latitude\":39.90676527744907,\"linkPid\":625962}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdatePoi()
	{
		String parameter = "{\"dbId\":42,\"command\":\"UPDATE\",\"type\":\"IXPOI\",\"objId\":100000030,\"data\":{\"addresses\":[{\"addons\":\"\",\"addonsPhonetic\":\"\",\"addrname\":\"\",\"addrnamePhonetic\":\"\",\"building\":\"\",\"buildingPhonetic\":\"\",\"city\":\"平谷区\",\"cityPhonetic\":\"Ping Gu Qu\",\"county\":\"\",\"countyPhonetic\":\"\",\"estab\":\"\",\"estabPhonetic\":\"\",\"floor\":\"\",\"floorPhonetic\":\"\",\"fullname\":\"北京市平谷区峪口村育才路４６号\",\"fullnamePhonetic\":\"Bei Jing Shi Ping Gu Qu Yu Kou Cun Yu Cai Lu 46 Hao\",\"housenumPhonetic\":\"46\",\"housesum\":\"４６\",\"landmark\":\"\",\"landmarkPhonetic\":\"\",\"langCode\":\"CHI\",\"nameGroupid\":1,\"pid\":10841289,\"place\":\"峪口村\",\"placePhonetic\":\"Yu Kou Cun\",\"poiPid\":1317,\"prefix\":\"\",\"prefixPhonetic\":\"\",\"provPhonetic\":\"Bei Jing Shi\",\"province\":\"北京市\",\"roadnamePhonetic\":\"\",\"rodename\":\"\",\"room\":\"\",\"roomPhonetic\":\"\",\"rowId\":\"3524E79CE0C66E1AE050A8C08304BA17\",\"srcFlag\":0,\"street\":\"育才路\",\"streetPhonetic\":\"Yu Cai Lu\",\"subnum\":\"\",\"subsumPhonetic\":\"\",\"surfix\":\"\",\"surfixPhonetic\":\"\",\"town\":\"\",\"townPhonetic\":\"\",\"type\":\"号\",\"typePhonetic\":\"Hao\",\"uDate\":\"\",\"uRecord\":0,\"unit\":\"\",\"unitPhonetic\":\"\",\"objStatus\":\"INSERT\"}],\"pid\":100000030}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testDeleteParent()
	{
		String parameter = "{\"command\":\"DELETE\",\"type\":\"IXPOIPARENT\",\"dbId\":42,\"objId\":73341675}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
