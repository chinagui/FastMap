/**
 * 
 */
package com.navinfo.dataservice.control.row.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.control.row.query.PoiQuery;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.batch.BatchProcess;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

/**
 * @ClassName: PoiSaveTest
 * @author Zhang Xiaolong
 * @date 2016年9月10日 上午8:36:49
 * @Description: TODO
 */
public class PoiSaveTest {
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testGetPoiList() throws Exception
	{
		String parameter = "{\"dbId\":17,\"subtaskId\":\"61\",\"type\":2,\"pageNum\":1,\"pageSize\":20,\"pidName\":\"\",\"pid\":0}";
		PoiQuery query = new PoiQuery();
		query.getPoiList(parameter);
	}
	@Test
	public void testUpdatePoi() throws SQLException {
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"IXPOI\",\"objId\":80070981,\"data\":{\"addresses\":[{\"nameGroupid\":1,\"poiPid\":0,\"langCode\":\"CHI\",\"fullname\":\"１１１１\",\"objStatus\":\"INSERT\"}],\"rowId\":\"3AE1FB8B133A92F7E050A8C08304EE4C\",\"pid\":80070981}}";
		Connection conn = null;
		JSONObject result = null;
		try {

			JSONObject json = JSONObject.fromObject(parameter);

			OperType operType = Enum.valueOf(OperType.class, json.getString("command"));

			ObjType objType = Enum.valueOf(ObjType.class, json.getString("type"));

			int dbId = json.getInt("dbId");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			EditApiImpl editApiImpl = new EditApiImpl(conn);

			editApiImpl.setToken(2);

			result = editApiImpl.runPoi(json);

			StringBuffer buf = new StringBuffer();

			int pid = 0;

			if (operType != OperType.CREATE) {
				if (objType == ObjType.IXSAMEPOI) {
					String poiPids = JsonUtils.getStringValueFromJSONArray(json.getJSONArray("poiPids"));
					buf.append(poiPids);
				} else {
					pid = json.getInt("objId");

					buf.append(String.valueOf(pid));
				}
			} else {
				pid = result.getInt("pid");
				buf.append(String.valueOf(pid));
			}

			if (operType == OperType.UPDATE) {
				json.put("objId", pid);
				BatchProcess batchProcess = new BatchProcess();
				batchProcess.execute(json, conn, editApiImpl);
			}

			upatePoiStatus(buf.toString(), conn);

		} catch (DataNotChangeException e) {
			DbUtils.rollback(conn);

		} catch (Exception e) {
			DbUtils.rollback(conn);
			e.printStackTrace();
		} finally {
			DbUtils.commitAndClose(conn);
		}
		System.out.println(result.toString());
	}

	/**
	 * poi操作修改poi状态为已作业，鲜度信息为0 zhaokk sourceFlag 0 web 1 Android
	 * 
	 * @param row
	 * @throws Exception
	 */
	public void upatePoiStatus(String pids, Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder(" MERGE INTO poi_edit_status T1 ");
		sb.append(" USING (SELECT row_id as a , 2 AS b,0 AS C FROM ix_poi where pid in (" + pids + ")) T2 ");
		sb.append(" ON ( T1.row_id=T2.a) ");
		sb.append(" WHEN MATCHED THEN ");
		sb.append(" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c ");
		sb.append(" WHEN NOT MATCHED THEN ");
		sb.append(" INSERT (T1.row_id,T1.status,T1.fresh_verified) VALUES(T2.a,T2.b,T2.c)");
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;

		} finally {
			DBUtils.closeStatement(pstmt);
		}

	}
}
