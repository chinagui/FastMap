package com.navinfo.dataservice.dao.glm.selector.rd.speedbump;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

/**
 * @Title: RdSpeedbumpSelector.java
 * @Description: 减速带查询
 * @author zhangyt
 * @date: 2016年8月5日 下午1:58:34
 * @version: v1.0
 */
public class RdSpeedbumpSelector extends AbstractSelector {

	private Connection conn;

	public RdSpeedbumpSelector(Connection conn) throws Exception {
		super(RdSpeedbump.class, conn);
		this.conn = conn;
	}

	public List<RdSpeedbump> loadByLinkPid(int linkPid, boolean isLock) throws Exception {
		List<RdSpeedbump> speedbumps = new ArrayList<RdSpeedbump>();
		String sql = "select * from rd_speedbump where link_pid = :1 and u_record != 2";
		if (isLock) {
			sql += " for update no wait";
		}
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, linkPid);
			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				RdSpeedbump speedbump = new RdSpeedbump();
				ReflectionAttrUtils.executeResultSet(speedbump, resultSet);
				speedbumps.add(speedbump);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return speedbumps;
	}

}
