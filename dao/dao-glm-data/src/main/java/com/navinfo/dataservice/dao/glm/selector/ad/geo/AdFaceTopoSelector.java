package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class AdFaceTopoSelector extends AbstractSelector {


	private Connection conn;

	public AdFaceTopoSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(AdFaceTopo.class);
	}
	public List<AdFaceTopo> loadByLinkPid(Integer linkPid, boolean isLock) throws Exception {
		
		List<AdFaceTopo> adFaceTopos = new ArrayList<AdFaceTopo>();
		String sql = "SELECT a.* FROM ad_face_topo a WHERE a.link_pid =:1 and  a.u_record !=2 ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				AdFaceTopo adFaceTopo = new AdFaceTopo();
				ReflectionAttrUtils.executeResultSet(adFaceTopo, resultSet);
				adFaceTopos.add(adFaceTopo);
			} 
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);

		}

		return adFaceTopos;
	}


}
