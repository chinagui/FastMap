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

/*
 * @ClassName：GLM01229
 * @author:Feng Haixia
 * @data:2017/03/22
 * @Description: 轮渡/人渡种别，只能和无属性形态共存
 */
public class GLM01229 extends baseRule {

	private Set<Integer> noPropertyLinkPidSet = new HashSet<>();

	private static Logger logger = Logger.getLogger(GLM01229.class);

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareLinkPidData(checkCommand);

		for (Integer linkPid : noPropertyLinkPidSet) {
			String sqlStr = String.format(
					"SELECT L.GEOMETRY,'[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM RD_LINK L,RD_LINK_FORM LF WHERE LF.FORM_OF_WAY <> 1 AND "
							+ "L.KIND IN (11,13) AND LF.LINK_PID = L.LINK_PID AND LF.U_RECORD <> 2 AND L.U_RECORD <> 2 AND L.LINK_PID = %d",
					linkPid);

			logger.info("RdLink后检查GLM01229 SQL:" + sqlStr);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sqlStr);

			if (resultList.isEmpty()) {
				continue;
			}
			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2));
		}
	}

	/*
	 * 准备检查数据
	 * 
	 * @param checkCommand
	 * 
	 * @result 修改link为轮渡/人渡or修改linkForm不为无属性
	 */
	private void prepareLinkPidData(CheckCommand checkCommand) {
		for (IRow row : checkCommand.getGlmList()) {

			if ((row instanceof RdLink) && row.status() != ObjStatus.DELETE) {
				prepareDateForLinkAndForm(row);
			} else if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
				prepareDateForLinkForm(row);
			}
		}
	}

	private void prepareDateForLinkAndForm(IRow row) {
		RdLink link = (RdLink) row;
		int kind = link.getTollInfo();

		if (link.changedFields().containsKey("kind")) {
			kind = (int) link.changedFields().get("kind");
		}

		// 人渡/轮渡
		if (kind == 11 || kind == 13) {
			noPropertyLinkPidSet.add(link.getPid());
		}
	}

	private void prepareDateForLinkForm(IRow row) {
		RdLinkForm linkForm = (RdLinkForm) row;
		int formOfWay = linkForm.getFormOfWay();

		if (linkForm.changedFields().containsKey("formOfWay")) {
			formOfWay = (int) linkForm.changedFields().get("formOfWay");
		}

		// linkForm不为“无属性”道路
		if (formOfWay != 1) {
			noPropertyLinkPidSet.add(linkForm.getLinkPid());
		}
	}
}
