package com.navinfo.dataservice.engine.edit.zhaokk.rd;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.SelectorUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;

public class SearchTest extends InitApplication{
	private Connection conn;
	@Override
	@Before
	public void init() {
		initContext();
		try {
			this.conn = DBConnector.getInstance().getConnectionById(19);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public SearchTest() throws Exception{
		
		
	}
	@Test
	public void testQuery(){
		//Connection conn = null;

		try {
			//JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			String objType = "RDBRANCH";

			//int dbId = 19;

			//conn = DBConnector.getInstance().getConnectionById(dbId);

			
				int detailId = 92878;
				int branchType = 3;
				String rowId = "";
				RdBranchSelector selector = new RdBranchSelector(conn);
				IRow row = selector.loadByDetailId(detailId, branchType, rowId,
						false);

				if (row != null) {
					JSONObject obj = row.Serialize(ObjLevel.FULL);
					if (!obj.containsKey("geometry")) {
						int pageNum = 1;
						int pageSize = 1;
						JSONObject data = new JSONObject();
						String primaryKey = "branch_pid";
						if(row instanceof IObj){
							IObj iObj = (IObj)row;
							primaryKey = iObj.primaryKey().toLowerCase();
						}
						data.put(primaryKey, row.parentPKValue());
						SelectorUtils selectorUtils = new SelectorUtils(conn);
						JSONObject jsonObject = selectorUtils
								.loadByElementCondition(data,
										row.objType(), pageSize,
										pageNum, false);
						obj.put("geometry", jsonObject.getJSONArray("rows")
								.getJSONObject(0).getString("geometry"));
					}
					
					obj.put("geoLiveType", objType);
					
					
				//return new ModelAndView("jsonView", success(obj));

				} else {
					//return new ModelAndView("jsonView", success());
				}

			
		} catch (Exception e) {
			//logger.info(e.getMessage(), e);

			//logger.error(e.getMessage(), e);

			//return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	

}
