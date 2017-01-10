package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
/**
 * GLM60044
 * 检查条件：以下条件其中之一满足时，需要进行检查：
 * 对IX_POI表中“STATE(状态)”字段不为“1（删除）”的进行检查。
 * 检查原则： 
 * 如果POI标准化官方中文名称、显示坐标、种别、chain与其他非删除POI的标准化官方中文名称相同、显示坐标、种别、chain全部相同，则这些POI全部报出
 * 屏蔽：
 * POI的类别为、磁悬浮主点（230115）、地铁站主点（230112）、长途客运站出入口（行人导航）（230110）、出租车停靠站（230117）、
 * 出租车站出入口（230118）、公交车站、BRT（230101）、公交车站出入口（230102）、缆车站出入口（230122）、缆车站主点（230121）、
 * 水上公交车站出入口（230124）、水上公交站（230123）、小巴出入口（行人导航）（230120）、专线小巴站（230119）、彩票销售(180208)、
 * 停车场（230210）且两条数据备注字段（label）分别包含‘室内|地下’与‘室外’、充电桩(230227),不报log
 * @author zhangxiaoyi
 */
public class GLM60044 extends BasicCheckRule {
	
	public void run() throws Exception {
		Map<Long, BasicObj> rows=getRowList();
		List<Long> pid=new ArrayList<Long>();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi =(IxPoi) poiObj.getMainrow();
			//已删除的数据不检查
			if(poi.getOpType().equals(OperationType.PRE_DELETED)){continue;}
			String kind=poi.getKindCode();
			if(kind.equals("230112")||kind.equals("230115")||kind.equals("230110")||kind.equals("230117")
					||kind.equals("230118")||kind.equals("230101")||kind.equals("230102")||kind.equals("230122")
					||kind.equals("230121")||kind.equals("230124")||kind.equals("230123")||kind.equals("230120")
					||kind.equals("230119")||kind.equals("180208")||kind.equals("230227"))
				{continue;}
			pid.add(poi.getPid());
		}
		//同点的设施，名称（name）、地址（address）、分类和品牌相同
		if(pid!=null&&pid.size()>0){
			String pids=pid.toString().replace("[", "").replace("]", "");
			List<Clob> values=new ArrayList<Clob>();
			String pidString="";
			Connection conn = this.getCheckRuleCommand().getConn();
			if(pid.size()>1000){
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				pidString=" PID IN (select to_number(column_value) from table(clob_to_table(?)))";
				values.add(clob);
				values.add(clob);
			}else{
				pidString=" PID IN ("+pids+")";
			}
			String sqlStr="WITH TEMP AS"
					+ " (SELECT PID,"
					+ "         KIND_CODE,"
					+ "         A.GEOMETRY,"
					+ "         A.X_GUIDE,"
					+ "         A.Y_GUIDE,"
					+ "         B.NAME,"
					+ "         NVL(A.CHAIN, '0') CHAIN,"
					+ "         A.LABEL"
					+ "    FROM IX_POI A, IX_POI_NAME B"
					+ "   WHERE A.PID = B.POI_PID"
					+ "     AND A.STATE <> 1"
					+ "     AND B.NAME_CLASS = 1"
					+ "     AND B.NAME_TYPE = 1"
					+ "     AND B.LANG_CODE IN ('CHI', 'CHT'))"
					+ " SELECT T1.PID"
					+ "  FROM TEMP T1, TEMP T2"
					+ " WHERE T1.KIND_CODE = T2.KIND_CODE"
					+ "   AND T1.GEOMETRY.SDO_POINT.X = T2.GEOMETRY.SDO_POINT.X"
					+ "   AND T1.GEOMETRY.SDO_POINT.Y = T2.GEOMETRY.SDO_POINT.Y"
					+ "   AND T1.NAME = T2.NAME"
					+ "   AND T1.CHAIN = T2.CHAIN"
					+ "   AND T1."+pidString
					+ "   AND T1.PID <> T2.PID"
					+ " MINUS"
					+ " SELECT T1.PID"
					+ "  FROM TEMP T1, TEMP T2"
					+ " WHERE T1.KIND_CODE = '230210'"
					+ "   AND T2.KIND_CODE = '230210'"
					+ "   AND T1.NAME = T2.NAME"
					+ "   AND T1.CHAIN = T2.CHAIN"
					+ "   AND T1."+pidString
					+ "   AND ((REGEXP_LIKE(T1.LABEL, '室内|地下') AND REGEXP_LIKE(T2.LABEL, '室外')) OR"
					+ "       (REGEXP_LIKE(T1.LABEL, '室外') AND REGEXP_LIKE(T2.LABEL, '室内|地下')))";
			PreparedStatement pstmt=conn.prepareStatement(sqlStr);;
			if(values!=null&&values.size()>0){
				for(int i=0;i<values.size();i++){
					pstmt.setClob(i+1,values.get(i));
				}
			}
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Long pidTmp1=rs.getLong("PID");
				String targets="[IX_POI,"+pidTmp1+"]";
				setCheckResult("", targets, 0);
			}
		}
	}
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
