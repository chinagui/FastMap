package com.navinfo.dataservice.engine.meta.pinyin;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.sql.ProcedureBase;


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
	
	/**
	 * @Title: pyConvert
	 * @Description: 转拼音
	 * @param word  
	 * @param adminId  行政区划号
	 * @param isRdName 是否是道路名标识   1 是 默认是否
	 * @return
	 * @throws Exception  String
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月16日 下午5:32:33 
	 */
	public String pyConvert(String word,String adminId,String isRdName) throws Exception {
		CallableStatement cs = null;
		
		String initSql = "{call py_utils_word.init_context_param}";
		
		String sql = "select py_utils_word.convert_hz_tone(:1,    :2,    :3) phonetic from dual";
	
		PreparedStatement pstmt = null;
	
		ResultSet resultSet = null;
	
		String result = "";
	
		Connection conn = null;
	
		try {
	
			conn = DBConnector.getInstance().getMetaConnection();
	
			cs = conn.prepareCall(initSql);
			
			cs.execute();
			
			pstmt = conn.prepareStatement(sql);
	
			pstmt.setString(1, word);
			
			pstmt.setString(2, adminId);
			
			pstmt.setString(3, isRdName);
	
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
			DbUtils.closeQuietly(cs);
	
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
		
	/**
	 * @Title: voiceConvert
	 * @Description: 转语音
	 * @param word
	 * @param phonetic  拼音
	 * @param adminId   行政区划号
	 * @param isRdName  是否是道路名
	 * @return
	 * @throws Exception  String
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月16日 下午5:48:44 
	 */
	public String voiceConvert(String word,String phonetic,String adminId,String isRdName) throws Exception {
		CallableStatement cs = null;
		
		String initSql = "{call py_utils_word.init_context_param}";
		
		String sql = "select py_utils_word.convert_rd_name_voice (:1,    :2,  :3, :4) voicefile from dual";
	
		PreparedStatement pstmt = null;
	
		ResultSet resultSet = null;
	
		String result = "";
	
		Connection conn = null;
	
		try {
	
			conn = DBConnector.getInstance().getMetaConnection();
	
			cs = conn.prepareCall(initSql);
			
			cs.execute();
			
			pstmt = conn.prepareStatement(sql);
	
			pstmt.setString(1, word);
			
			pstmt.setString(2, phonetic);
			
			pstmt.setString(3, adminId);
			
			pstmt.setString(4, isRdName);
	
		
			resultSet = pstmt.executeQuery();
	
			if (resultSet.next()) {
	
				result = resultSet.getString("voicefile");
	
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
			DbUtils.closeQuietly(cs);
	
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
	
	
	/**
	 * @Title: pyVoiceConvert
	 * @Description: 转拼音 + 转语音
	 * @param word
	 * @param phonetic
	 * @param adminId
	 * @param isRdName
	 * @return
	 * @throws Exception  String[]
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月17日 上午9:39:10 
	 */
	public String[] pyVoiceConvert(String word,String phonetic,String adminId,String isRdName) throws Exception {
		CallableStatement cs = null;
		
		String initSql = "{call py_utils_word.init_context_param}";
		
		String sql = "select py_utils_word.convert_hz_tone(:1, :2, :3) phonetic, py_utils_word.convert_rd_name_voice (:4, :5,  :6, :7) voicefile from dual";
	
		PreparedStatement pstmt = null;
	
		ResultSet resultSet = null;
	
		String[] result = new String[2];
	
		Connection conn = null;
	
		try {
	
			conn = DBConnector.getInstance().getMetaConnection();
	
			cs = conn.prepareCall(initSql);
			
			cs.execute();
			
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, word);
			
			pstmt.setString(2, adminId);
			
			pstmt.setString(3, isRdName);
	
			pstmt.setString(4, word);
			
			pstmt.setString(5, phonetic);
			
			pstmt.setString(6, adminId);
			
			pstmt.setString(7, isRdName);
	
		
			resultSet = pstmt.executeQuery();
	
			if (resultSet.next()) {
	
				result[0] = resultSet.getString("phonetic");
	
				result[1] = resultSet.getString("voicefile");
	
			} 
			else {
				return result;
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
			DbUtils.closeQuietly(cs);
	
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
	
				}
			}
		}
		
		return result;
	}
		
	/**
	 * @Title: engConvert
	 * @Description: 转英文
	 * @param word
	 * @param phonetic
	 * @param adminId
	 * @param isRdName
	 * @return
	 * @throws Exception  String
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月18日 下午2:56:11 
	 */
	public String engConvert(String word,String adminId) throws Exception {
		CallableStatement cs = null;
		
		String initSql = "{call py_utils_word.init_context_param}";
		
		//String sql = "select py_utils_word.convert_rd_name_voice (:1,    :2,  :3, :4) voicefile from dual";
		//String phonetic,
		//,String isRdName
		String phonetic = 	pyConvert(word, adminId, null);
		
		String sqlEng = "SELECT PY_UTILS_WORD.CONVERT_BASE_ENG(:1,:2,:3,:4) engName FROM DUAL";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		String result = "";

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();

			cs = conn.prepareCall(initSql);
			
			cs.execute();
			
			pstmt = conn.prepareStatement(sqlEng);

			pstmt.setString(1, word);
			
			pstmt.setString(2, phonetic);
			
			pstmt.setString(3, null);
			
			pstmt.setString(4, "CHI");
		
			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				result = resultSet.getString("engName");
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
			DbUtils.closeQuietly(cs);

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
		
	public String[] autoConvert(String word) throws Exception {
//		String sql = "select py_utils_word.conv_to_english_mode_voicefile(:1,      null,      null,      null) voicefile ,  py_utils_word.convert_hz_tone(:2,    null,    null) phonetic from dual";
		String sql = " select PY_UTILS_WORD.CONVERT_HZ_TONE(:2, NULL, NULL) phonetic,"
				+ "py_utils_word.convert_rd_name_voice(:1,null,null,null) voicefile"
				+ "   from dual ";
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		String[] result = new String[2];

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();
			
			String initSql = "BEGIN PY_UTILS_WORD.C_CONVERT_NUMBER:= 0;END;";
			ProcedureBase procedureBase = new ProcedureBase(conn);
	        procedureBase.callProcedure(initSql);

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, word);

			pstmt.setString(2, word);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				result[0] = resultSet.getString("phonetic");

				result[1] = resultSet.getString("voicefile");

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
		CallableStatement cs = null;
		
		String initSql = "{call py_utils_word.init_context_param}";
		
		String sql = "select py_utils_word.convert_hz_tone(:1,    null,    null) phonetic from dual";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		String result = "";

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();

			cs = conn.prepareCall(initSql);
			
			cs.execute();
			
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
			DbUtils.closeQuietly(cs);

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
