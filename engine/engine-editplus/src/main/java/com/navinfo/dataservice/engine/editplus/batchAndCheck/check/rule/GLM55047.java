package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * @ClassName: GLM55047
 * @author: zhangpengpeng
 * @date: 2017年10月13日
 * @Desc: GLM55047.java 检查条件： 非删除点门牌对象 检查原则：
 *        点门牌的引导坐标，不应落在类别为非删除水系LC_FACE.KIND（1～６）的土地覆盖面内，否则报Log：点门牌引导坐标落入XX中
 *        排除：“外业LABEL”字段标包含“绿地”字样的
 */
public class GLM55047 extends BasicCheckRule {

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
		Map<Long, IxPointaddress> map = new HashMap<>();

		for (Map.Entry<Long, BasicObj> entry : getRowList().entrySet()) {
			BasicObj basicObj = entry.getValue();
			if (basicObj.objName().equals(ObjectName.IX_POINTADDRESS) && !basicObj.opType().equals(OperationType.PRE_DELETED)) {
				IxPointAddressObj ixPointaddressObj = (IxPointAddressObj) basicObj;
				IxPointaddress ixPonitaddress = (IxPointaddress) ixPointaddressObj.getMainrow();
				map.put(basicObj.objPid(), ixPonitaddress);
			}
		}
		if (map.isEmpty()) {
			return;
		}

		Connection conn = getCheckRuleCommand().getConn();
		Clob clob = null;
		String pidString;
		if (map.size() > 1000) {
			clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(map.keySet(), ","));
			pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
		} else {
			pidString = " PID IN (" + StringUtils.join(map.keySet(), ",") + ")";
		}
        String sql = "SELECT T1.PID, T2.KIND" + 
                "  FROM IX_POINTADDRESS T1, LC_FACE T2" + 
                " WHERE T1." + pidString +
                "   AND T1.U_RECORD <> 2" + 
                "   AND SDO_RELATE(T2.GEOMETRY,  SDO_GEOMETRY('POINT (' || T1.X_GUIDE || ' ' || T1.Y_GUIDE || ')',8307), 'MASK=ANYINTERACT') = 'TRUE'" + 
                "   AND T2.KIND IN (1, 2, 3, 4, 5, 6)" + 
                "   AND T2.U_RECORD <> 2";
		
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
        	pstmt = conn.prepareStatement(sql);
        	if (map.size() > 1000) {
        		pstmt.setClob(1, clob);
        	}
        	rs = pstmt.executeQuery();
        	Set<String> validate = new HashSet<>();
        	 
        	while(rs.next()){
        		long pid = rs.getLong("PID");
        		IxPointaddress point = map.get(pid);
        		String memoire = point.getMemoire() == null ? "" : point.getMemoire();
        		int kind = rs.getInt("KIND");
        		
                String targets = null;
                if (memoire.indexOf("绿地") == -1) {
                	targets = String.format("[IX_POINTADDRESS,%s]", pid);
                }

                if (StringUtils.isNotEmpty(targets) && !validate.contains(targets)) {
                	String log = "点门牌引导坐标落入%s中！";
                	switch (kind) {
					case 1:
						log = String.format(log, "海域");
						break;
					case 2:
						log = String.format(log, "河川域");
						break;
					case 3:
						log = String.format(log, "湖沼池");
						break;
					case 4:
						log = String.format(log, "水库");
						break;
					case 5:
						log = String.format(log, "港湾");
						break;
					case 6:
						log = String.format(log, "运河");
						break;
					}
                    setCheckResult(point.getGeometry(), targets, point.getMeshId(), log);
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
