/**
 * 
 */
package com.navinfo.dataservice.engine.meta.tmc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * @ClassName: TmcSelector
 * @author Zhang Xiaolong
 * @date 2016年11月11日 下午1:32:07
 * @Description: TODO
 */
public class TmcSelector {

	private Connection conn;

	public TmcSelector() {
	}

	public TmcSelector(Connection conn) {
		this.conn = conn;
	}

	public TmcLineTree queryTmcTree(int[] tmcIds) throws Exception {
		
		TmcLineTree result = null;
		
		for (int tmcId : tmcIds) {
			// 1.查询根据tmcPointId查询对应的tmcLine对象
			TmcLine tmcLine = queryTmcLineByPointId(tmcId);

			// 2.根据tmcline_id查询line下所有的tmc_point
			List<TmcPoint> tmcPointList = queryTmcPointByLineId(tmcLine.getTmcId());

			// 3.根据tmcline_id查询父tmcline
			int upperTmcId = tmcLine.getUpLineTmcId();

			List<TmcLine> upTmcLineList = new ArrayList<>();

			while (upperTmcId != 0) {
				TmcLine upperTmcLine = queryTmcLineByTmcLineId(upperTmcId);
				
				if(upperTmcLine != null)
				{
					upTmcLineList.add(upperTmcLine);

					upperTmcId = upperTmcLine.getUpLineTmcId();
				}
			}
			
			//upTmcLineList的最后一条数据的upperTmcId是0，代表它的上级是TmcArea
			upTmcLineList.get(upTmcLineList.size() - 1);

			// 第一层tmcline
			TmcLineTree tree = new TmcLineTree(tmcLine);

			for (TmcPoint point : tmcPointList) {
				TmcLineTree pointTree = new TmcLineTree(point);

				tree.getChildren().add(pointTree);
			}
			
			TmcLineTree copyTree = new TmcLineTree();
			
			copyTree.copy(tree);
			
			for (TmcLine upperLine : upTmcLineList) {
				TmcLineTree tmcLineTree = new TmcLineTree(upperLine);

				tmcLineTree.getChildren().add(copyTree);
				
				copyTree = new TmcLineTree();
					
				copyTree.copy(tmcLineTree);
				
				result = tmcLineTree;
			}
		}
		
		return result;
	}

	public TmcLine queryTmcLineByTmcLineId(int tcmLineId) throws Exception {
		String sql = "select t1.tmc_id,t1.cid,t1.area_tmc_id,t1.UPLINE_TMC_ID,t2.TRANSLATE_NAME from tmc_line t1 left join TMC_LINE_TRANSLATENAME t2 on t1.TMC_ID = t2.TMC_ID where t1.TMC_ID =:1 and t2.NAME_FLAG = 0";

		PreparedStatement pstmt = null;

		TmcLine tmcLine = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, tcmLineId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				tmcLine = new TmcLine();

				tmcLine.setCid(resultSet.getInt("cid"));

				tmcLine.setName(resultSet.getString("TRANSLATE_NAME"));

				tmcLine.setTmcId(resultSet.getInt("TMC_ID"));

				tmcLine.setAreaTmcId(resultSet.getInt("area_tmc_id"));

				tmcLine.setUpLineTmcId(resultSet.getInt("upline_tmc_id"));
			}
		} catch (Exception e) {
			throw new Exception("根据tmcline查询父节点失败");
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return tmcLine;
	}

	public TmcLine queryTmcLineByPointId(int tmcPointId) throws Exception {
		String sql = "with tmp1 as ( select t2.tmc_id,t2.cid,t2.area_tmc_id,t2.upline_tmc_id from tmc_point t1,tmc_line t2 where t1.tmc_id = :1 and t1.LINE_TMC_ID = t2.TMC_ID and t1.u_record !=2 ) select tmp1.*,n.TRANSLATE_NAME from tmp1 left join TMC_LINE_TRANSLATENAME n on tmp1.tmc_id = n.tmc_id where n.NAME_FLAG = 0";

		PreparedStatement pstmt = null;

		TmcLine tmcLine = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, tmcPointId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				tmcLine = new TmcLine();

				tmcLine.setCid(resultSet.getInt("cid"));

				tmcLine.setName(resultSet.getString("TRANSLATE_NAME"));

				tmcLine.setTmcId(resultSet.getInt("TMC_ID"));

				tmcLine.setAreaTmcId(resultSet.getInt("area_tmc_id"));

				tmcLine.setUpLineTmcId(resultSet.getInt("upline_tmc_id"));
			}
		} catch (Exception e) {
			throw new Exception("根据tmcPointId查询TMCLine失败");
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return tmcLine;
	}

	/**
	 * 根据tmcLineId查询TmcPoint
	 * 
	 * @param tmcLineId
	 * @return
	 * @throws Exception
	 */
	public List<TmcPoint> queryTmcPointByLineId(int tmcLineId) throws Exception {
		String sql = "WITH TMP1 AS (SELECT T.LINE_TMC_ID, T.GEOMETRY, T.TMC_ID,T.CID,T.AREA_TMC_ID FROM TMC_POINT T WHERE T.LINE_TMC_ID = :1) SELECT TMP1.*, B.TRANSLATE_NAME FROM TMP1 LEFT JOIN TMC_POINT_TRANSLATENAME B ON TMP1.TMC_ID = B.TMC_ID WHERE B.NAME_FLAG = 1";

		PreparedStatement pstmt = null;

		List<TmcPoint> tmcPointList = new ArrayList<>();

		TmcPoint tmcPoint = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, tmcLineId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				tmcPoint = new TmcPoint();

				tmcPoint.setCid(resultSet.getInt("cid"));

				tmcPoint.setName(resultSet.getString("TRANSLATE_NAME"));

				tmcPoint.setTmcId(resultSet.getInt("TMC_ID"));

				tmcPoint.setAreaTmcId(resultSet.getInt("area_tmc_id"));

				tmcPoint.setLineTmcId(resultSet.getInt("LINE_TMC_ID"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct);

				tmcPoint.setGeo(geometry);

				tmcPointList.add(tmcPoint);
			}
		} catch (Exception e) {
			throw new Exception("根据tmcPointId查询TMCLine失败");
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return tmcPointList;
	}

	public List<SearchSnapshot> queryTmcLine(int x, int y, int z, int gap) throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT line_tmc_id FROM TMC_POINT WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2 group by LINE_TMC_ID), tmp2 as ( select t.LINE_TMC_ID,t.geometry,t.TMC_ID,t.LOC_CODE from tmc_point t,tmp1 where t.line_tmc_id = tmp1.line_tmc_id ) select tmp2.*,B.TRANSLATE_NAME from tmp2 left join TMC_POINT_TRANSLATENAME B on tmp2.TMC_ID = B.TMC_ID WHERE B.NAME_FLAG = 1 ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			Map<String, List<JSONObject>> linePointMap = new HashMap<>();

			while (resultSet.next()) {

				String tmcLineId = resultSet.getString("LINE_TMC_ID");

				List<JSONObject> pointJSONList = null;

				if (linePointMap.containsKey(tmcLineId)) {
					pointJSONList = linePointMap.get(tmcLineId);
				} else {
					pointJSONList = new ArrayList<>();

					linePointMap.put(tmcLineId, pointJSONList);
				}

				JSONObject m = new JSONObject();

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				Geojson.point2Pixel(geojson, z, px, py);

				JSONArray pointGeo = geojson.getJSONArray("coordinates");

				m.put("g", pointGeo);

				m.put("tmcId", resultSet.getInt("tmc_id"));

				m.put("locCode", resultSet.getInt("LOC_CODE"));

				m.put("name", resultSet.getString("TRANSLATE_NAME"));

				pointJSONList.add(m);

			}

			for (Map.Entry<String, List<JSONObject>> entry : linePointMap.entrySet()) {
				String tmcLineId = entry.getKey();

				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setI(tmcLineId);

				snapshot.setT(50);

				List<JSONObject> pointArrayList = entry.getValue();

				JSONArray gArray = new JSONArray();

				JSONArray tmcIdArray = new JSONArray();

				JSONArray locCodeArray = new JSONArray();

				JSONArray nameArray = new JSONArray();

				for (JSONObject obj : pointArrayList) {
					JSONArray pointGeo = obj.getJSONArray("g");

					gArray.add(pointGeo);

					String tmcId = obj.getString("tmcId");

					tmcIdArray.add(Integer.parseInt(tmcId));

					String locCode = obj.getString("locCode");

					locCodeArray.add(Integer.parseInt(locCode));

					String name = obj.getString("name");

					nameArray.add(name);
				}

				snapshot.setG(gArray);

				JSONObject m = new JSONObject();

				m.put("a", tmcIdArray);

				m.put("b", locCodeArray);

				m.put("c", nameArray);

				snapshot.setM(m);

				list.add(snapshot);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return list;
	}

	/**
	 * 渲染接口
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	public List<SearchSnapshot> queryTmcPoint(int x, int y, int z, int gap) throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT TMC_ID, LOC_CODE,GEOMETRY FROM TMC_POINT WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.TMC_ID,A.LOC_CODE,A.GEOMETRY, B.TRANSLATE_NAME FROM TMP1 A LEFT JOIN TMC_POINT_TRANSLATENAME B ON A.TMC_ID = B.TMC_ID WHERE B.NAME_FLAG = 1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			Map<String, Integer> pointSizeMap = new HashMap<>();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setI(resultSet.getString("TMC_ID"));

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("LOC_CODE"));

				m.put("b", resultSet.getString("TRANSLATE_NAME"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				Geojson.point2Pixel(geojson, z, px, py);

				snapshot.setG(geojson.getJSONArray("coordinates"));

				if (pointSizeMap.containsKey(geojson.toString())) {
					int size = pointSizeMap.get(geojson.toString());
					m.put("c", size);
					pointSizeMap.put(geojson.toString(), size + 1);
				} else {
					m.put("c", 0);
					pointSizeMap.put(geojson.toString(), 1);
				}

				snapshot.setM(m);

				snapshot.setT(48);

				list.add(snapshot);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return list;
	}

	private TmcLineTree queryTmcLineTreeByTmcId(int tmcId) throws Exception {
		TmcLineTree tree = new TmcLineTree();

		String tmcIdStr = String.valueOf(tmcId);

		String cid = "";

		if (tmcIdStr.length() == 8) {
			cid = tmcIdStr.substring(0, 2);
		} else if (tmcIdStr.length() == 9) {
			cid = tmcIdStr.substring(0, 3);
		}

		Map<Integer, String> tmcTreeInfoMap = getTopTmcLineInfo(cid);

		for (Map.Entry<Integer, String> entry : tmcTreeInfoMap.entrySet()) {
			int id = entry.getKey();

			String name = entry.getValue();

//			if (tree.getLevel() != 1) {
//				build(tree, tmcTreeInfoMap);
//			} else {
//				tree.setTmcLineId(id);
//
//				tree.setLevel(1);
//
//				tree.setName(name);
//			}
		}

		return tree;
	}

	private void build(TmcLineTree tree, Map<Integer, String> tmcTreeInfoMap) {
		List<TmcLineTree> treeList = getChildren(tree, tmcTreeInfoMap);
		if (CollectionUtils.isNotEmpty(treeList)) {
			for (TmcLineTree childTree : treeList) {
				tree.getChildren().add(childTree);

				build(tree, tmcTreeInfoMap);
			}
		}
	}

	/**
	 * @param tree
	 * @return
	 */
	private List<TmcLineTree> getChildren(TmcLineTree tree, Map<Integer, String> tmcTreeInfoMap) {
//		int tmcLineId = tree.getTmcLineId();
//
//		TmcLineTree childTree = new TmcLineTree();
//
//		childTree.setTmcLineId(tmcLineId + 1);
//
//		childTree.setName(tmcTreeInfoMap.get(tmcLineId + 1));
//
//		childTree.setLevel(tree.getLevel() + 1);

		List<TmcLineTree> treeList = new ArrayList<>();

		//treeList.add(childTree);

		return treeList;
	}

	/**
	 * 根据CID查询tmcline信息
	 * 
	 * @param cid
	 * @return
	 * @throws Exception
	 */
	public Map<Integer, String> getTopTmcLineInfo(String cid) throws Exception {
		Map<Integer, String> tmcInfoMap = new HashMap<>();

		StringBuffer cidStrBuf = new StringBuffer();

		cidStrBuf.append(cid).append("000001,").append(cid).append("000002,").append(cid).append("000003");

		String sql = "select tmc_id,translate_name from TMC_AREA_TRANSLATENAME where tmc_id in(" + cidStrBuf.toString()
				+ ") order by tmc_id ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				tmcInfoMap.put(resultSet.getInt("tmc_id"), resultSet.getString("translate_name"));
			}
		} catch (Exception e) {
			throw new Exception("查询TMC top3层级树失败");
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return tmcInfoMap;
	}
}
