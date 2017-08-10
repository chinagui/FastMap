package com.navinfo.dataservice.engine.meta.patternimage;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.BLOB;

import org.apache.commons.codec.binary.Base64;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class PatternImageSelector {

	public JSONObject searchByName(String name, int pageSize, int pageNum)
			throws Exception {

		JSONObject result = new JSONObject();

		JSONArray array = new JSONArray();

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();

			int total = 0;

			if (name.length() == 0) {
				result.put("total", total);

				result.put("data", array);

				return result;
			}

			String sql = "SELECT *   FROM (SELECT a.*, rownum rn           FROM (select count(1) over(partition by 1) total,file_name,file_content,format                 from sc_model_match_g                  where file_name like :1) a          WHERE rownum <= :2)  WHERE rn >= :3";

			int startRow = pageNum * pageSize + 1;

			int endRow = (pageNum + 1) * pageSize;

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, name + "%");

			pstmt.setInt(2, endRow);

			pstmt.setInt(3, startRow);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				JSONObject json = new JSONObject();

				String fileName = resultSet.getString("file_name");

				json.put("fileName", fileName);

				String format = resultSet.getString("format");

				BLOB blob = (BLOB) resultSet.getBlob("file_content");

				InputStream is = blob.getBinaryStream();
				int length = (int) blob.length();
				byte[] buffer = new byte[length];
				is.read(buffer);
				is.close();

				String fileContent = "data:image/" + format + ";base64,"
						+ new String(Base64.encodeBase64(buffer));

				json.put("fileContent", fileContent);

				if (total == 0) {
					total = resultSet.getInt("total");
				}

				array.add(json);
			}

			result.put("total", total);

			result.put("data", array);

			return result;
		} catch (Exception e) {

			throw e;

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

	}

	public byte[] getById(String id) throws Exception {

		String sql = "select file_name,file_content from sc_model_match_g where file_name = :1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				BLOB blob = (BLOB) resultSet.getBlob("file_content");

				InputStream is = blob.getBinaryStream();
				int length = (int) blob.length();
				byte[] buffer = new byte[length];
				is.read(buffer);
				is.close();

				return buffer;

			}

		} catch (Exception e) {

			throw e;

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

		return new byte[0];
	}

	/**
	 * 检查是否有可下载的
	 * 
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public boolean checkUpdate(String date) throws Exception {
		String sql = "select null from sc_model_match_g where b_type in ('2D','3D') and imp_date > to_date(:1,'yyyymmddhh24miss')";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, date);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				return true;
			}

		} catch (Exception e) {

			throw e;

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

		return false;
	}

	public static void main(String[] args) throws Exception {

		PatternImageSelector selector = new PatternImageSelector();

		System.out.println(selector.searchByName("", 1, 10));
	}
}
