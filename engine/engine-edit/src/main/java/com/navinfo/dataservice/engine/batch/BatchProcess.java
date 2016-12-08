package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONObject;

public class BatchProcess {
	private static final Logger logger = Logger.getLogger(BatchProcess.class);
	
	public String batchType;
	public String batchStep;
	
	public BatchProcess(){
		
	}
	
	public BatchProcess(String batchType,String batchStep) {
		this.batchType = batchType;
		this.batchStep = batchStep;
	}
	
	/**
	 * 执行批处理
	 * @param classNames
	 * @param poi
	 * @throws Exception
	 */
	public void execute(JSONObject json,Connection conn,EditApiImpl editApiImpl, List<String> batchList) throws Exception {
		JSONObject poiObj = new JSONObject();
		try {
			
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);

			IxPoi poi = (IxPoi) ixPoiSelector.loadById(json.getInt("objId"), false);
			
			// 修改为参数传入 -- zpp 2016.11.17 
			//List<String> batchList = getRowRules();
			
			for (String batch:batchList) {
				IBatch obj = (IBatch) Class.forName(batch).newInstance();
				logger.info("开始执行批处理："+obj.getClass().getName());
				JSONObject data = obj.run(poi,conn,json,editApiImpl);
				if (data.size()>0) {
					data.put("pid", poi.getPid());
					data.put("rowId", poi.getRowId());
					poiObj.put("change", data);
					poiObj.put("pid", poi.getPid());
					poiObj.put("type", "IXPOI");
					poiObj.put("command", "BATCH");
					poiObj.put("dbId", json.getInt("dbId"));
					poiObj.put("isLock", false);
					
					editApiImpl.runPoi(poiObj);
					
				}
			}
			conn.commit();
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	/**
	 * 查询需要执行的批处理
	 * @return
	 * @throws Exception
	 */
	public List<String> getRowRules() throws Exception {
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		Connection conn = null;
		try {
			String sql = "select process_path from batch_rule where kind='"+batchType+"' and steps='"+batchStep+"' and rule_status=1";
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
