package com.navinfo.dataservice.dao.glm.selector.rd.gate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class RdGateSelector extends AbstractSelector {
		
	private Connection conn;
	
	public RdGateSelector(Connection conn) {
		super(conn);
		this.setCls(RdGate.class);
		this.conn = conn;
	}

	/**
	 * 根据linkPid加载gate
	 * @param linkPid
	 * @return
	 * @throws Exception
	 */
	public List<RdGate> loadByLink(int linkPid) throws Exception {
		
		List<RdGate> rows = new ArrayList<RdGate>();
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		try {
			String sql = "SELECT pid FROM rd_gate WHERE in_link_pid=:1 or out_link_pid=:1";
			
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				AbstractSelector abSelector = new AbstractSelector(RdGate.class,conn);
				RdGate rdGate = (RdGate) abSelector.loadById(resultSet.getInt("pid"), false);
				rows.add(rdGate);
			}
			
			return rows;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
}
