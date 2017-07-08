package com.navinfo.dataservice.dao.glm.search;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.render.GpsRecord;
import com.navinfo.dataservice.dao.glm.model.render.MissRoadDiDi;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class MissRoadDiDiSearch  implements ISearch{

	private Logger log = LoggerRepos.getLogger(this.getClass());
    /**
     * 数据库链接
     */
    private Connection conn;

    public MissRoadDiDiSearch(Connection conn) {
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
        return (IObj) new AbstractSelector(MissRoadDiDi.class, conn).loadById(pid, false);
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
	        
	    	String sql = "SELECT A.RID, A.GEOMETRY,A.LABEL,A.COLOUR  " 
	    					+" FROM MISS_ROAD_DIDI A "
	    					+"    WHERE  "
							+"  sdo_relate(A.GEOMETRY, "
							+"            SDO_GEOMETRY(:1, 8307), "
							+"            'mask=anyinteract') = 'TRUE'";
	
	        PreparedStatement pstmt = null;
	
	        ResultSet resultSet = null;
	        try {
	        	log.info("MISS_ROAD_DIDI Search sql : "+sql);
				pstmt = conn.prepareStatement(sql);
	
				String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);
				log.info("wkt :"+wkt);
				pstmt.setString(1, wkt);
	
				resultSet = pstmt.executeQuery();
	
				double px = MercatorProjection.tileXToPixelX(x);
	
				double py = MercatorProjection.tileYToPixelY(y);
	
				while (resultSet.next()) {
					SearchSnapshot snapshot = new SearchSnapshot();
	
					JSONObject m = new JSONObject();
	
					m.put("a", resultSet.getString("LABEL"));
	
					m.put("b", resultSet.getInt("COLOUR"));
	
//					m.put("i", resultSet.getString("Width"));
	
	
					snapshot.setM(m);
	
					snapshot.setT(1002);
	
					snapshot.setI(resultSet.getInt("RID"));
	
					STRUCT struct = (STRUCT) resultSet.getObject("geometry");
	
					JSONObject geojson = Geojson.spatial2Geojson(struct);
	
					JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);
	
					snapshot.setG(jo.getJSONArray("coordinates"));
	
					list.add(snapshot);
				}
			} catch (Exception e) {
	
				throw new Exception(e);
			} finally {
				if (resultSet != null) {
					try {
						resultSet.close();
					} catch (Exception e) {
	
					}
				}
	
				if (pstmt != null) {
					try {
						pstmt.close();
					} catch (Exception e) {
	
					}
				}
			}
	
			return list;
    }
}
