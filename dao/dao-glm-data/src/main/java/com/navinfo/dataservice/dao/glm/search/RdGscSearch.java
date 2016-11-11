package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RdGscSearch implements ISearch {

	private Connection conn;
	
	public RdGscSearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdGscSelector selector = new RdGscSelector(conn);

		IObj obj = (IObj) selector.loadById(pid, false);

		return obj;
	}

	@Override
	public IObj searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "with tmp1 as (select a.pid   from rd_gsc a  where a.u_record != 2    and sdo_within_distance(a.geometry, sdo_geometry(    :1 , 8307), 'DISTANCE=0') =        'TRUE'),          tmp2 as( select a.pid from rd_gsc_link a,tmp1 b where a.pid = b.pid and a.table_name in ('RD_LINK','RW_LINK') group by a.pid having count(1)>1),          tmp3 as( select a.*,b.geometry from rd_gsc_link a, rd_link b,tmp2 c where a.link_pid=b.link_pid  and a.table_name ='RD_LINK'   and a.pid=c.pid), tmp4 as (   select a.*,b.geometry from rd_gsc_link a, rw_link b,tmp2 c where a.link_pid=b.link_pid  and a.table_name ='RW_LINK'   and a.pid=c.pid)      select * from (select * from tmp3   union all   select * from tmp4) order by pid";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			SearchSnapshot snapshot = new SearchSnapshot();
			
			JSONArray g = new JSONArray();
			
			int lastPid = 0;
			
			while (resultSet.next()) {
                int pid = resultSet.getInt("pid");
                
                if(pid != lastPid){
                	
                	snapshot.setG(g);
                	
                	list.add(snapshot);
                	
                	snapshot = new SearchSnapshot();
                	
                	g = new JSONArray();
                	
                	lastPid = pid;
                }
                
                snapshot.setT(11);
                
                snapshot.setI(String.valueOf(pid));
                
                int zlevel = resultSet.getInt("zlevel");
                
                int linkPid = resultSet.getInt("link_pid");
                
                int seqNum = resultSet.getInt("shp_seq_num");
                
                int startEnd = resultSet.getInt("start_end");

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geo = GeoTranslator.struct2Jts(struct);

				Geometry line = DisplayUtils.getGscLine4Web(geo, startEnd, seqNum, 18);
				
				JSONObject geojson = GeoTranslator.jts2Geojson(line);
				
				JSONObject obj = new JSONObject();
				
				obj.put("g", geojson.getJSONArray("coordinates"));
				
				obj.put("z", zlevel);
				
				obj.put("i", linkPid);
				
				g.add(obj);
				
			}
			
			if(g.size()>0){
				snapshot.setG(g);
            	
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

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {

		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT A.PID FROM RD_GSC A WHERE A.U_RECORD != 2 AND SDO_WITHIN_DISTANCE(A.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE'), TMP2 AS (SELECT A.PID FROM RD_GSC_LINK A, TMP1 B WHERE A.PID = B.PID AND A.U_RECORD != 2 AND A.TABLE_NAME IN ('RD_LINK', 'RW_LINK','LC_LINK') GROUP BY A.PID HAVING COUNT(1) > 1), TMP3 AS (SELECT A.*, B.GEOMETRY FROM RD_GSC_LINK A, RD_LINK B, TMP2 C WHERE A.LINK_PID = B.LINK_PID AND A.U_RECORD != 2 AND A.TABLE_NAME = 'RD_LINK' AND A.PID = C.PID), TMP4 AS (SELECT A.*, B.GEOMETRY FROM RD_GSC_LINK A, RW_LINK B, TMP2 C WHERE A.LINK_PID = B.LINK_PID AND A.U_RECORD != 2 AND A.TABLE_NAME = 'RW_LINK' AND A.PID = C.PID), TMP5 AS (SELECT A.*, B.GEOMETRY FROM RD_GSC_LINK A, LC_LINK B, TMP2 C WHERE A.LINK_PID = B.LINK_PID AND A.U_RECORD != 2 AND A.TABLE_NAME = 'LC_LINK' AND A.PID = C.PID) SELECT TMP.*, A.GEOMETRY AS GSC_GEO FROM (SELECT * FROM TMP3 UNION ALL SELECT * FROM TMP4 UNION ALL SELECT * FROM TMP5) TMP, RD_GSC A WHERE TMP.PID = A.PID ORDER BY TMP.PID, TMP.ZLEVEL  ";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			
			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();
			
			double px =  MercatorProjection.tileXToPixelX(x);
			
			double py =  MercatorProjection.tileYToPixelY(y);
			
			int lastPid = 0;
			
			SearchSnapshot snapshot = new SearchSnapshot();
			
			JSONArray g = new JSONArray();
			
			while (resultSet.next()) {
                int pid = resultSet.getInt("pid");
                
                if(lastPid==0){
                	lastPid = pid;
                }
                
                if(pid != lastPid){
                	
                	snapshot.setG(g);
                	
                	list.add(snapshot);
                	
                	snapshot = new SearchSnapshot();
                	
                	g = new JSONArray();
                	
                	lastPid = pid;
                }
                
                snapshot.setT(11);
                
                snapshot.setI(String.valueOf(pid));
                
                int zlevel = resultSet.getInt("zlevel");
                
                int linkPid = resultSet.getInt("link_pid");
                
                int seqNum = resultSet.getInt("shp_seq_num");
                
                int startEnd = resultSet.getInt("start_end");

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				
				STRUCT structGsc = (STRUCT) resultSet.getObject("gsc_geo");
				
				Geometry geo = GeoTranslator.struct2Jts(struct);
				
				Geometry geoGsc = GeoTranslator.struct2Jts(structGsc);

				Geometry line = DisplayUtils.getGscLine4Web(geo, startEnd, seqNum, z);
				
				JSONObject geojson = GeoTranslator.jts2Geojson(line);
				
				JSONObject geojsonGsc = GeoTranslator.jts2Geojson(geoGsc);
				
				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);
				
				JSONObject obj = new JSONObject();
				
				obj.put("g", jo.getJSONArray("coordinates"));
				
				obj.put("z", zlevel);
				
				obj.put("i", linkPid);
				
				Geojson.point2Pixel(geojsonGsc, z, px, py);
				
				JSONObject m = new JSONObject();
				
				m.put("a", geojsonGsc.getJSONArray("coordinates"));
				
				snapshot.setM(m);
				
				g.add(obj);
				
			}
			
			if(g.size()>0){
				snapshot.setG(g);
            	
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
	public static void main(String[] args) throws Exception {
		
		Connection conn = DBConnector.getInstance().getConnectionById(11);
		
		RdGscSearch s = new RdGscSearch(conn);
		
		List<SearchSnapshot> list = s.searchDataByTileWithGap(215885, 99231, 18, 80);
		
		for(SearchSnapshot snap : list){
			System.out.println(snap.Serialize(null));
		}
		
//		System.out.println(MercatorProjection.longitudeToTileX(116.48821, (byte)19));
//		
//		System.out.println(MercatorProjection.latitudeToTileY(39.98898, (byte)19));
	}
}