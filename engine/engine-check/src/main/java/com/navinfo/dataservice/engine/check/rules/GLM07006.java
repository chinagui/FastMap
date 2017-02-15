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
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM07006
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 交叉口Link不允许作为信号灯的进入线
 */
public class GLM07006 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM07006.class);

	private Set<Integer> resultLinkPidSet = new HashSet<>();

	public GLM07006() {
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
					"SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID FROM RD_LINK R, RD_LINK_FORM F WHERE R.LINK_PID = F.LINK_PID AND F.FORM_OF_WAY = 50 AND R.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND R.u_record！=2 AND F.u_record！=2 AND EXISTS (SELECT 1 FROM RD_TRAFFICSIGNAL T WHERE T.LINK_PID = R.LINK_PID AND T.u_record！=2) ");

			logger.info("RdLink后检查GLM07006 SQL:" + sb.toString());

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
				if (formOfWay == 50) {
					resultLinkPidSet.add(form.getLinkPid());
				}
			} else if (row instanceof RdTrafficsignal) {
				RdTrafficsignal signal = (RdTrafficsignal) row;

				int linkPid = signal.getLinkPid();

				resultLinkPidSet.add(linkPid);
			}
		}
	}

}
