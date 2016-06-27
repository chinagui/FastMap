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
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;

/**
 * ZONE:Face Topo 查询接口
 * 
 * @author zhaokk
 * 
 */
public class ZoneFaceTopoSelector implements ISelector {

	private static Logger logger = Logger.getLogger(ZoneFaceTopoSelector.class);

	private Connection conn;

	public ZoneFaceTopoSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		ZoneFaceTopo zoneFaceTopo = new ZoneFaceTopo();

		String sql = "SELECT * FROM zone_face_topo  WHERE a.row_id=hextoraw(:1)  and a.u_record !=2 ";

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

				zoneFaceTopo.setFacePid(resultSet.getInt("face_pid"));

				zoneFaceTopo.setLinkPid(resultSet.getInt("link_pid"));

				zoneFaceTopo.setRowId(resultSet.getString("row_id"));

				zoneFaceTopo.setSeqNum(resultSet.getInt("seq_num"));
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

		return zoneFaceTopo;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		StringBuilder sb = new StringBuilder(
				"SELECT * FROM ad_face_topo  WHERE a.face_pid=:1  and  a.u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		List<IRow> list = new ArrayList<IRow>();

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				ZoneFaceTopo zoneFaceTopo = new ZoneFaceTopo();

				zoneFaceTopo.setFacePid(resultSet.getInt("face_pid"));

				zoneFaceTopo.setLinkPid(resultSet.getInt("link_pid"));

				zoneFaceTopo.setRowId(resultSet.getString("row_id"));

				zoneFaceTopo.setSeqNum(resultSet.getInt("seq_num"));

				list.add(zoneFaceTopo);
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

		return list;
	}
	/**
	 * 
	 * 加载Zone_link和topo的信息
	 * @param linkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<ZoneFaceTopo> loadByLinkPid(Integer linkPid, boolean isLock)
			throws Exception {

		List<ZoneFaceTopo> zoneFaceTopos= new ArrayList<ZoneFaceTopo>();
		String sql = "SELECT a.* FROM zone_face_topo a WHERE a.link_pid =:1 and  a.u_record !=2 ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				ZoneFaceTopo zoneFaceTopo = new ZoneFaceTopo();
				zoneFaceTopo.setFacePid(resultSet.getInt("face_pid"));

				zoneFaceTopo.setLinkPid(resultSet.getInt("link_pid"));

				zoneFaceTopo.setRowId(resultSet.getString("row_id"));

				zoneFaceTopo.setSeqNum(resultSet.getInt("seq_num"));
				zoneFaceTopos.add(zoneFaceTopo);
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

		return zoneFaceTopos;
	}

}
