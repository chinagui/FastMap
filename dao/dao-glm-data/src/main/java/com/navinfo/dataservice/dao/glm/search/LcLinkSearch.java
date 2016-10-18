package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;

import com.navinfo.dataservice.datahub.api.Db;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;

public class LcLinkSearch implements ISearch {

    private Connection conn;

    public LcLinkSearch(Connection conn) {
        this.conn = conn;
    }

    @Override
    public IObj searchDataByPid(int pid) throws Exception {
        LcLinkSelector lcLinkSelector = new LcLinkSelector(conn);

        IObj lcLink = (IObj) lcLinkSelector.loadById(pid, false);

        return lcLink;
    }

    @Override
    public List<SearchSnapshot> searchDataBySpatial(String wkt)
            throws Exception {

        List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

        String sql = "select a.link_pid, a.geometry, a.s_node_pid, a.e_node_pid, b.kind from lc_link a, lc_link_kind b where a.u_record != 2 and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') = 'TRUE' and a.link_pid = b.link_pid";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, wkt);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                SearchSnapshot snapshot = new SearchSnapshot();

                JSONObject m = new JSONObject();

                m.put("a", resultSet.getString("s_nodePid"));

                m.put("b", resultSet.getString("e_nodePid"));

                m.put("c", resultSet.getInt("kind") <= 8 ? 21 : 22);

                snapshot.setM(m);

                snapshot.setT(31);

                snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

                STRUCT struct = (STRUCT) resultSet.getObject("geometry");

                JSONObject jo = Geojson.spatial2Geojson(struct);

                snapshot.setG(jo.getJSONArray("coordinates"));

                list.add(snapshot);
            }
        } catch (Exception e) {

            throw new Exception(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }

        return list;
    }

    @Override
    public List<SearchSnapshot> searchDataByCondition(String condition)
            throws Exception {

        return null;
    }

    @Override
    public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
                                                        int gap) throws Exception {

        List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

        String sql = "select a.link_pid, a.geometry, a.s_node_pid, a.e_node_pid, b.kind from lc_link a, lc_link_kind b where a.u_record != 2 and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') = 'TRUE' and a.link_pid = b.link_pid";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sql);

            String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

            pstmt.setString(1, wkt);

            resultSet = pstmt.executeQuery();

            double px = MercatorProjection.tileXToPixelX(x);

            double py = MercatorProjection.tileYToPixelY(y);

            while (resultSet.next()) {
                SearchSnapshot snapshot = new SearchSnapshot();

                JSONObject m = new JSONObject();

                m.put("a", resultSet.getString("s_node_pid"));

                m.put("b", resultSet.getString("e_node_pid"));

                m.put("c", resultSet.getInt("kind") <= 8 ? 21 : 22);

                snapshot.setM(m);

                snapshot.setT(31);

                snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

                STRUCT struct = (STRUCT) resultSet.getObject("geometry");

                JGeometry geo = JGeometry.load(struct);

                if (geo.getType() != 2) {
                    continue;
                }

                JSONObject geojson = Geojson.spatial2Geojson(struct);

                JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

                snapshot.setG(jo.getJSONArray("coordinates"));

                list.add(snapshot);
            }
        } catch (Exception e) {

            throw new Exception(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }

        return list;
    }

    public static void main(String[] args) throws Exception {
        Connection conn = DBConnector.getInstance().getConnectionById(11);

        LcLinkSearch search = new LcLinkSearch(conn);

        List<SearchSnapshot> res = search.searchDataByTileWithGap(215829, 99329, 18, 20);

        for (SearchSnapshot s : res) {
            System.out.println(s.Serialize(null));
        }
    }
}