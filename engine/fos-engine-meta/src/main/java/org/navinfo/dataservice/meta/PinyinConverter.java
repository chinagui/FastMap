package org.navinfo.dataservice.meta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.commons.db.OracleAddress;

public class PinyinConverter {

	private Connection conn;
	
	public PinyinConverter(Connection conn){
		this.conn = conn;
	}
	
	public String[] convert(String word) throws Exception{
		
		String sql = "select py_utils_word.conv_to_english_mode_voicefile(:1,      null,      null,      null) voicefile ,  py_utils_word.convert_hz_tone(:2,    null,    null) phonetic from dual";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		String[] result = new String[2];
		
		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, word);
			
			pstmt.setString(2, word);
			
			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				result[0] = resultSet.getString("voicefile");
				
				result[1] = resultSet.getString("phonetic");

			}
			else{
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
			

		}
		
		return result;
	}
	
	public static void main(String[] args) throws Exception{
		
		String username1 = "mymeta3";
		
		String password1 ="mymeta3";
		
		int port1 =1521;
		
		String ip1 = "192.168.4.131";
		
		String serviceName1 = "orcl";
		
		OracleAddress oa1 = new OracleAddress(username1,password1,port1,ip1,serviceName1);
		
		PinyinConverter py = new PinyinConverter(oa1.getConn());
		
		String[] res = py.convert("北京市");
				
		System.out.println(res[0]);
		
		System.out.println(res[1]);
	}
}
