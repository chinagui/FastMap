package com.navinfo.dataservice.engine.dropbox.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.engine.dao.DBConnector;

import net.sf.json.JSONObject;
import oracle.jdbc.OraclePreparedStatement;

public class DBController {

	public int addUploadRecord(String fileName, String md5, int fileSize,
			int chunkSize) throws Exception {

		Connection conn = null;

		OraclePreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			conn = DBConnector.getInstance().getConnection();

			String sql = "insert into dropbox_upload(job_id,file_name,file_path,md5,file_size,chunk_size) values(seq_upload.nextval,:1,:2,:3,:4,:5) returning job_id into :6";

			pstmt = (OraclePreparedStatement) conn.prepareStatement(sql);

			pstmt.setString(1, fileName);
			
			String uploadPath = SystemConfigFactory.getSystemConfig().getValue(PropConstant.uploadPath);

			pstmt.setString(2, uploadPath);

			pstmt.setString(3, md5);

			pstmt.setInt(4, fileSize);

			pstmt.setInt(5, chunkSize);

			pstmt.registerReturnParameter(6, Types.INTEGER);

			pstmt.executeUpdate();

			resultSet = pstmt.getResultSet();

			resultSet.next();

			int jobId = resultSet.getInt("job_id");

			return jobId;
		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}

	}

	public void updateProgress(int jobId) throws Exception {
		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			conn = DBConnector.getInstance().getConnection();

			String sql = "select progress,trunc(chunk_size/file_size*100) split from dropbox_upload where job_id = :1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, jobId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				int progress = resultSet.getInt("progress");

				int split = resultSet.getInt("split");

				if ((split + progress) > 100) {
					progress = 100;
				} else {
					progress = split + progress;
				}

				sql = "update dropbox_upload set progress = :1 where job_id= :2";

				PreparedStatement pstmt2 = null;

				try {
					pstmt2 = conn.prepareStatement(sql);

					pstmt2.setInt(1, progress);

					pstmt2.setInt(2, jobId);

					pstmt2.executeUpdate();
				} catch (Exception e) {
					throw e;
				} finally {
					if (pstmt != null) {
						pstmt.close();
					}
				}

			} else {
				throw new Exception("不存在对应的jobid:" + jobId);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

	public void updateUploadEndDate(int jobId) throws Exception {

		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			conn = DBConnector.getInstance().getConnection();

			String sql = "update dropbox_upload set end_date = sysdate where job_id = :1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, jobId);

			int rows = pstmt.executeUpdate();

			if (rows == 0) {
				throw new Exception("不存在对应的jobid:" + jobId);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

	public JSONObject getUploadInfo(int jobId) throws Exception {

		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			conn = DBConnector.getInstance().getConnection();

			JSONObject json = new JSONObject();

			String sql = "select * from dropbox_upload where job_id = :1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, jobId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				String fileName = resultSet.getString("file_name");

				String filePath = resultSet.getString("file_path");

				String md5 = resultSet.getString("md5");

				json.put("fileName", fileName);

				json.put("filePath", filePath);

				json.put("md5", md5);

			} else {
				throw new Exception("不存在对应的jobid:" + jobId);
			}

			return json;

		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

	public void insertChunk(int jobId, int chunkNo) throws Exception {

		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			conn = DBConnector.getInstance().getConnection();

			String sql = "insert into dropbox_upload_chunk(job_id,chunk_no) values (:1,:2)";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, jobId);

			pstmt.setInt(2, chunkNo);

			pstmt.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

	public String getChunkList(int jobId) throws Exception {
		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			conn = DBConnector.getInstance().getConnection();

			StringBuilder sb = new StringBuilder("[");

			String sql = "select listagg(chunk_no,',') within group(order by chunk_no) nos from (select distinct chunk_no from dropbox_upload_chunk where job_id = :1)";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, jobId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				sb.append(resultSet.getString(1));
			}

			sb.append("]");

			conn.close();

			return sb.toString();

		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

}
