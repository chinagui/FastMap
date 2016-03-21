package com.navinfo.dataservice.engine.edit.edit.model.selector.rd.branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.engine.edit.edit.model.IRow;
import com.navinfo.dataservice.engine.edit.edit.model.ISelector;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.branch.RdBranchName;

public class RdBranchNameSelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdBranchNameSelector.class);

	private Connection conn;

	public RdBranchNameSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdBranchName name = new RdBranchName();

		String sql = "select * from "+name.tableName()+" where name_id=:1";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				name.setPid(resultSet.getInt("name_id"));

				name.setSeqNum(resultSet.getInt("seq_num"));

				name.setNameGroupid(resultSet.getInt("name_groupid"));
				
				name.setDetailId(resultSet.getInt("detail_id"));
				
				name.setNameClass(resultSet.getInt("name_class"));

				name.setLangCode(resultSet.getString("lang_code"));

				name.setCodeType(resultSet.getInt("code_type"));
				
				name.setName(resultSet.getString("name"));

				name.setPhonetic(resultSet.getString("phonetic"));
				
				name.setVoiceFile(resultSet.getString("voice_file"));

				name.setSrcFlag(resultSet.getInt("src_flag"));

				name.setRowId(resultSet.getString("row_id"));
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

		return name;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_branch_name where detail_id=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdBranchName name = new RdBranchName();

				name.setPid(resultSet.getInt("name_id"));

				name.setSeqNum(resultSet.getInt("seq_num"));

				name.setNameGroupid(resultSet.getInt("name_groupid"));
				
				name.setDetailId(resultSet.getInt("detail_id"));
				
				name.setNameClass(resultSet.getInt("name_class"));

				name.setLangCode(resultSet.getString("lang_code"));

				name.setCodeType(resultSet.getInt("code_type"));
				
				name.setName(resultSet.getString("name"));

				name.setPhonetic(resultSet.getString("phonetic"));
				
				name.setVoiceFile(resultSet.getString("voice_file"));

				name.setSrcFlag(resultSet.getInt("src_flag"));

				name.setRowId(resultSet.getString("row_id"));

				rows.add(name);
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

		return rows;
	}

}
