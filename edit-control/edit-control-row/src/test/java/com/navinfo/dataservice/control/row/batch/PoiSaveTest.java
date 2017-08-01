/**
 * 
 */
package com.navinfo.dataservice.control.row.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.control.row.query.PoiQuery;
import com.navinfo.dataservice.control.row.save.PoiSave;
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
		String parameter = "{\"dbId\":18,\"subtaskId\":\"4\",\"type\":2,\"pageNum\":1,\"pageSize\":20,\"pidName\":\"\",\"pid\":0}";
		PoiQuery query = new PoiQuery();
		query.getPoiList(parameter);
	}
	@Test
	public void testUpdatePoi() throws SQLException {
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":22,\"type\":\"IXPOI\",\"objId\":4602392,\"data\":{\"addresses\":[{\"nameGroupid\":1,\"poiPid\":0,\"langCode\":\"CHI\",\"fullname\":\"\",\"objStatus\":\"INSERT\"}],\"rowId\":\"3F836D0B49AA4604E050A8C083041544\",\"pid\":4602392}}";
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
				BatchProcess batchProcess = new BatchProcess("row","save");
				batchProcess.execute(json, conn, editApiImpl, batchProcess.getRowRules());
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
	
	@Test
	public void testUpdatePoiStatus() throws Exception {
		Connection conn = null;
		try {
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			
			Map<String, Integer> taskInfo = apiService.getTaskBySubtaskId(25);
			
			Map<String, Integer> newTaskInfo= changeTaskInfo(25,taskInfo);
			
			conn = DBConnector.getInstance().getConnectionById(13);
			
			upatePoiStatus("18094", conn, newTaskInfo, true);
		} catch (DataNotChangeException e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DBUtils.closeConnection(conn);;
		}
	}
	public void upatePoiStatus(String pids, Connection conn,Map<String, Integer> newTaskInfo, boolean flag)
			throws Exception {
		int qst=newTaskInfo.get("QUICK_SUBTASK_ID");
		int qt=newTaskInfo.get("QUICK_TASK_ID");
		int mst=newTaskInfo.get("MEDIUM_SUBTASK_ID");
		int mt=newTaskInfo.get("MEDIUM_TASK_ID");
		
		String str = qst+","+qt+","+mst+","+mt;
		StringBuilder sb = new StringBuilder();
		if (flag) {
			//对应需求任务4403，其中涉及任务号的变更，详细见《一体化日编任务作业.vsd》
			sb.append(" MERGE INTO poi_edit_status T1 ");
			sb.append(" USING (SELECT ");
			sb.append(" 	(CASE WHEN "+mst+" = 0 THEN T.MEDIUM_SUBTASK_ID WHEN T.STATUS IN (1, 2) AND T.MEDIUM_SUBTASK_ID NOT IN (0,"+mst+") THEN T.MEDIUM_SUBTASK_ID ELSE "+mst+" END) MST,");
			sb.append(" 	(CASE WHEN "+mt+" = 0 THEN T.MEDIUM_TASK_ID WHEN T.STATUS IN (1, 2) AND T.MEDIUM_TASK_ID NOT IN (0,"+mt+") THEN T.MEDIUM_TASK_ID ELSE "+mt+" END) MT,");
			sb.append(" 	(CASE WHEN "+qst+" = 0 THEN T.QUICK_SUBTASK_ID WHEN T.STATUS IN (1, 2) AND T.QUICK_SUBTASK_ID NOT IN (0,"+qst+") THEN T.QUICK_SUBTASK_ID ELSE "+qst+" END) QST,");
			sb.append(" 	(CASE WHEN "+qt+" = 0 THEN T.QUICK_TASK_ID WHEN T.STATUS IN (1, 2) AND T.QUICK_TASK_ID NOT IN (0,"+qt+") THEN T.QUICK_TASK_ID ELSE "+qt+" END) QT,");
			sb.append(" 	(CASE WHEN "+mst+" <> 0 AND T.STATUS IN (1, 2) AND T.MEDIUM_SUBTASK_ID <> 0 AND "+mst+" <> T.MEDIUM_SUBTASK_ID THEN T.STATUS");
			sb.append(" 		  WHEN "+qst+" <> 0 AND T.STATUS IN (1, 2) AND T.QUICK_SUBTASK_ID <> 0 AND "+qst+" <> T.QUICK_SUBTASK_ID THEN T.STATUS");
			sb.append(" 		  ELSE 2 END) B,");
			sb.append(" 	0 AS C,");
			sb.append(" 	IX.PID AS D");
			sb.append(" 	FROM IX_POI IX, POI_EDIT_STATUS T WHERE IX.PID = T.PID(+) AND IX.PID IN ("+ pids + ")) T2 ");
			sb.append(" ON ( T1.pid=T2.d) ");
			sb.append(" WHEN MATCHED THEN ");
			sb.append(" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c,T1.QUICK_SUBTASK_ID=T2.QST,T1.QUICK_TASK_ID=T2.QT,T1.MEDIUM_SUBTASK_ID=T2.MST,T1.MEDIUM_TASK_ID=T2.MT ");
			sb.append(" WHEN NOT MATCHED THEN ");
			// zl 2016.12.08 新增时为 commit_his_status 字段赋默认值 0
			sb.append(" INSERT (T1.status,T1.fresh_verified,T1.pid,T1.commit_his_status,T1.QUICK_SUBTASK_ID,T1.QUICK_TASK_ID,T1.MEDIUM_SUBTASK_ID,T1.MEDIUM_TASK_ID) VALUES(T2.b,T2.c,T2.d,0,"+ str +")");
		} else {
			//鲜度验证保存时调用
			sb.append(" UPDATE poi_edit_status T1 SET T1.status = 2 where T1.pid in ("
					+ pids + ")");
		}

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
	private Map<String, Integer> changeTaskInfo(int subtaskId,Map<String, Integer> taskInfo) throws Exception {
		Map<String, Integer> newTaskInfo =new HashMap<String, Integer>();
		if(taskInfo.get("programType")==1){
			newTaskInfo.put("MEDIUM_SUBTASK_ID",subtaskId);
			newTaskInfo.put("MEDIUM_TASK_ID",taskInfo.get("taskId"));
			newTaskInfo.put("QUICK_SUBTASK_ID",0);
			newTaskInfo.put("QUICK_TASK_ID",0);
		}else{
			newTaskInfo.put("MEDIUM_SUBTASK_ID",0);
			newTaskInfo.put("MEDIUM_TASK_ID",0);
			newTaskInfo.put("QUICK_SUBTASK_ID",subtaskId);
			newTaskInfo.put("QUICK_TASK_ID",taskInfo.get("taskId"));
		}
		
		return newTaskInfo;
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
	
	@Test
	public void testPoiSave() throws Exception{
		String parameter = "{\"command\":\"BATCHMOVE\",\"type\":\"IXPOI\",\"dbId\":13,\"subtaskId\":114,\"data\":[{\"location\":[116.43889,39.94855],"
				+ "\"guidePoint\":[116.43889,39.94864099308741],\"guideLink\":41575389,\"pid\":509000133},{\"location\":[116.655,39.927],"
				+ "\"guidePoint\":[116.4551,39.92487],\"guideLink\":476052,\"pid\":2806}]}}";
		
		
		PoiSave ps = new PoiSave();
		JSONObject res = ps.save(parameter, 2);
		System.out.println(res);
	}
	
	
	
	
	
}
