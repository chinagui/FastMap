package com.navinfo.dataservice.control.row.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.control.row.batch.util.IBatch;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONObject;

public class BatchProcess {

	public BatchProcess() {
		
	}
	
	/**
	 * 执行批处理
	 * @param classNames
	 * @param poi
	 * @throws Exception
	 */
	public void execute(JSONObject json,Connection conn,EditApiImpl editApiImpl) throws Exception {
		JSONObject poiObj = new JSONObject();
		try {
			
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);

			IxPoi poi = (IxPoi) ixPoiSelector.loadById(json.getInt("objId"), false);
			
			List<String> batchList = getRowRules();
			
			JSONObject result = new JSONObject();
			for (String batch:batchList) {
				IBatch obj = (IBatch) Class.forName(batch).newInstance();
				JSONObject data = obj.run(poi,conn,json,editApiImpl);
				result.putAll(data);
			}
			
			if (result.size()>0) {
				result.put("pid", poi.getPid());
				result.put("rowId", poi.getRowId());
				poi.fillChangeFields(result);
				poiObj.put("poi", poi.Serialize(null));
				poiObj.put("pid", poi.getPid());
				poiObj.put("type", "IXPOI");
				poiObj.put("command", "BATCH");
				poiObj.put("dbId", json.getInt("dbId"));
				poiObj.put("isLock", false);
				
				editApiImpl.runPoi(poiObj);
			}
			
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	/**
	 * 查询需要执行的批处理
	 * @return
	 * @throws Exception
	 */
	private List<String> getRowRules() throws Exception {
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		Connection conn = null;
		try {
			String sql = "select process_path from batch_rule where kind='row' and steps='save' and rule_status=1";
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			List<String> batchList = new ArrayList<String>();
			while (resultSet.next()) {
				batchList.add(resultSet.getString("process_path"));
			}
			return batchList;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
		
	}
	
}
