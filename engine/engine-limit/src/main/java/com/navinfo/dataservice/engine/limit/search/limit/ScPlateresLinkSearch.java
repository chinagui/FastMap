package com.navinfo.dataservice.engine.limit.search.limit;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.engine.limit.glm.iface.IRenderParam;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.ISearch;
import com.navinfo.dataservice.engine.limit.glm.model.ReflectionAttrUtils;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresLink;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class ScPlateresLinkSearch implements ISearch {

    private Connection conn;

    public ScPlateresLinkSearch(Connection conn) {
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

        String sqlStr = "SELECT t.*, row_number() over(order by GEOMETRY_ID) as row_num FROM SC_PLATERES_LINK t WHERE t.GROUP_ID = ? ";

        boolean Paging = (condition.containsKey("pageSize") && condition.containsKey("pageNum"));

        if (Paging) {

            int pageSize = condition.getInt("pageSize");
            int pageNum = condition.getInt("pageNum");

            StringBuilder sql = new StringBuilder(" WITH query AS ( ");
            sql.append(sqlStr);
            sql.append(" ) SELECT query.*,(SELECT count(1) FROM query) AS TOTAL_ROW_NUM FROM query ");

            sql.append(" WHERE row_num BETWEEN ");
            sql.append((pageNum - 1) * pageSize + 1);
            sql.append(" AND ");
            sql.append((pageNum * pageSize));
            
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

                ScPlateresLink info = new ScPlateresLink();

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
    public List<SearchSnapshot> searchDataByTileWithGap(IRenderParam param) throws Exception {
        List<SearchSnapshot> list = new ArrayList<>();

        String sql = "SELECT GEOMETRY_ID, GROUP_ID, GEOMETRY, BOUNDARY_LINK FROM SC_PLATERES_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE'";

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

                JSONObject geojson = GeoTranslator.jts2Geojson(geom);


                JSONObject m = new JSONObject();

                m.put("a", resultSet.getString("GEOMETRY_ID"));

                m.put("b", resultSet.getString("GROUP_ID"));

                m.put("c", resultSet.getString("BOUNDARY_LINK"));

                m.put("e", geom.getGeometryType());

                m.put("g", GeoTranslator.jts2Geojson(geom).getJSONArray("coordinates"));

                snapshot.setM(m);

                snapshot.setT(1001);

                JSONObject jo = Geojson.link2Pixel(geojson, param.getMPX(), param.getMPY(), param.getZ());

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

        sql.append(" SELECT MAX(GEOMETRY_ID) FROM SC_PLATERES_LINK WHERE GROUP_ID = ? ");

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

    public ScPlateresLink loadById(String geomId) throws Exception {

        ScPlateresLink info = new ScPlateresLink();

        StringBuilder sql = new StringBuilder();

        sql.append(" SELECT * FROM SC_PLATERES_LINK WHERE GEOMETRY_ID = ? ");

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());

            pstmt.setString(1, geomId);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ReflectionAttrUtils.executeResultSet(info, resultSet);

            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return info;
    }

    public List<ScPlateresLink> loadByIds(JSONArray links) throws Exception {
        StringBuilder where = new StringBuilder();

        List<ScPlateresLink> objList = new ArrayList<>();

        for (int i = 0; i < links.size(); i++) {
            if (i > 0) {
                where.append(",");
            }
            where.append("'" + links.get(i) + "'");
        }

        String sql = "SELECT * FROM SC_PLATERES_LINK WHERE GEOMETRY_ID IN (" + where + ")";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresLink info = new ScPlateresLink();

                ReflectionAttrUtils.executeResultSet(info, resultSet);

                objList.add(info);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
        return objList;
    }

    public List<ScPlateresLink> loadByGroupId(String groupId) throws Exception {

        List<ScPlateresLink> objs = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        sql.append(" SELECT * FROM SC_PLATERES_LINK WHERE GROUP_ID = ? ");

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());

            pstmt.setString(1, groupId);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresLink obj = new ScPlateresLink();

                ReflectionAttrUtils.executeResultSet(obj, resultSet);

                objs.add(obj);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return objs;
    }

    public List<Integer> getLinkPidByGroupId(String groupId) throws Exception {

        List<Integer> objs = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        sql.append(" SELECT LINK_PID FROM SC_PLATERES_LINK WHERE GROUP_ID = ? ");

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());

            pstmt.setString(1, groupId);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                objs.add(resultSet.getInt("LINK_PID"));
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return objs;
    }
}

