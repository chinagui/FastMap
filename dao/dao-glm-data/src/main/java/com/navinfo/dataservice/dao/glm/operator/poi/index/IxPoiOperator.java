package com.navinfo.dataservice.dao.glm.operator.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;


/**
 * POI主表操作类
 * 
 * @author zhangxiaolong
 * 
 */
public class IxPoiOperator  {

	private IxPoi ixPoi;

	private int freshFlag;

	private String rawFields;
	private Connection conn;

	public IxPoiOperator(Connection conn, String rowId, int freshFlag,
			String rawFields) throws Exception {

		ixPoi = new IxPoi();
		ixPoi.setRowId(rowId);
		this.freshFlag = freshFlag;
		this.rawFields = rawFields;
		upatePoiStatusForAndroid();
	}

	

	/**
	 * poi操作修改poi状态为待作业 by wdb
	 * 
	 * @param row
	 * @throws Exception
	 */
	public void upatePoiStatusForAndroid() throws Exception {
		StringBuilder sb = new StringBuilder(" MERGE INTO poi_edit_status T1 ");
		sb.append(" USING (SELECT '" + ixPoi.getRowId() + "' as a, 1 as b,"
				+ freshFlag + " as c,'" + rawFields + "' as d,"
				+ "sysdate as e" + "  FROM dual) T2 ");
		sb.append(" ON ( T1.row_id=T2.a) ");
		sb.append(" WHEN MATCHED THEN ");
		sb.append(" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c,T1.is_upload = T2.b,T1.raw_fields = T2.d,T1.upload_date = T2.e ");
		sb.append(" WHEN NOT MATCHED THEN ");
		sb.append(" INSERT (T1.row_id,T1.status,T1.fresh_verified,T1.is_upload,T1.raw_fields,T1.upload_date) VALUES(T2.a,T2.b,T2.c,T2.b,T2.d,T2.e)");
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.executeUpdate();
			conn.commit();
		} catch (Exception e) {
			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}
	}



}
