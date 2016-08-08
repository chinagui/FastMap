package com.navinfo.dataservice.dao.glm.selector.rd.slope;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

/***
 * 
 * @author zhaokk
 * 
 */
public class RdSlopeSelector extends AbstractSelector {

	private Connection conn;

	public RdSlopeSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdSlope.class);
	}

	/***
	 * 
	 * 通过退出线查找坡度信息
	 * 
	 * @param linkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdSlope> loadByOutLink(int linkPid, boolean isLock)
			throws Exception {

		List<RdSlope> rows = new ArrayList<RdSlope>();

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			String sql = "SELECT pid FROM rd_slope WHERE link_pid =:1 and u_record !=2";

			if (isLock) {
				sql += " for update nowait";
			}

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				AbstractSelector abSelector = new AbstractSelector(
						RdSlope.class, conn);
				RdSlope slope = (RdSlope) abSelector.loadById(
						resultSet.getInt("pid"), false);
				rows.add(slope);
			}

			return rows;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/***
	 * 
	 * 通过退出线查找坡度信息
	 * 
	 * @param linkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdSlopeVia> loadBySeriesLink(int linkPid, boolean isLock)
			throws Exception {

		List<RdSlopeVia> rows = new ArrayList<RdSlopeVia>();

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			String sql = "SELECT link_pid,slope_pid,seq_num FROM rd_slope_via WHERE link_pid =:1 and u_record !=2";

			if (isLock) {
				sql += " for update nowait";
			}

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdSlopeVia slopeVia = new RdSlopeVia();
				ReflectionAttrUtils.executeResultSet(slopeVia, resultSet);
				rows.add(slopeVia);
			}

			return rows;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
}
