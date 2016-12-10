package com.navinfo.dataservice.dao.glm.selector.poi.deep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.model.poi.deep.PoiColumnOpConf;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class IxPoiOpConfSelector extends AbstractSelector {

	private Connection conn;
	
	public IxPoiOpConfSelector(Connection conn) {
		super(conn);
		this.conn = conn;
	}
	
	/**
	 * 查询精编配置表
	 * @param secondWorkItem
	 * @param type
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public PoiColumnOpConf getDeepOpConf(String firstWorkItem,String secondWorkItem,int type) throws Exception {
		PoiColumnOpConf result = new PoiColumnOpConf();
		
		String sql = "SELECT * FROM poi_column_op_conf WHERE type=" + type;
		if (!firstWorkItem.isEmpty()) {
			sql += " AND first_work_item='" + firstWorkItem + "'";
		}
		if (!secondWorkItem.isEmpty()) {
			sql += " AND second_work_item='" + secondWorkItem + "'";
		}
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();
			
			result = getDeepOpConfObj(resultSet);
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	private PoiColumnOpConf getDeepOpConfObj(ResultSet resultSet) throws Exception {
		PoiColumnOpConf result = new PoiColumnOpConf();
		try {
			if (resultSet.next()) {
				result.setId(resultSet.getString("ID"));
				result.setFirstWorkItem(resultSet.getString("FIRST_WORK_ITEM"));
				result.setSecondWorkItem(resultSet.getString("SECOND_WORK_ITEM"));
				result.setSaveExebatch(resultSet.getInt("SAVE_EXEBATCH"));
				result.setSaveBatchrules(resultSet.getString("SAVE_BATCHRULES"));
				result.setSaveExecheck(resultSet.getInt("SAVE_EXECHECK"));
				result.setSaveCkrules(resultSet.getString("SAVE_CKRULES"));
				result.setSaveExeclassify(resultSet.getInt("SAVE_EXECLASSIFY"));
				result.setSaveClassifyrules(resultSet.getString("SAVE_CLASSIFYRULES"));
				result.setSubmitExebatch(resultSet.getInt("SUBMIT_EXEBATCH"));
				result.setSubmitBatchrules(resultSet.getString("SUBMIT_BATCHRULES"));
				result.setSubmitExecheck(resultSet.getInt("SUBMIT_EXECHECK"));
				result.setSubmitCkrules(resultSet.getString("SUBMIT_CKRULES"));
				result.setSubmitExeclassify(resultSet.getInt("SUBMIT_EXECLASSIFY"));
				result.setSubmitClassifyrules(resultSet.getString("SUBMIT_CLASSIFYRULES"));
				result.setType(resultSet.getInt("type"));
			}
			return result;
		} catch (Exception e) {
			throw e;
		}
	}
	
}
