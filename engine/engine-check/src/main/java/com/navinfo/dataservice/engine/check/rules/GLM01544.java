package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/*
 * @ClassName：GLM01399
 * @author:Feng Haixia
 * @data:2017/03/24
 * @Description:同一根link上不同的道路名称中不允许有相同的NAME值
 */
public class GLM01544 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01544.class);

	private Set<Integer> nameLinkPidSet = new HashSet<>();

	@Override
	public void preCheck(CheckCommand checkCommand) {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareChangeDataSet(checkCommand);

		for (Integer linkPid : nameLinkPidSet) {
			String sqlStr = String.format(
					"SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM RD_LINK_NAME LM,RD_LINK L WHERE LM.NAME_GROUPID IN "
					+ "(SELECT NAME_GROUPID FROM RD_LINK_NAME GROUP BY NAME_GROUPID,LINK_PID HAVING COUNT(1) > 1) AND LM.U_RECORD <> 2 AND L.U_RECORD <> 2 AND LM.LINK_PID = L.LINK_PID AND L.LINK_PID = %d",
					linkPid);

			logger.info("RdLinkLimit后检查GLM01544 SQL:" + sqlStr);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sqlStr);

			if (resultList.isEmpty()) {
				continue;
			}

			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2));
		} // for循环
	}

	private void prepareChangeDataSet(CheckCommand checkCommand) {
		for (IRow row : checkCommand.getGlmList()) {
			if (!(row instanceof RdLinkName) || row.status() == ObjStatus.DELETE) {
				continue;
			}

			RdLinkName rdlinkName = (RdLinkName) row;

			nameLinkPidSet.add(rdlinkName.getLinkPid());
		}
	}
}
