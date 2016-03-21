package com.navinfo.dataservice.FosEngine.edit.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.FosEngine.edit.model.IObj;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkForm;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkLimit;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkName;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.Relate;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.vividsolutions.jts.geom.Geometry;

public class RdLinkSearch implements ISearch {

	private Connection conn;

	public RdLinkSearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		
		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		
		IObj link = (IObj)linkSelector.loadById(pid, false);
		
		return link;
	}

	public IObj searchDataByPid2(int pid) throws Exception {

		String sql = "select a.link_pid, a.s_node_pid, a.e_node_pid, a.direct, a.kind, a.lane_num, a.lane_left, a.lane_right, a.multi_digitized, a.function_class, a.geometry, b.forms, c.limits, d.names, e.restricts "
				+ "from rd_link a, (select listagg(row_id || ',' || form_of_way, '-') within group(order by form_of_way) forms   from rd_link_form  where link_pid = :1 and u_record!=2) b, "
				+ "(select listagg(row_id || ',' || type || ',' || limit_dir || ',' ||  nvl(time_domain, ' '),  '-') within group(order by type) limits   from rd_link_limit  where link_pid = :2 and u_record!=2) c, "
				+ "(select listagg(a.row_id || '^' || a.seq_num || '^' || name_class || '^' ||  name_type || '^' || b.name,  '@') within group(order by 1) names   from rd_link_name a, rd_name b  where a.link_pid = :3 and a.name_groupid = b.name_groupid and a.u_record!=2) d, "
				+ "(select listagg(pid, ',') within group(order by 1) restricts   from rd_restriction  where in_link_pid = :4  and u_record!=2) e  where a.link_pid = :5";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		RdLink link = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, pid);

			pstmt.setInt(2, pid);

			pstmt.setInt(3, pid);

			pstmt.setInt(4, pid);

			pstmt.setInt(5, pid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				link = new RdLink();

				link.setPid(resultSet.getInt("link_pid"));

				link.seteNodePid(resultSet.getInt("e_node_pid"));

				link.setsNodePid(resultSet.getInt("s_node_pid"));

				link.setDirect(resultSet.getInt("direct"));

				link.setKind(resultSet.getInt("kind"));

				link.setLaneNum(resultSet.getInt("lane_num"));

				link.setLaneLeft(resultSet.getInt("lane_left"));

				link.setLaneRight(resultSet.getInt("lane_right"));

				link.setMultiDigitized(resultSet.getInt("multi_digitized"));

				link.setFunctionClass(resultSet.getInt("function_class"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				link.setGeometry(geometry);

				String formStr = resultSet.getString("forms");

				List<IRow> forms = new ArrayList<IRow>();

				if (formStr != null) {

					String splits[] = formStr.split("-");

					for (String split : splits) {

						String s[] = split.split(",");

						RdLinkForm form = new RdLinkForm();
						
						form.setLinkPid(pid);

						form.setRowId(s[0]);

						form.setFormOfWay(Integer.valueOf(s[1]));

						forms.add(form);
					}
				}

				link.setForms(forms);

				String limitStr = resultSet.getString("limits");

				List<IRow> limits = new ArrayList<IRow>();

				if (limitStr != null) {
					String[] splits = limitStr.split("-");

					for (String split : splits) {

						String s[] = split.split(",");

						RdLinkLimit limit = new RdLinkLimit();
						
						limit.setLinkPid(pid);

						limit.setRowId(s[0]);

						limit.setType(Integer.valueOf(s[1]));

						limit.setLimitDir(Integer.valueOf(s[2]));

						if (!" ".equals(s[3])) {
							limit.setTimeDomain(s[3]);
						}

						limits.add(limit);
					}
				}

				link.setLimits(limits);

				List<IRow> names = new ArrayList<IRow>();

				String nameStr = resultSet.getString("names");

				if (nameStr != null) {
					String[] splits = nameStr.split("@");

					for (String split : splits) {

						String s[] = split.split("\\^");

						RdLinkName name = new RdLinkName();

						name.setRowId(s[0]);
						
						name.setLinkPid(pid);

						name.setSeqNum(Integer.valueOf(s[1]));

						name.setNameClass(Integer.valueOf(s[2]));

						name.setNameType(Integer.valueOf(s[3]));

						name.setName(s[4]);

						names.add(name);
					}
				}
				link.setNames(names);

				List<Relate> relates = new ArrayList<Relate>();

				String restrictStr = resultSet.getString("restricts");

				if (restrictStr != null) {

					Relate relate = new Relate();

					List<Integer> pids = new ArrayList<Integer>();

					String[] splits = restrictStr.split(",");

					for (String split : splits) {

						pids.add(Integer.valueOf(split));
					}

					relate.setPids(pids);

					relate.setType(ObjType.RDRESTRICTION);

					relates.add(relate);
				}

				link.setRelates(relates);

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

		return link;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "select /*+ index(b) */  a.link_pid,  a.direct,  a.kind,  a.s_node_pid,  a.e_node_pid,  b.name,  a.geometry   from rd_link a, (select b.link_pid,c.name from  rd_link_name b,rd_name c where b.name_groupid = c.name_groupid and b.name_class=1 and b.seq_num =1 and c.lang_code='CHI' and b.u_record != 2) b   where a.u_record != 2    and a.link_pid = b.link_pid(+)    and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') =        'TRUE'";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				int kind = resultSet.getInt("kind");
                
                JSONObject m = new JSONObject();
                
                m.put("a", String.valueOf(DisplayUtils.kind2Color(kind)));
                
                m.put("b", resultSet.getString("name"));
                
                m.put("c", String.valueOf(kind));
                
                m.put("d",String.valueOf(resultSet.getInt("direct")));
                
                snapshot.setM(m);
                
                snapshot.setT(4);

                snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

                STRUCT struct = (STRUCT) resultSet.getObject("geometry");

                JSONObject jo = Geojson.spatial2Geojson(struct);

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

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {

		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "select a.link_pid,  a.direct,  a.kind,  a.s_node_pid,  a.e_node_pid,  b.name,  a.geometry   from rd_link a, (select /*+ index(b) */ b.link_pid,c.name from  rd_link_name b,rd_name c where b.name_groupid = c.name_groupid and b.name_class=1 and b.seq_num =1 and c.lang_code='CHI' and b.u_record != 2  ) b   where a.u_record != 2    and a.link_pid = b.link_pid(+)    and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') =        'TRUE'";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			
			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();
			
			double px =  MercatorProjection.tileXToPixelX(x);
			
			double py =  MercatorProjection.tileYToPixelY(y);
			
			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				int kind = resultSet.getInt("kind");
                
                JSONObject m = new JSONObject();
                
                m.put("a", String.valueOf(DisplayUtils.kind2Color(kind)));
                
                m.put("b", resultSet.getString("name"));
                
                m.put("c", String.valueOf(kind));
                
                m.put("d",String.valueOf(resultSet.getInt("direct")));
                
                m.put("e", String.valueOf(resultSet.getInt("s_node_pid")));
                
                m.put("f", String.valueOf(resultSet.getInt("e_node_pid")));
                
                snapshot.setM(m);
                
                snapshot.setT(4);
                
                snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject jo = Geojson.link2Pixel(struct, px, py, z);
				
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
