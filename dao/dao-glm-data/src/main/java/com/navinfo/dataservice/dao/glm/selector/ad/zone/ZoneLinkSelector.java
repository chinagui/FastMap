package com.navinfo.dataservice.dao.glm.selector.ad.zone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;


/**
 * ZONE:Link  查询接口
 *
 * @author zhaokk
 */
public class ZoneLinkSelector extends AbstractSelector {

    private Connection conn;

    public ZoneLinkSelector(Connection conn) {
        super(conn);
        this.conn = conn;
        this.setCls(ZoneLink.class);
    }

    public List<ZoneLink> loadByNodePid(int nodePid, boolean isLock)
            throws Exception {

        List<ZoneLink> links = new ArrayList<ZoneLink>();

        StringBuilder sb = new StringBuilder(
                "select * from zone_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

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
                ZoneLink zoneLink = new ZoneLink();

                ReflectionAttrUtils.executeResultSet(zoneLink, resultSet);
                List<IRow> meshes = new AbstractSelector(ZoneLinkMesh.class, conn).loadRowsByParentId(zoneLink.getPid(), isLock);
                List<IRow> kinds = new AbstractSelector(ZoneLinkKind.class, conn).loadRowsByParentId(zoneLink.getPid(), isLock);

                for (IRow row : meshes) {
                    ZoneLinkMesh mesh = (ZoneLinkMesh) row;
                    zoneLink.meshMap.put(mesh.rowId(), mesh);
                }
                zoneLink.setMeshes(meshes);
                for (IRow row : kinds) {
                    ZoneLinkKind kind = (ZoneLinkKind) row;
                    zoneLink.kindMap.put(kind.rowId(), kind);
                }
                links.add(zoneLink);
                zoneLink.setKinds(kinds);
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

        StringBuilder sb = new StringBuilder("select link_pid from ZONE_LINK where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

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
