package com.navinfo.dataservice.FosEngine.edit.search;

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

import com.navinfo.dataservice.FosEngine.edit.model.IObj;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class RdRestrictionSearch implements ISearch {

	private static final WKT wktSpatial = new WKT();
	
	private static final WKTReader wktReader = new WKTReader();

	private Connection conn;

	public RdRestrictionSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {

		String sql = "select * from rd_restriction c,(  "
				+ "select listagg(a.detail_id || ',' || restric_pid || ',' || out_link_pid || ',' || flag || ',' || restric_info || ',' || type || ',' || relationship_type || ',' || decode(nvl(conds, '^^ ^^^^^'),'^^ ^^^^^', ' ', conds), '-') within group(order by 1) details  "
				+ "from  (select  detail_id,listagg(b.row_id || '^' || b.detail_id || '^' ||  nvl(b.time_domain, ' ') || '^' || vehicle || '^' ||  res_trailer || '^' || res_weigh || '^' ||  res_axle_load || '^' || res_axle_count,  '@') within group(order by detail_id) conds    "
				+ "from rd_restriction_condition b where u_record!=2 group by detail_id   ) b,    rd_restriction_detail a "
				+ "where a.restric_pid=:1    and a.detail_id = b.detail_id(+) and a.u_record!=2 ) b where c.pid=:2 ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		RdRestriction restriction = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, pid);

			pstmt.setInt(2, pid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				restriction = new RdRestriction();

				restriction.setPid(resultSet.getInt("pid"));

				restriction.setInLinkPid(resultSet.getInt("in_link_pid"));

				restriction.setNodePid(resultSet.getInt("node_pid"));

				restriction.setRestricInfo(resultSet.getString("restric_info"));

				restriction.setKgFlag(resultSet.getInt("kg_flag"));

				String detailStr = resultSet.getString("details");

				List<IRow> details = new ArrayList<IRow>();

				if (detailStr != null) {

					String[] splits = detailStr.split("-");

					for (String split : splits) {

						String[] s = split.split(",");

						RdRestrictionDetail detail = new RdRestrictionDetail();

						detail.setPid(Integer.valueOf(s[0]));

						detail.setRestricPid(Integer.valueOf(s[1]));

						detail.setOutLinkPid(Integer.valueOf(s[2]));

						detail.setFlag(Integer.valueOf(s[3]));

						detail.setRestricInfo(Integer.valueOf(s[4]));

						detail.setType(Integer.valueOf(s[5]));

						detail.setRelationshipType(Integer.valueOf(s[6]));

						List<IRow> conditions = new ArrayList<IRow>();

						if (!" ".equals(s[7])) {

							String[] conds = s[7].split("@");

							for (String cond : conds) {

								RdRestrictionCondition condition = new RdRestrictionCondition();

								String[] ss = cond.split("\\^");

								condition.setRowId(ss[0]);

								condition.setDetailId(Integer.valueOf(ss[1]));

								if (" ".equals(ss[2])) {

									condition.setTimeDomain(ss[2]);
								}

								condition.setVehicle(Integer.valueOf(ss[3]));

								condition.setResTrailer(Integer.valueOf(ss[4]));

								condition.setResWeigh(Integer.valueOf(ss[5]));

								condition
										.setResAxleLoad(Integer.valueOf(ss[6]));

								condition.setResAxleCount(Integer
										.valueOf(ss[7]));

								conditions.add(condition);

							}

						}

						detail.setConditions(conditions);

						details.add(detail);

					}
				}

				restriction.setDetails(details);

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

		return restriction;
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

				double[] point = null;

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

		String sql = "with tmp1 as  (select link_pid, geometry     from rd_link    where sdo_relate(geometry,                     sdo_geometry(:1,                                  8307),                     'mask=anyinteract') = 'TRUE'), tmp2 as  (select a.node_pid,a.in_link_pid,a.pid,a.restric_info     from rd_restriction a    where exists (select null from tmp1 b where a.in_link_pid = b.link_pid)), tmp3 as  (select listagg(vehicle, ',') within group(order by 1) vehicles     from rd_restriction_detail a, rd_restriction_condition b    where exists (select             null             from tmp2 c            where a.restric_pid = c.pid)      and a.detail_id = b.detail_id) select  a.pid,a.restric_info,b.vehicles,c.geometry link_geom,d.geometry point_geom  from tmp2 a,tmp3 b,tmp1 c,rd_node d where a.in_link_pid = c.link_pid and a.node_pid = d.node_pid";

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

				jsonM.put("b",resultSet.getString("restric_info"));

				jsonM.put("a", "0");

				STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

				JGeometry geom1 = JGeometry.load(struct1);

				String linkWkt = new String(wktSpatial.fromJGeometry(geom1));

				STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

				JGeometry geom2 = JGeometry.load(struct2);

				String pointWkt = new String(wktSpatial.fromJGeometry(geom2));

				int direct = getDirect(linkWkt, pointWkt);
				
				double angle = DisplayUtils.calIncloudedAngle(linkWkt, direct);

				jsonM.put("c", String.valueOf((int)angle));

				double[][] point = DisplayUtils
						.getLinkPointPos(linkWkt, pointWkt, 1, 0);

				snapshot.setG(Geojson.lonlat2Pixel(point[1][0],point[1][1],z,px,py));
				
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
	
	
	private static int getDirect(String linkWkt,String pointWkt) throws ParseException{
		
		int direct = 2;
		
		Geometry link = wktReader.read(linkWkt);
		
		Geometry point = wktReader.read(pointWkt);
		
		Coordinate[] csLink = link.getCoordinates();
		
		Coordinate cPoint = point.getCoordinate();
		
		if (csLink[0].x != cPoint.x || csLink[1].y != cPoint.y){
			direct = 3;
		}
		
		return direct;
	}

}
