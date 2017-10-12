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
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * @ClassName: GLM55063
 * @author: zhangpengpeng
 * @date: 2017年10月11日
 * @Desc: GLM55063.java 检查条件： 非删除点门牌对象 检查原则：
 *        点门牌的显示坐标与引导坐标之间的连线（以显示坐标和引导坐标为端点的线段），不应与非删除道路相交，否则报log：
 *        点门牌的显示坐标与引导坐标之间的连线与道路相交！
 *        排除：
 *        若与连线相交的非删除道路属性为高架（RD_LINK.IS_VIADUCT=1）、隧道、公交专用道路，不报log；
 *        （RD_LINK_FORM.FORM_OF_WAY:31 隧道; 22 公交专用道路）
 *        若与该点门牌自身的引导非删除Link相交，不报log；
 *        若与连线相交的非删除道路为10级路（RD_LINK.KIND=10），不报log；
 *        若与连线相交的非删除道路为8级非辅路（RD_LINK.KIND=8），不报log；
 */
public class GLM55063 extends BasicCheckRule {

	/**
	 * 允许相交的RDLINK形态
	 */
	private final static List<Integer> ALLOW_LINK_FORM = Arrays.asList(22, 31);

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
		String sql = "SELECT T1.LINK_PID, T1.KIND, T1.IS_VIADUCT, T2.PID" + "  FROM RD_LINK T1, IX_POINTADDRESS T2"
				+ " WHERE T2." + pidString + "   AND T1.LINK_PID <> T2.GUIDE_LINK_PID" + "   AND T1.U_RECORD <> 2"
				+ "   AND SDO_RELATE(T1.GEOMETRY,"
				+ "                  SDO_GEOMETRY('LINESTRING(' || T2.GEOMETRY.SDO_POINT.X || ' ' ||"
				+ "                               T2.GEOMETRY.SDO_POINT.Y || ' , ' ||"
				+ "                               T2.X_GUIDE || ' ' || T2.Y_GUIDE || ')',"
				+ "                               8307),"
				+ "                  'mask=011001111+001011111+101011111+100011011') = 'TRUE'"
				+ "   AND T2.U_RECORD <> 2";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sql);
			if (map.size() > 1000) {
				pstmt.setClob(1, clob);
			}
			rs = pstmt.executeQuery();

			Set<String> filters = new HashSet<>();
			while (rs.next()) {
				boolean flag = true;

				int kind = rs.getInt("KIND");
				if (10 == kind) {
					flag = false;
				}

				int isViaduct = rs.getInt("IS_VIADUCT");
				if (1 == isViaduct) {
					flag = false;
				}

				int linkPid = rs.getInt("LINK_PID");
				List<IRow> forms = new AbstractSelector(RdLinkForm.class, conn).loadRowsByParentId(linkPid, false);

				if (8 == kind) {
					boolean hasRoads = false;
					for (IRow row : forms) {
						RdLinkForm form = (RdLinkForm) row;
						if (34 == form.getFormOfWay()) {
							hasRoads = true;
						}
					}

					if (!hasRoads) {
						flag = false;
					}
				}

				for (IRow row : forms) {
					RdLinkForm form = (RdLinkForm) row;
					if (ALLOW_LINK_FORM.contains(Integer.valueOf(form.getFormOfWay()))) {
						flag = false;
					}
				}

				Long pid = rs.getLong("PID");
				if (flag) {
					String targets = String.format("[IX_POINTADDRESS,%s]", pid);
					IxPointaddress point = map.get(pid);
					if (!filters.contains(targets)) {
						filters.add(targets);
						setCheckResult(point.getGeometry(), targets, point.getMeshId());
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(rs);
			DBUtils.closeStatement(pstmt);
		}
	}

}
