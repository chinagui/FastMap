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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01395
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 如果是7级及以上的环岛或特殊交通为“是”，限速值小于等于11时报log。
 */
public class GLM01395 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01395.class);

	private Set<Integer> check1 = new HashSet<>();

	public GLM01395() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Integer linkPid : check1) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID FROM RD_LINK RL, RD_LINK_FORM RLF, RD_LINK_SPEEDLIMIT RLS WHERE RL.LINK_PID = RLF.LINK_PID AND RL.LINK_PID = RLS.LINK_PID AND RL.LINK_PID = ");

			sb.append(linkPid);

			sb.append(
					" AND RL.U_RECORD <> 2 AND RLF.U_RECORD <> 2 AND RLS.U_RECORD <> 2 AND RL.KIND <= 7 AND ((RL.DIRECT != 3 AND RLS.FROM_SPEED_LIMIT < 110) OR (RL.DIRECT != 2 AND RLS.TO_SPEED_LIMIT < 110)) AND (RLF.FORM_OF_WAY = 33 OR RL.SPECIAL_TRAFFIC = 1)  ");

			logger.info("RdLink后检查GLM01395 check1-> SQL:" + sb.toString());

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
			if (row instanceof RdSpeedlimit) {
				RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;

				int fromSpeedLimit = speedlimit.getFromSpeedLimit();

				int toSpeedLimit = speedlimit.getToSpeedLimit();

				if (speedlimit.status() != ObjStatus.DELETE) {
					if (speedlimit.changedFields().containsKey("fromSpeedLimit")) {
						fromSpeedLimit = (int) speedlimit.changedFields().get("fromSpeedLimit");
					}
					if (speedlimit.changedFields().containsKey("toSpeedLimit")) {
						toSpeedLimit = (int) speedlimit.changedFields().get("toSpeedLimit");
					}
					if(toSpeedLimit < 110 || fromSpeedLimit < 110)
					{
						check1.add(speedlimit.getLinkPid());
					}
				} else if (row instanceof RdLink) {
					RdLink rdLink = (RdLink) row;
					int kind = rdLink.getKind();
					if (rdLink.status() != ObjStatus.DELETE) {
						if (rdLink.changedFields().containsKey("direct")) {
							check1.add(rdLink.getPid());
						}
						if(rdLink.changedFields().containsKey("kind"))
						{
							kind = (int) rdLink.changedFields().get("kind");
						}
						if(kind <= 7)
						{
							check1.add(rdLink.getPid());
						}
					}
				} else if (row instanceof RdLinkForm) {
					RdLinkForm form = (RdLinkForm) row;

					int formOfWay = form.getFormOfWay();

					if (form.status() != ObjStatus.DELETE) {
						if (form.changedFields().containsKey("formOfWay")) {
							formOfWay = (int) form.changedFields().get("formOfWay");
						}
						if (formOfWay == 33) {
							check1.add(form.getLinkPid());
						}
					}
				}
			}
		}
	}
}
