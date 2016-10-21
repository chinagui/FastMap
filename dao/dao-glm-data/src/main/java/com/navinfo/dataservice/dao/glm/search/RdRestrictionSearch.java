package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;

public class RdRestrictionSearch implements ISearch {

	private Connection conn;

	public RdRestrictionSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {

		RdRestrictionSelector selector = new RdRestrictionSelector(conn);
		
		IObj obj = (IObj)selector.loadById(pid, false);
		
		return obj;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String box)
			throws SQLException {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "select a.pid,        a.restric_info,        a.node_pid,        b.geometry,        b.s_node_pid,        b.e_node_pid,        c.vt   from rd_restriction a,        rd_link b,        (select a.pid, sum(decode(vt, null, 0, 0, 0, 1)) vt           from rd_restriction a,                rd_restriction_detail b,                (select detail_id, sum(package_utils.parse_vehicle(vehicle)) vt                   from rd_restriction_condition                  group by detail_id) c          where a.pid = b.restric_pid            and b.detail_id = c.detail_id(+)                   group by a.pid) c  where a.in_link_pid = b.link_pid    and a.pid = c.pid    and sdo_within_distance(b.geometry,                            sdo_geometry(:1,                                         8307),                            'DISTANCE=0') = 'TRUE'";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, box);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();
				
				JSONObject jsonM = new JSONObject();

				snapshot.setI(String.valueOf(resultSet.getInt("pid")));
				
				snapshot.setT(3);

				jsonM.put("b",resultSet.getString("restric_info"));

				int vt = resultSet.getInt("vt");

				if (vt > 0) {
					vt = 1;
				}

				jsonM.put("a", vt);

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JGeometry geom = JGeometry.load(struct);

				int sNodePid = resultSet.getInt("s_node_pid");

				int eNodePid = resultSet.getInt("e_node_pid");

				int nodePid = resultSet.getInt("node_pid");

				double angle = AngleCalculator.getDisplayAngle(nodePid,
						sNodePid, eNodePid, geom);

				jsonM.put("c", String.valueOf((int)angle));

				double[] point = geom.getPoint();

				JSONArray geo = new JSONArray();

				geo.add(point[0]);

				geo.add(point[1]);

				snapshot.setG(JSONArray.fromObject(point));

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

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {

		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "with tmp1 as  (select link_pid, geometry     from rd_link    where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') =          'TRUE'      and u_record != 2), tmp2 as  (select a.node_pid, a.in_link_pid, a.pid, a.restric_info     from rd_restriction a    where exists (select null from tmp1 b where a.in_link_pid = b.link_pid)      and a.u_record != 2), tmp3 as  (select listagg(vehicle, ',') within group(order by 1) vehicles     from rd_restriction_detail a, rd_restriction_condition b    where exists (select null from tmp2 c where a.restric_pid = c.pid)      and a.detail_id = b.detail_id      and a.u_record != 2      and b.u_record != 2) select /*+ index(d) */        a.pid,        a.restric_info,        b.vehicles,        c.geometry     link_geom,        d.geometry     point_geom   from tmp2 a, tmp3 b, tmp1 c, rd_node d  where a.in_link_pid = c.link_pid    and a.node_pid = d.node_pid    and d.u_record != 2";
		
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
				
				snapshot.setT(3);
				
				String b = resultSet.getString("restric_info");
				
				if(b.startsWith("[") && b.endsWith("]")){
					b=b+StringUtils.PlaceHolder;
				}

				jsonM.put("b",b);

				jsonM.put("a", "0");

				STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

				JGeometry geom1 = JGeometry.load(struct1);

				String linkWkt = new String(new WKT().fromJGeometry(geom1));

				STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

				JGeometry geom2 = JGeometry.load(struct2);

				String pointWkt = new String(new WKT().fromJGeometry(geom2));

				int direct = DisplayUtils.getDirect(linkWkt, pointWkt);
				
				double angle = DisplayUtils.calIncloudedAngle(linkWkt, direct);

				jsonM.put("c", String.valueOf((int)angle));

				double linkLength = GeometryUtils.getLinkLength(linkWkt);

				if (linkLength < 5) {
					
					double[] point = DisplayUtils.getRatioPointForLink(geom1, direct, 0.4);

					JSONObject geojson = new JSONObject();

					geojson.put("type", "Point");

					geojson.put("coordinates", point);

					Geojson.point2Pixel(geojson, z, px, py);

					snapshot.setG(geojson.getJSONArray("coordinates"));
					
				} else {
					double[][] point = DisplayUtils.getGdbPointPos(linkWkt,
							pointWkt, 1);

					snapshot.setG(Geojson.lonlat2Pixel(point[1][0],
							point[1][1], z, px, py));
				}
				
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
	
	public static void main(String[] args) throws Exception {

		Connection conn = DBConnector.getInstance().getConnectionById(11);

		RdRestrictionSearch a = new RdRestrictionSearch(conn);
		
		List<SearchSnapshot> res = a.searchDataByTileWithGap(
				107914, 49663, 17, 20);

		List<String> array = new ArrayList<String>();
		int i=0;
		for(SearchSnapshot s : res){
			System.out.println(s.Serialize(null));
			array.add(s.Serialize(null).toString());
		}
		
	}

}
