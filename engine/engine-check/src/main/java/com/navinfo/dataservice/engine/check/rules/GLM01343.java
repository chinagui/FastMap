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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01343
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 未定义交通区域属性的道路Link不制作道路名
 */
public class GLM01343 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01343.class);

	private Set<Integer> resultLinkPidSet = new HashSet<>();

	public GLM01343() {
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
					"SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM RD_LINK_FORM F, RD_LINK_NAME LN, RD_LINK L WHERE L.LINK_PID = F.LINK_PID AND F.LINK_PID = LN.LINK_PID AND F.U_RECORD <> 2 AND LN.U_RECORD <> 2 AND L.U_RECORD <> 2 AND L.LINK_PID = LN.LINK_PID AND F.FORM_OF_WAY = 51 AND L.LINK_PID =");

			sb.append(linkPid);

			logger.info("RdLink后检查GLM01343 SQL:" + sb.toString());

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
				if (formOfWay == 51) {
					resultLinkPidSet.add(form.getLinkPid());
				}
			} else if (row instanceof RdLinkName) {
				RdLinkName rdLinkName = (RdLinkName) row;

				int linkPid = rdLinkName.getLinkPid();
				
				if(rdLinkName.status() != ObjStatus.DELETE)
				{
					resultLinkPidSet.add(linkPid);
				}
			}
		}
	}

}
