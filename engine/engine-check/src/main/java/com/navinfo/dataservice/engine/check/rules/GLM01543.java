package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/*
 * @ClassName：GLM01543
 * @author:Feng Haixia
 * @data:2017/03/23
 * @Description: link道路名称中的名称组号码不能为0
 */
public class GLM01543 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01543.class);

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (!(row instanceof RdLinkName) || row.status() == ObjStatus.DELETE) {
				continue;
			}

			RdLinkName linkName = (RdLinkName) row;
			String sqlStr = String.format(
					"SELECT L.GEOMETRY,'[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM RD_LINK_NAME LN, RD_LINK L "
							+ "WHERE LN.NAME_GROUPID = 0 AND L.LINK_PID = LN.LINK_PID AND L.U_RECORD <> 2 AND LN.U_RECORD <> 2 AND L.LINK_PID = %d",
					linkName.getLinkPid());

			logger.info("RdLinkName后检查GLM01543 SQL:" + sqlStr);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sqlStr);

			if (resultList.isEmpty()) {
				continue;
			}

			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2));
		}
	}
}
