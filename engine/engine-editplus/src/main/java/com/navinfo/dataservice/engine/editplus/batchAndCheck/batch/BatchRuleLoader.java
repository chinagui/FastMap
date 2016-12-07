package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRule;
import com.navinfo.navicommons.database.sql.DBUtils;

public class BatchRuleLoader {
	
	private Map<String, BatchRule> batchRuleMap = new HashMap<String, BatchRule>();

	private static class SingletonHolder {
		private static final BatchRuleLoader INSTANCE = new BatchRuleLoader();
	}

	public static final BatchRuleLoader getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public BatchRule loadByRuleId(String ruleId) throws Exception {
		if (!batchRuleMap.containsKey(ruleId)) {
			synchronized (this) {
				if (!batchRuleMap.containsKey(ruleId)) {
					try {
						
						String sql = "SELECT RULE_ID, ACCESSOR, ACCESSOR_TYPE, OBJ_NAME_SET, REFER_SUBTABLE_MAP"
								+ "  FROM BATCH_PLUS"
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
								BatchRule batchRule=new BatchRule();
								batchRule.setRuleId(ruleId);
								batchRule.setAccessorType(accessorType);
								batchRule.setAccessor(accessor);
								batchRule.setObjNameSet(objNameSet);
								batchRule.setReferSubtableMap(referSubtableMap);
								batchRuleMap.put(ruleId,batchRule);					
							} 
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.commitAndCloseQuietly(conn);
						}
					} catch (Exception e) {
						throw new SQLException("获取批处理规则"+ruleId+"失败："
								+ e.getMessage(), e);
					}
				}
			}
		}
		return batchRuleMap.get(ruleId);
	}

}
