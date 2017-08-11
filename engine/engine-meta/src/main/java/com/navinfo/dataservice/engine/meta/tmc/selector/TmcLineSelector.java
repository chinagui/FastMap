/**
 * 
 */
package com.navinfo.dataservice.engine.meta.tmc.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.engine.meta.tmc.model.TmcLine;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcPoint;
import net.sf.json.JSONArray;

/**
 * @ClassName: TmcLineSelector
 * @author Zhang Xiaolong
 * @date 2016年11月16日 下午2:03:30
 * @Description: TODO
 */
public class TmcLineSelector {
	private Connection conn;

	public TmcLineSelector() {
	}

	public TmcLineSelector(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 根据TMC ID查询TMC对象
	 * 
	 * @param tmcLineId
	 * @return
	 * @throws Exception
	 */
	public TmcLine loadByTmcLineId(int tmcLineId) throws Exception {
		TmcLine tmcLine = null;

		String sql = "select LOC_CODE,CID,LOCTABLE_ID,TYPE_CODE,SEQ_NUM,LOCOFF_POS,LOCOFF_NEG,UPLINE_TMC_ID,AREA_TMC_ID from TMC_LINE where TMC_ID = :1 and U_RECORD !=2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, tmcLineId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				tmcLine = new TmcLine();
				
				tmcLine.setTmcId(tmcLineId);
				
				tmcLine.setAreaTmcId(resultSet.getInt("AREA_TMC_ID"));
				
				tmcLine.setLoctableId(resultSet.getString("LOCTABLE_ID"));
				
				tmcLine.setCid(resultSet.getString("CID"));
				
				tmcLine.setLocCode(resultSet.getInt("LOC_CODE"));
				
				tmcLine.setSeqNum(resultSet.getInt("SEQ_NUM"));
				
				tmcLine.setLocoffNeg(resultSet.getInt("LOCOFF_NEG"));
				
				tmcLine.setLocoffPos(resultSet.getInt("LOCOFF_POS"));

				tmcLine.setTypeCode(resultSet.getString("TYPE_CODE"));
				
				tmcLine.setUplineTmcId(resultSet.getInt("UPLINE_TMC_ID"));
				
				TmcSelector selector = new TmcSelector(conn);
				
				List<TmcPoint> tmcPointList = selector.queryTmcPointByLineId(tmcLineId);

				JSONArray lineGeo = new JSONArray();

				for (TmcPoint tmcPoint : tmcPointList) {
					JSONArray pointGeo = tmcPoint.getGeometry();

					lineGeo.add(pointGeo);
				}
				
				tmcLine.setGeometry(lineGeo);
				
				// 获取LINK对应的关联数据 rd_link_name
				tmcLine.setNames(new TmcLineNameSelector(conn).loadRowsByParentId(tmcLine.getTmcId()));

			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return tmcLine;
	}
}
