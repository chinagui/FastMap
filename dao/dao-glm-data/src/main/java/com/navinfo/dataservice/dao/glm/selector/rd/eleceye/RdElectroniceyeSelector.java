package com.navinfo.dataservice.dao.glm.selector.rd.eleceye;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePair;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePart;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * @Title: RdElectroniceyeSelector.java
 * @Prject: dao-glm-data
 * @Package: com.navinfo.dataservice.dao.glm.selector.rd.eleceye
 * @Description: 查询电子眼
 * @author zhangyt
 * @date: 2016年7月20日 下午5:46:19
 * @version: v1.0
 *
 */
public class RdElectroniceyeSelector implements ISelector {

	private Connection conn;

	public RdElectroniceyeSelector(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 根据RdElectroniceye的Pid查询
	 */
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		RdElectroniceye eleceye = new RdElectroniceye();

		String sql = "select a.*,b.mesh_id from " + eleceye.tableName()
				+ " a, rd_link b where a.pid = :1 and a.u_record != 2 and b.link_pid = b.link_pid";

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

//				setAttr(eleceye, resultSet);
				ReflectionAttrUtils.executeResultSet(eleceye, resultSet);

			} else {

				throw new DataNotFoundException("数据不存在");
			}

			List<IRow> parts = new RdEleceyePartSelector(conn).loadRowsByEleceyePid(eleceye.pid(), isLock);
			for (IRow row : parts) {
				RdEleceyePart part = (RdEleceyePart) row;
				part.setMesh(resultSet.getInt("mesh_id"));
				eleceye.partMap.put(part.rowId(), part);

				List<IRow> pairs = new ArrayList<IRow>();
				RdEleceyePair pair = (RdEleceyePair) new RdEleceyePairSelector(conn).loadById(part.getGroupId(), false);
				pair.setMesh(resultSet.getInt("mesh_id"));
				pairs.add(pair);
				eleceye.setPairs(pairs);
				eleceye.pairMap.put(pair.pid(), pair);
			}
			eleceye.setParts(parts);
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

		return eleceye;
	}

	/**
	 * 根据RdElectroniceye的RowId查询
	 */
	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		RdElectroniceye eleceye = new RdElectroniceye();

		String sql = "select a.*,c.mesh_id from " + eleceye.tableName()
				+ " a, rd_link b where a.row_id = :1 and a.u_record != 2 and a.eleceye_pid = b.eleceye_pid and b.link_pid = c.link_pid";

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

//				setAttr(eleceye, resultSet);
				ReflectionAttrUtils.executeResultSet(eleceye, resultSet);

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

		return eleceye;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}

	public List<RdElectroniceye> loadListByRdLinkId(int rdLinkPid, boolean isLock) throws Exception {
		List<RdElectroniceye> eleceyes = new ArrayList<RdElectroniceye>();
		RdElectroniceye eleceye = new RdElectroniceye();

		String sql = "select a.*,b.mesh_id from " + eleceye.tableName()
				+ " a, rd_link b where a.link_pid = :1 and a.u_record != 2 and a.link_pid = b.link_pid";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, rdLinkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				eleceye = new RdElectroniceye();
//				this.setAttr(eleceye, resultSet);
				ReflectionAttrUtils.executeResultSet(eleceye, resultSet);
				
				List<IRow> parts = new RdEleceyePartSelector(conn).loadRowsByEleceyePid(eleceye.pid(), isLock);
				for (IRow row : parts) {
					RdEleceyePart part = (RdEleceyePart) row;
					part.setMesh(resultSet.getInt("mesh_id"));
					eleceye.partMap.put(part.rowId(), part);

					List<IRow> pairs = new ArrayList<IRow>();
					RdEleceyePair pair = (RdEleceyePair) new RdEleceyePairSelector(conn).loadById(part.getGroupId(), false);
					pair.setMesh(resultSet.getInt("mesh_id"));
					pairs.add(pair);
					eleceye.setPairs(pairs);
					eleceye.pairMap.put(pair.pid(), pair);
				}
				eleceye.setParts(parts);
				
				eleceyes.add(eleceye);
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

		return eleceyes;
	}

	private void setAttr(RdElectroniceye eleceye, ResultSet resultSet) throws Exception {
		eleceye.setPid(resultSet.getInt("pid"));
		eleceye.setLinkPid(resultSet.getInt("link_pid"));
		eleceye.setDirect(resultSet.getInt("direct"));
		eleceye.setKind(resultSet.getInt("kind"));
		eleceye.setLocation(resultSet.getInt("location"));
		eleceye.setAngle(resultSet.getDouble("angle"));
		eleceye.setSpeedLimit(resultSet.getInt("speed_limit"));
		eleceye.setVerifiedFlag(resultSet.getInt("verified_flag"));
		eleceye.setMeshId(resultSet.getInt("mesh_id"));
		STRUCT struct = (STRUCT) resultSet.getObject("geometry");
		Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
		eleceye.setGeometry(geometry);
		eleceye.setSrcFlag(resultSet.getString("src_flag"));
		eleceye.setCreationDate(resultSet.getDate("creation_date"));
		eleceye.setHighViolation(resultSet.getInt("high_violation"));
		eleceye.setRowId(resultSet.getString("row_id"));

	}
}
