package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class CmgBuildingSearch  implements ISearch{

    /**
     * 数据库链接
     */
    private Connection conn;

    public CmgBuildingSearch(Connection conn) {
        this.conn = conn;
    }

    /**
     * 通过pid获取数据
     *
     * @param pid
     * @return
     * @throws Exception
     */
    @Override
    public IObj searchDataByPid(int pid) throws Exception {
        return (IObj) new AbstractSelector(CmgBuildface.class, conn).loadById(pid, false);
    }

    /**
     * 通过pids获取数据(框选功能)
     *
     * @param pidList@return
     * @throws Exception
     */
    @Override
    public List<? extends IObj> searchDataByPids(List<Integer> pidList) throws Exception {
        return null;
    }

    /**
     * 通过范围获取数据
     *
     * @param wkt
     * @return
     * @throws Exception
     */
    @Override
    public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
        return null;
    }

    /**
     * 通过条件获取数据
     *
     * @param condition
     * @return
     * @throws Exception
     */
    @Override
    public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
        return null;
    }

    /**
     * 通过瓦片号获取数据
     *
     * @param x
     * @param y
     * @param z
     * @param gap
     * @return
     * @throws Exception
     */
    @Override
    public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {
        
    	List<SearchSnapshot> list = new ArrayList<>();
        
    	String sql = "WITH TMP1 AS (SELECT A.FACE_PID, A.BUILDING_PID, A.GEOMETRY FROM CMG_BUILDFACE A WHERE A.U_RECORD <> 2 AND SDO_WITHIN_DISTANCE(A.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE') SELECT /*+ index(b) */ A.FACE_PID, B.PID BUILDING_PID, A.GEOMETRY FROM CMG_BUILDING B, TMP1 A WHERE A.BUILDING_PID = B.PID AND B.U_RECORD <> 2";
        
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sql);
            String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);
            pstmt.setString(1, wkt);
            resultSet = pstmt.executeQuery();
            double px = MercatorProjection.tileXToPixelX(x);
            double py = MercatorProjection.tileYToPixelY(y);
            
            //<Integer:建筑物pid,<Integer:facePid,JSONArray：face几何>
			Map<Integer, Map<Integer, JSONArray>> buildingInfo = new HashMap<Integer, Map<Integer, JSONArray>>();
			
			  while (resultSet.next()) 
			  {
				int buildingPid = resultSet.getInt("BUILDING_PID");

				int facePid = resultSet.getInt("face_pid");

				if (!buildingInfo.containsKey(buildingPid)) {
					
					buildingInfo.put(buildingPid, new HashMap<Integer, JSONArray>());
				}
				
				Map<Integer, JSONArray> faceInfo =buildingInfo.get(buildingPid);

				if (!faceInfo.containsKey(facePid)) {
					
					STRUCT struct = (STRUCT) resultSet.getObject("geometry");

					JSONObject geojson = Geojson.spatial2Geojson(struct);

					JSONObject jo = Geojson.face2Pixel(geojson, px, py, z);

					JSONArray jsonGeo = jo.getJSONArray("coordinates");

					faceInfo.put(facePid, jsonGeo);
				}
			}
			  
			for (Integer buildingPid : buildingInfo.keySet()) {
				
				SearchSnapshot snapshot = new SearchSnapshot();
                
				snapshot.setT(54);
               
				snapshot.setI(buildingPid);
				
				snapshot.setG(new JSONArray());
                
				JSONObject jsonM = new JSONObject();
				
				JSONArray maArray = new JSONArray();

				Map<Integer, JSONArray> faceInfo = buildingInfo
						.get(buildingPid);

				for (Integer facePid : faceInfo.keySet()) {

					JSONObject faceJson = new JSONObject();

					faceJson.put("i", facePid);
					faceJson.put("g", faceInfo.get(facePid));
					
					maArray.add(faceJson);
				}
				
				jsonM.put("a", maArray);

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
}
