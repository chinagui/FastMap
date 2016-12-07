package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.voiceguide.RdVoiceguideSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

public class RdVoiceguideSearch implements ISearch {
	
	private WKT wktSpatial = new WKT();
	
	private Connection conn;

	public RdVoiceguideSearch(Connection conn) {

		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {

		RdVoiceguideSelector selector = new RdVoiceguideSelector(conn);
		
		IObj obj = (IObj)selector.loadById(pid, false);
		
		return obj;
	}
	
	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		return null;
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

		String sql = "WITH TMP1 AS  (SELECT NODE_PID, GEOMETRY     FROM RD_NODE    WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') =          'TRUE'      AND U_RECORD != 2)     SELECT  /*+ index(c)*/  A.PID, B.GEOMETRY point_geom,C.GEOMETRY link_geom   FROM RD_VOICEGUIDE A, TMP1 B,RD_LINK C where A.NODE_PID = B.NODE_PID and a.IN_LINK_PID = c.LINK_PID and a.u_record !=2 and c.U_RECORD !=2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);
			
			System.out.println(wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();
				
				JSONObject jsonM = new JSONObject();

				snapshot.setT(44);

				snapshot.setI(String.valueOf(resultSet.getInt("pid")));

				STRUCT struct = (STRUCT) resultSet.getObject("point_geom");
				
				JGeometry geom2 = JGeometry.load(struct);
				
				STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

				JGeometry geom1 = JGeometry.load(struct1);

				String linkWkt = new String(wktSpatial.fromJGeometry(geom1));
				
				String pointWkt = new String(wktSpatial.fromJGeometry(geom2));

				int direct = DisplayUtils.getDirect(linkWkt, pointWkt);
				
				double angle = DisplayUtils.calIncloudedAngle(linkWkt, direct);

				jsonM.put("a", String.valueOf((int)angle));
				
				double offset = 10;
				switch(z){
				case 16:
				case 17:
					offset = 20; break;
				case 18:
					offset = 4; break;
				case 19:
					offset = 1; break;
				case 20:
					offset = 0; break;
				}
				
				double[][] point = DisplayUtils.getGdbPointPos(linkWkt, pointWkt, 2, (21-z)*7.5+offset, 6,z);

				snapshot.setG(Geojson.lonlat2Pixel(point[1][0], point[1][1], z,
						px, py));

				snapshot.setM(jsonM);

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new SQLException(e);
		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

}
