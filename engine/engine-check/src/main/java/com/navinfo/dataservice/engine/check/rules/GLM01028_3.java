package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * 10级路/步行街/人渡不能是分歧的进入线，退出线，经过线
* @ClassName: GLM01028_3 
* @author Zhang Xiaolong
* @date 2017年2月9日 下午3:55:59 
* @Description: 10级路/步行街/人渡不能是分歧的进入线，退出线，经过线
 */
public class GLM01028_3 extends baseRule {

	private Set<Integer> linkPidSet = new HashSet<>();

	private static Logger logger = Logger.getLogger(GLM01028_3.class);

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		preparedData(checkCommand);

		for (int linkPid : linkPidSet) {
			logger.debug("检查类型：postCheck， 检查规则：GLM01028_3， 检查要素：RDLINK(" + linkPid + "), 触法时机：LINK种别编辑");

			StringBuilder sb = new StringBuilder();

			sb.append(
					"WITH T AS (SELECT R.LINK_PID, R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID FROM RD_LINK R WHERE (R.KIND IN (10, 11) OR EXISTS (SELECT 1 FROM RD_LINK_FORM F WHERE R.LINK_PID = F.LINK_PID AND F.FORM_OF_WAY = 20 AND F.U_RECORD <> 2)) AND R.U_RECORD != 2 AND R.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" ) SELECT T.GEOMETRY, T.TARGET, T.MESH_ID FROM T WHERE EXISTS (SELECT 1 FROM RD_BRANCH_VIA RT WHERE T.LINK_PID = RT.LINK_PID AND RT.U_RECORD != 2) OR EXISTS (SELECT 1 FROM RD_BRANCH RB WHERE (T.LINK_PID = RB.IN_LINK_PID OR T.LINK_PID = RB.OUT_LINK_PID) AND RB.U_RECORD <> 2) ");

			
			log.info("RdLink后检查GLM01028_3 SQL:" + sb.toString());
			
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
	 */
	private void preparedData(CheckCommand checkCommand) {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() != ObjStatus.DELETE) {
				if (row instanceof RdLink) {
					RdLink link = (RdLink) row;

					int kind = link.getKind();

					if (link.changedFields().containsKey("kind")) {
						kind = (int) link.changedFields().get("kind");
					}
					if (kind == 10 || kind == 11) {
						linkPidSet.add(link.getPid());
					}
				} else if (row instanceof RdLinkForm) {
					RdLinkForm form = (RdLinkForm) row;

					int formOfWay = form.getFormOfWay();
					if(form.status() != ObjStatus.DELETE)
					{
						if (form.changedFields().containsKey("formOfWay")) {
							formOfWay = (int) form.changedFields().get("formOfWay");
						}
						if (formOfWay == 20) {
							linkPidSet.add(form.getLinkPid());
						}
					}
				}
			}
		}
	}

}
