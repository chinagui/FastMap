package com.navinfo.dataservice.dao.glm.selector.rd.slope;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;

import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

public class RdSlopeSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdSlopeSelector.class);

	private Connection conn;

	public RdSlopeSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		RdSlope slpoe = new RdSlope();

		StringBuilder sb = new StringBuilder("select * from "
				+ slpoe.tableName() + " WHERE pid = :1 and  u_record !=2");

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
				ReflectionAttrUtils.executeResultSet(slpoe, resultSet);
				// 获取AD_Node对应的关联数据

				// ad_node_mesh
				List<IRow> slopeVias = new RdSlopeViaSelector(conn)
						.loadRowsByParentId(id, isLock);

				slpoe.setSlopeVias(slopeVias);

				for (IRow row : slpoe.getSlopeVias()) {
					RdSlopeVia rdSlopeVia = (RdSlopeVia) row;

					slpoe.rdSlopeMap.put(rdSlopeVia.rowId(), rdSlopeVia);
				}

				return slpoe;
			} else {

				throw new Exception("对应RD_SLOPE不存在!");
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
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		return null;
	}
}
