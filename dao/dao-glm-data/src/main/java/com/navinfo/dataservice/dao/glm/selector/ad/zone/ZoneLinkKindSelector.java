package com.navinfo.dataservice.dao.glm.selector.ad.zone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkMesh;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;


/**
 * ZONE:LINK种类表  查询接口
 * @author zhaokk
 *
 */
public class ZoneLinkKindSelector implements ISelector{
	
	private static Logger logger = Logger.getLogger(ZoneLinkKindSelector.class);

	private Connection conn;
	
	public ZoneLinkKindSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		ZoneLinkKind kind = new ZoneLinkKind();

		String sql = "select * from " + kind.tableName() + " where row_id=hextoraw(:1)";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, rowId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				kind.setLinkPid(resultSet.getInt("link_pid"));
				kind.setKind(resultSet.getInt("kind"));
				kind.setFrom(resultSet.getInt("form"));
				kind.setRowId(resultSet.getString("row_id"));
			} else {
				
				throw new DataNotFoundException("数据不存在");
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

		return kind;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from zone_link_kind where link_pid =:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				ZoneLinkKind kind = new ZoneLinkKind();

				kind.setLinkPid(resultSet.getInt("link_pid"));
				kind.setKind(resultSet.getInt("kind"));
				kind.setFrom(resultSet.getInt("form"));
				kind.setRowId(resultSet.getString("row_id"));

				rows.add(kind);
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

		return rows;
	}

}
