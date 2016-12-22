package com.navinfo.dataservice.engine.meta.svg;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.BLOB;

import org.apache.commons.codec.binary.Base64;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class SvgImageSelector {

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

			String sql = "SELECT * FROM (SELECT A.*, ROWNUM RN FROM (SELECT COUNT(1) OVER(PARTITION BY 1) TOTAL, FILE_NAME, FILE_CONTENT, FORMAT, PANEL FROM SC_VECTOR_MATCH WHERE FILE_NAME LIKE :1) A WHERE ROWNUM <= :2) WHERE RN >= :3";

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
				
				String panel = resultSet.getString("PANEL");

				BLOB blob = (BLOB) resultSet.getBlob("file_content");

				InputStream is = blob.getBinaryStream();
				int length = (int) blob.length();
				byte[] buffer = new byte[length];
				is.read(buffer);
				is.close();

				String fileContent = "data:image/" + format + ";base64,"
						+ new String(Base64.encodeBase64(buffer));

				json.put("fileContent", fileContent);
				
				json.put("panel", panel);

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
}
