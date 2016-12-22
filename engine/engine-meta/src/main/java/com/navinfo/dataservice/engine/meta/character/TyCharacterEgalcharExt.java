package com.navinfo.dataservice.engine.meta.character;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class TyCharacterEgalcharExt {
	private Map<String, List<String>> extentionTypeMap = new HashMap<String, List<String>>();
	private List<String> halfCharList=new ArrayList<String>();
	
	private static class SingletonHolder {
		private static final TyCharacterEgalcharExt INSTANCE = new TyCharacterEgalcharExt();
	}

	public static final TyCharacterEgalcharExt getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/**
	 * 返回“TY_CHARACTER_EGALCHAR_EXT”表中数据。
	 * @return Map<String, List<String>> key:EXTENTION_TYPE value:CHARACTER字段列表
	 * @throws Exception
	 */
	public Map<String, List<String>> getExtentionTypeMap() throws Exception{
		if (extentionTypeMap==null||extentionTypeMap.isEmpty()) {
				synchronized (this) {
					if (extentionTypeMap==null||extentionTypeMap.isEmpty()) {
						try {
							String sql = "SELECT EXTENTION_TYPE, CHARACTER"
									+ "  FROM TY_CHARACTER_EGALCHAR_EXT"
									+ " ORDER BY EXTENTION_TYPE";
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								List<String> chars=new ArrayList<String>();
								String beforeExtentionType="";
								while (rs.next()) {
									String curExtentionType=rs.getString("EXTENTION_TYPE");
									if(beforeExtentionType.isEmpty()){
										beforeExtentionType=curExtentionType;
									}
									if(!curExtentionType.equals(beforeExtentionType)){
										extentionTypeMap.put(beforeExtentionType, chars);
										chars=new ArrayList<String>();
										beforeExtentionType=curExtentionType;
									}
									chars.add(rs.getString("CHARACTER"));
								} 
								extentionTypeMap.put(beforeExtentionType, chars);
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.commitAndCloseQuietly(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载extentionTypeMap失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return extentionTypeMap;
	}
	
	/**
	 * 1.“TY_CHARACTER_EGALCHAR_EXT”表，“EXTENTION_TYPE”字段中，“ENG_H_U”、“ENG_H_L”、“DIGIT_H”、
	 * “SYMBOL_H”类型对应的“CHARACTER”字段的内容;
	 * 2.“TY_CHARACTER_EGALCHAR_EXT”表，和 “EXTENTION_TYPE ”字段里“SYMBOL_F”类型，
	 * 		2.1在全半角对照关系表中（TY_CHARACTER_FULL2HALF表）FULL_WIDTH字段一致，
	 * 找到FULL_WIDTH字段对应的半角“HALF_WIDTH”,且“HALF_WIDTH”字段非空
	 * 		2.2.如果“HALF_WIDTH”字段对应的半角字符为空，则FULL_WIDTH字段对应的全角字符也是拼音的合法字符
	 * @return List<String> 返回合法的所有半角字符列表
	 */
	public List<String> getHalfCharList()  throws Exception{
		if (halfCharList==null||halfCharList.isEmpty()) {
			synchronized (this) {
				if (halfCharList==null||halfCharList.isEmpty()) {
					try {
						String sql = "SELECT E.CHARACTER"
								+ "  FROM TY_CHARACTER_EGALCHAR_EXT E"
								+ " WHERE EXTENTION_TYPE IN ('ENG_H_U', 'ENG_H_L', 'DIGIT_H', 'SYMBOL_H')"
								+ " UNION ALL"
								+ " SELECT F.HALF_WIDTH"
								+ "  FROM TY_CHARACTER_EGALCHAR_EXT E, TY_CHARACTER_FULL2HALF F"
								+ " WHERE E.EXTENTION_TYPE = 'SYMBOL_F'"
								+ "   AND E.CHARACTER = F.FULL_WIDTH"
								+ "   AND F.HALF_WIDTH IS NOT NULL"
								+ " UNION ALL"
								+ " SELECT F.FULL_WIDTH"
								+ "  FROM TY_CHARACTER_EGALCHAR_EXT E, TY_CHARACTER_FULL2HALF F"
								+ " WHERE E.EXTENTION_TYPE = 'SYMBOL_F'"
								+ "   AND E.CHARACTER = F.FULL_WIDTH"
								+ "   AND F.HALF_WIDTH IS NULL";
						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								halfCharList.add(rs.getString("CHARACTER"));
							} 
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.commitAndCloseQuietly(conn);
						}
					} catch (Exception e) {
						throw new SQLException("加载halfCharList失败："+ e.getMessage(), e);
					}
				}
			}
		}
		return halfCharList;
	}

}
