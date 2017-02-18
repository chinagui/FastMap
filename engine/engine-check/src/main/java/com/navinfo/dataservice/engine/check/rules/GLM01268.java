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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01268
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 检查对象：含辅路属性的link；
 * 检查原则：限速类型为“普通”时，该link上的速度限制等级不能为1，2，否则报err
 */
public class GLM01268 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01268.class);

	private Set<Integer> resultLinkPidSet = new HashSet<>();

	public GLM01268() {
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
					"SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID FROM RD_LINK_SPEEDLIMIT LS, RD_LINK_FORM F, RD_LINK R WHERE R.LINK_PID = LS.LINK_PID AND R.LINK_PID = F.LINK_PID AND LS.LINK_PID = F.LINK_PID AND R.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND LS.U_RECORD !=2 AND F.U_RECORD !=2 AND R.U_RECORD !=2 AND F.FORM_OF_WAY = 34 AND LS.SPEED_CLASS IN (1, 2) AND LS.SPEED_TYPE = 0");

			logger.info("RdLink后检查GLM01268 SQL:" + sb.toString());

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

				if (form.status() == ObjStatus.UPDATE && form.changedFields().containsKey("formOfWay")) {
					formOfWay = (int) form.changedFields().get("formOfWay");
				}
				if (form.status() != ObjStatus.DELETE && formOfWay == 34) {
					resultLinkPidSet.add(form.getLinkPid());
				}
			}
			else if(row instanceof RdLinkSpeedlimit)
			{
				RdLinkSpeedlimit limit = (RdLinkSpeedlimit) row;
				
				int speedType = limit.getSpeedType();
				
				//限速等级
				int speedClass = limit.getSpeedClass();
				
				if(limit.status() != ObjStatus.DELETE)
				{
					if (limit.changedFields().containsKey("speedType")) {
						speedType = (int) limit.changedFields().get("speedType");
					}
					if (speedType == 0) {
						
						if(limit.changedFields().containsKey("speedClass"))
						{
							speedClass = (int) limit.changedFields().get("speedClass");
						}
						if(speedClass == 1 || speedClass == 2)
						{
							resultLinkPidSet.add(limit.getLinkPid());
						}
					}
				}
			}
		}
	}

}
