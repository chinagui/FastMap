package com.navinfo.dataservice.engine.meta.pinyin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;


public class PinyinConverter {

	public String[] convert(String word) throws Exception {

		String sql = "select py_utils_word.conv_to_english_mode_voicefile(:1,      null,      null,      null) voicefile ,  py_utils_word.convert_hz_tone(:2,    null,    null) phonetic from dual";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		String[] result = new String[2];

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, word);

			pstmt.setString(2, word);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				result[0] = resultSet.getString("voicefile");

				result[1] = resultSet.getString("phonetic");

			} else {
				return null;
			}
		} catch (Exception e) {

			throw new Exception(e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {

				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}

		}

		return result;
	}

	public String convertHz(String word) throws Exception {

		String sql = "select py_utils_word.convert_hz_tone(:1,    null,    null) phonetic from dual";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		String result = "";

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, word);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				result = resultSet.getString("phonetic");

			} else {
				return "";
			}
		} catch (Exception e) {

			throw new Exception(e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {

				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}

		}
		
		if (StringUtils.isEmpty(result)){
			return "";
		}
		return result;
	}
	
	public static void main(String[] args) throws Exception {

		PinyinConverter py = new PinyinConverter();
		
		String res = py.convertHz("1号楼");
		
		System.out.println(res);

//		System.out.println(res[0]);
//
//		System.out.println(res[1]);
	}
}
