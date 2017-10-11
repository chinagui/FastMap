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

/**
 * @ClassName: GLM55049
 * @author: zhangpengpeng
 * @date: 2017年10月10日
 * @Desc: GLM55049.java检查条件： 非删除点门牌对象 检查原则：
 *        点门牌的显示坐标与引导坐标之间的连线（以显示坐标和引导坐标为端点的线段），不应与非删除铁路种别为1即RW_LINK.kind＝1相交，
 *        否则报log：点门牌显示坐标与引导坐标之间跨越铁路！
 */
public class GLM55049 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
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
                "  FROM IX_POINTADDRESS T1, RW_LINK T2" + 
                " WHERE T1." + pidString +
                "   AND T1.U_RECORD <> 2" + 
                "   AND SDO_RELATE(T2.GEOMETRY," + 
                "                  SDO_GEOMETRY('LINESTRING(' || T1.GEOMETRY.SDO_POINT.X || ' ' ||" + 
                "                               T1.GEOMETRY.SDO_POINT.Y || ' , ' ||" + 
                "                               T1.X_GUIDE || ' ' || T1.Y_GUIDE || ')'," + 
                "                               8307)," + 
                "                  'MASK=011001111+001011111+101011111+100011011') = 'TRUE'" + 
                "   AND T2.U_RECORD <> 2" + 
                "   AND T2.KIND = 1 ";

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
		try{
			pstmt = conn.prepareStatement(sql);
			if(pids.size() > 1000){
				pstmt.setClob(1, clob);
			}
			
            resultSet = pstmt.executeQuery();
            List<String> validate = new ArrayList<>();
            while (resultSet.next()) {
                int pid = resultSet.getInt("PID");
                String targets = "[IX_POINTADDRESS," + pid + "]";

                if (!validate.contains(targets)) {
                	STRUCT struct = (STRUCT)resultSet.getObject("GEOMETRY");
                    Geometry geo = GeoTranslator.struct2Jts(struct);
                    setCheckResult(geo, targets, resultSet.getInt("MESH_ID"));
                    validate.add(targets);
                }
            }
		}catch(Exception e){
			throw e;
		}finally{
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
