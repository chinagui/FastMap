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
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01241
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description:限速类型为“普通”时，
（1）步行街为双方向时，速度限制必须等于10km/h
（2）当步行街为单方向时，和道路方向相同的速度限制必须等于10km/h，和方向相反的速度限制必须等于0km/h
 */
public class GLM01241 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01241.class);

	private Set<Integer> check1 = new HashSet<>();

	public GLM01241() {
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
					"SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID FROM RD_LINK RL, RD_LINK_FORM RLF, RD_LINK_SPEEDLIMIT RLS WHERE RL.LINK_PID = RLS.LINK_PID AND RL.LINK_PID = RLF.LINK_PID AND RL.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND RLF.FORM_OF_WAY = 20 AND RLF.U_RECORD <> 2 AND RLS.U_RECORD <> 2 AND ((RL.DIRECT = 1 AND (RLS.FROM_SPEED_LIMIT <> 100 OR RLS.TO_SPEED_LIMIT <> 100)) OR (RL.DIRECT = 2 AND (RLS.FROM_SPEED_LIMIT <> 100 OR RLS.TO_SPEED_LIMIT <> 0)) OR (RL.DIRECT = 3 AND (RLS.FROM_SPEED_LIMIT <> 0 OR RLS.TO_SPEED_LIMIT <> 100)))");
			
			logger.info("RdLink后检查GLM01241 check1-> SQL:" + sb.toString());

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
			if (row instanceof RdLinkSpeedlimit) {
				RdLinkSpeedlimit rdLinkSpeedlimit = (RdLinkSpeedlimit) row;
				int fromSpeedLimit = rdLinkSpeedlimit.getFromSpeedLimit();
				
				int toSpeedLimit = rdLinkSpeedlimit.getToSpeedLimit();

				if (rdLinkSpeedlimit.status() != ObjStatus.DELETE) {
					if (rdLinkSpeedlimit.changedFields().containsKey("fromSpeedLimit")) {
						fromSpeedLimit = (int) rdLinkSpeedlimit.changedFields().get("fromSpeedLimit");
					}
					if (rdLinkSpeedlimit.changedFields().containsKey("toSpeedLimit")) {
						toSpeedLimit = (int) rdLinkSpeedlimit.changedFields().get("toSpeedLimit");
					}
					
					if (fromSpeedLimit != 100 || toSpeedLimit != 100) {
						check1.add(rdLinkSpeedlimit.getLinkPid());
					}
				}
			}
			else if(row instanceof RdLinkForm)
			{
				RdLinkForm form = (RdLinkForm) row;
				int formOfWay = form.getFormOfWay();
				
				if(form.status() != ObjStatus.DELETE)
				{
					if(form.changedFields().containsKey("formOfWay"))
					{
						formOfWay = (int) form.changedFields().get("formOfWay");
					}
					if(formOfWay == 20)
					{
						check1.add(form.getLinkPid());
					}
				}
			}
			else if(row instanceof RdLink)
			{
				RdLink rdLink = (RdLink) row;
				
				int direct = rdLink.getDirect();
				
				if(rdLink.status() != ObjStatus.DELETE)
				{
					if(rdLink.changedFields().containsKey("direct"))
					{
						direct = (int) rdLink.changedFields().get("direct");
					}
					if(direct != 0)
					{
						check1.add(rdLink.getPid());
					}
				}
			}
		}
	}

}
