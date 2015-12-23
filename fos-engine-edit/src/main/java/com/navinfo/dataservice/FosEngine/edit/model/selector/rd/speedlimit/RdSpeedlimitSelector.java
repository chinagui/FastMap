package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.speedlimit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import oracle.sql.STRUCT;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.geom.GeoTranslator;

public class RdSpeedlimitSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdSpeedlimitSelector.class);

	private Connection conn;

	public RdSpeedlimitSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdSpeedlimit obj = new RdSpeedlimit();

		String sql = "select * from " + obj.tableName() + " where node_pid=:1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				obj.setPid(resultSet.getInt("pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				obj.setGeometry(GeoTranslator.struct2Jts(struct, 100000, 0));

				obj.setRowId(resultSet.getString("row_id"));
				
				obj.setLinkPid(resultSet.getInt("linkPid"));
				
				obj.setDirect(resultSet.getInt("direct"));
				
				obj.setSpeedValue(resultSet.getInt("speed_value"));
				
				obj.setSpeedType(resultSet.getInt("speedType"));
				
				obj.setTollgateFlag(resultSet.getInt("tollgate_flag"));
				
				obj.setSpeedDependent(resultSet.getInt("speed_dependent"));
				
				obj.setSpeedFlag(resultSet.getInt("speed_flag"));
				
				obj.setLimitSrc(resultSet.getInt("limit_src"));
				
				obj.setTimeDomain(resultSet.getString("time_domain"));
				
				obj.setCaptureFlag(resultSet.getInt("capture_flag"));
				
				obj.setDescript(resultSet.getString("descript"));
				
				obj.setMeshId(resultSet.getInt("mesh_id"));
				
				obj.setStatus(resultSet.getInt("status"));
				
				obj.setCkStatus(resultSet.getInt("ck_status"));
				
				obj.setAdjaFlag(resultSet.getInt("adja_flag"));
				
				obj.setRecStatusIn(resultSet.getInt("rec_status_in"));
				
				obj.setRecStatusOut(resultSet.getInt("rec_status_out"));
				
				obj.setTimeDescript(resultSet.getString("time_descript"));
				
				obj.setLaneSpeedValue(resultSet.getString("lane_speed_value"));

			} else {
				
				throw new DataNotFoundException(null);
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

		return obj;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		return null;
	}

}
