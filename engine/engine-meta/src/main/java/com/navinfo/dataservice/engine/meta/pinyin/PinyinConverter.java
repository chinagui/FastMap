package com.navinfo.dataservice.engine.meta.pinyin;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;


public class PinyinConverter {

	 private static final Logger logger = Logger.getLogger(PinyinConverter.class);
	
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
	
			logger.error("转拼音出错",new Exception(e));
	
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
			
			if(phonetic == null || StringUtils.isEmpty(phonetic)){
				phonetic = pyConvert(word, adminId, isRdName);
			}
			System.out.println(phonetic);
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
			logger.error("转语音出错",new Exception(e));
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
			
			if(phonetic == null || StringUtils.isEmpty(phonetic)){
				phonetic = pyConvert(word, adminId, isRdName);
			}
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
	
			logger.error("转拼音 + 转语音出错",new Exception(e));
	
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
		
		String phonetic = pyConvert(word, adminId, null);
		
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
			logger.error("英文翻译出错", new Exception(e));
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
	 * @Title: pyPolyphoneConvert
	 * @Description: 转换成含多音字的拼音
	 * @param word
	 * @param adminId
	 * @return
	 * @throws Exception  String
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月25日 上午10:18:42 
	 */
	public String pyPolyphoneConvert(String word,String adminId) throws Exception {
		
		Connection conn = null;

		String phonetic = "";
		try {

			conn = DBConnector.getInstance().getMetaConnection();
			
			char[] chars = word.toCharArray();
			
			for(char strChar : chars){
				//去字符并且去空格
				String str = String.valueOf(strChar).replaceAll(" ", "");
				
				List<String> pyList = polyphoneConvert( str, conn);
				String py ="";

				if(pyList != null && pyList.size() > 0){
					String polyphone ="";
					for(String p : pyList){
						if(polyphone != null && StringUtils.isNotEmpty(polyphone)){
							polyphone+=" ";
						}
						polyphone += p.substring(0, 1).toUpperCase() + p.substring(1);
					}
					if(polyphone.contains(" ")){
						polyphone="{"+polyphone+"}";
					}
					if(phonetic !=null && StringUtils.isNotEmpty(phonetic)){
						phonetic +=" ";
					}
					phonetic +=polyphone;
				}else{
					if(str != null && StringUtils.isNotEmpty(str)){
						py = pyConvert(str, adminId, null, conn);
						py = py.substring(0, 1).toUpperCase() + py.substring(1);
					}
					if(phonetic !=null && StringUtils.isNotEmpty(phonetic)){
						phonetic +=" ";
					}
						phonetic += py; 
				}
			}
			return phonetic;
		} catch (Exception e) {
			logger.error("含多音字的转拼音出错: ", new Exception(e));
			return "";

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}
	}
	/**
	 * @Title: pyConvertPolyphone
	 * @Description: 获取
	 * @param word
	 * @return
	 * @throws Exception  List<String>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月23日 下午4:41:25 
	 */
	public List<String> polyphoneConvert(String word,Connection conn) throws Exception {
		
		String sql = "SELECT py FROM TY_NAVICOVPY_PY  where JT = '"+word+"' ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		List<String> pys = null;
		try {

			pys = new ArrayList<String>();
			pstmt = conn.prepareStatement(sql);

//			pstmt.setString(1, word);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				String py = resultSet.getString("py");
				pys.add(py);
			} 
			
			return pys;
			
		} catch (Exception e) {
			logger.error("多音字翻译出错: ", new Exception(e));
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
	}
	
	/**
	 * @Title: pyConvert
	 * @Description: 转拼音(带连接的)
	 * @param word
	 * @param adminId
	 * @param isRdName
	 * @param conn
	 * @return
	 * @throws Exception  String
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月12日 下午3:51:55 
	 */
	public String pyConvert(String word,String adminId,String isRdName,Connection conn) throws Exception {
		CallableStatement cs = null;
		
		String initSql = "{call py_utils_word.init_context_param}";
		
		String sql = "select py_utils_word.convert_hz_tone(:1,    :2,    :3) phonetic from dual";
	
		PreparedStatement pstmt = null;
	
		ResultSet resultSet = null;
	
		String result = "";
	
		try {
	
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
			logger.error("转拼音2出错: ", new Exception(e));
//			throw new Exception(e);
	
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
	
		}
		
		if (StringUtils.isEmpty(result)){
			return "";
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
	public String wordConvert(String word,String adminId) throws Exception {
		CallableStatement cs = null;
		
		String initSql = "";
		
		String result = "";
	
		Connection conn = null;
	
		try {
	
			conn = DBConnector.getInstance().getMetaConnection();
			initSql = "{call PY_UTILS_WORD.CONVERT_ROAD_NAME(?,?)}";
			cs = conn.prepareCall(initSql);
			cs.setString(1, word);
			cs.setString(2, "");
			cs.registerOutParameter(1, Types.VARCHAR);
			cs.registerOutParameter(2, Types.VARCHAR);
			cs.executeUpdate();
			result =cs.getString(1);
			System.out.println(result);
			logger.info("newWord: "+cs.getString(2));
		} catch (Exception e) {
			logger.error("特殊词处理出错: ", new Exception(e));
//			throw new Exception(e);
		} finally {
			DbUtils.closeQuietly(cs);
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
		if (result == null){
			return "";
		}
		return result;
	}

}
