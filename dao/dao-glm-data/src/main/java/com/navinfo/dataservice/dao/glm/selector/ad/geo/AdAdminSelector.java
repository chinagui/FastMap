package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminName;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.DAOException;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AdAdminSelector extends AbstractSelector {

	private Connection conn;

	public AdAdminSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(AdAdmin.class);
	}

	public AdAdmin loadByAdminId(int adadminId, boolean isLock) throws DAOException {
		AdAdmin adAdmin = new AdAdmin();
		String sql = "select * from " + adAdmin.tableName() + " where admin_id =:1 and  u_record !=2";
        if (isLock) {
            sql += " for update nowait";
        }
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, adadminId);
			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				ReflectionAttrUtils.executeResultSet(adAdmin, resultSet);
			} else {
				throw new DAOException("数据不存在");
			}
		} catch (Exception e) {
			throw new DAOException(e.getMessage());
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return adAdmin;
	}

	public AdAdmin loadAdAdminRefAdminType(int adminId, int adminType, boolean isLock) throws DAOException{
	    AdAdmin adAdmin = null;
        String sql = "SELECT * FROM AD_ADMIN WHERE ADMIN_ID = :1 AND ADMIN_TYPE < :2 AND U_RECORD <> 2";
        if (isLock) {
            sql += "FOR UPDATE NOWAIT";
        }
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, adminId);
            pstmt.setInt(2, adminType);
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                adAdmin = new AdAdmin();
                ReflectionAttrUtils.executeResultSet(adAdmin, resultSet);
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage());
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
        return adAdmin;
    }

	/**
	 * 根据引导LinkPid查询行政区划代表点
	 * 
	 * @param id
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<AdAdmin> loadRowsByLinkId(int id, boolean isLock) throws DAOException {
		List<AdAdmin> adAdminList = new ArrayList<AdAdmin>();
		String sql = "SELECT * FROM ad_admin WHERE link_pid = :1 and u_record!=2";
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
				ReflectionAttrUtils.executeResultSet(adAdmin, resultSet);
				this.setChildData(adAdmin, isLock);
				adAdminList.add(adAdmin);
			}
		} catch (Exception e) {
			throw new DAOException(e.getMessage());
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return adAdminList;
	}

	/**
	 * 根据行政区划代表点引导Link pid查询代表点对象
	 * 
	 * @param linkPids
	 * @param isLock
	 * @return 代表点对象集合
	 * @throws Exception
	 */
	public List<AdAdmin> loadRowsByLinkPids(List<Integer> linkPids, boolean isLock) throws DAOException {
		List<AdAdmin> adAdminList = new ArrayList<>();
		if (linkPids.size() == 0) {
			return adAdminList;
		}
		// 去重操作
		HashSet<Integer> linkPidsSet = new HashSet<>(linkPids);
		//StringBuffer s = new StringBuffer("");
		//for (Integer pid : linkPidsSet) {
		//	s.append(pid + ",");
		//}
		//s.deleteCharAt(s.lastIndexOf(","));

		String sql = "SELECT * FROM ad_admin WHERE link_pid in (" + StringUtils.join(linkPidsSet, ",") + ") and u_record!=2";
		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				AdAdmin adAdmin = new AdAdmin();
				ReflectionAttrUtils.executeResultSet(adAdmin, resultSet);
				this.setChildData(adAdmin, isLock);
				adAdminList.add(adAdmin);
			}
		} catch (Exception e) {
			throw new DAOException(e.getMessage());
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return adAdminList;
	}

	private void setChildData(AdAdmin adAdmin, boolean isLock) throws Exception {
		// ad_admin_name
		List<IRow> adAdminNameList = new AdAdminNameSelector(conn).loadRowsByParentId(adAdmin.getPid(), isLock);

		for (IRow row : adAdminNameList) {
			row.setMesh(adAdmin.mesh());
		}
		adAdmin.setNames(adAdminNameList);

		for (IRow row : adAdminNameList) {
			AdAdminName obj = (AdAdminName) row;
			adAdmin.adAdminNameMap.put(obj.rowId(), obj);
		}
	}

}
