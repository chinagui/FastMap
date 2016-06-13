package com.navinfo.dataservice.engine.edit.xiaolong.ad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class POITest {
	public static void testGetByPid()
	{
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_prjgdb250_bj02", "fm_prjgdb250_bj02").getConnection();

//			SearchProcess p = new SearchProcess(conn);
//			
//			System.out.println(p.searchDataByPid(ObjType.IXPOI, 163).Serialize(ObjLevel.FULL));
			
			IxPoiSelector selector = new IxPoiSelector(conn);
			
			JSONObject jsonObject = selector.loadPids(false, 10,1);
			
			System.out.println(jsonObject);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void getTitleWithGap()
	{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getConnectionById(8);

			SearchProcess p = new SearchProcess(conn);
			
			List<ObjType> objType = new ArrayList<>();
			
			objType.add(ObjType.IXPOI);

			System.out.println(p.searchDataByTileWithGap(objType, 863538, 396925, 20, 80));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		getTitleWithGap();
	}
}
