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
 * @ClassName: GLM01267
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 检查对象：含“环岛”属性的link；
				   检查原则：限速类型为“普通”时，该link上的速度限制等级不能为1，2，3，否则报err
 */
public class GLM01267 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01267.class);

	/**
	 * 最终需要提示log的linkPid集合
	 */
	private Set<Integer> resultLinkPidSet = new HashSet<>();

	public GLM01267() {
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
					" AND LS.U_RECORD != 2 AND F.U_RECORD != 2 AND R.U_RECORD != 2 AND F.FORM_OF_WAY = 33 AND LS.SPEED_CLASS IN (1, 2, 3) AND LS.SPEED_TYPE = 0 ");

			
			log.info("RdLink后检查GLM01267 SQL:" + sb.toString());
			
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
				if (form.status() != ObjStatus.DELETE && formOfWay == 33) {
					resultLinkPidSet.add(form.getLinkPid());
				}
				if (form.status() == ObjStatus.DELETE && formOfWay == 33) {
					resultLinkPidSet.remove(form.getLinkPid());
				}
			}
			else if(row instanceof RdLinkSpeedlimit)
			{
				RdLinkSpeedlimit limit = (RdLinkSpeedlimit) row;
				
				int speedType = limit.getSpeedType();
				
				if(limit.status() != ObjStatus.DELETE)
				{
					if (limit.changedFields().containsKey("speedType")) {
						speedType = (int) limit.changedFields().get("speedType");
					}
					if (speedType == 0) {
						//限速等级
						int speedClass = limit.getSpeedClass();
						
						if(limit.changedFields().containsKey("speedClass"))
						{
							speedClass = limit.getSpeedClass();
						}
						if(speedClass == 1 || speedClass == 2 || speedClass == 3)
						{
							resultLinkPidSet.add(limit.getLinkPid());
						}
					}
				}
				else
				{
					resultLinkPidSet.remove(limit.getLinkPid());
				}
			}
		}
	}

}
