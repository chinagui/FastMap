package com.navinfo.dataservice.engine.editplus.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import oracle.sql.STRUCT;


public class AdFaceSelector {
    private Connection conn;

    public AdFaceSelector(Connection conn) {
        this.conn = conn;
    }
    
    // 获取AdFace，没有关联Face时返回Null
    public long getAdFaceRegionId(Geometry geometry) throws Exception {
    	Map<Long,Geometry> faces = loadRelateFaceByGeometry(geometry);
    	long defaultId = 0;
        if (faces.isEmpty())
            return 0;

        Point point = geometry.getCentroid();
        for (long regionId : faces.keySet()) {
        	defaultId = regionId;
            Geometry geo = GeoTranslator.transform(faces.get(regionId), 0.00001, 5);
            if (point.coveredBy(geo)) {
                return regionId;
            }
        }
        return defaultId;
    }
    
    /**
     * 根据传入几何参数查找与之相关联的ZoneFace面</br>
     * ADMIN_TYPE类型为:</br>
     * 国家地区级（0），省/直辖市/自治区（1），地级市/自治州/省直辖县（2）</br>
     * DUMMY地级市（2.5），地级市市区GCZone（3），地级市市区（未作区界 3.5）</br>
     * 区县/自治县（4），DUMMY区县（4.5），DUMMY区县（地级市下无区县 4.8）</br>
     * 区中心部（5），乡镇/街道（6）,飞地（7）
     *
     * @param geometry
     * @return
     */
    public Map<Long,Geometry> loadRelateFaceByGeometry(Geometry geometry) {
    	Map<Long,Geometry> faceMap = new HashMap<Long,Geometry>();
        String sql = "select t1.geometry, t2.region_id from ad_face t1, ad_admin t2 where t1.u_record <> 2 and t2.u_record <> 2 and t1.region_id = t2.region_id and (t2.admin_type = 0 or t2.admin_type = 1 or t2.admin_type = 2 or t2.admin_type = 2.5 or t2.admin_type = 3 or t2.admin_type = 3.5 or t2.admin_type = 4 or t2.admin_type = 4.5 or t2.admin_type = 4.8 or t2.admin_type = 5 or t2.admin_type = 6 or t2.admin_type = 7) and sdo_relate(t1.geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') = 'TRUE' ";
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sql);
            String wkt = GeoTranslator.jts2Wkt(geometry);
            pstmt.setString(1, wkt);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
            	long region_id=resultSet.getLong("region_id");
            	Geometry geo = GeoTranslator.struct2Jts((STRUCT) resultSet.getObject("geometry"), 100000, 0);
                faceMap.put(region_id,geo);
            }
        } catch (Exception e) {
        	System.out.println(e.getMessage());
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return faceMap;
    }
    
}