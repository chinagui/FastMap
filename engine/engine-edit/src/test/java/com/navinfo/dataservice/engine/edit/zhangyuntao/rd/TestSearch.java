package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import java.net.URLDecoder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Title: TestSearch.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月26日 下午2:06:20
 * @version: v1.0
 */
public class TestSearch {

	@Test
	public static void testSearchGap(String url) throws Exception {
		url = URLDecoder.decode(url, "UTF-8");
		String parameter = url.substring(url.indexOf("=") + 1);

		Connection conn = null;

		JSONObject jsonReq = JSONObject.fromObject(parameter);

		JSONArray type = jsonReq.getJSONArray("types");

		int dbId = jsonReq.getInt("dbId");

		int x = jsonReq.getInt("x");

		int y = jsonReq.getInt("y");

		int z = jsonReq.getInt("z");

		int gap = 0;

		if (jsonReq.containsKey("gap")) {
			gap = jsonReq.getInt("gap");
		}

		List<ObjType> types = new ArrayList<ObjType>();

		for (int i = 0; i < type.size(); i++) {
			types.add(ObjType.valueOf(type.getString(i)));
		}

		JSONObject data = null;

		if (z <= 16) {

			List<ObjType> tileTypes = new ArrayList<ObjType>();

			List<ObjType> gdbTypes = new ArrayList<ObjType>();

			for (ObjType t : types) {
				if (t == ObjType.RDLINK || t == ObjType.ADLINK || t == ObjType.RWLINK) {
					tileTypes.add(t);
				} else {
					gdbTypes.add(t);
				}
			}

			if (!gdbTypes.isEmpty()) {

				conn = DBConnector.getInstance().getConnectionById(dbId);

				SearchProcess p = new SearchProcess(conn);

				JSONObject jo = p.searchDataByTileWithGap(gdbTypes, x, y, z, gap);

				if (data == null) {
					data = new JSONObject();
				}

				data.putAll(jo);
			}

			if (!tileTypes.isEmpty()) {
				JSONObject jo = new JSONObject();

				if (data == null) {
					data = new JSONObject();
				}

				data.putAll(jo);
			}

		} else {
			conn = DBConnector.getInstance().getConnectionById(dbId);

			SearchProcess p = new SearchProcess(conn);

			data = p.searchDataByTileWithGap(types, x, y, z, gap);

		}

		System.out.println(ResponseUtils.assembleRegularResult(data));
	}
}
