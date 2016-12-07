/**
 * 
 */
package com.navinfo.dataservice.engine.meta.tmc.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.engine.meta.tmc.model.TmcLineName;
import com.navinfo.navicommons.database.sql.DBUtils;

/** 
* @ClassName: TmcLineNameSelector 
* @author Zhang Xiaolong
* @date 2016年11月16日 下午2:25:56 
* @Description: TODO
*/
public class TmcLineNameSelector {
	private Connection conn;

	public TmcLineNameSelector() {
	}

	public TmcLineNameSelector(Connection conn) {
		this.conn = conn;
	}
	
	public List<TmcLineName> loadRowsByParentId(int tmcId) throws Exception
	{
		List<TmcLineName> tmcLineNames = new ArrayList<TmcLineName>();

		String sql = "select NAME_FLAG,TRANS_LANG,TRANSLATE_NAME,PHONETIC from TMC_LINE_TRANSLATENAME where tmc_id = :1 and U_RECORD !=2 order by name_flag";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, tmcId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				TmcLineName lineName = new TmcLineName();
				
				lineName.setTmcId(tmcId);
				
				lineName.setNameFlag(resultSet.getInt("NAME_FLAG"));
				
				lineName.setPhonetic(resultSet.getString("PHONETIC"));
				
				lineName.setTransLang(resultSet.getString("TRANS_LANG"));
				
				lineName.setTranslateName(resultSet.getString("TRANSLATE_NAME"));

				tmcLineNames.add(lineName);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return tmcLineNames;
	}
}
