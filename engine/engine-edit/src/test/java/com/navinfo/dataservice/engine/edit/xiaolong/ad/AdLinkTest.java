package com.navinfo.dataservice.engine.edit.xiaolong.ad;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.edit.edit.search.rd.utils.RdLinkSearchUtils;

import net.sf.json.JSONObject;

/**
 * @author zhaokk
 *
 */
public class AdLinkTest {
	private static final String configPath = "D:/ws_new/DataService/web/edit-web/src/main/resources/config.properties";
	static {
		ConfigLoader.initDBConn(configPath);
	}

	public static void testBreakAdLink() {
		String parameter = "{\"command\":\"BREAK\",\"projectId\":11,\"objId\":100031763,\"data\":{\"longitude\":116.4725149146884,\"latitude\":40.012285569566565},\"type\":\"ADLINK\"}";

		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		testBreakAdLink();
	}
}
