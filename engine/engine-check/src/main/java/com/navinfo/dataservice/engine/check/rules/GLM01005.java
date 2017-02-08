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
 * GLM01118
 * 
 * @ClassName: GLM01005
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 一根有SA/PA属性的Link，起止点不能挂在同一条高速（或城高）线上
 */

public class GLM01005 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01005.class);

	private Set<Integer> saPaLinkSet = new HashSet<>();

	private Set<Integer> kindOf1And2LinkSet = new HashSet<>();

	public GLM01005() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		saPaLinkSet.addAll(kindOf1And2LinkSet);
		for (int linkPid : saPaLinkSet) {
			logger.debug("检查类型：postCheck， 检查规则：GLM01005， 检查要素：RDLINK(" + linkPid + "), 触法时机：LINK种别编辑");

			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM RD_LINK L, RD_LINK_FORM F, RD_LINK LE, RD_LINK LS WHERE L.LINK_PID = F.LINK_PID AND L.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND L.U_RECORD != 2 AND F.U_RECORD != 2 AND LE.U_RECORD != 2 AND LS.U_RECORD != 2 "
					+ " AND F.FORM_OF_WAY IN (12, 13) AND (LE.S_NODE_PID = L.E_NODE_PID OR LE.E_NODE_PID = L.E_NODE_PID) "
					+ " AND (LS.S_NODE_PID = L.S_NODE_PID OR LS.E_NODE_PID = L.S_NODE_PID) AND LE.LINK_PID <> L.LINK_PID "
					+ " AND LE.LINK_PID = LS.LINK_PID AND LE.KIND IN (1, 2) AND LE.DIRECT > 1 AND NOT EXISTS "
					+ " (SELECT 1 FROM RD_LINK_FORM FE WHERE FE.LINK_PID = LE.LINK_PID AND FE.U_RECORD != 2 AND FE.FORM_OF_WAY IN (10, 11, 12, 13)) "
					+ " AND EXISTS (SELECT 1 FROM RD_LINK_FORM FE WHERE FE.LINK_PID = LE.LINK_PID AND FE.U_RECORD != 2 AND FE.FORM_OF_WAY = 14) ");

			log.info("RdLink后检查GLM01005 SQL:" + sb.toString());

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
			if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
				RdLink rdLink = (RdLink) row;
				int kind = rdLink.getKind();
				if (rdLink.changedFields().containsKey("kind")) {
					kind = (int) rdLink.changedFields().get("kind");
				}
				if (kind == 1 || kind == 2) {
					kindOf1And2LinkSet.add(rdLink.getPid());
				}
			} else if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
				RdLinkForm form = (RdLinkForm) row;
				int formOfWay = form.getFormOfWay();
				if (form.changedFields().containsKey("formOfWay")) {
					formOfWay = (int) form.changedFields().get("formOfWay");
				}
				if (formOfWay == 12 || formOfWay == 13) {
					saPaLinkSet.add(form.getLinkPid());
				}
			}
		}
	}
}
