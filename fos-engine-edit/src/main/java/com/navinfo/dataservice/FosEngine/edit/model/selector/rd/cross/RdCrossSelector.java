package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.cross;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.exception.DataNotFoundException;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossName;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossNode;

public class RdCrossSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdCrossSelector.class);

	private Connection conn;

	public RdCrossSelector(Connection conn) {
		this.conn = conn;

	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdCross cross = new RdCross();

		String sql = "select * from " + cross.tableName() + " where pid=:1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				cross.setPid(resultSet.getInt("pid"));

				cross.setType(resultSet.getInt("type"));

				cross.setSignal(resultSet.getInt("signal"));

				cross.setElectroeye(resultSet.getInt("electroeye"));

				cross.setKgFlag(resultSet.getInt("kgFlag"));

				List<IRow> links = new RdCrossLinkSelector(conn)
						.loadRowsByParentId(id, isLock);

				cross.setLinks(links);
				
				for (IRow row : cross.getLinks()) {
					RdCrossLink obj = (RdCrossLink) row;

					cross.linkMap.put(obj.rowId(), obj);
				}

				List<IRow> nodes = new RdCrossNodeSelector(conn)
						.loadRowsByParentId(id, isLock);

				cross.setNodes(nodes);
				
				for (IRow row : cross.getNodes()) {
					RdCrossNode obj = (RdCrossNode) row;

					cross.nodeMap.put(obj.rowId(), obj);
				}

				List<IRow> names = new RdCrossNameSelector(conn)
						.loadRowsByParentId(id, isLock);

				cross.setNames(names);
				
				for (IRow row : cross.getNames()) {
					RdCrossName obj = (RdCrossName) row;

					cross.nameMap.put(obj.rowId(), obj);
				}

				cross.setRowId(resultSet.getString("row_id"));

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

		return cross;
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
