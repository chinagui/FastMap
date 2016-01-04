package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.speedlimit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.geom.GeoTranslator;

public class RdSpeedlimitSelector implements ISelector {

	private Connection conn;

	public RdSpeedlimitSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdSpeedlimit obj = new RdSpeedlimit();

		String sql = "select * from " + obj.tableName() + " where pid=:1";

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
				
				obj.setLinkPid(resultSet.getInt("link_pid"));
				
				obj.setDirect(resultSet.getInt("direct"));
				
				obj.setSpeedValue(resultSet.getInt("speed_value"));
				
				obj.setSpeedType(resultSet.getInt("speed_type"));
				
//				obj.setTollgateFlag(resultSet.getInt("tollgate_flag"));
				
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
	
	public List<RdSpeedlimit> loadSpeedlimitByLinkPid(int linkPid,
			boolean isLock) throws Exception {
		List<RdSpeedlimit> limits = new ArrayList<RdSpeedlimit>();

		String sql = "select * from rd_speedlimit where link_pid = :1 and u_record!=:2";

		if (isLock) {
			sql = sql + " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdSpeedlimit limit = new RdSpeedlimit();

				limit.setPid(resultSet.getInt("pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				limit.setGeometry(GeoTranslator.struct2Jts(struct, 100000, 0));

				limit.setRowId(resultSet.getString("row_id"));
				
				limit.setLinkPid(resultSet.getInt("linkPid"));
				
				limit.setDirect(resultSet.getInt("direct"));
				
				limit.setSpeedValue(resultSet.getInt("speed_value"));
				
				limit.setSpeedType(resultSet.getInt("speedType"));
				
				limit.setSpeedDependent(resultSet.getInt("speed_dependent"));
				
				limit.setSpeedFlag(resultSet.getInt("speed_flag"));
				
				limit.setLimitSrc(resultSet.getInt("limit_src"));
				
				limit.setTimeDomain(resultSet.getString("time_domain"));
				
				limit.setCaptureFlag(resultSet.getInt("capture_flag"));
				
				limit.setDescript(resultSet.getString("descript"));
				
				limit.setMeshId(resultSet.getInt("mesh_id"));
				
				limit.setStatus(resultSet.getInt("status"));
				
				limit.setCkStatus(resultSet.getInt("ck_status"));
				
				limit.setAdjaFlag(resultSet.getInt("adja_flag"));
				
				limit.setRecStatusIn(resultSet.getInt("rec_status_in"));
				
				limit.setRecStatusOut(resultSet.getInt("rec_status_out"));
				
				limit.setTimeDescript(resultSet.getString("time_descript"));
				
				limit.setLaneSpeedValue(resultSet.getString("lane_speed_value"));

				limits.add(limit);
			}
		} catch (Exception e) {
			
			throw e;
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {
				
			}

			try {
				pstmt.close();
			} catch (Exception e) {
				
			}
		}

		return limits;
	}
	
	
	//通过传入点限速的LINKPID和通行方向，返回跟踪LINK路径
	public String trackSpeedLimitLink(int linkPid,int direct) throws Exception
	{
		String path = null;
		
		String sql = "select package_utils.track_links(:1,:2) v_path from dual ";
				
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, direct);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				path = resultSet.getString("v_path");
			}
		} catch (Exception e) {
			
			throw e;
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {
				
			}

			try {
				pstmt.close();
			} catch (Exception e) {
				
			}
		}
		
		return path;
	}

}
