package com.navinfo.dataservice.dao.glm.selector.ad.zone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminGroup;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminPart;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminTree;

public class AdAdminTreeSelector implements ISelector {

	private Connection conn;

	public AdAdminTreeSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}

	public IRow loadRowsByProjectId(int projectId, boolean isLock) throws Exception {
		AdAdminTree result = new AdAdminTree();

		AdAdminSelector adAdminSelector = new AdAdminSelector(conn);

		// 添加中国大陆为top层级
		AdAdmin topAdmin = adAdminSelector.loadByAdminId(214, isLock);

		int topRegionId = topAdmin.getPid();

		result = getAdAdminTreeById(topRegionId, isLock);

		// 项目库ID+0000 对应的行政区划代表点AdAdminId

		int cityAdadminId = Integer.parseInt(projectId + "0000");

		AdAdmin adAdmin = adAdminSelector.loadByAdminId(cityAdadminId, isLock);

		int regionId = adAdmin.getPid();

		AdAdminTree beiJinTree = getAdAdminTreeById(regionId, isLock);

		result.getChildren().add(beiJinTree);

		List<Integer> regionIdList = getChildRegion(beiJinTree.getGroup().getPid(), isLock);

		for (Integer childRegionId : regionIdList) {
			AdAdminTree childTree = loadRowsByRegionId(childRegionId, isLock);

			result.getChildren().get(0).getChildren().add(childTree);

		}

		return result;
	}
	
	/**
	 * 递归调用生成树的节点
	 * @param regionId
	 * @param isLock
	 * @return
	 */
	private AdAdminTree loadRowsByRegionId(int regionId, boolean isLock) {
		AdAdminTree tree = getAdAdminTreeById(regionId, isLock);

		if (tree == null) {
			return null;
		}

		List<Integer> regionIdList = getChildRegion(tree.getGroup().getPid(), isLock);

		for (Integer childRegionId : regionIdList) {
			AdAdminTree childTree = loadRowsByRegionId(childRegionId, isLock);
			if (childTree != null) {
				tree.getChildren().add(childTree);
			}
		}

		return tree;
	}
	
	/**
	 * 根据regionId生成树
	 * @param regionId
	 * @param isLock
	 * @return
	 */
	private AdAdminTree getAdAdminTreeById(int regionId, boolean isLock) {

		AdAdminTree result = null;

		String sql = "SELECT B.NAME, C.GROUP_ID " + " FROM AD_ADMIN A, AD_ADMIN_GROUP C, AD_ADMIN_NAME B "
				+ " WHERE A.REGION_ID = B.REGION_ID " + " AND A.REGION_ID = :1 " + " AND A.REGION_ID = C.REGION_ID_UP "
				+ " AND B.LANG_CODE = 'CHI' " + " AND B.NAME_CLASS = 1 AND A.u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, regionId);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				int groupId = resultSet.getInt("group_id");

				AdAdminGroupSelector groupSelector = new AdAdminGroupSelector(conn);

				AdAdminGroup group = (AdAdminGroup) groupSelector.loadById(groupId, isLock);

				if (group == null) {
					return result;
				} else {
					result = new AdAdminTree();
				}

				result.setGroup(group);

				result.setName(resultSet.getString("name"));

				result.setRegionId(regionId);

				AdAdminPartSelector partSelector = new AdAdminPartSelector(conn);

				AdAdminPart part = partSelector.loadByRegionId(regionId, isLock);

				if (part != null) {
					result.setPart(part);
				}
			}
		} catch (Exception e) {
			result.setPart(null);

			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * 根据groupId获取part中的regionId
	 * @param groupId
	 * @param isLock
	 * @return
	 */
	private List<Integer> getChildRegion(int groupId, boolean isLock) {
		List<Integer> result = new ArrayList<>();

		String sql = "select region_id_down from ad_admin_part where group_id=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, groupId);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				result.add(resultSet.getInt("region_id_down"));
			}
		} catch (Exception e) {
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

		return result;
	}
}
