package com.navinfo.dataservice.engine.editplus.batchAndCheck.check;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRule;

public class CheckRuleLoader {
	
	private Map<String, CheckRule> checkRuleMap = new HashMap<String, CheckRule>();

	private static class SingletonHolder {
		private static final CheckRuleLoader INSTANCE = new CheckRuleLoader();
	}

	public static final CheckRuleLoader getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public CheckRule loadByRuleId(String ruleId) throws Exception {
		if (!checkRuleMap.containsKey(ruleId)) {
			synchronized (this) {
				if (!checkRuleMap.containsKey(ruleId)) {
					try {
						
						String sql = "SELECT RULE_ID, ACCESSOR, ACCESSOR_TYPE, OBJ_NAME_SET, REFER_SUBTABLE_MAP,LOG,RULE_LEVEL"
								+ "  FROM CHECK_PLUS"
								+ "  where RULE_ID = ? AND STATUS='E'";
						PreparedStatement pstmt = null;
						ResultSet resultSet = null;
						Connection conn = null;
						try {
							conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,ruleId);
							resultSet = pstmt.executeQuery();
							if (resultSet.next()) {
								String accessor=resultSet.getString("ACCESSOR");
								String accessorType=resultSet.getString("ACCESSOR_TYPE");
								String objNameSet=resultSet.getString("OBJ_NAME_SET");
								String referSubtableMap=resultSet.getString("REFER_SUBTABLE_MAP");
								int level=resultSet.getInt("RULE_LEVEL");
								CheckRule checkRule=new CheckRule();
								checkRule.setRuleId(ruleId);
								checkRule.setAccessorType(accessorType);
								checkRule.setAccessor(accessor);
								checkRule.setObjNameSet(objNameSet);
								checkRule.setReferSubtableMap(referSubtableMap);
								checkRule.setLog(resultSet.getString("LOG"));
								checkRule.setRuleLevel(level);
								checkRuleMap.put(ruleId,checkRule);					
							} 
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.commitAndCloseQuietly(conn);
						}
					} catch (Exception e) {
						throw new SQLException("获取规则"+ruleId+"失败："
								+ e.getMessage(), e);
					}
				}
			}
		}
		return checkRuleMap.get(ruleId);
	}

}
