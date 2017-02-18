package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.search.RdLinkSearch;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.SelectorUtils;
import com.navinfo.dataservice.dao.glm.utils.TableNameFactory;
import com.navinfo.dataservice.dao.glm.utils.TableNameSqlInfo;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class RdLinkTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	public RdLinkTest() throws Exception {
	}

	@Test
	public void testGetByPid() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDLINK, 733775).Serialize(ObjLevel.FULL));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLoadByIds() throws SQLException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			AbstractSelector selector = new AbstractSelector(IxPoi.class, conn);
			
			List<Integer> pidList = new ArrayList<>();
			
			pidList.add(1152117237);
			
			pidList.add(472);
			
			List<IRow> list = selector.loadByIds(pidList, false,false);
			
			System.out.println(list.get(0).Serialize(null));
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.close(conn);
		}

	}

	@Test
	public void testDelete() {
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":202003410,\"data\":{\"forms\":[{\"linkPid\":202003410,\"formOfWay\":50,\"extendedForm\":0,\"auxiFlag\":0,\"kgFlag\":0,\"objStatus\":\"INSERT\"}],\"rowId\":\"0E72E3A8F3F04E3DB5E4C70F2102B4DE\",\"pid\":202003410,\"objStatus\":\"UPDATE\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAddRdLink() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"objId\":209003265,\"data\":{\"longitude\":116.21100917458534,\"latitude\":40.53466047250868},\"type\":\"RDNODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRepairLink() {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDSLOPE\",\"dbId\":17,\"data\":{\"objStatus\":\"UPDATE\",\"pid\":203000000,\"linkPids\":[301000097,304000077,205000088]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testBreakRdLink() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"RDSAMENODE\",\"data\":{\"nodes\":[{\"nodePid\":\"210001823\",\"type\":\"RDNODE\",\"isMain\":1},{\"nodePid\":\"309000026\",\"type\":\"LUNODE\",\"isMain\":0}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetByElementCondition()
	{
		String parameter = "{\"dbId\":19,\"pageNum\":1,\"pageSize\":5,\"data\":{\"detailId\":\"10208\"},\"type\":\"RDBRANCHDETAIL\"}";

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			ObjType tableName = ObjType.valueOf(jsonReq.getString("type"));
			int pageNum = jsonReq.getInt("pageNum");
			int pageSize = jsonReq.getInt("pageSize");
			int dbId = jsonReq.getInt("dbId");
			JSONObject data = jsonReq.getJSONObject("data");
			conn = DBConnector.getInstance().getConnectionById(dbId);
			SelectorUtils selectorUtils = new SelectorUtils(conn);
			JSONObject jsonObject = selectorUtils.loadByElementCondition(data,tableName, pageSize, pageNum, false);
			System.out.println(jsonObject.toString());

		} catch (Exception e) {
			e.printStackTrace();
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
	
	@Test
	public void testBatch()
	{
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":259,\"type\":\"RDLINK\",\"objId\":400001194,\"data\":{\"forms\":[{\"linkPid\":400001194,\"formOfWay\":20,\"extendedForm\":0,\"auxiFlag\":0,\"kgFlag\":0,\"objStatus\":\"INSERT\"},{\"linkPid\":400001194,\"rowId\":\"2AED548354544F6C804F2D67C3DDB351\",\"objStatus\":\"DELETE\"}],\"pid\":400001194,\"objStatus\":\"UPDATE\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRdLinkRender()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			RdLinkSearch search = new RdLinkSearch(conn);
			
			List<SearchSnapshot> data = search.searchDataByTileWithGap(220933, 98013, 18, 10);
			
			System.out.println("data:"+ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testParseTableName() throws Exception
	{
		TableNameFactory tableNameFactory = TableNameFactory.getInstance();
		
		TableNameSqlInfo sqlInfo = tableNameFactory.getSqlInfoByTableName("RW_LINK");
		
		System.out.println(sqlInfo.getLeftJoinSql());
		
		TableNameSqlInfo sqlInfo2 = tableNameFactory.getSqlInfoByTableName("RD_OBJECT2");
		
		System.out.println(sqlInfo2.getLeftJoinSql());
	}
}
