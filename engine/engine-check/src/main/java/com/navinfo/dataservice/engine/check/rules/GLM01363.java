package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * @ClassName:GLM01363
 * @author:Feng Haixia
 * @data:2017/03/22
 * @Description:SA、PA属性的道路的收费信息不能为“收费”
 */
public class GLM01363 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01363.class);

	private Set<Integer> chargeLinkPidSet = new HashSet<>();

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareLinkPidData(checkCommand);

		for (Integer linkPid : chargeLinkPidSet) {
			String sqlStr = String.format(
					"SELECT L.GEOMETRY,'[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM RD_LINK L,RD_LINK_FORM LF WHERE LF.FORM_OF_WAY IN (12,13) "
							+ "AND L.TOLL_INFO = 1 AND LF.LINK_PID=L.LINK_PID AND LF.U_RECORD<>2 AND L.U_RECORD<>2 AND L.LINK_PID={0}",
					linkPid);

			logger.info("RdLinkLimit后检查GLM01363 SQL:" + sqlStr);

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
	 * @result 修改link为收费or修改linkForm为SA\PA
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
		int tollInfo = link.getTollInfo();

		if (link.changedFields().containsKey("tollInfo")) {
			tollInfo = (int) link.changedFields().get("tollInfo");
		}

		// 收费道路
		if (tollInfo == 1) {
			chargeLinkPidSet.add(link.getPid());
		}
	}

	private void prepareDateForLinkForm(IRow row) {
		RdLinkForm linkForm = (RdLinkForm) row;
		int formOfWay = linkForm.getFormOfWay();

		if (linkForm.changedFields().containsKey("formOfWay")) {
			formOfWay = (int) linkForm.changedFields().get("formOfWay");
		}

		// linkForm为SA\PA的道路
		if (formOfWay == 12 || formOfWay == 13) {
			chargeLinkPidSet.add(linkForm.getLinkPid());
		}
	}
}
