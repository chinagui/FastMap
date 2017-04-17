package com.navinfo.dataservice.dao.glm.selector.lc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcLinkKind;
import com.navinfo.dataservice.dao.glm.model.lc.LcLinkMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * @author zhangyt
 * @Title: LcLinkSelector.java
 * @Description: TODO
 * @date: 2016年7月27日 下午1:50:25
 * @version: v1.0
 */
public class LcLinkSelector extends AbstractSelector {

    private Connection conn;

    public LcLinkSelector(Connection conn)  {
        super(LcLink.class, conn);
        this.conn = conn;
    }

    public List<LcLink> loadByNodePid(int nodePid, boolean isLock) throws Exception {
        List<LcLink> links = new ArrayList<LcLink>();
        StringBuilder sb = new StringBuilder(
                "select * from lc_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");
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
                LcLink lcLink = new LcLink();
                ReflectionAttrUtils.executeResultSet(lcLink, resultSet);
//                for (IRow row : forms) {
//                    row.setMesh(lcLink.mesh());
//                }
                List<IRow> meshes = new LcLinkMeshSelector(conn).loadRowsByParentId(lcLink.getPid(), isLock);
                lcLink.setMeshes(meshes);
                for (IRow row : lcLink.getMeshes()) {
                    LcLinkMesh mesh = (LcLinkMesh) row;
                    lcLink.lcLinkMeshMap.put(mesh.rowId(), mesh);
                }
                links.add(lcLink);
                List<IRow> kinds = new LcLinkKindSelector(conn).loadRowsByParentId(lcLink.getPid(), isLock);
                lcLink.setKinds(kinds);
                for (IRow row : lcLink.getKinds()) {
                    LcLinkKind kind = (LcLinkKind) row;
                    lcLink.lcLinkKindMap.put(kind.rowId(), kind);
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

   	public List<LcLink> loadTrackLinkNoDirect(int linkPid, int nodePidDir,
   			boolean isLock) throws Exception {
   		List<LcLink> list = new ArrayList<LcLink>();
   		StringBuilder sb = new StringBuilder();
   		sb.append(" select rl.* from lc_link rl  where (rl.s_node_pid = :1 or rl.e_node_pid = :2) and rl.link_pid <> :3 and rl.u_record !=2 ");
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
   				LcLink lcLink = new LcLink();
   				ReflectionAttrUtils.executeResultSet(lcLink, resultSet);
   				list.add(lcLink);

   			}
   			return list;
   		} catch (Exception e) {

   			throw e;

   		} finally {
   			DBUtils.closeResultSet(resultSet);
   			DBUtils.closeStatement(pstmt);
   		}
   	}
}
