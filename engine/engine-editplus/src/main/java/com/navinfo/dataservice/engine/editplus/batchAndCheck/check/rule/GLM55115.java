package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * @Title: GLM55115
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description:检查条件：
					非删除点门牌对象
				检查原则：
					点门牌引导非删除link的种别为高速或城市高速（RD_LINK.KIND=1或2），报LOG：点门牌引导link为高速或城市高速！
 * @Author: LittleDog
 * @Date: 2017年10月13日
 * @Version: V1.0
 */
public class GLM55115 extends BasicCheckRule {

	@Override
	public void run() throws Exception {
		
        List<Long> pids = new ArrayList<>();
        for (Map.Entry<Long, BasicObj> entryRow : getRowList().entrySet()) {
            BasicObj basicObj = entryRow.getValue();

            // 已删除的数据不检查
            if (basicObj.opType().equals(OperationType.PRE_DELETED)) {
				continue;
			}
            
            if (basicObj.objName().equals(ObjectName.IX_POINTADDRESS)) {
                pids.add(entryRow.getKey());
            }
        }

        if (CollectionUtils.isEmpty(pids)) {
            return;
        }

        String pidStr = org.apache.commons.lang.StringUtils.join(pids, ",");

        Connection conn = getCheckRuleCommand().getConn();

        List<Clob> values = new ArrayList<>();
        String pidString;
        if (pids.size() > 1000) {
            Clob clob = ConnectionUtil.createClob(conn);
            clob.setString(1, pidStr);
            pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
            values.add(clob);
        } else {
            pidString = " PID IN (" + pidStr + ")";
        }
        
        String sql = "SELECT P.PID, P.GEOMETRY, P.MESH_ID, R.KIND" + 
        		"  FROM IX_POINTADDRESS P, RD_LINK R" + 
        		" WHERE P.GUIDE_LINK_PID = R.LINK_PID" + 
        		"   AND P.U_RECORD <> 2" + 
        		"   AND R.U_RECORD <> 2" + 
        		"   AND P." + pidString;

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sql);
            if (CollectionUtils.isNotEmpty(values)) {
                for (int i = 0; i < values.size(); i++) {
                    pstmt.setClob(i + 1, values.get(i));
                }
            }

            Set<String> validate = new HashSet<>();

            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                int pid = resultSet.getInt("PID");
                int kind = resultSet.getInt("KIND");
                int meshId = resultSet.getInt("MESH_ID");
                STRUCT struct = (STRUCT)resultSet.getObject("GEOMETRY");
                Geometry geo = GeoTranslator.struct2Jts(struct);

                String targets = null;
                if (kind == 1 || kind == 2) {
                	targets = String.format("[IX_POINTADDRESS,%s]", pid);
                }

                if (StringUtils.isNotEmpty(targets) && !validate.contains(targets)) {
					setCheckResult(geo, targets, meshId);
                    validate.add(targets);
                }
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
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
