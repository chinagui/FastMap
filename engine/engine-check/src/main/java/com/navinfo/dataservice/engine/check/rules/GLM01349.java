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

/**
 * @ClassName: GLM01349
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description:若点具有障碍物信息，且该点挂接有FC为1-4的Link报log
 */
public class GLM01349 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01349.class);

	private Set<Integer> check1 = new HashSet<>();

	public GLM01349() {
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
					"SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID FROM RD_LINK RL, RD_NODE RN, RD_NODE_FORM RNF WHERE RL.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND (RL.S_NODE_PID = RN.NODE_PID OR RL.E_NODE_PID = RN.NODE_PID) AND RN.NODE_PID = RNF.NODE_PID AND RL.FUNCTION_CLASS BETWEEN 1 AND 4 AND RNF.FORM_OF_WAY = 15 AND RN.U_RECORD <> 2 AND RNF.U_RECORD <> 2 ");
			
			logger.info("RdLink后检查GLM01349 check1-> SQL:" + sb.toString());

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
			if (row instanceof RdLink) {
				RdLink rdLink = (RdLink) row;
				int functionClass = rdLink.getFunctionClass();

				if (rdLink.status() != ObjStatus.DELETE) {
					if (rdLink.changedFields().containsKey("functionClass")) {
						functionClass = (int) rdLink.changedFields().get("functionClass");
					}
					if (functionClass != 0 && functionClass != 5) {
						check1.add(rdLink.getPid());
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
					if(formOfWay == 15)
					{
						check1.add(form.getLinkPid());
					}
				}
			}
		}
	}

}
