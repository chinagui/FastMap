package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.search.RdLaneConnexitySearch;
import com.navinfo.dataservice.dao.glm.selector.SelectorUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchViaSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

import net.sf.json.JSONObject;

public class RdBranchTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testAdd3dBranch() {
		String parameter = "{\"command\":\"DELETE\",\"dbId\":259,\"type\":\"RDBRANCH\",\"detailId\":0,\"rowId\":\"061779F2EA25463698E74F78119E2DBF\",\"branchType\":5}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(t.getLogs());
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetByPid() {
		Connection conn;
		try {

			String parameter = "{\"dbId\":19,\"type\":\"RDBRANCH\",\"detailId\":\"92878\",\"rowId\":\"\",\"branchType\":3}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			if (jsonReq.containsKey("detailId")) {
				int detailId = jsonReq.getInt("detailId");
				int branchType = jsonReq.getInt("branchType");
				String rowId = jsonReq.getString("rowId");
				RdBranchSelector selector = new RdBranchSelector(conn);
				IRow row = selector.loadByDetailId(detailId, branchType, rowId, false);
				if (row != null) {
					JSONObject obj = row.Serialize(ObjLevel.FULL);
					if (!obj.containsKey("geometry")) {
						int pageNum = 1;
						int pageSize = 1;
						JSONObject data = new JSONObject();
						String primaryKey = "branch_pid";
						if (row instanceof IObj) {
							IObj iObj = (IObj) row;
							primaryKey = iObj.primaryKey().toLowerCase();
						}
						data.put(primaryKey, row.parentPKValue());
						SelectorUtils selectorUtils = new SelectorUtils(conn);
						JSONObject jsonObject = selectorUtils.loadByElementCondition(data, row.objType(), pageSize,
								pageNum, false);
						obj.put("geometry", jsonObject.getJSONArray("rows").getJSONObject(0).getString("geometry"));
					}
					
					Map<String,Object> result = new HashMap<String,Object>();
			    	result.put("errcode", 1);
			    	result.put("errmsg", "");
			    	result.put("data", obj);
					new ModelAndView("jsonView", result);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDeleteBranch() {
		String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"RDBRANCH\",\"detailId\":306000039,\"rowId\":\"\",\"branchType\":0}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(t.getLogs());
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testUpdateBranch() {
		String parameter = "{\"dbId\":25,\"type\":\"RDBRANCH\",\"detailId\":\"111401467052\",\"rowId\":\"\",\"branchType\":9}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(t.getLogs());
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRwRender() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			RdLaneConnexitySearch search = new RdLaneConnexitySearch(conn);

			List<SearchSnapshot> searchDataByTileWithGap = search.searchDataByTileWithGap(215663, 99422, 18, 80);

			System.out.println("data:" + ResponseUtils.assembleRegularResult(searchDataByTileWithGap));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetByLinkPid() {
		int linkPid = 208003004;

		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			RdBranchViaSelector viaSelector = new RdBranchViaSelector(conn);

			List<List<RdBranchVia>> loadRdBranchViaByLinkPid = viaSelector.loadRdBranchViaByLinkPid(linkPid, false);

			for (List<RdBranchVia> viaList : loadRdBranchViaByLinkPid) {
				for (RdBranchVia via : viaList) {
					System.out.println(via.Serialize(null));
				}
			}
		} catch (Exception e) {
		}
	}
}
