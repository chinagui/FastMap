package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.hgwg.RdHgwgLimitSelector;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RdHgwgLimitSearch implements ISearch {

    private Connection conn;

    public RdHgwgLimitSearch(Connection conn) {
        this.conn = conn;
    }

    @Override
    public IObj searchDataByPid(int pid) throws Exception {
        RdHgwgLimitSelector selector = new RdHgwgLimitSelector(conn);
        return (IObj) selector.loadById(pid, false);
    }
    
    @Override
	public IObj searchDataByPids(List<Integer> pidList) throws Exception {
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
        String sql = "select a.pid, a.geometry point_geom from rd_hgwg_limit a where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') = 'TRUE' and a.u_record != 2";
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
                snapshot.setT(47);
                snapshot.setI(resultSet.getString("pid"));
                STRUCT struct = (STRUCT) resultSet.getObject("point_geom");
                JSONObject geojson = Geojson.spatial2Geojson(struct);
                Geojson.point2Pixel(geojson, z, px, py);
                snapshot.setG(geojson.getJSONArray("coordinates"));
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

}
