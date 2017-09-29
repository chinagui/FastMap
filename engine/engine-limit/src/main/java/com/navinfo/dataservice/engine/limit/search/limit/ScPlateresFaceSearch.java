package com.navinfo.dataservice.engine.limit.search.limit;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.ISearch;
import com.navinfo.dataservice.engine.limit.glm.model.ReflectionAttrUtils;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresInfo;
import com.navinfo.navicommons.database.sql.DBUtils;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ScPlateresFaceSearch implements ISearch {

    private Connection conn;

    public ScPlateresFaceSearch(Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
        return null;
    }

    public int searchDataByCondition(JSONObject condition, List<IRow> objList) throws Exception {

        if (!condition.containsKey("groupId")) {
            throw new Exception("请输入groupId！");
        }

        String groupId = condition.getString("groupId");

        String sqlStr = "SELECT * FROM SC_PLATERES_FACE WHERE ADMIN_CODE = ? ";

        boolean Paging = (condition.containsKey("pageSize") && condition.containsKey("pageNum"));

        if (Paging) {

            int pageSize = condition.getInt("pageSize");
            int pageNum = condition.getInt("pageNum");

            StringBuilder sql = new StringBuilder(" WITH query AS ( ");
            sql.append(sqlStr);
            sql.append(") SELECT query.*,(SELECT count(1) FROM query) AS TOTAL_ROW_NUM FROM query ");

            sql.append(" WHERE rownum BETWEEN ");
            sql.append((pageNum - 1) * pageSize + 1);
            sql.append(" AND ");
            sql.append((pageNum * pageSize));
            sql.append(" for update nowait");

            sqlStr = sql.toString();
        }

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;
        int total = 0;

        try {
            pstmt = this.conn.prepareStatement(sqlStr);

            pstmt.setString(1, groupId);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresInfo info = new ScPlateresInfo();

                ReflectionAttrUtils.executeResultSet(info, resultSet);

                if (Paging && total == 0) {
                    total = resultSet.getInt("TOTAL_ROW_NUM");
                }

                objList.add(info);
            }
            if (!Paging) {
                total = objList.size();
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return total;
    }

    @Override
    public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {
        List<SearchSnapshot> list = new ArrayList<>();

        String sql = "SELECT GEOMETRY_ID, GROUP_ID, GEOMETRY, BOUNDARY_LINK FROM SC_PLATERES_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE'";

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

                m.put("a", resultSet.getInt("GEOMETRY_ID"));

                m.put("b", resultSet.getString("GROUP_ID"));

                m.put("c", resultSet.getString("BOUNDARY_LINK"));

                snapshot.setM(m);

                snapshot.setT(1001);

                STRUCT struct = (STRUCT) resultSet.getObject("geometry");

                JSONObject geojson = Geojson.spatial2Geojson(struct);

                JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

                snapshot.setG(jo.getJSONArray("coordinates"));

                list.add(snapshot);
            }
        } catch (Exception e) {

            throw new Exception(e);
        } finally {
            DBUtils.closeStatement(pstmt);
            DBUtils.closeResultSet(resultSet);
        }

        return list;
    }
    
    public String loadMaxKeyId(String groupId) throws Exception {
        StringBuilder sql = new StringBuilder();

        sql.append(" SELECT MAX(GEOMETRY_ID) FROM SC_PLATERES_FACE WHERE GROUP_ID = ? ");

        PreparedStatement pstmt = null;
 
        String geometryId = "";

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());
            
            pstmt.setString(1, groupId);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                geometryId = resultSet.getString(1);
                
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return geometryId;
    }
    

}
