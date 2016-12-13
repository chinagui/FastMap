/**
 * 
 */
package com.navinfo.dataservice.engine.meta.tmc.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcArea;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcLine;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcLineTree;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcPoint;
import com.navinfo.navicommons.database.sql.DBUtils;

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

	public TmcLineTree queryTmcTree(JSONArray tmcIds) throws Exception {

		List<TmcLineTree> treeList = new ArrayList<>();

		for (int i=0;i<tmcIds.size();i++) {
			
			TmcLineTree tmcTree = null;

			// 1.查询根据tmcPointId查询对应的tmcLine对象
			TmcLine tmcLine = queryTmcLineByPointId(tmcIds.getInt(i));

			// 2.根据tmcline_id查询line下所有的tmc_point
			List<TmcPoint> tmcPointList = queryTmcPointByLineId(tmcLine.getTmcId());

			JSONArray lineGeo = new JSONArray();

			for (TmcPoint tmcPoint : tmcPointList) {
				JSONArray pointGeo = tmcPoint.getGeometry();

				lineGeo.add(pointGeo);
			}

			tmcLine.setGeometry(lineGeo);

			// 3.根据tmcline_id查询父tmcline
			int upperTmcId = tmcLine.getUplineTmcId();

			List<TmcLine> upTmcLineList = new ArrayList<>();

			while (upperTmcId != 0) {
				TmcLine upperTmcLine = queryTmcLineByTmcLineId(upperTmcId);

				if (upperTmcLine != null) {
					upTmcLineList.add(upperTmcLine);

					upperTmcId = upperTmcLine.getUplineTmcId();
				}
			}

			// 获取TMCAREA数据
			int startAreaTmcId = tmcLine.getAreaTmcId();

			List<TmcArea> upTmcAreaList = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(upTmcLineList)) {
				// upTmcLineList的最后一条数据的upperTmcId是0，代表它的上级是TmcArea
				TmcLine line = upTmcLineList.get(upTmcLineList.size() - 1);

				startAreaTmcId = line.getAreaTmcId();

				while (startAreaTmcId != 0) {
					TmcArea upperTmcArea = queryTmcAreaByTmcAreaId(startAreaTmcId);

					if (upperTmcArea != null) {
						upTmcAreaList.add(upperTmcArea);

						startAreaTmcId = upperTmcArea.getUperTmcId();
					}
				}
			} else {
				// 第一层tmcline向上没有tmcline的情况下使用第一层tmcline的upAreaTmcId
				startAreaTmcId = tmcLine.getAreaTmcId();

				while (startAreaTmcId != 0) {
					TmcArea upperTmcArea = queryTmcAreaByTmcAreaId(startAreaTmcId);

					if (upperTmcArea != null) {
						upTmcAreaList.add(upperTmcArea);

						startAreaTmcId = upperTmcArea.getUperTmcId();
					}
				}
			}

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

				tmcTree = tmcLineTree;
			}
			if (tmcTree == null) {
				tmcTree = tree;
			}
			TmcLineTree copyResultTree = new TmcLineTree();

			copyResultTree.copy(tmcTree);

			for (TmcArea upperArea : upTmcAreaList) {
				TmcLineTree tmcAreaTree = new TmcLineTree(upperArea);

				tmcAreaTree.getChildren().add(copyResultTree);

				copyResultTree = new TmcLineTree();

				copyResultTree.copy(tmcAreaTree);

				tmcTree = copyResultTree;
			}
			if (tmcTree != null) {
				treeList.add(tmcTree);
			}
		}

		// 对树节点进行合并
		TmcLineTree firstTree = treeList.get(0);

		for (int i = 1; i < treeList.size(); i++) {
			TmcLineTree tmpTree = treeList.get(i);

			if (firstTree.equals(tmpTree)) {
				firstTree = addTree2NewTree(firstTree, firstTree.getChildren(), tmpTree.getChildren());
			} else {
				throw new Exception("tmc顶层区域无法合并");
			}
		}

		return firstTree;
	}

	/**
	 * 将两个树结构数据合并为一个新的树，相同层级相同类型的相同tmcId进行合并
	 * 
	 * @param firstTree
	 * @param tmpTree
	 * @return
	 */
	private TmcLineTree addTree2NewTree(TmcLineTree result, List<TmcLineTree> firstTreeChild,
			List<TmcLineTree> tmpTreeChild) {
		for (TmcLineTree firstTree : firstTreeChild) {
			boolean hasAdd = false;
			for (TmcLineTree tmpTree : tmpTreeChild) {
				if (!firstTreeChild.contains(tmpTree)) {
					result.getChildren().add(tmpTree);
					hasAdd = true;
				} else {
					addTree2NewTree(firstTree, firstTree.getChildren(), tmpTree.getChildren());
				}
			}
			if(hasAdd)
			{
				break;
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
				
//				List<TmcPoint> tmcPointList = this.queryTmcPointByLineId(tcmLineId);
				
//				//返回tmc线的几何
//				JSONArray lineGeo = new JSONArray();
//
//				for (TmcPoint tmcPoint : tmcPointList) {
//					JSONArray pointGeo = tmcPoint.getGeometry();
//
//					lineGeo.add(pointGeo);
//				}

//				tmcLine.setGeometry(lineGeo);
				
				tmcLine.setCid(resultSet.getString("cid"));

				tmcLine.setTranslateName(resultSet.getString("TRANSLATE_NAME"));

				tmcLine.setTmcId(resultSet.getInt("TMC_ID"));

				tmcLine.setAreaTmcId(resultSet.getInt("area_tmc_id"));

				tmcLine.setUplineTmcId(resultSet.getInt("upline_tmc_id"));
			}
		} catch (Exception e) {
			throw new Exception("根据tmcline查询父节点失败");
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return tmcLine;
	}

	/**
	 * 根据tmcAreaId查询TmcArea对象
	 * 
	 * @param tcmAreaId
	 * @return
	 * @throws Exception
	 */
	public TmcArea queryTmcAreaByTmcAreaId(int tcmAreaId) throws Exception {
		String sql = "SELECT t1.tmc_id,t1.cid,t1.UPAREA_TMC_ID,t2.TRANSLATE_NAME FROM tmc_area t1 LEFT JOIN TMC_AREA_TRANSLATENAME t2 ON t1.TMC_ID = t2.TMC_ID WHERE t1.TMC_ID =:1";

		PreparedStatement pstmt = null;

		TmcArea tmcArea = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, tcmAreaId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				tmcArea = new TmcArea();

				tmcArea.setCid(resultSet.getInt("cid"));

				tmcArea.setTranslateName(resultSet.getString("TRANSLATE_NAME"));

				tmcArea.setTmcId(resultSet.getInt("TMC_ID"));

				tmcArea.setUperTmcId(resultSet.getInt("UPAREA_TMC_ID"));
			}
		} catch (Exception e) {
			throw new Exception("根据tmcline查询父节点失败");
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return tmcArea;
	}

	public TmcLine queryTmcLineByPointId(int tmcPointId) throws Exception {
		String sql = "with tmp1 as ( select t2.tmc_id,t2.cid,t2.area_tmc_id,t2.upline_tmc_id from tmc_point t1,tmc_line t2 where t1.tmc_id = :1 and t1.LINE_TMC_ID = t2.TMC_ID and t1.u_record !=2 ) select tmp1.*,n.TRANSLATE_NAME from tmp1 left join TMC_LINE_TRANSLATENAME n on tmp1.tmc_id = n.tmc_id and n.NAME_FLAG = 0";

		PreparedStatement pstmt = null;

		TmcLine tmcLine = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, tmcPointId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				tmcLine = new TmcLine();

				tmcLine.setCid(resultSet.getString("cid"));
				
				int tmcLineId = resultSet.getInt("TMC_ID");
				
				String tmcLineName = resultSet.getString("TRANSLATE_NAME");

				tmcLine.setTranslateName(tmcLineName == null?String.valueOf(tmcLineId):tmcLineName);

				tmcLine.setTmcId(tmcLineId);

				tmcLine.setAreaTmcId(resultSet.getInt("area_tmc_id"));

				tmcLine.setUplineTmcId(resultSet.getInt("upline_tmc_id"));
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
		String sql = "WITH TMP1 AS (SELECT T.LINE_TMC_ID, T.GEOMETRY, T.TMC_ID, T.CID, T.AREA_TMC_ID,T.LOCOFF_POS,T.LOCOFF_NEG FROM TMC_POINT T WHERE T.LINE_TMC_ID = :1) SELECT TMP1.*, B.TRANSLATE_NAME FROM TMP1 LEFT JOIN TMC_POINT_TRANSLATENAME B ON TMP1.TMC_ID = B.TMC_ID WHERE B.NAME_FLAG = 1 ";

		PreparedStatement pstmt = null;

		List<TmcPoint> tmcPointList = new ArrayList<>();

		Map<Integer, TmcPoint> tmcPointMap = new HashMap<>();

		TmcPoint tmcPoint = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, tmcLineId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				tmcPoint = new TmcPoint();

				tmcPoint.setCid(resultSet.getString("cid"));

				tmcPoint.setTranslateName(resultSet.getString("TRANSLATE_NAME"));

				tmcPoint.setTmcId(resultSet.getInt("TMC_ID"));

				tmcPoint.setAreaTmcId(resultSet.getInt("area_tmc_id"));

				tmcPoint.setLineTmcId(resultSet.getInt("LINE_TMC_ID"));

				tmcPoint.setLocoffNeg(resultSet.getInt("LOCOFF_NEG"));

				tmcPoint.setLocoffPos(resultSet.getInt("LOCOFF_POS"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONArray pointGeo = geojson.getJSONArray("coordinates");

				tmcPoint.setGeometry(pointGeo);

				tmcPointMap.put(tmcPoint.getTmcId(), tmcPoint);
			}
			// 对TMCPOINT排序
			if (tmcPointMap.size() > 0) {
				TmcPoint firstTmcPoint = tmcPointMap.values().iterator().next();

				tmcPointList.add(0, firstTmcPoint);

				boolean posFlag = true;

				boolean negFlag = true;

				int pos = firstTmcPoint.getLocoffPos();

				int neg = firstTmcPoint.getLocoffNeg();

				while (posFlag) {
					if (tmcPointMap.get(pos) != null) {
						tmcPointList.add(tmcPointList.size(), tmcPointMap.get(pos));

						pos = tmcPointMap.get(pos).getLocoffPos();
					} else {
						posFlag = false;
					}
				}

				while (negFlag) {
					if (tmcPointMap.get(neg) != null) {
						tmcPointList.add(0, tmcPointMap.get(neg));

						neg = tmcPointMap.get(neg).getLocoffNeg();
					} else {
						negFlag = false;
					}
				}
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

				snapshot.setI(Integer.parseInt(tmcLineId));

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

		String sql = "WITH TMP1 AS (SELECT TMC_ID, LOCOFF_POS,LOCOFF_NEG,LOCTABLE_ID,LOC_CODE,GEOMETRY FROM TMC_POINT WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.TMC_ID,A.LOC_CODE,A.GEOMETRY,A.LOCTABLE_ID,A.LOCOFF_POS,A.LOCOFF_NEG,B.TRANSLATE_NAME FROM TMP1 A LEFT JOIN TMC_POINT_TRANSLATENAME B ON A.TMC_ID = B.TMC_ID WHERE B.NAME_FLAG = 1";

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

				snapshot.setI(Integer.parseInt(resultSet.getString("TMC_ID")));

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
				
				m.put("d", resultSet.getString("LOCTABLE_ID"));
				
				m.put("e", resultSet.getInt("LOCOFF_POS"));
				
				m.put("f", resultSet.getInt("LOCOFF_NEG"));
				
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
}
