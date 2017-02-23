package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;



public class AdLinkSelector extends AbstractSelector {

	private Connection conn;

	public AdLinkSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(AdLink.class);
	}

	public List<AdLink> loadByNodePid(int nodePid, boolean isLock)
			throws Exception {

		List<AdLink> links = new ArrayList<AdLink>();

		StringBuilder sb = new StringBuilder(
				"select * from ad_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				AdLink adLink = new AdLink();

				ReflectionAttrUtils.executeResultSet(adLink, resultSet);
				List<IRow> meshes = new AbstractSelector(AdLinkMesh.class, conn).loadRowsByParentId(adLink.getPid(), isLock);
				// loadRowsByParentId已经查询了mesh,是否可以不做设置
//				for (IRow row : meshes) {
//					row.setMesh(adLink.mesh());
//				}
				adLink.setMeshes(meshes);
				for (IRow row : adLink.getMeshes()) {
					AdLinkMesh mesh = (AdLinkMesh) row;
					adLink.meshMap.put(mesh.rowId(), mesh);
				}
				links.add(adLink);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);

		}

		return links;

	}

	
	/*
     * 仅加载LINK的pid
     */
    public List<Integer> loadLinkPidByNodePid(int nodePid, boolean isLock) throws Exception {

        List<Integer> links = new ArrayList<Integer>();

        StringBuilder sb = new StringBuilder("select link_pid from AD_LINK where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

        if (isLock) {
            sb.append(" for update nowait");
        }

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sb.toString());

            pstmt.setInt(1, nodePid);

            pstmt.setInt(2, nodePid);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                int value = resultSet.getInt("link_pid");

                links.add(value);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return links;

    }
}
