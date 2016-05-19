package com.navinfo.dataservice.dao.glm.selector.ad.zone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminGroup;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminPart;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;

/**
 * @Title: AdAdminGroupSelector.java
 * @Description: 行政区划代表点层级查询类
 * @author 张小龙
 * @date 2016年4月18日 下午5:18:50
 * @version V1.0
 */
public class AdAdminGroupSelector implements ISelector {

	private static Logger logger = Logger.getLogger(AdFaceSelector.class);

	private Connection conn;

	public AdAdminGroupSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		AdAdminGroup group = new AdAdminGroup();

		String sql = "select * from " + group.tableName() + " where group_id =:1 and  u_record !=2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				group.setPid(resultSet.getInt("group_id"));
				
				group.setRegionIdUp(resultSet.getInt("region_id_up"));
				
				group.setRowId(resultSet.getString("row_id"));
				
				// ad_admin_part
				List<IRow> adAdminPart = new AdAdminPartSelector(conn).loadRowsByParentId(group.getPid(), isLock);

				group.setParts(adAdminPart);

				for (IRow row : adAdminPart) {
					AdAdminPart obj = (AdAdminPart) row;

					group.adAdminPartMap.put(obj.rowId(), obj);
				}

			} else {
				throw new DataNotFoundException("数据不存在");
			}
		} catch (Exception e) {
			
			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				
			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {
				
			}

		}

		return group;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}

}
