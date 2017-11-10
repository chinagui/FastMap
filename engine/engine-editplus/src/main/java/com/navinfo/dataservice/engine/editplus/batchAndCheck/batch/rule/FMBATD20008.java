package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 
 * @Title:FMBATD20008
 * @Package:com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule
 * @Description: 
 *  查询条件：满足以下条件之一，均执行批处理：
         （1）新增POI对象且存在电话；
         （2）修改POI对象且修改电话且存在电话；
	批处理原则：
	将PRIORITY字段从1开始重新排序，排序原则：保证同类型电话原有先后顺序不变，不同类型之间电话的排序顺序：CONTACT_TYPE=1>2>3>4>11>21>22；并生成履历；
 * @author:Jarvis 
 * @date: 2017年11月10日
 */
public class FMBATD20008 extends BasicBatchRule {
	
	private final String sortRule = "1, 1, 2, 2, 3, 3, 4, 4, 11, 5, 21, 6,22, 7";//排序规则，语法规则为oracle中DECODE规则， if 条件 then 值
	private Map<String,Integer> contactRowIdRankMap = new HashMap<String,Integer>(); //根据pid分组，根据排序原则排序，排出的rowId和要赋值的PRIORITY对应的map

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
    	Set<Long> pidList = new HashSet<>();

        for (BasicObj obj : batchDataList) {
        	
            pidList.add(obj.objPid());
            
        }
        
        if(pidList != null && !pidList.isEmpty()){
        	contactRowIdRankMap = getContactRowIdRankMap(getBatchRuleCommand().getConn(), pidList);
        }
    }

	@Override
    public void runBatch(BasicObj obj) throws Exception {
    	if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			
			List<IxPoiContact> ixPoiContacts = poiObj.getIxPoiContacts();
			
			if(ixPoiContacts != null && !ixPoiContacts.isEmpty()){
				boolean flag = false;
				for (IxPoiContact ixPoiContact : ixPoiContacts) {
					if(ixPoiContact.getOpType().equals(OperationType.UPDATE)){
						flag = true;
						break;
					}
				}
				if(poi.getOpType().equals(OperationType.INSERT)||(poi.getOpType().equals(OperationType.UPDATE) && flag)){
					for (IxPoiContact ixPoiContact : ixPoiContacts) {
						Integer rank = contactRowIdRankMap.get(ixPoiContact.getRowId());
						if(rank != null){
							ixPoiContact.setPriority(rank);
						}
					}
				}
			}
		}
    }

	
	/**
	 * 排序后的PRIORITY map
	 * @param conn
	 * @param pidList
	 * @return
	 * @throws Exception 
	 */
	private Map<String, Integer> getContactRowIdRankMap(Connection conn, Set<Long> pidList) throws Exception {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(" SELECT POI_PID,CONTACT_TYPE,PRIORITY, ROW_ID, " );
			sb.append(" ROW_NUMBER() OVER(PARTITION BY POI_PID ORDER BY DECODE(CONTACT_TYPE, "+sortRule+"), PRIORITY) ROW_NUMBER ");
			sb.append(" FROM IX_POI_CONTACT WHERE U_RECORD <> 2 ");
			
			String ids = org.apache.commons.lang.StringUtils.join(pidList, ",");
			Clob pidClod = null;
	        if (pidList.size() > 1000) {
	            pidClod = ConnectionUtil.createClob(conn);
	            pidClod.setString(1, ids);
	            sb.append(" AND POI_PID IN (select to_number(column_value) from table(clob_to_table(?)))");
	        } else {
	        	sb.append(" AND POI_PID IN (" + ids + ")");
	        }

	        Map<String, Integer> contactRowIdRankMap = new LinkedHashMap<>();
			
			pstmt = conn.prepareStatement(sb.toString());
			if(pidClod != null){
				pstmt.setClob(1, pidClod);
			}

			rs = pstmt.executeQuery();
			
			while(rs.next()){
				contactRowIdRankMap.put(rs.getString("ROW_ID"), rs.getInt("ROW_NUMBER"));
			}
			
			return contactRowIdRankMap;
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
	}
}


