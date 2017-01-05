package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminGroup;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminPart;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminTree;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

public class AdAdminTreeSelector extends AbstractSelector {

	private Connection conn;

	public AdAdminTreeSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(AdAdminTree.class);
	}

	public IRow loadRowsBySubTaskId(int subtaskId, boolean isLock) throws Exception {
		AdAdminTree result = new AdAdminTree();

		AdAdminSelector adAdminSelector = new AdAdminSelector(conn);

		// 添加中国大陆为top层级
		AdAdmin topAdmin = adAdminSelector.loadByAdminId(214, isLock);

		int topRegionId = topAdmin.getPid();

		result = getAdAdminTreeById(topRegionId, isLock, 0);

		// 项目库ID+0000 对应的行政区划代表点AdAdminId
		ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		
		int  cityAdadminId = manApi.queryAdminIdBySubtask(subtaskId);
		
		if(cityAdadminId == 0)
		{
			throw new Exception("根据子任务Id"+subtaskId+"获取admin_id失败");
		}

		AdAdmin adAdmin = adAdminSelector.loadByAdminId(cityAdadminId, isLock);

		int regionId = adAdmin.getPid();

		AdAdminTree beiJinTree = getAdAdminTreeById(regionId, isLock, 0);

		result.getChildren().add(beiJinTree);

		List<Integer> regionIdList = getChildRegion(beiJinTree.getGroup().getPid(), isLock);

		for (Integer childRegionId : regionIdList) {
			AdAdminTree childTree = loadRowsByRegionId(childRegionId, isLock, beiJinTree.getGroup().getPid());
			if(childTree != null)
			{
				result.getChildren().get(0).getChildren().add(childTree);
			}
		}

		return result;
	}

	/**
	 * 递归调用生成树的节点
	 * 
	 * @param regionId
	 * @param isLock
	 * @return
	 */
	public AdAdminTree loadRowsByRegionId(int regionId, boolean isLock, int groupId) {
		AdAdminTree tree = getAdAdminTreeById(regionId, isLock, groupId);

		if (tree == null) {
			return null;
		}
		
		if(tree.getGroup() == null)
		{
			return tree;
		}
		int group_id = tree.getGroup().getPid();

		List<Integer> regionIdList = getChildRegion(group_id, isLock);

		for (Integer childRegionId : regionIdList) {
			AdAdminTree childTree = loadRowsByRegionId(childRegionId, isLock, group_id);
			if (childTree != null) {
				tree.getChildren().add(childTree);
			}
		}

		return tree;
	}

	/**
	 * 根据regionId生成树
	 * 
	 * @param regionId
	 * @param isLock
	 * @param groupId
	 * @return
	 */
	private AdAdminTree getAdAdminTreeById(int regionId, boolean isLock, int groupId) {

		AdAdminTree result = null;

		String sql = "SELECT tmp.group_id,CASE WHEN c.name IS NULL THEN '无' ELSE c.name END AS NAME FROM (SELECT b.GROUP_ID,a.REGION_ID "
				+ "FROM AD_ADMIN A, AD_ADMIN_GROUP b " + "WHERE " + "A.REGION_ID = :1 "
				+ "AND A.REGION_ID = b.REGION_ID_UP " + "AND b.U_RECORD != :2)tmp "
				+ "Left join AD_ADMIN_NAME c " + "on tmp.region_id = c.region_id "
				+ "AND c.LANG_CODE = 'CHI' " + "AND c.NAME_CLASS = 1";

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
				int group_id = resultSet.getInt("group_id");

				AdAdminGroup group = (AdAdminGroup) new AbstractSelector(AdAdminGroup.class,conn).loadById(group_id, isLock);

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
			} else {
				String sql2 = "SELECT tmp.group_id,CASE WHEN c.name IS NULL THEN '无' ELSE c.name END AS NAME FROM (SELECT b.GROUP_ID,a.REGION_ID "
						+ "FROM AD_ADMIN A, AD_ADMIN_PART b " + "WHERE " + "A.REGION_ID = :1 "
						+ "AND A.REGION_ID = b.REGION_ID_DOWN and b.group_id = :2 "
						+ "AND b.U_RECORD != :3)tmp " + "Left join AD_ADMIN_NAME c "
						+ "on tmp.region_id = c.region_id " + "AND c.LANG_CODE = 'CHI' "
						+ "AND c.NAME_CLASS = 1";
				PreparedStatement pstmt2 = null;

				ResultSet resultSet2 = null;
				
				pstmt2 = this.conn.prepareStatement(sql2);

				pstmt2.setInt(1, regionId);

				pstmt2.setInt(2, groupId);

				pstmt2.setInt(3, 2);

				resultSet2 = pstmt2.executeQuery();

				if (resultSet2.next()) {

					result = new AdAdminTree();

					result.setGroup(null);

					result.setName(resultSet2.getString("name"));

					result.setRegionId(regionId);

					AdAdminPartSelector partSelector = new AdAdminPartSelector(conn);

					AdAdminPart part = partSelector.loadByRegionId(regionId, isLock);

					if (part != null) {
						result.setPart(part);
					}
				}
			}
		} catch (Exception e) {
			if(result != null)
			{
				result.setPart(null);
			}

			e.printStackTrace();
		}
		finally{
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return result;
	}

	/**
	 * 根据groupId获取part中的regionId
	 * 
	 * @param groupId
	 * @param isLock
	 * @return
	 */
	private List<Integer> getChildRegion(int groupId, boolean isLock) {
		List<Integer> result = new ArrayList<>();

		String sql = "SELECT a.REGION_ID_DOWN FROM AD_ADMIN_PART a,Ad_admin b WHERE a.GROUP_ID = :1 AND a.region_id_down = b.region_id AND a.U_RECORD != :2 AND b.ADMIN_TYPE <= 4.0 ";

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
				Integer regionIdDown = resultSet.getInt("region_id_down");

				if (!result.contains(regionIdDown)) {
					result.add(resultSet.getInt("region_id_down"));
				}

			}
		} catch (Exception e) {
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);

		}

		return result;
	}
}
