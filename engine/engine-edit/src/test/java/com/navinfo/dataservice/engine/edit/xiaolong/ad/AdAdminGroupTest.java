package com.navinfo.dataservice.engine.edit.xiaolong.ad;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminTree;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminTreeSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class AdAdminGroupTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void searchAdminGroupLevel() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			String parameter = "{\"type\":\"ADADMINGROUP\",\"dbId\":17,\"subTaskId\":30}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByCondition(ObjType.ADADMINGROUP, jsonReq));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testLoadAdminGroupByRegionId() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			AdAdminTreeSelector selector = new AdAdminTreeSelector(conn);
			
			int regionId = 211172;
			
			int groupId = 114;
			
			AdAdminTree tree = selector.loadRowsByRegionId(regionId, false, groupId);
			
			System.out.println(tree.Serialize(null));
		} catch (Exception e) {
		}
	}

	@Test
	public void deleteAdminGroupLevel() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			String parameter = "{\"command\":\"UPDATE\",\"type\":\"ADADMINGROUP\",\"dbId\":17,\"data\":{\"groupTree\":{\"regionId\":1273,\"name\":\"中国大陆\",\"group\":{\"groupId\":248,\"regionIdUp\":1273,\"rowId\":\"3AE1FE0DE2D892F7E050A8C08304EE4C\"},\"children\":[{\"regionId\":163,\"name\":\"北京市\",\"group\":{\"groupId\":40,\"regionIdUp\":163,\"rowId\":\"3AE1FE0DE04992F7E050A8C08304EE4C\"},\"part\":{\"groupId\":248,\"regionIdDown\":163,\"rowId\":\"3AE1FCCAB82692F7E050A8C08304EE4C\"},\"children\":[{\"regionId\":580,\"name\":\"北京市\",\"group\":{\"groupId\":114,\"regionIdUp\":580,\"rowId\":\"3AE1FE0DE08392F7E050A8C08304EE4C\"},\"part\":{\"groupId\":40,\"regionIdDown\":580,\"rowId\":\"3AE1FCCAB45092F7E050A8C08304EE4C\"},\"children\":[{\"regionId\":1013,\"name\":\"朝阳区\",\"part\":{\"groupId\":114,\"regionIdDown\":1013,\"rowId\":\"3AE1FCCAB61D92F7E050A8C08304EE4C\"},\"children\":[]},{\"regionId\":1021,\"name\":\"延庆区\",\"part\":{\"groupId\":114,\"regionIdDown\":1021,\"rowId\":\"3AE1FCCAB62592F7E050A8C08304EE4C\"},\"children\":[]},{\"regionId\":1878,\"name\":\"海淀区\",\"part\":{\"groupId\":114,\"regionIdDown\":1878,\"rowId\":\"3AE1FCCAB20D92F7E050A8C08304EE4C\"},\"children\":[]},{\"regionId\":1879,\"name\":\"东城区\",\"part\":{\"groupId\":286,\"regionIdDown\":1879,\"rowId\":\"3AE1FCCAB21092F7E050A8C08304EE4C\"},\"children\":[]},{\"regionId\":2338,\"name\":\"石景山区\",\"part\":{\"groupId\":286,\"regionIdDown\":2338,\"rowId\":\"3AE1FCCAB3E892F7E050A8C08304EE4C\"},\"children\":[]},{\"regionId\":3240,\"name\":\"房山区\",\"part\":{\"groupId\":114,\"regionIdDown\":3240,\"rowId\":\"3AE1FCCABD4092F7E050A8C08304EE4C\"},\"children\":[]},{\"regionId\":3241,\"name\":\"大兴区\",\"part\":{\"groupId\":114,\"regionIdDown\":3241,\"rowId\":\"3AE1FCCABD4292F7E050A8C08304EE4C\"},\"children\":[]},{\"regionId\":3249,\"name\":\"昌平区\",\"part\":{\"groupId\":114,\"regionIdDown\":3249,\"rowId\":\"3AE1FCCABD4D92F7E050A8C08304EE4C\"},\"children\":[]},{\"regionId\":3250,\"name\":\"怀柔区\",\"part\":{\"groupId\":114,\"regionIdDown\":3250,\"rowId\":\"3AE1FCCABD4F92F7E050A8C08304EE4C\"},\"children\":[]},{\"regionId\":579,\"name\":\"西城区\",\"part\":{\"groupId\":114,\"regionIdDown\":579,\"rowId\":\"3AE1FCCAB44E92F7E050A8C08304EE4C\"},\"children\":[]},{\"regionId\":170,\"name\":\"顺义区\",\"part\":{\"groupId\":114,\"regionIdDown\":170,\"rowId\":\"3AE1FCCAB82F92F7E050A8C08304EE4C\"},\"children\":[]},{\"regionId\":1028,\"name\":\"密云区\",\"part\":{\"groupId\":114,\"regionIdDown\":1028,\"rowId\":\"3AE1FCCAB62E92F7E050A8C08304EE4C\"},\"children\":[]},{\"regionId\":1876,\"name\":\"丰台区\",\"part\":{\"groupId\":286,\"regionIdDown\":1876,\"rowId\":\"3AE1FCCAB20B92F7E050A8C08304EE4C\"},\"children\":[]}]},{\"regionId\":174,\"name\":\"平谷区\",\"part\":{\"groupId\":40,\"regionIdDown\":174,\"rowId\":\"3AE1FCCAB83492F7E050A8C08304EE4C\",\"objType\":\"update\"},\"children\":[]}]}]}},\"rowId\":\"1C81E63BC13C459DAD8C6C79AB71CA88\"}";

			Transaction t = new Transaction(parameter);
			try {
				String msg = t.run();
				System.out.println(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}

			String parameter2 = "{\"subTaskId\":30,\"type\":\"ADADMINGROUP\"}";

			JSONObject jsonReq2 = JSONObject.fromObject(parameter2);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByCondition(ObjType.ADADMINGROUP, jsonReq2));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
