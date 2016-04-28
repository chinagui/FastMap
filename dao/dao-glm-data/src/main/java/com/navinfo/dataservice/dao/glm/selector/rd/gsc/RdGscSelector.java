package com.navinfo.dataservice.dao.glm.selector.rd.gsc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class RdGscSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdCrossSelector.class);

	private Connection conn;

	public RdGscSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		RdGsc rdGsc = new RdGsc();

		String sql = "select * from " + rdGsc.tableName() + " where pid = :1";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				rdGsc.setPid(resultSet.getInt("pid"));

				rdGsc.setProcessFlag(resultSet.getInt("PROCESS_FLAG"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				rdGsc.setGeometry(geometry);

				List<IRow> links = new RdGscLinkSelector(conn).loadRowsByParentId(id, isLock);

				rdGsc.setLinks(links);

				for (IRow row : rdGsc.getLinks()) {
					RdGscLink obj = (RdGscLink) row;

					rdGsc.rdGscLinkMap.put(obj.rowId(), obj);
				}

				rdGsc.setRowId(resultSet.getString("row_id"));

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

		return rdGsc;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}

	public List<RdGsc> loadRdGscLinkByLinkPid(int linkPid, boolean isLock) throws Exception {
		List<RdGsc> rdGscList = new ArrayList<RdGsc>();

		String sql = "SELECT a.* FROM rd_gsc a,rd_gsc_link b WHERE a.pid = b.pid AND b.link_pid = :1 and a.u_record!=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdGsc rdGsc = new RdGsc();

				rdGsc.setPid(resultSet.getInt("pid"));

				rdGsc.setProcessFlag(resultSet.getInt("PROCESS_FLAG"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				rdGsc.setGeometry(geometry);

				List<IRow> links = new RdGscLinkSelector(conn).loadRowsByParentId(rdGsc.getPid(),
						isLock);

				rdGsc.setLinks(links);

				for (IRow row : rdGsc.getLinks()) {
					RdGscLink obj = (RdGscLink) row;

					rdGsc.rdGscLinkMap.put(obj.rowId(), obj);
				}

				rdGsc.setRowId(resultSet.getString("row_id"));

				rdGscList.add(rdGsc);
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

		return rdGscList;
	}

	public List<RdGsc> loadRdGscLinkByLinkPids(List<Integer> linkPids, boolean isLock)
			throws Exception {
		List<RdGsc> rdgscs = new ArrayList<RdGsc>();

		if (linkPids.size() == 0) {
			return rdgscs;
		}

		String s = "";
		for (int i = 0; i < linkPids.size(); i++) {
			if (i > 0) {
				s += ",";
			}

			s += linkPids.get(i);
		}

		String sql = "SELECT a.* FROM rd_gsc a,rd_gsc_link b WHERE a.pid = b.pid AND b.link_pid in ("
				+ s + ") and a.u_record!=2";

		if (isLock) {
			sql = sql + " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdGsc rdGsc = new RdGsc();

				rdGsc.setPid(resultSet.getInt("pid"));

				rdGsc.setProcessFlag(resultSet.getInt("PROCESS_FLAG"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				rdGsc.setGeometry(geometry);

				List<IRow> links = new RdGscLinkSelector(conn).loadRowsByParentId(rdGsc.getPid(),
						isLock);

				rdGsc.setLinks(links);

				for (IRow row : rdGsc.getLinks()) {
					RdGscLink obj = (RdGscLink) row;

					rdGsc.rdGscLinkMap.put(obj.rowId(), obj);
				}

				rdGsc.setRowId(resultSet.getString("row_id"));

				rdgscs.add(rdGsc);
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

		return rdgscs;
	}
}
