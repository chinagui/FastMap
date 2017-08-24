package com.navinfo.dataservice.dao.glm.selector.rd.speedlimit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdSpeedlimitSelector extends AbstractSelector {

	private Connection conn;

	public RdSpeedlimitSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdSpeedlimit.class);
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
				ReflectionAttrUtils.executeResultSet(limit, resultSet);
				limits.add(limit);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return limits;
	}

	// 通过传入点限速的LINKPID和通行方向，返回跟踪LINK路径
	public String trackSpeedLimitLink(int linkPid, int direct) throws Exception {
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
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return path;
	}

	public List<RdSpeedlimit> loadSpeedlimitByLinkPids(List<Integer> linkPids,
			boolean isLock) throws Exception {
		List<RdSpeedlimit> limits = new ArrayList<RdSpeedlimit>();

		if (linkPids.size() == 0) {
			return limits;
		}

		String s = "";
		for (int i = 0; i < linkPids.size(); i++) {
			if (i > 0) {
				s += ",";
			}

			s += linkPids.get(i);
		}

		String sql = "select * from rd_speedlimit where link_pid in (" + s
				+ ") and u_record!=2";

		if (isLock) {
			sql = sql + " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdSpeedlimit limit = new RdSpeedlimit();

				ReflectionAttrUtils.executeResultSet(limit, resultSet);

				limits.add(limit);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return limits;
	}

	public RdSpeedlimit loadDelSpeedlimit(int pid) throws Exception {

		String sql = "SELECT * FROM RD_SPEEDLIMIT WHERE PID = :1 AND U_RECORD =2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, pid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				RdSpeedlimit limit = new RdSpeedlimit();

				ReflectionAttrUtils.executeResultSet(limit, resultSet);

				return limit;
			} else {
				throw new Exception("删除状态的点限速PID：" + pid + "不存在");
			}

		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}

}
