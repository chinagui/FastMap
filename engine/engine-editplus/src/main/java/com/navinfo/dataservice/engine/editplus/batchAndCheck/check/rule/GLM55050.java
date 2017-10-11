package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class GLM55050 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() throws Exception {
		List<Long> pids = new ArrayList<>();
		for (Map.Entry<Long, BasicObj> entryRow : getRowList().entrySet()) {
			BasicObj basicObj = entryRow.getValue();
			if (basicObj.objName().equals(ObjectName.IX_POINTADDRESS)
					&& !basicObj.opType().equals(OperationType.PRE_DELETED)) {
				pids.add(entryRow.getKey());
			}
		}

		if (CollectionUtils.isEmpty(pids)) {
			return;
		}

		String pidStr = StringUtils.join(pids, ",");
		Connection conn = getCheckRuleCommand().getConn();
		String pidString;
		Clob clob = null;
		if (pids.size() > 1000) {
			clob = ConnectionUtil.createClob(conn);
			clob.setString(1, pidStr);
			pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
		} else {
			pidString = " PID IN (" + pidStr + ")";
		}
		
        String sql = "SELECT T1.PID, T1.GEOMETRY, T1.MESH_ID " +
                "  FROM IX_POI T1, LC_FACE T2" + 
                " WHERE T1." + pidString +
                "   AND T1.U_RECORD <> 2" + 
                "   AND T1.X_GUIDE != 0" + 
                "   AND T1.Y_GUIDE != 0" + 
                "   AND SDO_RELATE(T2.GEOMETRY," + 
                "                  SDO_GEOMETRY('LINESTRING(' || T1.GEOMETRY" + 
                "                               .SDO_POINT.X || ' ' || T1.GEOMETRY.SDO_POINT.Y ||" + 
                "                               ' , ' || T1.X_GUIDE || ' ' || T1.Y_GUIDE || ')'," + 
                "                               8307)," + "                  'MASK=011011111') = 'TRUE'" + 
                "   AND T2.U_RECORD <> 2" + 
                "   AND T2.KIND IN (1,2,3,4,5,6,11,12,13) ";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			pstmt = conn.prepareStatement(sql);
			if(pids.size() > 1000){
				pstmt.setClob(1, clob);
			}
			
            rs = pstmt.executeQuery();
            List<String> validate = new ArrayList<>();
            while (rs.next()) {
                int pid = rs.getInt("PID");
                String targets = "[IX_POINTADDRESS," + pid + "]";

                if (!validate.contains(targets)) {
                	STRUCT struct = (STRUCT)rs.getObject("GEOMETRY");
                    Geometry geo = GeoTranslator.struct2Jts(struct);
                    setCheckResult(geo, targets, rs.getInt("MESH_ID"));
                    validate.add(targets);
                }
            }
		}catch(Exception e){
			throw e;
		}finally{
            DBUtils.closeResultSet(rs);
            DBUtils.closeStatement(pstmt);
		}
	}
}
