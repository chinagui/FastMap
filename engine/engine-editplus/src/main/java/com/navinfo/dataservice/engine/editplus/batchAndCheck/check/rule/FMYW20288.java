package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * @ClassName FMYW20288
 * @author Han Shaoming
 * @date 2017年2月28日 下午3:15:02
 * @Description TODO 检查条件： 非删除POI对象 检查原则：
 * (1)POI同点(显示坐标完全相同)，同点有部分有地址，有部分无地址，（不判断地址是否相同)报log：不存在地址，请确认！
 * (2)POI同点(显示坐标完全相同)，但是地址中“路”或“道”后面的阿拉伯数字不一致报log：地址中“路”(道)后面的数字不一致
 * ，请确认！ 注："路"与"路"后面的阿拉伯数字比较；"道"与"道"后面的阿拉伯数字比较；
 */
public class FMYW20288 extends BasicCheckRule {

	public void run() throws Exception {
		Map<Long, BasicObj> rows = getRowList();
		List<Long> pid = new ArrayList<Long>();
		for (Long key : rows.keySet()) {
			BasicObj obj = rows.get(key);
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			// 已删除的数据不检查
			if (poi.getOpType().equals(OperationType.PRE_DELETED)) {
				continue;
			}
			pid.add(poi.getPid());
		}
		// POI同点(显示坐标完全相同)
		if (pid != null && pid.size() > 0) {
			String pids = pid.toString().replace("[", "").replace("]", "");
			Connection conn = this.getCheckRuleCommand().getConn();
			List<Clob> values = new ArrayList<Clob>();
			String pidString = "";
			if (pid.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
				values.add(clob);
			} else {
				pidString = " PID IN (" + pids + ")";
			}

			String sqlStr = "SELECT P1.PID PID,P1.GEOMETRY,P1.MESH_ID,P2.PID PID2 " 
					+ " FROM IX_POI P1,IX_POI P2"
					+ " WHERE P1.GEOMETRY.SDO_POINT.X =P2.GEOMETRY.SDO_POINT.X"
					+ " AND P1.GEOMETRY.SDO_POINT.Y =P2.GEOMETRY.SDO_POINT.Y" 
					+ "	AND P1. " + pidString
					+ " AND P1.U_RECORD <>2 AND P2.U_RECORD <>2	" 
					+ " AND P1.PID <> P2.PID";
			PreparedStatement pstmt = conn.prepareStatement(sqlStr);
			if (values != null && values.size() > 0) {
				for (int i = 0; i < values.size(); i++) {
					pstmt.setClob(i + 1, values.get(i));
				}
			}
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Long pidTmp1 = rs.getLong("PID");
				Long pidTmp2 = rs.getLong("PID2");
				int meshId = rs.getInt("MESH_ID");
				STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				String targets = "[IX_POI," + pidTmp1 + "];[IX_POI," + pidTmp2 + "]";
				Set<Long> pidList = new HashSet<Long>();
				pidList.add(pidTmp1);
				pidList.add(pidTmp2);
				Set<String> referSubrow = new HashSet<String>();
				referSubrow.add("IX_POI_ADDRESS");
				Map<Long, BasicObj> referObjs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, referSubrow, false, pidList, false, true);

				BasicObj obj1 = referObjs.get(pidTmp1);
				IxPoiObj poiObj1 = (IxPoiObj) obj1;
				IxPoiAddress address1 = poiObj1.getCHAddress();

				BasicObj obj2 = referObjs.get(pidTmp2);
				IxPoiObj poiObj2 = (IxPoiObj) obj2;
				IxPoiAddress address2 = poiObj2.getCHAddress();
				if ((address1 == null && address2 != null) || (address1 != null && address2 == null)) {
					setCheckResult(geometry, targets, meshId,"不存在地址，请确认");
				}
				if (address1 != null && address2 != null) {
					String fullName1 = address1.getFullname();
					String fullName2 = address2.getFullname();
					if(fullName1.contains("路")&&fullName2.contains("路")){
						String digit11 = this.getDigit(fullName1, "路");
						String digit21 = this.getDigit(fullName2, "路");
						if(digit11!=null&&digit21!=null&&!StringUtils.equals(digit11, digit21)){
							setCheckResult(geometry, targets, meshId,"地址中“路”(道)后面的数字不一致，请确认");
						}
					}
					if(fullName1.contains("道")&&fullName2.contains("道")){
						String digit12 = this.getDigit(fullName1, "道");
						String digit22 = this.getDigit(fullName2, "道");
						if(digit12!=null&&digit22!=null&&!StringUtils.equals(digit12, digit22)){
							setCheckResult(geometry, targets, meshId,"地址中“路”(道)后面的数字不一致，请确认");
						}
					}
				}
			}
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

	private String getDigit(String param, String key) throws Exception {
		try {
			if (param == null || key == null || !param.contains(key)) {
				return null;
			}
			String sub = null;
			for(int i=0;i<param.length();){
				if(!param.contains(key)){break;}
				int index = param.indexOf(key);
				if(index<param.length()-1){
					String str = param.substring(index+1,index+2);
					if(CheckUtil.isDigit(str)){
						sub = param.substring(index+1);
						break;
					}else{
						param = param.substring(index+1);
					}
				}
				i=index;
			}
			if (sub != null) {
				String regex = "\\d*";
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(sub);
				while (m.find()) {
					if (!"".equals(m.group())) {
						return m.group(0);
					}
				}
			}
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

}
