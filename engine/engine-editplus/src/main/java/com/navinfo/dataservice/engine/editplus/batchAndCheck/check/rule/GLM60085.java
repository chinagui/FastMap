package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * 在IX_POI_Parent表中存在的POI记录，在IX_POI_Children表中一定存在相应的子POI，
 * 否则报log：父POI的子不存在
 *
 */
public class GLM60085 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			List<Long> parentGroupIds = CheckUtil.getParentGroupIds(poi.getPid(), getCheckRuleCommand().getConn());
			if (parentGroupIds.size() == 0) {
				return;
			}
			if (checkChildren(parentGroupIds,getCheckRuleCommand().getConn())) {
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"父POI的子不存在");
			}
		}

	}
	
	/**
	 * 查询是否有不存在的子
	 * @param groupIds
	 * @param conn
	 * @return
	 * @throws Exception
	 */
    private boolean checkChildren(List<Long> groupIds,Connection conn) throws Exception {
    	String sql = "select t.group_id from ix_poi_children t where t.u_record != 2 and t.group_id in (select column_value from table(clob_to_table(?)))";
    	
    	PreparedStatement pstmt = null;
		
		ResultSet resultSet = null;
		
		Clob groupClod = null;
    	try {
    		String groups = StringUtils.join(groupIds, ",");
    		groupClod = ConnectionUtil.createClob(conn);
    		groupClod.setString(1, groups);
    		pstmt = conn.prepareStatement(sql);
			pstmt.setClob(1, groupClod);
			resultSet = pstmt.executeQuery();
			List<Long> childGroupIds = new ArrayList<Long>();
			while (resultSet.next()) {
				childGroupIds.add(resultSet.getLong("group_id"));
			}
    		for (Long parent:groupIds) {
    			if (!childGroupIds.contains(parent)) {
    				return true;
    			}
    		}
    		return false;
    	} catch (Exception e) {
    		throw e;
    	} finally {
    		
    	}
    }

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
