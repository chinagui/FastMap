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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01529
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 风景路线形态的link上制作了穿行限制
 */
public class GLM01529 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01529.class);

	private Set<Integer> resultLinkPidSet = new HashSet<>();

	public GLM01529() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Integer linkPid : resultLinkPidSet) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID FROM RD_LINK R, RD_LINK_FORM RLF WHERE R.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND RLF.FORM_OF_WAY = 60 AND R.LINK_PID = RLF.LINK_PID AND R.U_RECORD <> 2 AND RLF.U_RECORD <> 2 AND EXISTS (SELECT 1 FROM RD_LINK_LIMIT RLL WHERE R.LINK_PID = RLL.LINK_PID AND RLL.U_RECORD <> 2 AND RLL.TYPE = 3) ");

			logger.info("RdLink后检查GLM01529 SQL:" + sb.toString());

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
				if (form.status() != ObjStatus.DELETE && form.changedFields().containsKey("formOfWay")) {
					formOfWay = (int) form.changedFields().get("formOfWay");
				}
				if (formOfWay == 60) {
					resultLinkPidSet.add(form.getLinkPid());
				}
			} else if (row instanceof RdLinkLimit) {
				RdLinkLimit limit = (RdLinkLimit) row;

				int type = limit.getType();

				if (limit.status() != ObjStatus.DELETE && limit.changedFields().containsKey("type")) {
					type = (int) limit.changedFields().get("type");
				}
				if (type == 3) {
					resultLinkPidSet.add(limit.getLinkPid());
				}
			}
		}
	}

}
