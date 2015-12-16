package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkName;

public class RdLinkNameSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdLinkNameSelector.class);

	private Connection conn;

	public RdLinkNameSelector(Connection conn) {
		this.conn = conn;

	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		StringBuilder sb = new StringBuilder(
				"select a.*,b.name from rd_link_name a,rd_name b where link_pid =:1 and a.name_groupid=b.name_groupid(+) and b.lang_code(+)='CHI'");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		List<IRow> list = new ArrayList<IRow>();

		try {

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLinkName name = new RdLinkName();

				name.setNameClass(resultSet.getInt("name_class"));

				name.setNameGroupid(resultSet.getInt("name_groupid"));

				name.setNameType(resultSet.getInt("name_type"));

				name.setSeqNum(resultSet.getInt("seq_num"));

				name.setLinkPid(id);

				name.setRowId(resultSet.getString("row_id"));
				
				name.setInputTime(resultSet.getString("input_time"));
				
				name.setSrcFlag(resultSet.getInt("src_flag"));
				
				name.setRouteAtt(resultSet.getInt("route_att"));
				
				name.setCode(resultSet.getInt("code"));
				
				name.setName(resultSet.getString("name"));

				list.add(name);
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
		return list;
	}

}
