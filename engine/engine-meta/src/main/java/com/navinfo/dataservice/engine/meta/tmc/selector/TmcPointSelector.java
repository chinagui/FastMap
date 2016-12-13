/**
 * 
 */
package com.navinfo.dataservice.engine.meta.tmc.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcLine;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcPoint;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * @ClassName: TmcLineSelector
 * @author Zhang Xiaolong
 * @date 2016年11月16日 下午2:03:30
 * @Description: TODO
 */
public class TmcPointSelector {
	private Connection conn;

	public TmcPointSelector() {
	}

	public TmcPointSelector(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 根据TMC ID查询TMC对象
	 * 
	 * @param tmcPointId
	 * @return
	 * @throws Exception
	 */
	public TmcPoint loadByTmcPointId(int tmcPointId) throws Exception {
		TmcPoint tmcPoint = null;

		String sql = "SELECT LOCTABLE_ID, JUNC_LOCCODE,LOC_CODE, TYPE_CODE, IN_POS, IN_NEG, OUT_POS, OUT_NEG, PRESENT_POS, PRESENT_NEG, LOCOFF_POS, LOCOFF_NEG, LINE_TMC_ID, AREA_TMC_ID,NEIGHBOUR_BOUND, NEIGHBOUR_TABLE, URBAN, INTERUPT_ROAD, GEOMETRY, EDIT_FLAG, CID FROM TMC_POINT WHERE TMC_ID = :1 AND U_RECORD !=2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, tmcPointId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				tmcPoint = new TmcPoint();
				
				tmcPoint.setTmcId(tmcPointId);
				
				tmcPoint.setAreaTmcId(resultSet.getInt("AREA_TMC_ID"));
				
				tmcPoint.setCid(resultSet.getString("CID"));
				
				tmcPoint.setLocCode(resultSet.getInt("LOC_CODE"));
				
				tmcPoint.setJuncLoccode(resultSet.getInt("JUNC_LOCCODE"));
				
				tmcPoint.setLocoffNeg(resultSet.getInt("LOCOFF_NEG"));
				
				tmcPoint.setLocoffPos(resultSet.getInt("LOCOFF_POS"));
				
				tmcPoint.setEditFlag(resultSet.getInt("EDIT_FLAG"));
				
				//Geometry几何
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONArray pointGeo = geojson.getJSONArray("coordinates");
				
				tmcPoint.setGeometry(pointGeo);
				
				tmcPoint.setInNeg(resultSet.getInt("IN_NEG"));
				
				tmcPoint.setInPos(resultSet.getInt("IN_POS"));
				
				tmcPoint.setInteruptRoad(resultSet.getInt("INTERUPT_ROAD"));
				
				tmcPoint.setLoctableId(resultSet.getInt("LOCTABLE_ID"));
				
				tmcPoint.setNeighbourBound(resultSet.getString("NEIGHBOUR_BOUND"));
				
				tmcPoint.setNeighbourTable(resultSet.getInt("NEIGHBOUR_TABLE"));
				
				tmcPoint.setLineTmcId(resultSet.getInt("LINE_TMC_ID"));
				
				tmcPoint.setUrban(resultSet.getInt("URBAN"));
				
				tmcPoint.setInteruptRoad(resultSet.getInt("INTERUPT_ROAD"));
				
				tmcPoint.setTypeCode(resultSet.getString("TYPE_CODE"));
				
				tmcPoint.setOutNeg(resultSet.getInt("OUT_NEG"));
				
				tmcPoint.setOutPos(resultSet.getInt("OUT_POS"));
				
				tmcPoint.setPresentNeg(resultSet.getInt("PRESENT_NEG"));
				
				tmcPoint.setPresentPos(resultSet.getInt("PRESENT_POS"));
				
				TmcSelector selector = new TmcSelector(this.conn);
				
				TmcLine line = selector.queryTmcLineByPointId(tmcPointId);
				
				tmcPoint.setLineGeometry(line.getGeometry());
				
				// 获取LINK对应的关联数据 rd_link_name
				tmcPoint.setNames(new TmcPointNameSelector(conn).loadRowsByParentId(tmcPoint.getTmcId()));

			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return tmcPoint;
	}
}
