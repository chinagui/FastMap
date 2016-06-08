package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class RdGscSearch implements ISearch {

	private Connection conn;

	private final GeometryFactory geometryFactory = new GeometryFactory();
	
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

		String sql = "with tmp1 as (select a.pid   from rd_gsc a  where a.u_record != 2    and sdo_within_distance(a.geometry, sdo_geometry(    :1 , 8307), 'DISTANCE=0') =        'TRUE'),          tmp2 as( select a.pid from rd_gsc_link a,tmp1 b where a.pid = b.pid and a.table_name in ('RD_LINK','RW_LINK') group by a.pid having count(1)>1),          tmp3 as( select a.*,b.geometry from rd_gsc_link a, rd_link b,tmp2 c where a.link_pid=b.link_pid  and a.table_name ='RD_LINK'   and a.pid=c.pid), tmp4 as (   select a.*,b.geometry from rd_gsc_link a, rw_link b,tmp2 c where a.link_pid=b.link_pid  and a.table_name ='RW_LINK'   and a.pid=c.pid)      select tmp.*,A.Geometry AS gsc_geo from (select * from tmp3   union all   select * from tmp4) tmp,RD_GSC A WHERE tmp.pid = a.pid order by tmp.PID, tmp.ZLEVEL";
		
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
//	public static void main(String[] args) throws Exception {
//		
//		Connection conn = GlmDbPoolManager.getInstance().getConnection(11);
//		
//		RdGscSearch s = new RdGscSearch(conn);
//		
//		List<SearchSnapshot> list = s.searchDataByTileWithGap(215885, 99231, 18, 80);
//		
//		for(SearchSnapshot snap : list){
//			System.out.println(snap.Serialize(null));
//		}
//		
////		System.out.println(MercatorProjection.longitudeToTileX(116.48821, (byte)19));
////		
////		System.out.println(MercatorProjection.latitudeToTileY(39.98898, (byte)19));
//	}
}