package com.navinfo.dataservice.FosEngine.edit.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.FosEngine.edit.model.IObj;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;

public class RdSpeedlimitSearch implements ISearch {
	
	private static final WKT wktSpatial = new WKT();

	private Connection conn;

	public RdSpeedlimitSearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(conn);
		
		IObj obj = (IObj)selector.loadById(pid, false);
		
		return obj;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "with tmp1 as  (select link_pid, geometry     from rd_link    where sdo_relate(geometry,                     sdo_geometry(:1,                                  8307),                     'mask=anyinteract') = 'TRUE') select  a.pid,a.direct,a.capture_flag||'|'||a.speed_flag||'|'||a.speed_value a_val,b.geometry link_geom,a.geometry point_geom  from rd_speedlimit a,tmp1 b where a.link_pid = b.link_pid";

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
				
				JSONObject jsonM = new JSONObject();

				snapshot.setI(String.valueOf(resultSet.getInt("pid")));
				
				snapshot.setT(6);

				jsonM.put("a",resultSet.getString("a_val"));

				STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

				JGeometry geom1 = JGeometry.load(struct1);

				String linkWkt = new String(wktSpatial.fromJGeometry(geom1));

				STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

				JGeometry geom2 = JGeometry.load(struct2);
				
				double angle = DisplayUtils.calIncloudedAngle(linkWkt, resultSet.getInt("direct"));

				jsonM.put("c", String.valueOf((int)angle));

				snapshot.setG(Geojson.lonlat2Pixel(geom2.getFirstPoint()[0],geom2.getFirstPoint()[1],z,px,py));
				
				snapshot.setM(jsonM);

				list.add(snapshot);
			}
		} catch (Exception e) {
			
			throw new SQLException(e);
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
	
	
	//通过传入点限速的LINKPID和通行方向，返回跟踪LINK路径
	public String trackSpeedLimitLink(int linkPid,int direct) throws Exception{
		
		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(conn);
		
		return selector.trackSpeedLimitLink(linkPid, direct);
	}
	
	
	public static void main(String[] args) throws Exception {
		ConfigLoader.initDBConn("C:/Users/wangshishuai3966/git/FosEngine/FosEngine/src/config.properties");
		
		Connection conn = DBOraclePoolManager.getConnection(1);
		
		RdSpeedlimitSearch s = new RdSpeedlimitSearch(conn);
		
		IObj obj = s.searchDataByPid(7039);
		
		System.out.println(obj.Serialize(null));
	}
}
