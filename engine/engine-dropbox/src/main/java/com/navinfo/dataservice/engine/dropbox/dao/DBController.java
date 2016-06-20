package com.navinfo.dataservice.engine.dropbox.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;

public class DBController {

	public int addUploadRecord(String fileName, String md5, int fileSize,
			int chunkSize) throws Exception {

		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();

			String autoIncreateSql = "select seq_upload.nextval from dual";

			PreparedStatement pst = conn.prepareStatement(autoIncreateSql);

			ResultSet rs = pst.executeQuery();

			int autoId = -1;

			if (rs.next()) {
				autoId = rs.getInt(1);
			}

			String sql = "insert into dropbox_upload(upload_id,file_name,file_path,md5,file_size,chunk_size) values(:1,:2,:3,:4,:5,:6)";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, autoId);

			pstmt.setString(2, fileName);

			String uploadPath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.uploadPath);

			pstmt.setString(3, uploadPath);

			pstmt.setString(4, md5);

			pstmt.setInt(5, fileSize);

			pstmt.setInt(6, chunkSize);

			pstmt.executeUpdate();

			conn.commit();

			return autoId;
		} catch (Exception e) {
			if (conn != null)
				conn.rollback();
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
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();

			String sql = "select progress,trunc(chunk_size/file_size*100) split from dropbox_upload where upload_id = :1";

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

				sql = "update dropbox_upload set progress = :1 where upload_id= :2";

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
			conn.commit();

		} catch (Exception e) {
			if (conn != null)
				conn.rollback();
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
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();

			String sql = "update dropbox_upload set end_date = sysdate where upload_id = :1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, jobId);

			int rows = pstmt.executeUpdate();

			if (rows == 0) {
				throw new Exception("不存在对应的jobid:" + jobId);
			}
			conn.commit();
		} catch (Exception e) {
			if (conn != null)
				conn.rollback();
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
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();

			JSONObject json = new JSONObject();

			String sql = "select * from dropbox_upload where upload_id = :1";

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
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();

			String sql = "insert into dropbox_upload_chunk(upload_id,chunk_no) values (:1,:2)";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, jobId);

			pstmt.setInt(2, chunkNo);

			pstmt.executeUpdate();
			conn.commit();

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
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

	public List<Integer> getChunkList(int jobId) throws Exception {
		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();

			List<Integer> results = new ArrayList<Integer>();

			String sql = "select distinct chunk_no from dropbox_upload_chunk where upload_id = :1 order by chunk_no";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, jobId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				results.add(resultSet.getInt(1));
			}

			conn.close();

			return results;

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

	/**
	 * @param dbId
	 * @param pid
	 * @param string
	 * @throws Exception 
	 */
	public void insertIxPoiPhoto(int dbId, int pid, String photoId) throws Exception {
		// TODO Auto-generated method stub
		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			conn = DBConnector.getInstance().getConnectionById(dbId);

			List<Integer> results = new ArrayList<Integer>();

			String sql = "insert into ix_poi_photo  (poi_pid,photo_id) values  ("
					+ pid
					+ ",'" + photoId + "')";

			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			conn.close();

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
