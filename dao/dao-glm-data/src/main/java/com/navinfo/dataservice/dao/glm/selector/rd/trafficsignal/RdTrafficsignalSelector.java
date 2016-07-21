/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;

/** 
* @ClassName: RdTrafficsignalSelector 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:34:21 
* @Description: TODO
*/
public class RdTrafficsignalSelector implements ISelector {

	private Connection conn;

	public RdTrafficsignalSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		RdTrafficsignal rdTrafficsignal = new RdTrafficsignal();

		StringBuilder sb = new StringBuilder(
				"select * from " + rdTrafficsignal.tableName() + " where pid = :1 and u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				rdTrafficsignal.setPid(id);
				
				rdTrafficsignal.setRowId(resultSet.getString("row_id"));
			}
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

		return rdTrafficsignal;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}

}
