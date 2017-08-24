package com.navinfo.dataservice.dao.glm.search;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.rdlinkwarning.RdLinkWarningSelector;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class RdLinkWarningSearch implements ISearch {

    private Connection conn;

    public RdLinkWarningSearch(Connection conn) {
        this.conn = conn;
    }

    @Override
    public IObj searchDataByPid(int pid) throws Exception {
        RdLinkWarningSelector selector = new RdLinkWarningSelector(conn);

        IObj obj = (IObj) selector.loadById(pid, false);

        return obj;
    }

    @Override
    public List<IRow> searchDataByPids(List<Integer> pidList) throws Exception {

        RdLinkWarningSelector selector = new RdLinkWarningSelector(conn);

        List<IRow> rows = selector.loadByIds(pidList, false, true);

        return rows;
    }

    @Override
    public List<SearchSnapshot> searchDataBySpatial(String wkt)
            throws Exception {

        return null;
    }

    @Override
    public List<SearchSnapshot> searchDataByCondition(String condition)
            throws Exception {

        return null;
    }

    @Override
    public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
                                                        int gap) throws Exception {
        List<SearchSnapshot> list = new ArrayList<>();

        String sql = "SELECT A.PID, A.DIRECT, A.TYPE_CODE, A.LINK_PID, A.GEOMETRY, L.GEOMETRY AS LINK_GEOM FROM RD_LINK_WARNING A LEFT JOIN RD_LINK L ON A.LINK_PID = L.LINK_PID WHERE SDO_RELATE(A.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND A.U_RECORD != 2";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

            String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

            pstmt.setString(1, wkt);

            resultSet = pstmt.executeQuery();

            double px = MercatorProjection.tileXToPixelX(x);

            double py = MercatorProjection.tileYToPixelY(y);

            while (resultSet.next()) {

                SearchSnapshot snapshot = new SearchSnapshot();

                snapshot.setI(resultSet.getInt("PID"));

                snapshot.setT(25);

                int direct = resultSet.getInt("DIRECT");

                String typeCode = resultSet.getString("TYPE_CODE");

                int linkPid = resultSet.getInt("LINK_PID");

                STRUCT struct1 = (STRUCT) resultSet.getObject("GEOMETRY");

                JGeometry geom1= JGeometry.load(struct1);

                snapshot.setG(Geojson.lonlat2Pixel(geom1.getFirstPoint()[0],
                        geom1.getFirstPoint()[1], z, px, py));

                double angle = calAngle(resultSet);

                JSONObject jsonM = new JSONObject();

                jsonM.put("a", direct);

                jsonM.put("b", angle);

                jsonM.put("d", linkPid);

                jsonM.put("e", typeCode);

                snapshot.setM(jsonM);

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

    // 计算角度
    private double calAngle(ResultSet resultSet) throws Exception {

        double angle = 0;

        STRUCT struct2 = (STRUCT) resultSet.getObject("LINK_GEOM");

        if (struct2 == null) {
            return angle;
        }

        STRUCT struct1 = (STRUCT) resultSet.getObject("GEOMETRY");

        JGeometry geom1 = JGeometry.load(struct1);

        double[] point = geom1.getFirstPoint();

        JGeometry geom2 = JGeometry.load(struct2);

        int ps = geom2.getNumPoints();

        int startIndex = 0;

        for (int i = 0; i < ps - 1; i++) {
            double sx = geom2.getOrdinatesArray()[i * 2];

            double sy = geom2.getOrdinatesArray()[i * 2 + 1];

            double ex = geom2.getOrdinatesArray()[(i + 1) * 2];

            double ey = geom2.getOrdinatesArray()[(i + 1) * 2 + 1];

            if (isBetween(sx, ex, point[0]) && isBetween(sy, ey, point[1])) {
                startIndex = i;
                break;
            }
        }

        StringBuilder sbWkt = new StringBuilder("LINESTRING (");

        sbWkt.append(geom2.getOrdinatesArray()[startIndex * 2]);

        sbWkt.append(" ");

        sbWkt.append(geom2.getOrdinatesArray()[startIndex * 2 + 1]);

        sbWkt.append(", ");

        sbWkt.append(geom2.getOrdinatesArray()[(startIndex + 1) * 2]);

        sbWkt.append(" ");

        sbWkt.append(geom2.getOrdinatesArray()[(startIndex + 1) * 2 + 1]);

        sbWkt.append(")");

        angle = DisplayUtils.calIncloudedAngle(sbWkt.toString(),
                resultSet.getInt("direct"));

        return angle;

    }

    private static boolean isBetween(double a, double b, double c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }

}
