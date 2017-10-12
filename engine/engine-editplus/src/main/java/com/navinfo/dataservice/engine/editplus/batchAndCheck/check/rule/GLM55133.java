package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @Title: GLM55133
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description:检查条件：
					非删除点门牌对象
				检查原则：
					逐一判断点门牌显示坐标距离非删除道路最近距离，如果＞1.5米且＜5米，则报log：点门牌显示坐标距离道路＞1.5米且＜5米！
 * @Author: LittleDog
 * @Date: 2017年10月12日
 * @Version: V1.0
 */
public class GLM55133 extends BasicCheckRule {

	@Override
    public void run() throws Exception {

        Map<Long, IxPointaddress> map = new HashMap<>();

        for (Map.Entry<Long, BasicObj> entry : getRowList().entrySet()) {
            BasicObj basicObj = entry.getValue();
            // 已删除的数据不检查
            if (basicObj.opType().equals(OperationType.PRE_DELETED)) {
				continue;
			}
            
            if (basicObj.objName().equals(ObjectName.IX_POINTADDRESS)) {
                IxPointAddressObj ixPointaddressObj = (IxPointAddressObj) basicObj;
                IxPointaddress ixPonitaddress = (IxPointaddress) ixPointaddressObj.getMainrow();
                map.put(basicObj.objPid(), ixPonitaddress);
            }
            
        }

        if (map.isEmpty()) {
            return;
        }

        Connection conn = getCheckRuleCommand().getConn();

        List<Clob> values = new ArrayList<>();

        String pidString;
        if (map.size() > 1000) {
            Clob clob = ConnectionUtil.createClob(conn);
            clob.setString(1, StringUtils.join(map.keySet(), ","));
            pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
            values.add(clob);
        } else {
            pidString = " PID IN (" + StringUtils.join(map.keySet(), ",") + ")";
        }

        String fiveMetersSql = "SELECT P.PID " + 
        		"  FROM IX_POINTADDRESS P, RD_LINK R " + 
        		" WHERE SDO_WITHIN_DISTANCE(R.GEOMETRY, P.GEOMETRY, 'DISTANCE=5 UNIT=METER') = 'TRUE' " + 
        		"   AND P.U_RECORD <> 2 " + 
        		"   AND R.U_RECORD <> 2 " + 
        		"   AND P." + pidString;
        
        List<Long>  fiveMetersPidList = filterPid(conn, fiveMetersSql);
        
        String onePointFiveMetersSql = "SELECT P.PID " + 
        		"  FROM IX_POINTADDRESS P, RD_LINK R " + 
        		" WHERE SDO_WITHIN_DISTANCE(R.GEOMETRY, P.GEOMETRY, 'DISTANCE=1.5 UNIT=METER') = 'TRUE' " + 
        		"   AND P.U_RECORD <> 2 " + 
        		"   AND R.U_RECORD <> 2 " + 
        		"   AND P." + pidString;
        
        List<Long>  onePointFiveMetersPidList = filterPid(conn, onePointFiveMetersSql);
        
        if(fiveMetersPidList != null && fiveMetersPidList.size() > 0 && onePointFiveMetersPidList != null && onePointFiveMetersPidList.size() > 0) {
        	fiveMetersPidList.removeAll(onePointFiveMetersPidList);
        }
        
        Set<String> validate = new HashSet<>();
        for (Long pid : fiveMetersPidList) {
        	String targets = String.format("[IX_POINTADDRESS,%s]", pid);
        	IxPointaddress ixPointaddress = map.get(pid);
            if (!validate.contains(targets)) {
            	Geometry geo = ixPointaddress.getGeometry();
            	int meshId = ixPointaddress.getMeshId();
				setCheckResult(geo, targets, meshId);
                validate.add(targets);
            }
		}
        
    }
	
	private List<Long> filterPid(Connection conn, String sql) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<Long>> rs = new ResultSetHandler<List<Long>>() {
				List<Long> list = new ArrayList<>();
				@Override
				public List<Long> handle(ResultSet rs) throws SQLException {
					while (rs.next()) {
						list.add(rs.getLong(1));
					}
					return list;
				}
			};
			return run.query(conn, sql, rs);
		}catch(Exception e){
			throw e;
		}
	}
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
