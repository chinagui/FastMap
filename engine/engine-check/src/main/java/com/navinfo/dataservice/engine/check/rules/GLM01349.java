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
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01349
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description:若点具有障碍物信息，且该点挂接有FC为1-4的Link报log
 * @alter: Feng Haixia
 */
public class GLM01349 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01349.class);

	private Set<Integer> kindChangeLinkPidSet = new HashSet<>();
	private Set<Integer> nodeChangePidSet = new HashSet<>();

	public GLM01349() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			prepareDataLink(row);
			prepareDataNode(row);
		}

		String sql = "";

		// 更新link触发
		for (Integer linkPid : kindChangeLinkPidSet) {

			sql = String.format(
					"SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID FROM RD_LINK RL, RD_NODE RN, RD_NODE_FORM RNF WHERE RL.LINK_PID = %d"
							+ " AND (RL.S_NODE_PID = RN.NODE_PID OR RL.E_NODE_PID = RN.NODE_PID) AND RN.NODE_PID = RNF.NODE_PID AND RNF.FORM_OF_WAY = 15 AND RN.U_RECORD <> 2 AND RNF.U_RECORD <> 2",
					linkPid);
			ExecuteCheckResult(sql);
		}

		// 更新node触发
		for (Integer nodePid : nodeChangePidSet) {
			sql = String.format(
					"SELECT N.GEOMETRY, '[RD_NODE,' || N.NODE_PID || ']' TARGET, L.MESH_ID FROM RD_NODE N,RD_LINK L WHERE N.NODE_PID = %d AND (L.S_NODE_PID = N.NODE_PID OR L.E_NODE_PID = N.NODE_PID) "
							+ "AND (L.FUNCTION_CLASS BETWEEN 1 AND 4) AND L.U_RECORD <> 2 AND N.U_RECORD <> 2  ",
					nodePid);
			ExecuteCheckResult(sql);
		}
	}

	/**
	 * @function:种别为1到4的link集合
	 * @param row
	 * @throws Exception
	 */
	private void prepareDataLink(IRow row) throws Exception {

		if (!(row instanceof RdLink) || row.status() == ObjStatus.DELETE) {
			return;
		}
		RdLink rdLink = (RdLink) row;
		int functionClass = rdLink.getFunctionClass();

		if (rdLink.changedFields().containsKey("functionClass")) {
			functionClass = (int) rdLink.changedFields().get("functionClass");
		}
		if (functionClass != 0 && functionClass != 5) {
			kindChangeLinkPidSet.add(rdLink.getPid());
		}

	}

	/**
	 * @function:端点node形态为15（障碍物）的node集合
	 * @param row
	 * @throws Exception
	 */
	private void prepareDataNode(IRow row) throws Exception {
		if (!(row instanceof RdNodeForm) || row.status() == ObjStatus.DELETE) {
			return;
		}
		RdNodeForm form = (RdNodeForm) row;
		int formOfWay = form.getFormOfWay();

		if (form.changedFields().containsKey("formOfWay")) {
			formOfWay = (int) form.changedFields().get("formOfWay");
		}

		if (formOfWay == 15) {
			nodeChangePidSet.add(form.getNodePid());
		}
	}

	private void ExecuteCheckResult(String sql) throws Exception {
		DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (!resultList.isEmpty()) {
			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2));
		}
	}
}
