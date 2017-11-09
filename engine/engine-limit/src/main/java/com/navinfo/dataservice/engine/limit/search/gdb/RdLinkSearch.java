package com.navinfo.dataservice.engine.limit.search.gdb;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.limit.search.limit.SearchHelp;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class RdLinkSearch {
	private Connection conn = null;
	
	public RdLinkSearch(Connection conn){
		this.conn = conn;
	}
	
	public JSONObject searchDataByCondition(int type, JSONObject condition) throws Exception{
		
		if(!condition.containsKey("names")){
			throw new Exception("未输入道路名，无法查询道路信息");
		}
		
		JSONArray names = condition.getJSONArray("names");

		String adminCode =condition.getString("adminCode");
		
		StringBuilder sql = new StringBuilder();
		
		if (type == 1) {  
			componentSql(sql, names);   //模糊查询
		} else {
			componentSqlForAccurate(sql, names);   //精准查询
		}
		
		PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        
		try {
			pstmt = conn.prepareStatement(sql.toString());
			
			resultSet = pstmt.executeQuery();
			
			TreeMap<String,List<Integer>> classify = new TreeMap<>();

			while (resultSet.next()) {

				int pid = resultSet.getInt("pid");

				String nameout =resultSet.getString("name");

				if (!classify.containsKey(nameout)) {
					classify.put(nameout, new ArrayList<Integer>());
				}
				classify.get(nameout).add(pid);
			}

			JSONObject result = componetQueryResult(classify,adminCode);
			
			return result;
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
	}

	private JSONObject componetQueryResult(TreeMap<String, List<Integer>> classify) throws Exception {
		JSONArray array = new JSONArray();

		JSONObject result = new JSONObject();

		RdLinkSelector selector = new RdLinkSelector(this.conn);

		for (Map.Entry<String, List<Integer>> entry : classify.entrySet()) {

			JSONObject obj = new JSONObject();

			obj.put("name", entry.getKey());

			List<IRow> links = selector.loadByIds(entry.getValue(), true, false);

			JSONArray linkarray = new JSONArray();

			for (IRow row : links) {
				RdLink link = (RdLink) row;

				JSONObject subobj = new JSONObject();

				subobj.put("pid", link.getPid());
				subobj.put("geometry", getMidShapePoint(GeoTranslator.transform(link.getGeometry(), 0.00001, 5)));

				linkarray.add(subobj);
			}

			obj.put("links", linkarray);

			array.add(obj);
		}

		result.put("total", classify.size());

		result.put("rows", array);

		result.put("geoLiveType", ObjType.RDLINK);

		return result;
	}
	
	private JSONObject componetQueryResult(TreeMap<String, List<Integer>> classify,String adminCode) throws Exception {

		Set<Integer> subAdmin = getSubRegionId( adminCode);

		JSONArray array = new JSONArray();

		JSONObject result = new JSONObject();

		RdLinkSelector selector = new RdLinkSelector(this.conn);

		for (Map.Entry<String, List<Integer>> entry : classify.entrySet()) {

			List<IRow> links = selector.loadByIds(entry.getValue(), true, false);

			JSONArray linkarray = new JSONArray();

			for (IRow row : links) {
				RdLink link = (RdLink) row;

				if (subAdmin.contains(link.getLeftRegionId()) || subAdmin.contains(link.getRightRegionId())) {

					JSONObject subobj = new JSONObject();

					subobj.put("pid", link.getPid());

					JSONObject midPoint=getMidShapePoint(GeoTranslator.transform(link.getGeometry(), 0.00001, 5));

					subobj.put("geometry", midPoint);

					linkarray.add(subobj);
				}
			}
			if (linkarray.size()>0) {

				JSONObject obj = new JSONObject();

				obj.put("name", entry.getKey());

				obj.put("links", linkarray);

				array.add(obj);
			}
		}

		result.put("total", classify.size());

		result.put("rows", array);

		result.put("geoLiveType", ObjType.RDLINK);

		return result;
	}
	
	private JSONObject getMidShapePoint(Geometry linkgeo) {
		Coordinate coor = new Coordinate();
		JSONObject obj = new JSONObject();

		Coordinate[] coors = linkgeo.getCoordinates();

		if (coors.length < 2) {
			return obj;

		} else if (coors.length == 2) {
			double x1 = coors[0].x;
			double y1 = coors[0].y;

			double x2 = coors[1].x;
			double y2 = coors[1].y;

			coor.x = (x1 + x2) / 2;
			coor.y = (y1 + y2) / 2;

		} else {
			coor = coors[coors.length / 2 + 1];
		}

		obj = GeoTranslator.jts2Geojson(GeoTranslator.createPoint(coor));

		return obj;
	}

	private void componentSql(StringBuilder sql, JSONArray names) {
		sql.append("with tmp1 as ( select name_groupid,name from rd_name where");

		for (int i = 0; i < names.size(); i++) {
			if (i > 0) {
				sql.append(" or ");
			}
			sql.append(" name like '%");
			sql.append(names.getString(i));
			sql.append("%'");
		}

		sql.append(" and u_record != 2),");

		sql.append(" TMP2 AS");
		sql.append(" (SELECT /*+ index(r1)*/");
		sql.append(" RLN.LINK_PID PID, TMP1.NAME, RLN.NAME_GROUPID");
		sql.append(" FROM RD_LINK_NAME RLN, TMP1, RD_LINK RL");
		sql.append(" WHERE RLN.LINK_PID = RL.LINK_PID");
		sql.append(" AND TMP1.NAME_GROUPID = RLN.NAME_GROUPID");
		sql.append(" AND RLN.U_RECORD != 2");
		sql.append(" AND RL.U_RECORD != 2)");
		sql.append(" SELECT * FROM TMP2");
	}
	
	private void componentSqlForAccurate(StringBuilder sql,JSONArray names){
		sql.append("with tmp1 as ( select name_groupid,name from rd_name where name in (");

		for (int i = 0; i < names.size(); i++) {
			if (i > 0) {
				sql.append(", ");
			}
			sql.append("'" );
			sql.append( names.getString(i) );
			sql.append( "'");
		}

		sql.append(" ) and u_record != 2),");


		sql.append(" TMP2 AS");
		sql.append(" (SELECT /*+ index(r1)*/");
		sql.append(" RLN.LINK_PID PID, TMP1.NAME, RLN.NAME_GROUPID");
		sql.append(" FROM RD_LINK_NAME RLN, TMP1, RD_LINK RL");
		sql.append(" WHERE RLN.LINK_PID = RL.LINK_PID");
		sql.append(" AND TMP1.NAME_GROUPID = RLN.NAME_GROUPID");
		sql.append(" AND RLN.U_RECORD != 2");
		sql.append(" AND RL.U_RECORD != 2)");
		sql.append(" SELECT * FROM TMP2");

	}
	
	public JSONObject searchDataByPid(JSONObject condition) throws Exception {

		if (!condition.containsKey("linkPid")) {
			throw new Exception("未输入道路pid，无法查询道路信息");
		}

		int pid = condition.getInt("linkPid");

		StringBuilder sql = new StringBuilder();

		sql.append(
				"SELECT rl.link_pid pid, rd.name FROM RD_LINK rl, RD_NAME rd, RD_LINK_NAME rln WHERE rln.name_class=1 AND rln.link_pid = rl.link_pid and rd.name_groupid = rln.name_groupId AND rln.u_record !=2");
		sql.append(" AND rl.link_pid = " + pid);
		sql.append(" AND rd.lang_code = 'CHI'");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql.toString());

			resultSet = pstmt.executeQuery();

			TreeMap<String, List<Integer>> classify = new TreeMap<>();

			while (resultSet.next()) {
				int linkPid = resultSet.getInt("pid");

				String nameout = resultSet.getString("name");

				if (classify.containsKey(nameout)) {
					classify.get(nameout).add(linkPid);
				} else {
					List<Integer> pids = new ArrayList<>();
					pids.add(linkPid);
					classify.put(nameout, pids);
				}
			}

			JSONObject result = componetQueryResult(classify);

			return result;
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	public List<RdLink> searchDataByPids(List<Integer> pidList)
			throws Exception {

		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		List<RdLink> linkList = linkSelector.loadByPids(pidList, false);

		return linkList;
	}

	private Set<Integer> getSubRegionId(String adminCode) throws Exception {

		//母库与情报库admincode不一致，转换成情报库admincode进行情报查询
		String adminId = SearchHelp.updateInfoAdminCode(adminCode);

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Set<Integer> subAdmin = new HashSet<>();

		try {

			String sql = "SELECT REGION_ID FROM AD_ADMIN T WHERE (T.ADMIN_ID =? OR T.REGION_ID IN (SELECT ADP.REGION_ID_DOWN REGION_ID FROM AD_ADMIN_PART ADP, AD_ADMIN_GROUP ADG WHERE ADG.REGION_ID_UP IN (SELECT REGION_ID FROM AD_ADMIN WHERE ADMIN_ID =?) AND ADP.GROUP_ID = ADG.GROUP_ID)) AND T.ADMIN_TYPE < 5 AND T.ADMIN_TYPE > 0";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, adminId);
			pstmt.setString(2, adminId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				subAdmin.add(resultSet.getInt("REGION_ID"));
			}

		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return subAdmin;
	}

}
