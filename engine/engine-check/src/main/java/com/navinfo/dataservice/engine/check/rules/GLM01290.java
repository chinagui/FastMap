package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * @ClassName：GLM01290
 * @author:Feng Haixia
 * @data:2017/03/23
 * @Description:限制信息类型为“车辆限制”, 则车辆类型的值域范围只能是“客车、配送卡车、运输卡车、急救车、出租车、公交车、步行者”这7种类型，
 *              超出值域范围的选择为错误
 */
public class GLM01290 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01290.class);

	private Set<Integer> limitResultPidSet = new HashSet<>();

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareRDLinkLimitData(checkCommand);

		for (Integer linkPid : this.limitResultPidSet) {
			String sqlStr = String.format(
					"SELECT LM.VEHICLE FROM RD_LINK_LIMIT LM WHERE LM.TYPE=2 AND LM.U_RECORD <> 2 AND LM.LINK_PID = %d ",
					linkPid);

			logger.info("RdLinkLimit后检查GLM01290 SQL:" + sqlStr);

			List<Integer> resultList = this.ExecuteSQL(sqlStr);

			if (resultList.isEmpty()) {
				continue;
			}

			if (CheckVehicleType(resultList) == true) {
				continue;
			}

			RdLinkSelector linkSelector = new RdLinkSelector(getConn());
			RdLink link = (RdLink) linkSelector.loadByIdOnlyRdLink(linkPid, false);

			this.setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
		}
	}

	/*
	 * 检查车辆类型是否符合要求
	 * 
	 * @param “车辆限制”类型，且有车辆类型集合
	 * 
	 * @result 已修改限制信息数据集
	 */
	private boolean CheckVehicleType(List<Integer> resultList) {

		boolean isRightVechicleType = true;

		for (Integer vehicleType : resultList) {
			if (vehicleType == 0) {
				continue;
			}

			// 将车辆类型转换成16进制
			String hexString = Integer.toBinaryString((int) vehicleType);
			String hexStringReverse = new StringBuffer(hexString).reverse().toString();

			Integer[] rightPos = new Integer[] { 0, 1, 2, 3, 7, 8, 9 };
			List<Integer> rightPosSet = Arrays.asList(rightPos);

			isRightVechicleType = isRightVehicle(hexStringReverse, rightPosSet);
		} // for循环

		return isRightVechicleType;
	}

	private boolean isRightVehicle(String hexStringVehicle, List<Integer> rightPosSet) {
		boolean isRightVehicle = true;

		for (int pos = 0; pos < hexStringVehicle.length(); pos++) {
			if (rightPosSet.contains(pos)) {
				continue;
			}
			if (hexStringVehicle.charAt(pos) == '1') {
				isRightVehicle = false;
				break;
			}
		}
		return isRightVehicle;
	}

	/*
	 * 准备检查数据
	 * 
	 * @param checkCommand
	 * 
	 * @result 已修改限制信息数据集
	 */
	private void prepareRDLinkLimitData(CheckCommand checkCommand) {
		for (IRow row : checkCommand.getGlmList()) {
			if (!(row instanceof RdLinkLimit) || row.status() == ObjStatus.DELETE) {
				continue;
			}

			RdLinkLimit rdlinkLimit = (RdLinkLimit) row;

			limitResultPidSet.add(rdlinkLimit.getLinkPid());
		} // for循环
	}

	private List<Integer> ExecuteSQL(String sql) throws Exception {
		PreparedStatement pstmt = this.getConn().prepareStatement(sql);
		ResultSet resultSet = pstmt.executeQuery();
		List<Integer> result = new ArrayList<Integer>();

		if (resultSet.next()) {
			result.add(resultSet.getInt(1));
		}

		resultSet.close();
		pstmt.close();

		return result;
	}
}
