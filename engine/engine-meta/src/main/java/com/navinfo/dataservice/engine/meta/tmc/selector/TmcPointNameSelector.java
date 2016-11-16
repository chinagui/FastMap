/**
 * 
 */
package com.navinfo.dataservice.engine.meta.tmc.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.engine.meta.tmc.model.TmcPointName;
import com.navinfo.navicommons.database.sql.DBUtils;

/** 
* @ClassName: TmcLineNameSelector 
* @author Zhang Xiaolong
* @date 2016年11月16日 下午2:25:56 
* @Description: TODO
*/
public class TmcPointNameSelector {
	private Connection conn;

	public TmcPointNameSelector() {
	}

	public TmcPointNameSelector(Connection conn) {
		this.conn = conn;
	}
	
	public List<TmcPointName> loadRowsByParentId(int tmcId) throws Exception
	{
		List<TmcPointName> tmcLineNames = new ArrayList<TmcPointName>();

		String sql = "select NAME_FLAG,TRANS_LANG,TRANSLATE_NAME,PHONETIC from TMC_POINT_TRANSLATENAME where tmc_id = :1 and u_record　!=2 order by name_flag";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, tmcId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				TmcPointName pointName = new TmcPointName();
				
				pointName.setTmcId(tmcId);
				
				pointName.setNameFlag(resultSet.getInt("NAME_FLAG"));
				
				pointName.setPhonetic(resultSet.getString("PHONETIC"));
				
				pointName.setTransLang(resultSet.getString("TRANS_LANG"));
				
				pointName.setName(resultSet.getString("TRANSLATE_NAME"));

				tmcLineNames.add(pointName);
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
