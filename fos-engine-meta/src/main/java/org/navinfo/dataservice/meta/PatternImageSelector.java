package org.navinfo.dataservice.meta;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.BLOB;

import org.apache.commons.codec.binary.Base64;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.db.OracleAddress;

public class PatternImageSelector {

	private Connection conn;
	
	public PatternImageSelector(Connection conn){
		this.conn = conn;
	}

	public JSONArray searchByName(String name, int pageSize, int pageNum) throws Exception{

		JSONArray array = new JSONArray();
		
		String sql = "SELECT *   FROM (SELECT a.*, rownum rn           FROM (select file_name,file_content,format                 from sc_model_match_g                  where file_name like :1) a          WHERE rownum <= :2)  WHERE rn >= :3";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		int startRow = pageNum * pageSize + 1;

		int endRow = (pageNum+1) * pageSize;
		
		try {
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
				
				BLOB blob = (BLOB)resultSet.getBlob("file_content");
				
				InputStream is = blob.getBinaryStream();
				int length = (int) blob.length();
				byte[] buffer = new byte[length];
				is.read(buffer);
				is.close();
				
				String fileContent = "data:image/"+format+";base64," + new String(Base64.encodeBase64(buffer));
				
				json.put("fileContent", fileContent);

				array.add(json);
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

		return array;

	}
	
	public byte[] getById(String id) throws Exception{

		String sql = "select file_name,file_content from sc_model_match_g where file_name = :1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				BLOB blob = (BLOB)resultSet.getBlob("file_content");
				
				InputStream is = blob.getBinaryStream();
				int length = (int) blob.length();
				byte[] buffer = new byte[length];
				is.read(buffer);
				is.close();
				
				return buffer;
				
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

		return null;
	}
	
	/**
	 * 检查是否有可下载的
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public boolean checkUpdate(String date) throws Exception{
		String sql = "select null from sc_model_match_g where update_time > to_date(:1,'yyyymmddhh24miss')";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, date);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				return true;
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

		return false;
	}
	
	public static void main(String[] args) throws Exception{
		
		String username1 = "mymeta3";
		
		String password1 ="mymeta3";
		
		int port1 =1521;
		
		String ip1 = "192.168.4.131";
		
		String serviceName1 = "orcl";
		
		OracleAddress oa1 = new OracleAddress(username1,password1,port1,ip1,serviceName1);
		
		PatternImageSelector selector = new PatternImageSelector(oa1.getConn());
		
		System.out.println(selector.searchByName("03513112", 1, 0));
	}
}
