package com.navinfo.dataservice.engine.limit.search.meta;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.engine.limit.glm.iface.IRenderParam;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.ISearch;
import com.navinfo.dataservice.engine.limit.glm.model.ReflectionAttrUtils;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGeometry;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class ScPlateresGeometrySearch implements ISearch {

    private Connection conn;

    public ScPlateresGeometrySearch(Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
        return null;
    }

    public int searchDataByCondition(JSONObject condition, List<IRow> rows) throws Exception {
        StringBuilder sql = new StringBuilder();

        if (!condition.containsKey("groupId") || !condition.containsKey("pageSize") || !condition.containsKey("pageNum")) {
            throw new Exception("参数不足，无法查询SC_PLATERES_GEOMETRY信息");
        }

        String groupId = condition.getString("groupId");
        int pageSize = condition.getInt("pageSize");
        int pageNum = condition.getInt("pageNum");

        sql.append("WITH query AS (");
        sql.append(" SELECT * FROM SC_PLATERES_GEOMETRY WHERE GROUP_ID = '" + groupId + "')");
        sql.append(" SELECT query.*,(SELECT COUNT(1) FROM query) AS TOTAL_ROW_NUM FROM query WHERE");
        sql.append(" rownum BETWEEN " + ((pageNum - 1) * pageSize + 1) + " AND " + (pageNum * pageSize) + " FOR UPDATE NOWAIT");

        PreparedStatement pstmt = null;
        int total = 0;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresGeometry geometry = new ScPlateresGeometry();

                ReflectionAttrUtils.executeResultSet(geometry, resultSet);
                
                total = resultSet.getInt("TOTAL_ROW_NUM");

                rows.add(geometry);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return total;
    }

    public String loadMaxKeyId(String groupId) throws Exception {
        StringBuilder sql = new StringBuilder();

        sql.append(" SELECT MAX(GEOMETRY_ID) FROM SC_PLATERES_GEOMETRY WHERE GROUP_ID = ? ");

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

    public ScPlateresGeometry loadById(String id) throws Exception {

        ScPlateresGeometry geometry = new ScPlateresGeometry();

        String sqlstr = "SELECT * FROM SC_PLATERES_GEOMETRY WHERE GEOMETRY_ID=? ";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sqlstr);

            pstmt.setString(1, id);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {

                ReflectionAttrUtils.executeResultSet(geometry, resultSet);
            }
        } catch (Exception e) {

            throw new Exception("查询的ID为：" + id + "的" + geometry.tableName().toUpperCase() + "不存在");

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return geometry;
    }

    @Override
    public List<SearchSnapshot> searchDataByTileWithGap(IRenderParam param) throws Exception {
        List<SearchSnapshot> list = new ArrayList<>();

        String sql = "SELECT GEOMETRY_ID, GROUP_ID, GEOMETRY, BOUNDARY_LINK FROM SC_PLATERES_GEOMETRY WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE'";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, param.getWkt());

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                SearchSnapshot snapshot = new SearchSnapshot();

                STRUCT struct = (STRUCT) resultSet.getObject("geometry");

                Geometry geom = GeoTranslator.struct2Jts(struct);

                JSONObject m = new JSONObject();

                m.put("a", resultSet.getString("GEOMETRY_ID"));

                m.put("b", resultSet.getString("GROUP_ID"));

                m.put("c", resultSet.getString("BOUNDARY_LINK"));

                String geometryType = geom.getGeometryType();

                m.put("e", geometryType);

                m.put("g", GeoTranslator.jts2Geojson(geom).getJSONArray("coordinates"));

                snapshot.setM(m);

                JSONObject geojson = GeoTranslator.jts2Geojson(geom);

                if (geometryType.equals("LineString")) {

                    JSONObject jo = Geojson.link2Pixel(geojson, param.getMPX(), param.getMPY(), param.getZ());

                    snapshot.setG(jo.getJSONArray("coordinates"));

                    snapshot.setT(1004);

                } else if (geometryType.equals("Polygon")) {

                    JSONObject jo = Geojson.face2Pixel(geojson, param.getMPX(), param.getMPY(), param.getZ());

                    snapshot.setG(jo.getJSONArray("coordinates"));

                    snapshot.setT(1005);
                }

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


    public List<ScPlateresGeometry> loadByGroupId(String id) throws Exception {

        List<ScPlateresGeometry> geometrys = new ArrayList<>();

        String sqlstr = "SELECT * FROM SC_PLATERES_GEOMETRY WHERE GROUP_ID=? ";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sqlstr);

            pstmt.setString(1, id);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresGeometry geometry = new ScPlateresGeometry();

                ReflectionAttrUtils.executeResultSet(geometry, resultSet);
                geometrys.add(geometry);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return geometrys;
    }


}