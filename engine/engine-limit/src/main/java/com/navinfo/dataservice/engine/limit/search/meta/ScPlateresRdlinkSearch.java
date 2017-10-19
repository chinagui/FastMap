
package com.navinfo.dataservice.engine.limit.search.meta;

import com.alibaba.druid.util.StringUtils;
import com.ctc.wstx.util.StringUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.engine.limit.glm.iface.IRenderParam;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.ISearch;
import com.navinfo.dataservice.engine.limit.glm.model.ReflectionAttrUtils;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
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

/**
 * Created by ly on 2017/9/19.
 */
public class ScPlateresRdlinkSearch implements ISearch {

    private Connection conn;

    public ScPlateresRdlinkSearch(Connection conn) {
        this.conn = conn;
    }

    public ScPlateresRdLink loadById(int linkpid) throws Exception {

    	ScPlateresRdLink rdlink = new ScPlateresRdLink();

        String sqlstr = "SELECT * FROM SC_PLATERES_RDLINK WHERE LINK_PID=? ";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sqlstr);

            pstmt.setInt(1, linkpid);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {

                ReflectionAttrUtils.executeResultSet(rdlink, resultSet);
            }
        } catch (Exception e) {

            throw new Exception("查询的ID为：" + rdlink + "的" + rdlink.tableName().toUpperCase() + "不存在");

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return rdlink;
    }

    public List<ScPlateresRdLink> loadByGeometryId(String geometryId) throws Exception {

        List<ScPlateresRdLink> links = new ArrayList<>();

        String sqlstr = "SELECT * FROM SC_PLATERES_RDLINK WHERE GEOMETRY_ID=? ";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sqlstr);

            pstmt.setString(1, geometryId);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                ScPlateresRdLink link =new ScPlateresRdLink();

                ReflectionAttrUtils.executeResultSet(link, resultSet);

                links.add(link);
            }
        } catch (Exception e) {

            throw new Exception(e);
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return links;
    }

    public int searchDataByCondition(JSONObject condition, List<IRow> rows) throws Exception {
    	if(condition==null||condition.isNullObject()){
    		throw new Exception("输入信息为空，无法查询SC_PLATERES_RDLINK信息");
    	}
    	
        StringBuilder sqlstr = new StringBuilder();
        StringBuilder sql = new StringBuilder();
        
        int total = 0;
        
        sqlstr.append(" FROM SC_PLATERES_RDLINK t WHERE");
        componentSql(condition,sqlstr);
        
        sql.append("SELECT t.*, (SELECT COUNT(*) " + sqlstr + ") AS TOTAL_ROW_NUM");
        sql.append(sqlstr);

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresRdLink link = new ScPlateresRdLink();

                ReflectionAttrUtils.executeResultSet(link, resultSet);

                rows.add(link);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return total;
    }

    private void componentSql(JSONObject obj,StringBuilder sql){

        if (obj.containsKey("geoId")) {
            String geoId = obj.getString("geoId");

            if (geoId != null && !geoId.isEmpty()) {
                sql.append(" GEOMETRY_ID = ");
                sql.append("'" + geoId + "'");
            }
        }

        if (obj.containsKey("linkPid")) {
            int linkPid = obj.getInt("linkPid");
            
            if(!sql.toString().endsWith("WHERE")){
            	sql.append(" AND");
            }

            if (linkPid != 0) {
                sql.append(" LINK_PID = ");
                sql.append(linkPid);
            }
        }
    }

    @Override
    public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
        return null;
    }

    @Override
    public List<SearchSnapshot> searchDataByTileWithGap(IRenderParam param) throws Exception {
        List<SearchSnapshot> list = new ArrayList<>();

        String sql = "SELECT LINK_PID, LIMIT_DIR, GEOMETRY_ID, GEOMETRY_RDLINK FROM SC_PLATERES_RDLINK WHERE SDO_RELATE(GEOMETRY_RDLINK, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE'";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, param.getWkt());

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                SearchSnapshot snapshot = new SearchSnapshot();

                snapshot.setT(1006);

                STRUCT struct = (STRUCT) resultSet.getObject("GEOMETRY_RDLINK");

                Geometry geom = GeoTranslator.struct2Jts(struct);

                JSONObject geojson = GeoTranslator.jts2Geojson(geom);

                JSONObject jo = Geojson.link2Pixel(geojson, param.getMPX(), param.getMPY(), param.getZ());

                snapshot.setG(jo.getJSONArray("coordinates"));

                JSONObject m = new JSONObject();

                m.put("a", resultSet.getInt("LINK_PID"));

                m.put("b", resultSet.getInt("LIMIT_DIR"));

                m.put("c", resultSet.getString("GEOMETRY_ID"));

                snapshot.setM(m);

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

    @Override
    public String loadMaxKeyId(String groupId) throws Exception {
        return null;
    }
    
    public List<ScPlateresRdLink> loadByIds(JSONArray linkpids) throws Exception{

    	List<ScPlateresRdLink> rdlinks = new ArrayList<>();

        String sqlstr = "SELECT * FROM SC_PLATERES_RDLINK WHERE LINK_PID IN ";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {            
            String in = linkpids.toString().replace("[", "(").replace("]", ")");
            
            sqlstr += in;
            
            pstmt = this.conn.prepareStatement(sqlstr);

            resultSet = pstmt.executeQuery();

            while(resultSet.next()) {
            	ScPlateresRdLink rdlink = new ScPlateresRdLink();
            	
                ReflectionAttrUtils.executeResultSet(rdlink, resultSet);
                
                rdlinks.add(rdlink);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return rdlinks;
    }
    
    public List<ScPlateresRdLink> loadByGeometryIds(JSONArray geometryIds) throws Exception {

        List<ScPlateresRdLink> links = new ArrayList<>();

        String sqlstr = "SELECT * FROM SC_PLATERES_RDLINK WHERE GEOMETRY_ID IN ";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
			String in = "";

			for (int i = 0; i < geometryIds.size(); i++) {
				in = "'" + geometryIds.getString(i) + "'";
				if (i != geometryIds.size() - 1) {
					in += ",";
				}
			}
			
			sqlstr += "(" + in + ")";

			pstmt = this.conn.prepareStatement(sqlstr);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                ScPlateresRdLink link =new ScPlateresRdLink();

                ReflectionAttrUtils.executeResultSet(link, resultSet);

                links.add(link);
            }
        } catch (Exception e) {

            throw new Exception(e);
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return links;
    }
}