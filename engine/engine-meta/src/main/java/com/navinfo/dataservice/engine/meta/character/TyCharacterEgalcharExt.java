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

}
