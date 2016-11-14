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

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
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
	
	public void queryTmcTree(int [] tmcId)
	{
		String sql = "select a.line_tmc_id,a.tmc_id,b.TRANSLATE_NAME from tmc_point a,TMC_LINE_TRANSLATENAME b where a.LINE_TMC_ID = b.tmc_id group by a.line_tmc_id,a.tmc_id,b.TRANSLATE_NAME";
	}
	
	public List<SearchSnapshot> queryTmcLine(int x,int y,int z,int gap) throws Exception
	{
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
			
			Map<String,List<JSONObject>> linePointMap = new HashMap<>();

			while (resultSet.next()) {
				
				String tmcLineId = resultSet.getString("LINE_TMC_ID");
				
				List<JSONObject> pointJSONList = null;
				
				if(linePointMap.containsKey(tmcLineId))
				{
					pointJSONList = linePointMap.get(tmcLineId);
				}
				else
				{
					pointJSONList = new ArrayList<>();
					
					linePointMap.put(tmcLineId, pointJSONList);
				}
				
				JSONObject m = new JSONObject();
				
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				//Geojson.point2Pixel(geojson, z, px, py);
				
				JSONArray pointGeo = geojson.getJSONArray("coordinates");
				
				m.put("g", pointGeo);
				
				m.put("tmcId", resultSet.getInt("tmc_id"));
				
				m.put("locCode", resultSet.getInt("LOC_CODE"));

				m.put("name", resultSet.getString("TRANSLATE_NAME"));

				pointJSONList.add(m);
				
			}
			
		for(Map.Entry<String, List<JSONObject>> entry : linePointMap.entrySet())
		{
			String tmcLineId = entry.getKey();
			
			SearchSnapshot snapshot = new SearchSnapshot();

			snapshot.setI(tmcLineId);
			
			snapshot.setT(48);
			
			List<JSONObject> pointArrayList = entry.getValue();
			
			JSONArray gArray = new JSONArray();
			
			JSONArray tmcIdArray = new JSONArray();
			
			JSONArray locCodeArray = new JSONArray();
			
			JSONArray nameArray = new JSONArray();
			
			for(JSONObject obj : pointArrayList)
			{
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

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setI(resultSet.getString("TMC_ID"));

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("LOC_CODE"));

				m.put("b", resultSet.getString("TRANSLATE_NAME"));

				snapshot.setM(m);

				snapshot.setT(48);

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				Geojson.point2Pixel(geojson, z, px, py);

				snapshot.setG(geojson.getJSONArray("coordinates"));

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
