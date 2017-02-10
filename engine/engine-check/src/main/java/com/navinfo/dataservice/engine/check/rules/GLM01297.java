package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * GLM01297 线线结构退出线不能是交叉口内link
 * 
 * @ClassName: GLM01297
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 线线结构退出线不能是交叉口内link
 */

public class GLM01297 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01297.class);

	private Set<Integer> checkLinkSet = new HashSet<>();

	public GLM01297() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (int linkPid : checkLinkSet) {
			logger.info("检查类型：postCheck， 检查规则：GLM01297， 检查要素：RDLINK(" + linkPid + ")");
			StringBuilder sb = new StringBuilder();

			sb.append(
					"WITH T AS (SELECT T.OUT_LINK_PID LINK_PID FROM RD_LANE_TOPOLOGY T WHERE T.RELATIONSHIP_TYPE = 2 AND T.U_RECORD != 2  ");

			sb.append(
					" UNION SELECT T.OUT_LINK_PID FROM RD_RESTRICTION_DETAIL T WHERE T.RELATIONSHIP_TYPE = 2 AND T.U_RECORD != 2 ");

			sb.append(
					" UNION SELECT T.OUT_LINK_PID FROM RD_VOICEGUIDE_DETAIL T WHERE T.RELATIONSHIP_TYPE = 2 AND T.U_RECORD != 2 ");

			sb.append(
					" UNION SELECT T.OUT_LINK_PID FROM RD_DIRECTROUTE T WHERE T.RELATIONSHIP_TYPE = 2 AND T.U_RECORD != 2 ");

			sb.append(
					" UNION SELECT T.OUT_LINK_PID FROM RD_BRANCH T WHERE T.RELATIONSHIP_TYPE = 2 AND T.U_RECORD != 2)  ");

			sb.append(
					" SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM T, RD_LINK L, RD_LINK_FORM F WHERE T.LINK_PID = L.LINK_PID AND L.LINK_PID = F.LINK_PID AND L.U_RECORD != 2 AND F.U_RECORD != 2 AND T.LINK_PID = F.LINK_PID AND L.LINK_PID =");

			sb.append(linkPid);

			log.info("RdLink后检查GLM01297 SQL:" + sb.toString());

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sb.toString());

			if (!resultList.isEmpty()) {
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),
						(int) resultList.get(2));
			}
		}
	}

	/**
	 * @param checkCommand
	 * @throws Exception
	 */
	private void prepareData(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof RdLinkForm) {
				RdLinkForm form = (RdLinkForm) row;

				int formOfWay = form.getFormOfWay();

				if (form.status() != ObjStatus.DELETE) {
					if (form.changedFields().containsKey("formOfWay")) {
						formOfWay = (int) form.changedFields().get("formOfWay");
					}

					if (formOfWay == 50) {
						checkLinkSet.add(form.getLinkPid());
					}
				}
			}
		}
	}
}
