package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoiPart;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

/**
 * @Title: IxSamepoiPartSelector.java
 * @Description: POI同一关系组成表
 * @author zhangyt
 * @date: 2016年8月26日 上午10:33:09
 * @version: v1.0
 */
public class IxSamepoiPartSelector extends AbstractSelector {

	public IxSamepoiPartSelector(Connection conn) {
		super(IxSamepoiPart.class, conn);
	}

	public List<IRow> loadByPoiPid(int poiPid, boolean isLock) throws Exception {
		List<IRow> list = new ArrayList<IRow>();
		String sql = "SELECT * FROM IX_SAMEPOI_PART WHERE POI_PID = :1 AND U_RECORD != 2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = super.getConn().prepareStatement(sql);
		pstmt.setInt(1, poiPid);
		ResultSet resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			IxSamepoiPart part = new IxSamepoiPart();
			ReflectionAttrUtils.executeResultSet(part, resultSet);
			list.add(part);
		}

		return list;
	}

}
