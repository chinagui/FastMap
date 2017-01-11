package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminPart;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * @Title: AdAdminPartSelector.java
 * @Description: AdAdminPart的查询类
 * @author 张小龙
 * @date 2016年4月18日 下午5:30:06
 * @version V1.0
 */
public class AdAdminPartSelector extends AbstractSelector  {

	private Connection conn;

	public AdAdminPartSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(AdAdminPart.class);
	}
	
	public AdAdminPart loadByRegionId(int id, boolean isLock) throws Exception {
		AdAdminPart part = null;

		String sql = "select * from ad_admin_part where region_id_down =:1 and u_record !=2";
		
		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				part = new AdAdminPart();
				ReflectionAttrUtils.executeResultSet(part, resultSet);

			} else {
			}
		} catch (Exception e) {
			
			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return part;
	}

}
