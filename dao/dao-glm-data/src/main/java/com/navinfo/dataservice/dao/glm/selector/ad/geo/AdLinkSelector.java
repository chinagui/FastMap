package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

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
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);

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
        	DbUtils.closeQuietly(resultSet);
        	DbUtils.closeQuietly(pstmt);
        }

        return links;

    }
    /***
   	 * 加载联通link不考虑方向
   	 * 
   	 * @param linkPid
   	 * @param nodePidDir
   	 * @param isLock
   	 * @return
   	 * @throws Exception
   	 */

   	public List<AdLink> loadTrackLinkNoDirect(int linkPid, int nodePidDir,
   			boolean isLock) throws Exception {
   		List<AdLink> list = new ArrayList<AdLink>();
   		StringBuilder sb = new StringBuilder();
   		sb.append(" select rl.* from ad_link rl  where (rl.s_node_pid = :1 or rl.e_node_pid = :2) and rl.link_pid <> :3 and rl.u_record !=2 ");
   		if (isLock) {
   			sb.append(" for update nowait");
   		}

   		PreparedStatement pstmt = null;

   		ResultSet resultSet = null;

   		try {
   			pstmt = conn.prepareStatement(sb.toString());

   			pstmt.setInt(1, nodePidDir);
   			pstmt.setInt(2, nodePidDir);
   			pstmt.setInt(3, linkPid);

   			resultSet = pstmt.executeQuery();

   			while (resultSet.next()) {
   				AdLink rdLink = new AdLink();
   				ReflectionAttrUtils.executeResultSet(rdLink, resultSet);
   				list.add(rdLink);

   			}
   			return list;
   		} catch (Exception e) {

   			throw e;

   		} finally {
   			DbUtils.closeQuietly(resultSet);
   			DbUtils.closeQuietly(pstmt);
   		}
   	}
}
