package com.navinfo.dataservice.dao.glm.selector.ad.zone;

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
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminName;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;

import oracle.sql.STRUCT;

public class AdAdminSelector implements ISelector{
	
	private static Logger logger = Logger.getLogger(AdFaceSelector.class);

	private Connection conn;

	public AdAdminSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		AdAdmin adAdmin = new AdAdmin();

		String sql = "select * from " + adAdmin.tableName() + " where region_id =:1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				adAdmin.setPid(resultSet.getInt("region_id"));
				
				adAdmin.setRegionId(resultSet.getInt("region_id"));
				
				adAdmin.setAdminId(resultSet.getInt("admin_id"));
				
				adAdmin.setExtendId(resultSet.getInt("extend_id"));
				
				adAdmin.setExtendId(resultSet.getInt("admin_type"));
				
				adAdmin.setCapital(resultSet.getInt("capital"));
				
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				adAdmin.setGeometry(GeoTranslator.struct2Jts(struct, 100000, 0));
				
				adAdmin.setPopulation(resultSet.getString("population"));
				
				adAdmin.setLinkPid(resultSet.getInt("link_pid"));
				
				adAdmin.setNameGroupid(resultSet.getInt("name_groupid"));
				
				adAdmin.setSide(resultSet.getInt("side"));
				
				adAdmin.setMeshId(resultSet.getInt("MESH_ID"));
				
				adAdmin.setEditFlag(resultSet.getInt("edit_flag"));
				
				adAdmin.setRowId(resultSet.getString("row_id"));
				
				// ad_admin_name
				List<IRow> adAdminNameList = new AdAdminNameSelector(conn).loadRowsByParentId(adAdmin.getRegionId(), isLock);

				for (IRow row : adAdminNameList) {
					row.setMesh(adAdmin.mesh());
				}

				adAdmin.setNames(adAdminNameList);

				for (IRow row : adAdminNameList) {
					AdAdminName obj = (AdAdminName) row;

					adAdmin.adAdminNameMap.put(obj.rowId(), obj);
				}

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

		return adAdmin;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}
	
	public AdAdmin loadByAdminId(int adadminId,boolean isLock)throws Exception 
	{
		AdAdmin adAdmin = new AdAdmin();

		String sql = "select * from " + adAdmin.tableName() + " where admin_id =:1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, adadminId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				adAdmin.setPid(resultSet.getInt("region_id"));
				
				adAdmin.setAdminId(resultSet.getInt("admin_id"));
				
				adAdmin.setRowId(resultSet.getString("row_id"));

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

		return adAdmin;
	}
	
	public List<AdAdmin> loadRowsByLinkId(int id, boolean isLock) throws Exception {
		
		List<AdAdmin> adAdminList = new ArrayList<AdAdmin>();

		String sql = "SELECT * FROM ad_admin WHERE link_pid = :1";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				AdAdmin adAdmin = new AdAdmin();
				
				adAdmin.setPid(resultSet.getInt("region_id"));
				
				adAdmin.setRegionId(resultSet.getInt("region_id"));
				
				adAdmin.setLinkPid(resultSet.getInt("link_pid"));
				
				adAdmin.setNameGroupid(resultSet.getInt("name_groupid"));
				
				adAdmin.setSide(resultSet.getInt("side"));
				
				adAdmin.setMeshId(resultSet.getInt("MESH_ID"));
				
				adAdmin.setEditFlag(resultSet.getInt("edit_flag"));
				
				adAdmin.setRowId(resultSet.getString("row_id"));
				
				// ad_admin_name
				List<IRow> adAdminNameList = new AdAdminNameSelector(conn).loadRowsByParentId(adAdmin.getRegionId(), isLock);

				for (IRow row : adAdminNameList) {
					row.setMesh(adAdmin.mesh());
				}

				adAdmin.setNames(adAdminNameList);

				for (IRow row : adAdminNameList) {
					AdAdminName obj = (AdAdminName) row;

					adAdmin.adAdminNameMap.put(obj.rowId(), obj);
				}
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
		return adAdminList;
	}
	
}
