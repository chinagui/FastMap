package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

public class LuFaceTopoSelector extends AbstractSelector {

	private Connection conn;

	public LuFaceTopoSelector(Connection conn) throws Exception {
		super(LuFaceTopo.class, conn);
		this.conn = conn;
	}

	public List<LuFaceTopo> loadByLinkPid(Integer linkPid, boolean isLock) throws Exception {
		List<LuFaceTopo> luFaceTopos = new ArrayList<LuFaceTopo>();
		String sql = "SELECT a.* FROM lu_face_topo a WHERE a.link_pid =:1 and  a.u_record !=2 ";
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
				LuFaceTopo luFaceTopo = new LuFaceTopo();
				ReflectionAttrUtils.executeResultSet(luFaceTopo, resultSet);
				luFaceTopos.add(luFaceTopo);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return luFaceTopos;
	}

}
