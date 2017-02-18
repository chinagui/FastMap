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
 * GLM01170
 * 
 * @ClassName: GLM01170
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 多属性的link，不能存在无属性
 */

public class GLM01170 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01170.class);

	private Set<Integer> checkLinkList = new HashSet<>();

	public GLM01170() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (int linkPid : checkLinkList) {
			logger.info("检查类型：postCheck， 检查规则：GLM01170， 检查要素：RDLINK(" + linkPid + ")");
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID FROM RD_LINK R, RD_LINK_FORM F WHERE R.LINK_PID = F.LINK_PID AND F.FORM_OF_WAY = 1 AND R.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND R.U_RECORD != 2 AND F.U_RECORD != 2 AND EXISTS (SELECT 1 FROM RD_LINK_FORM FF WHERE R.LINK_PID = FF.LINK_PID AND FF.FORM_OF_WAY <> 1 AND FF.U_RECORD != 2)");

			log.info("RdLink后检查GLM01170 SQL:" + sb.toString());

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
				if (form.status() != ObjStatus.DELETE) {
					checkLinkList.add(form.getLinkPid());
				}
			}
		}
	}
}
