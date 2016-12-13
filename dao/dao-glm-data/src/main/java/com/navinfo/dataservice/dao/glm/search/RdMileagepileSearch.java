package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.util.DisplayUtils;
import oracle.spatial.geometry.JGeometry;
import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.mileagepile.RdMileagepileSelector;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RdMileagepileSearch implements ISearch {

    private Connection conn;

    public RdMileagepileSearch(Connection conn) {
        this.conn = conn;
    }

    @Override
    public IObj searchDataByPid(int pid) throws Exception {
        RdMileagepileSelector selector = new RdMileagepileSelector(conn);
        return (IObj) selector.loadById(pid, false);
    }

    @Override
    public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
        return null;
    }

    @Override
    public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
        return null;
    }

    @Override
    public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
        return null;
    }

    @Override
    public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {
        List<SearchSnapshot> list = new ArrayList<>();
        String sql = "select a.pid, a.geometry point_geom, b.geometry link_geom, a.direct, a.mileage_num num from rd_mileagepile a, rd_link b where sdo_relate(a.geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') = 'TRUE' and a.u_record != 2 and a.link_pid = b.link_pid";
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
                snapshot.setT(50);
                snapshot.setI(resultSet.getInt("pid"));
                STRUCT struct = (STRUCT) resultSet.getObject("point_geom");
                JSONObject geojson = Geojson.spatial2Geojson(struct);
                Geojson.point2Pixel(geojson, z, px, py);
                snapshot.setG(geojson.getJSONArray("coordinates"));
                JSONObject jsonM = new JSONObject();
                jsonM.put("a", resultSet.getDouble("num"));
                double angle = calAngle(resultSet);
                jsonM.put("c", String.valueOf((int) angle));
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
        STRUCT struct2 = (STRUCT) resultSet.getObject("link_geom");
        if (struct2 == null) {
            return angle;
        }
        STRUCT struct1 = (STRUCT) resultSet.getObject("point_geom");
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
        StringBuilder sb = new StringBuilder("LINESTRING (");
        sb.append(geom2.getOrdinatesArray()[startIndex * 2]);
        sb.append(" ");
        sb.append(geom2.getOrdinatesArray()[startIndex * 2 + 1]);
        sb.append(", ");
        sb.append(geom2.getOrdinatesArray()[(startIndex + 1) * 2]);
        sb.append(" ");
        sb.append(geom2.getOrdinatesArray()[(startIndex + 1) * 2 + 1]);
        sb.append(")");
        angle = DisplayUtils.calIncloudedAngle(sb.toString(), resultSet.getInt("direct"));
        return angle;
    }

    private static boolean isBetween(double a, double b, double c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
